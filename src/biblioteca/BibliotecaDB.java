package biblioteca;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Versão da Biblioteca que guarda tudo no PostgreSQL (Exercício 2) via JDBC.
 *
 * O banco não guarda prazo nem multa por dia: cada linha de "item" vira um
 * objeto Livro/Revista/DVD (pela Fabrica) e quem calcula prazo e multa é o
 * próprio objeto, por polimorfismo. Nenhum método aqui testa o tipo.
 */
public class BibliotecaDB {
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // SQLState do PostgreSQL
    private static final String VIOLA_UNIQUE = "23505";
    private static final String VIOLA_FK     = "23503";

    private final Connection con;

    public BibliotecaDB(Connection con) { this.con = con; }

    // ------------------------------------------------------------------ banco

    public boolean tabelasExistem() throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT to_regclass('public.emprestimo') IS NOT NULL")) {
            rs.next();
            return rs.getBoolean(1);
        }
    }

    /** Executa sql/biblioteca.sql: apaga, recria as tabelas e insere os dados de teste. */
    public void recriarBanco(String arquivoSql) throws SQLException, IOException {
        String script = new String(Files.readAllBytes(Paths.get(arquivoSql)), StandardCharsets.UTF_8);
        try (Statement st = con.createStatement()) {
            st.execute(script);
        }
        System.out.println("Tabelas recriadas com os dados de teste.");
    }

    // ------------------------------------------------------------------ itens

    /** Um único laço, sem if sobre o tipo (mesma regra do item 9). */
    public void listarAcervo() throws SQLException {
        String sql = "SELECT codigo, titulo, tipo, autor, edicao, disponivel FROM item ORDER BY codigo";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println("---- Acervo ----");
            while (rs.next()) {
                ItemBiblioteca item = montarItem(rs);
                System.out.println(item + detalhes(item));
            }
            System.out.println("----------------");
        }
    }

    public boolean cadastrarItem(ItemBiblioteca item) throws SQLException {
        String sql = "INSERT INTO item (codigo, titulo, tipo, autor, edicao) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.getCodigo());
            ps.setString(2, item.getTitulo());
            ps.setString(3, item.getTipo().toLowerCase());
            ps.setString(4, item.getAutor());
            ps.setString(5, item.getEdicao());
            ps.executeUpdate();
            System.out.println("OK: " + item.getTipo() + " \"" + item.getTitulo() + "\" cadastrado.");
            return true;
        } catch (SQLException e) {
            if (VIOLA_UNIQUE.equals(e.getSQLState())) {
                System.out.println("RECUSADO: já existe um item com o código " + item.getCodigo() + ".");
                return false;
            }
            throw e;
        }
    }

    /** Campos null são mantidos como estão. */
    public boolean atualizarItem(String codigo, String titulo, String autor, String edicao) throws SQLException {
        String sql = "UPDATE item SET titulo = COALESCE(?, titulo), autor = COALESCE(?, autor), "
                   + "edicao = COALESCE(?, edicao) WHERE codigo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, titulo);
            ps.setString(2, autor);
            ps.setString(3, edicao);
            ps.setString(4, codigo);
            boolean ok = ps.executeUpdate() == 1;
            System.out.println(ok ? "OK: item " + codigo + " atualizado." : "Item " + codigo + " não existe.");
            return ok;
        }
    }

    public boolean removerItem(String codigo) throws SQLException {
        ItemBiblioteca item = buscarItem(codigo);
        if (item == null) {
            System.out.println("RECUSADO: item " + codigo + " não existe.");
            return false;
        }
        if (!item.isDisponivel()) {
            System.out.println("RECUSADO: \"" + item.getTitulo() + "\" está emprestado. Devolva antes de remover.");
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM item WHERE codigo = ?")) {
            ps.setString(1, codigo);
            ps.executeUpdate();
            System.out.println("OK: \"" + item.getTitulo() + "\" removido do acervo.");
            return true;
        } catch (SQLException e) {
            if (VIOLA_FK.equals(e.getSQLState())) {
                System.out.println("RECUSADO: \"" + item.getTitulo()
                        + "\" tem histórico de empréstimos e não pode ser apagado (chave estrangeira).");
                return false;
            }
            throw e;
        }
    }

    public ItemBiblioteca buscarItem(String codigo) throws SQLException {
        String sql = "SELECT codigo, titulo, tipo, autor, edicao, disponivel FROM item WHERE codigo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montarItem(rs) : null;
            }
        }
    }

    // --------------------------------------------------------------- usuários

    public void listarUsuarios() throws SQLException {
        String sql = "SELECT u.id, u.nome, u.tipo, u.limite_itens, "
                   + "(SELECT COUNT(*) FROM emprestimo e WHERE e.usuario_id = u.id AND e.data_devolucao IS NULL) AS em_maos "
                   + "FROM usuario u ORDER BY u.id";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println("---- Usuários ----");
            while (rs.next()) {
                System.out.printf("id %-3d %-20s %-10s %d/%d itens%n", rs.getInt("id"), rs.getString("nome"),
                        rs.getString("tipo"), rs.getInt("em_maos"), rs.getInt("limite_itens"));
            }
            System.out.println("------------------");
        }
    }

    /** O limite vem do objeto (Aluno = 3, Professor = 5) e é gravado na coluna limite_itens. */
    public int cadastrarUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, tipo, limite_itens) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getTipo().toLowerCase());
            ps.setInt(3, usuario.getLimiteItens());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                System.out.println("OK: " + usuario.getNome() + " (" + usuario.getTipo() + ") cadastrado com id " + id + ".");
                return id;
            }
        }
    }

    public boolean atualizarUsuario(int id, String nome) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE usuario SET nome = ? WHERE id = ?")) {
            ps.setString(1, nome);
            ps.setInt(2, id);
            boolean ok = ps.executeUpdate() == 1;
            System.out.println(ok ? "OK: usuário " + id + " atualizado." : "Usuário " + id + " não existe.");
            return ok;
        }
    }

    public boolean removerUsuario(int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() == 1;
            System.out.println(ok ? "OK: usuário " + id + " removido." : "Usuário " + id + " não existe.");
            return ok;
        } catch (SQLException e) {
            if (VIOLA_FK.equals(e.getSQLState())) {
                System.out.println("RECUSADO: o usuário " + id
                        + " tem empréstimos registrados e não pode ser apagado (chave estrangeira).");
                return false;
            }
            throw e;
        }
    }

    // ------------------------------------------------------------ empréstimos

    public boolean emprestar(String codigo, int usuarioId, LocalDate retirada) throws SQLException {
        con.setAutoCommit(false);
        try {
            int itemId;
            ItemBiblioteca item;
            String sqlItem = "SELECT id, codigo, titulo, tipo, autor, edicao, disponivel FROM item "
                           + "WHERE codigo = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(sqlItem)) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return recusar("item " + codigo + " não existe.");
                    itemId = rs.getInt("id");
                    item = montarItem(rs);
                }
            }
            if (!item.isDisponivel()) return recusar("\"" + item.getTitulo() + "\" já está emprestado.");

            String nome, tipo;
            int limite, emMaos;
            String sqlUsuario = "SELECT nome, tipo, limite_itens, "
                    + "(SELECT COUNT(*) FROM emprestimo e WHERE e.usuario_id = u.id AND e.data_devolucao IS NULL) AS em_maos "
                    + "FROM usuario u WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return recusar("usuário " + usuarioId + " não existe.");
                    nome = rs.getString("nome");
                    tipo = rs.getString("tipo");
                    limite = rs.getInt("limite_itens");
                    emMaos = rs.getInt("em_maos");
                }
            }
            if (emMaos >= limite) {
                return recusar(nome + " atingiu o limite de " + limite + " itens (" + tipo + ").");
            }

            LocalDate prevista = retirada.plusDays(item.getPrazoDias());   // prazo vem do objeto
            String sqlEmp = "INSERT INTO emprestimo (item_id, usuario_id, data_retirada, data_devolucao_prevista) "
                          + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlEmp)) {
                ps.setInt(1, itemId);
                ps.setInt(2, usuarioId);
                ps.setDate(3, Date.valueOf(retirada));
                ps.setDate(4, Date.valueOf(prevista));
                ps.executeUpdate();
            }
            mudarDisponibilidade(itemId, false);
            con.commit();
            System.out.printf("OK: \"%s\" emprestado para %s. Devolver até %s (%d dias).%n",
                    item.getTitulo(), nome, prevista.format(BR), item.getPrazoDias());
            return true;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    /** Devolve o item e retorna a multa (0 se dentro do prazo), ou -1 se a devolução foi recusada. */
    public double devolver(String codigo, LocalDate dataDevolucao) throws SQLException {
        con.setAutoCommit(false);
        try {
            String sql = "SELECT e.id AS emp_id, e.data_retirada, i.id AS item_id, i.codigo, i.titulo, i.tipo, "
                       + "i.autor, i.edicao, i.disponivel, u.nome "
                       + "FROM emprestimo e JOIN item i ON i.id = e.item_id JOIN usuario u ON u.id = e.usuario_id "
                       + "WHERE i.codigo = ? AND e.data_devolucao IS NULL FOR UPDATE OF e, i";
            int empId, itemId;
            LocalDate retirada;
            ItemBiblioteca item;
            String nome;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        System.out.println("Devolução inválida: item " + codigo + " não está emprestado.");
                        return -1;
                    }
                    empId = rs.getInt("emp_id");
                    itemId = rs.getInt("item_id");
                    retirada = rs.getDate("data_retirada").toLocalDate();
                    item = montarItem(rs);
                    nome = rs.getString("nome");
                }
            }
            if (dataDevolucao.isBefore(retirada)) {
                con.rollback();
                System.out.println("Devolução inválida: data anterior à retirada (" + retirada.format(BR) + ").");
                return -1;
            }

            int dias = (int) ChronoUnit.DAYS.between(retirada, dataDevolucao);
            double multa = item.calcularMulta(dias);                         // multa vem do objeto

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE emprestimo SET data_devolucao = ?, valor_multa = ? WHERE id = ?")) {
                ps.setDate(1, Date.valueOf(dataDevolucao));
                ps.setBigDecimal(2, BigDecimal.valueOf(multa).setScale(2, RoundingMode.HALF_UP));
                ps.setInt(3, empId);
                ps.executeUpdate();
            }
            mudarDisponibilidade(itemId, true);
            con.commit();
            System.out.printf("Devolvido: \"%s\" por %s após %d dias. Multa: R$ %.2f%n",
                    item.getTitulo(), nome, dias, multa);
            return multa;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public void listarEmprestimos(boolean somenteAbertos) throws SQLException {
        String sql = "SELECT e.id, e.data_retirada, e.data_devolucao_prevista, e.data_devolucao, e.valor_multa, "
                   + "i.codigo, i.titulo, i.tipo, i.autor, i.edicao, i.disponivel, u.nome "
                   + "FROM emprestimo e JOIN item i ON i.id = e.item_id JOIN usuario u ON u.id = e.usuario_id "
                   + (somenteAbertos ? "WHERE e.data_devolucao IS NULL " : "")
                   + "ORDER BY e.data_retirada, e.id";
        LocalDate hoje = LocalDate.now();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println(somenteAbertos ? "---- Empréstimos em aberto ----" : "---- Todos os empréstimos ----");
            while (rs.next()) {
                LocalDate retirada = rs.getDate("data_retirada").toLocalDate();
                LocalDate prevista = rs.getDate("data_devolucao_prevista").toLocalDate();
                Date devolvidoEm = rs.getDate("data_devolucao");
                String situacao;
                if (devolvidoEm != null) {
                    situacao = String.format("devolvido em %s, multa R$ %.2f",
                            devolvidoEm.toLocalDate().format(BR), rs.getBigDecimal("valor_multa"));
                } else if (hoje.isAfter(prevista)) {
                    ItemBiblioteca item = montarItem(rs);
                    double multaAteHoje = item.calcularMulta((int) ChronoUnit.DAYS.between(retirada, hoje));
                    situacao = String.format("EM ABERTO - atrasado %d dia(s), multa até hoje R$ %.2f",
                            ChronoUnit.DAYS.between(prevista, hoje), multaAteHoje);
                } else {
                    situacao = "em aberto, no prazo";
                }
                System.out.printf("#%-3d [%s] %-25s -> %-12s retirada %s | prevista %s | %s%n",
                        rs.getInt("id"), rs.getString("codigo"), rs.getString("titulo"), rs.getString("nome"),
                        retirada.format(BR), prevista.format(BR), situacao);
            }
            System.out.println("-------------------------------");
        }
    }

    // ---------------------------------------------------------------- apoio

    private boolean recusar(String motivo) throws SQLException {
        con.rollback();
        System.out.println("RECUSADO: " + motivo);
        return false;
    }

    private void mudarDisponibilidade(int itemId, boolean disponivel) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE item SET disponivel = ? WHERE id = ?")) {
            ps.setBoolean(1, disponivel);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    /** Linha do banco -> objeto Java. Disponibilidade usa os mesmos métodos internos da Biblioteca. */
    private static ItemBiblioteca montarItem(ResultSet rs) throws SQLException {
        ItemBiblioteca item = Fabrica.novoItem(rs.getString("tipo"), rs.getString("codigo"),
                rs.getString("titulo"), rs.getString("autor"), rs.getString("edicao"));
        if (!rs.getBoolean("disponivel")) item.marcarEmprestado();
        return item;
    }

    private static String detalhes(ItemBiblioteca item) {
        StringBuilder sb = new StringBuilder();
        if (item.getAutor() != null)  sb.append(" | autor: ").append(item.getAutor());
        if (item.getEdicao() != null) sb.append(" | edição: ").append(item.getEdicao());
        return sb.toString();
    }
}
