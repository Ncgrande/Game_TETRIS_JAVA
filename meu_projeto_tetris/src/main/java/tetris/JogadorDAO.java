package tetris;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Data Access Object (DAO) para a Entidade Jogador.
 * Gerencia a persistência entre o objeto de domínio e o banco de dados (JDBC).
 * Baseado na Aula 4, Slide 28.
 */
public class JogadorDAO {

    /**
     * Salva ou atualiza um Jogador no banco de dados.
     * Implementa a lógica de INSERT/UPDATE conforme o TDD.
     */
    public void salvar(Jogador jogador) throws SQLException {
        // Tenta fazer um UPDATE primeiro (se o jogador já existe)
        if (atualizar(jogador) == 0) {
            // Se o UPDATE afetou 0 linhas, faz um INSERT (jogador é novo)
            inserir(jogador);
        }
    }

    /**
     * Salva um jogador usando a conexão provista. Útil para participar de transações
     * já abertas (evita abrir/fechar conexões adicionais).
     */
    public void salvar(Jogador jogador, Connection conn) throws SQLException {
        // Tenta atualizar usando a conexão fornecida
        if (atualizar(jogador, conn) == 0) {
            inserir(jogador, conn);
        }
    }
    
    private int inserir(Jogador jogador) throws SQLException {
        String sql = "INSERT INTO jogadores (id, nome) VALUES (?, ?)";
        
        try (Connection conn = ConexaoBD.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, jogador.getId().toString()); 
            stmt.setString(2, jogador.getNome());
            
            return stmt.executeUpdate();
        }
    }

    // inserir que usa conexão existente
    private int inserir(Jogador jogador, Connection conn) throws SQLException {
        String sql = "INSERT INTO jogadores (id, nome) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, jogador.getId().toString());
            stmt.setString(2, jogador.getNome());
            return stmt.executeUpdate();
        }
    }
    
    private int atualizar(Jogador jogador) throws SQLException {
        // Atualizamos apenas o nome, pois a identidade (ID) nunca muda.
        String sql = "UPDATE jogadores SET nome = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBD.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, jogador.getNome());
            stmt.setString(2, jogador.getId().toString());
            
            return stmt.executeUpdate();
        }
    }

    // atualizar que usa conexão existente
    private int atualizar(Jogador jogador, Connection conn) throws SQLException {
        String sql = "UPDATE jogadores SET nome = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, jogador.getNome());
            stmt.setString(2, jogador.getId().toString());
            return stmt.executeUpdate();
        }
    }

    /**
     * Busca um Jogador pelo seu ID (UUID) e reconstrói a Entidade.
     */
    public Jogador buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, nome FROM jogadores WHERE id = ?";
        
        try (Connection conn = ConexaoBD.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Reconstroi a Entidade Jogador
                    return new Jogador(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("nome")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Deleta um Jogador do banco de dados (Necessário para os testes de limpeza).
     */
   public void deletar(UUID id) throws SQLException {
    // 1. Deletar registros filhas (Partidas e Estatísticas)
    try (Connection conn = ConexaoBD.obterConexao();
         Statement stmt = conn.createStatement()) {
        
        // Deletar Estatísticas (tabela filha)
        stmt.executeUpdate("DELETE FROM estatisticas_jogador WHERE jogador_id = '" + id.toString() + "'");
        // Deletar Partidas (tabela filha)
        stmt.executeUpdate("DELETE FROM partidas WHERE jogador_id = '" + id.toString() + "'");
        
        // 2. Deletar o pai (Jogador)
        String sql = "DELETE FROM jogadores WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setString(1, id.toString());
             ps.executeUpdate();
        }
    }
}
}