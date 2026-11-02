package biblioteca;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Menu interativo no terminal, gravando no PostgreSQL.
 * Rode esta classe (Run As > Java Application) e digite no Console.
 * A classe Main continua sendo o cenário de teste pedido no item 6.
 */
public class MenuBiblioteca {
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SCRIPT = "sql/biblioteca.sql";

    private final Scanner in;
    private final BibliotecaDB bib;

    public MenuBiblioteca(Scanner in, BibliotecaDB bib) {
        this.in = in;
        this.bib = bib;
    }

    public static void main(String[] args) {
        Conexao conexao = new Conexao("db.properties");
        try (Connection con = conexao.abrir(); Scanner in = new Scanner(System.in)) {
            System.out.println("Conectado a " + conexao.getUrl());
            BibliotecaDB bib = new BibliotecaDB(con);
            if (!bib.tabelasExistem()) {
                System.out.println("Tabelas ainda não existem. Criando a partir de " + SCRIPT + "...");
                bib.recriarBanco(SCRIPT);
            }
            new MenuBiblioteca(in, bib).executar();
        } catch (SQLException e) {
            System.out.println("Não foi possível conectar ao PostgreSQL: " + e.getMessage());
            System.out.println("Confira se o serviço está rodando e se usuário/senha em db.properties estão certos.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void executar() {
        while (true) {
            System.out.println();
            System.out.println("========== BIBLIOTECA ==========");
            System.out.println(" 1  Listar acervo");
            System.out.println(" 2  Cadastrar item");
            System.out.println(" 3  Atualizar item");
            System.out.println(" 4  Remover item");
            System.out.println(" 5  Listar usuários");
            System.out.println(" 6  Cadastrar usuário");
            System.out.println(" 7  Atualizar usuário");
            System.out.println(" 8  Remover usuário");
            System.out.println(" 9  Emprestar");
            System.out.println("10  Devolver");
            System.out.println("11  Empréstimos em aberto");
            System.out.println("12  Histórico de empréstimos");
            System.out.println("13  Recriar banco com os dados de teste");
            System.out.println(" 0  Sair");
            String opcao = ler("Opção: ");
            if (opcao == null || opcao.equals("0")) {
                System.out.println("Até logo!");
                return;
            }
            try {
                switch (opcao) {
                    case "1":  bib.listarAcervo(); break;
                    case "2":  cadastrarItem(); break;
                    case "3":  atualizarItem(); break;
                    case "4":  removerItem(); break;
                    case "5":  bib.listarUsuarios(); break;
                    case "6":  cadastrarUsuario(); break;
                    case "7":  atualizarUsuario(); break;
                    case "8":  removerUsuario(); break;
                    case "9":  emprestar(); break;
                    case "10": devolver(); break;
                    case "11": bib.listarEmprestimos(true); break;
                    case "12": bib.listarEmprestimos(false); break;
                    case "13": recriar(); break;
                    default:   System.out.println("Opção inválida.");
                }
            } catch (SQLException e) {
                System.out.println("Erro no banco: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ ações

    private void cadastrarItem() throws SQLException {
        String tipo   = lerTipo(Fabrica.TIPOS_ITEM);
        String codigo = lerObrigatorio("Código (ex.: L04): ").toUpperCase();
        String titulo = lerObrigatorio("Título: ");
        String autor  = lerOpcional("Autor (livros; Enter para pular): ");
        String edicao = lerOpcional("Edição (revistas; Enter para pular): ");
        bib.cadastrarItem(Fabrica.novoItem(tipo, codigo, titulo, autor, edicao));
    }

    private void atualizarItem() throws SQLException {
        bib.listarAcervo();
        String codigo = lerObrigatorio("Código do item: ").toUpperCase();
        String titulo = lerOpcional("Novo título (Enter mantém): ");
        String autor  = lerOpcional("Novo autor (Enter mantém): ");
        String edicao = lerOpcional("Nova edição (Enter mantém): ");
        bib.atualizarItem(codigo, titulo, autor, edicao);
    }

    private void removerItem() throws SQLException {
        bib.listarAcervo();
        String codigo = lerObrigatorio("Código do item a remover: ").toUpperCase();
        if (confirmar("Remover " + codigo + "? (s/n): ")) bib.removerItem(codigo);
    }

    private void cadastrarUsuario() throws SQLException {
        String tipo = lerTipo(Fabrica.TIPOS_USUARIO);
        String nome = lerObrigatorio("Nome: ");
        bib.cadastrarUsuario(Fabrica.novoUsuario(tipo, nome));
    }

    private void atualizarUsuario() throws SQLException {
        bib.listarUsuarios();
        int id = lerInt("Id do usuário: ");
        bib.atualizarUsuario(id, lerObrigatorio("Novo nome: "));
    }

    private void removerUsuario() throws SQLException {
        bib.listarUsuarios();
        int id = lerInt("Id do usuário a remover: ");
        if (confirmar("Remover usuário " + id + "? (s/n): ")) bib.removerUsuario(id);
    }

    private void emprestar() throws SQLException {
        bib.listarAcervo();
        String codigo = lerObrigatorio("Código do item: ").toUpperCase();
        bib.listarUsuarios();
        int id = lerInt("Id do usuário: ");
        LocalDate retirada = lerData("Data de retirada (dd/mm/aaaa, Enter = hoje): ");
        bib.emprestar(codigo, id, retirada);
    }

    private void devolver() throws SQLException {
        bib.listarEmprestimos(true);
        String codigo = lerObrigatorio("Código do item devolvido: ").toUpperCase();
        LocalDate data = lerData("Data da devolução (dd/mm/aaaa, Enter = hoje): ");
        bib.devolver(codigo, data);
    }

    private void recriar() throws Exception {
        if (confirmar("Isso APAGA tudo e volta aos dados de teste. Continuar? (s/n): ")) {
            bib.recriarBanco(SCRIPT);
        }
    }

    // ---------------------------------------------------------- leitura

    /** Retorna null se a entrada acabou (Ctrl+Z / Ctrl+D). */
    private String ler(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private String lerOpcional(String prompt) {
        String s = ler(prompt);
        return (s == null || s.isEmpty()) ? null : s;
    }

    private String lerObrigatorio(String prompt) {
        while (true) {
            String s = ler(prompt);
            if (s == null) throw new IllegalArgumentException("Entrada encerrada.");
            if (!s.isEmpty()) return s;
            System.out.println("Campo obrigatório.");
        }
    }

    private String lerTipo(String[] tipos) {
        String prompt = "Tipo (" + String.join(" / ", tipos) + "): ";
        while (true) {
            String s = lerObrigatorio(prompt);
            if (Fabrica.aceita(tipos, s)) return s;
            System.out.println("Tipo inválido.");
        }
    }

    private int lerInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(lerObrigatorio(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número.");
            }
        }
    }

    private LocalDate lerData(String prompt) {
        while (true) {
            String s = lerOpcional(prompt);
            if (s == null) return LocalDate.now();
            try {
                return LocalDate.parse(s, BR);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use dd/mm/aaaa.");
            }
        }
    }

    private boolean confirmar(String prompt) {
        String s = ler(prompt);
        return s != null && s.equalsIgnoreCase("s");
    }
}
