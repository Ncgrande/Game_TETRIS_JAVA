package tetris;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
//import tetris.ConexaoBD;

/**
 * DAO para consultas de ranking e placar.
 */
public class RankingDAO {

    // Classe interna para representar um registro do Ranking (DTO)
    public static class RegistroRanking {
        public final int posicao;
        public final String nomeJogador;
        public final int pontuacao;

        public RegistroRanking(int posicao, String nomeJogador, int pontuacao) {
            this.posicao = posicao;
            this.nomeJogador = nomeJogador;
            this.pontuacao = pontuacao;
        }
    }

    /**
     * Obtém o Top N de pontuações globais.
     */
    public List<RegistroRanking> obterTopPontuacoes(int limite) throws SQLException {
        String sql = "SELECT p.pontuacao, j.nome FROM partidas p " +
                     "JOIN jogadores j ON p.jogador_id = j.id " +
                     "ORDER BY p.pontuacao DESC LIMIT ?";

        List<RegistroRanking> ranking = new ArrayList<>();
        
        try (Connection conn = ConexaoBD.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                int posicao = 1;
                while (rs.next()) {
                    ranking.add(new RegistroRanking(
                        posicao++,
                        rs.getString("nome"),
                        rs.getInt("pontuacao")
                    ));
                }
            }
        }
        return ranking;
    }
    public List<RegistroRanking> obterRankingPorJogador(String jogadorId, int limite) throws SQLException {
        String sql = "SELECT p.pontuacao, j.nome FROM partidas p " +
                     "JOIN jogadores j ON p.jogador_id = j.id " +
                     "WHERE p.jogador_id = ? " +
                     "ORDER BY p.pontuacao DESC LIMIT ?";

        List<RegistroRanking> ranking = new ArrayList<>();
        
        try (Connection conn = ConexaoBD.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, jogadorId);
            stmt.setInt(2, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                int posicao = 1;
                while (rs.next()) {
                    ranking.add(new RegistroRanking(
                        posicao++,
                        rs.getString("nome"),
                        rs.getInt("pontuacao")
                    ));
                }
            }
        }
        return ranking;
    }
}