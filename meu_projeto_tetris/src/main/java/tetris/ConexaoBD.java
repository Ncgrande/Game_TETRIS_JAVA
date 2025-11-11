package tetris;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Classe utilitária para gerenciar a conexão JDBC com o banco de dados.
 * Baseado na Aula 4 - Configuração JDBC.
 */
public class ConexaoBD {
    // Configurações do Banco de Dados (ATENÇÃO: SUBSTITUA ESSES VALORES)
    private static final String URL = "jdbc:mysql://localhost:3306/tetris_db"; 
    private static final String USUARIO = "root"; 
    private static final String SENHA = ""; 

    static {
        try {
            // Carrega o driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException e) {
            // Se o driver não for encontrado (verifique o pom.xml)
            throw new RuntimeException("Driver MySQL não encontrado. Verifique a dependência no pom.xml!", e);
        }
    }

    /**
     * Obtém uma nova conexão com o banco de dados.
     * @return Objeto Connection.
     * @throws SQLException Se a conexão falhar.
     */
    public static Connection obterConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA); 
    }

    /**
     * Método principal para testar a conexão (opcional, mas recomendado).
     */
    public static void main(String[] args) {
        try (Connection conn = obterConexao()) {
            System.out.println("Conexão estabelecida com sucesso! Database: " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
        }
    }
}