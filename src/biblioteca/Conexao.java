package biblioteca;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/** Lê db.properties e abre a conexão JDBC. Cria o banco se ele ainda não existir. */
public final class Conexao {
    private final String url;
    private final String usuario;
    private final String senha;

    public Conexao(String arquivo) {
        Properties p = new Properties();
        Path caminho = Paths.get(arquivo);
        if (Files.exists(caminho)) {
            try (InputStream in = Files.newInputStream(caminho)) {
                p.load(in);
            } catch (IOException e) {
                System.out.println("Não consegui ler " + arquivo + ": " + e.getMessage());
            }
        } else {
            System.out.println("Arquivo " + arquivo + " não encontrado. Copie db.properties.example para "
                    + arquivo + " e coloque a senha do seu PostgreSQL.");
        }
        url     = p.getProperty("url", "jdbc:postgresql://localhost:5432/biblioteca");
        usuario = p.getProperty("usuario", "postgres");
        senha   = p.getProperty("senha", "");
    }

    public String getUrl() { return url; }

    public Connection abrir() throws SQLException {
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            if ("3D000".equals(e.getSQLState())) {          // 3D000 = banco não existe
                criarBanco();
                return DriverManager.getConnection(url, usuario, senha);
            }
            throw e;
        }
    }

    /** Conecta no banco padrão "postgres" e executa CREATE DATABASE. */
    private void criarBanco() throws SQLException {
        int barra = url.lastIndexOf('/');
        int interrogacao = url.indexOf('?', barra);
        String nome = interrogacao < 0 ? url.substring(barra + 1) : url.substring(barra + 1, interrogacao);
        if (!nome.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new SQLException("Nome de banco inválido em db.properties: " + nome);
        }
        String urlPostgres = url.substring(0, barra + 1) + "postgres";
        try (Connection c = DriverManager.getConnection(urlPostgres, usuario, senha);
             Statement st = c.createStatement()) {
            st.execute("CREATE DATABASE " + nome);
            System.out.println("Banco \"" + nome + "\" criado.");
        }
    }
}
