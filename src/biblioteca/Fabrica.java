package biblioteca;

/**
 * Único lugar do projeto que transforma o texto "tipo" (vindo do banco ou do
 * teclado) num objeto. Escolher a classe na hora de CRIAR é inevitável; depois
 * disso, prazo, multa e limite vêm do polimorfismo, sem nenhum if por tipo.
 *
 * Categoria nova (ex.: "cd") = nova classe + uma linha aqui
 * + incluir o valor no CHECK da tabela item. Nenhum outro método muda.
 */
public final class Fabrica {
    /** Tipos aceitos (os mesmos do CHECK no banco). */
    public static final String[] TIPOS_ITEM    = {"livro", "revista", "dvd"};
    public static final String[] TIPOS_USUARIO = {"aluno", "professor"};

    private Fabrica() {}

    public static boolean aceita(String[] tipos, String tipo) {
        for (String t : tipos) {
            if (t.equalsIgnoreCase(tipo.trim())) return true;
        }
        return false;
    }

    public static ItemBiblioteca novoItem(String tipo, String codigo, String titulo,
                                          String autor, String edicao) {
        switch (tipo.trim().toLowerCase()) {
            case "livro":   return new Livro(codigo, titulo, autor);
            case "revista": return new Revista(codigo, titulo, edicao);
            case "dvd":     return new DVD(codigo, titulo);
            default: throw new IllegalArgumentException("Tipo de item desconhecido: " + tipo);
        }
    }

    public static Usuario novoUsuario(String tipo, String nome) {
        switch (tipo.trim().toLowerCase()) {
            case "aluno":     return new Aluno(nome);
            case "professor": return new Professor(nome);
            default: throw new IllegalArgumentException("Tipo de usuário desconhecido: " + tipo);
        }
    }
}
