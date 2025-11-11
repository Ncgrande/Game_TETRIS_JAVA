package tetris;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
//import java.sql.Statement;
//import java.util.Objects;
//import tetris.ConexaoBD;


public class PartidaDAO {

    /**
     * Salva a partida e atualiza as estatísticas do jogador em uma única transação.
     * @param partida A Entidade Partida a ser persistida.
     * @throws SQLException Em caso de falha no banco de dados.
     */
    public void salvarPartidaCompleta(Partida partida, long duracaoSegundos) throws SQLException {
        Connection conn = null;
        try {
            conn = ConexaoBD.obterConexao();
            conn.setAutoCommit(false); // Inicia a Transação

            // 1. Garantir que o jogador exista no banco (salva ou atualiza) antes de inserir a partida
            if (partida.getJogador() != null) {
                new JogadorDAO().salvar(partida.getJogador(), conn);
            }

            // 2. Salvar os dados da Partida
            inserirPartida(conn, partida, duracaoSegundos);

            // 3. Atualizar Estatísticas do Jogador (melhor pontuação, total de partidas)
            atualizarEstatisticasJogador(conn, partida); 

            conn.commit(); // Confirma a Transação 
        } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback(); // Reverte em caso de erro
            } catch (SQLException rollbackEx) { // <-- CAPTURA A EXCEÇÃO DE ROLLBACK AQUI
                e.addSuppressed(rollbackEx);
            }
        }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Retorna ao modo padrão 
                    conn.close();
                } catch (SQLException closeEx) {
                    // Logar ou ignorar erro de fechamento de conexão
                }
            }
        }
    }

    private void inserirPartida(Connection conn, Partida partida, long duracaoSegundos) throws SQLException {
        String sql = "INSERT INTO partidas (id, jogador_id, pontuacao, linhas_eliminadas, nivel_alcancado, duracao_segundos) " +
                     "VALUES (?, ?, ?, ?, ?, ?)"; 

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // O ID da Partida precisa ser gerado antes, ou você pode usar um getter
            stmt.setString(1, UUID.randomUUID().toString()); // Assumindo novo ID para o registro
            stmt.setString(2, partida.getJogador().getId().toString());
            stmt.setInt(3, partida.getPontuacao());
            stmt.setInt(4, partida.getTotalLinhas());
            stmt.setInt(5, partida.getNivel());
            stmt.setLong(6, duracaoSegundos);

            stmt.executeUpdate();
        }
    }

    private void atualizarEstatisticasJogador(Connection conn, Partida partida) throws SQLException {
    String sql = "INSERT INTO estatisticas_jogador (jogador_id, total_partidas, melhor_pontuacao) VALUES (?, 1, ?) " +
                 "ON DUPLICATE KEY UPDATE total_partidas = total_partidas + 1, melhor_pontuacao = GREATEST(melhor_pontuacao, ?)"; 

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        // TODOS OS COMANDOS STMT DEVEM ESTAR AQUI DENTRO
        String jogadorId = partida.getJogador().getId().toString();
        int pontuacao = partida.getPontuacao();
        
        stmt.setString(1, jogadorId);
        stmt.setInt(2, pontuacao); // Valor para o INSERT inicial
        stmt.setInt(3, pontuacao); // Valor para o GREATEST (UPDATE)

        stmt.executeUpdate();
    } // O stmt é fechado automaticamente aqui.
    }
}
