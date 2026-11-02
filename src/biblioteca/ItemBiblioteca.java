package biblioteca;

public abstract class ItemBiblioteca {
    private final String codigo;
    private final String titulo;
    private boolean disponivel = true;

    protected ItemBiblioteca(String codigo, String titulo) {
        this.codigo = codigo;
        this.titulo = titulo;
    }

    /** Prazo de empréstimo em dias. Cada tipo define o seu. */
    public abstract int getPrazoDias();

    /** Multa por dia de atraso, em reais. Cada tipo define a sua. */
    public abstract double getMultaPorDia();

    /** Nome do tipo para exibição (Livro, Revista...). */
    public abstract String getTipo();

    public double calcularMulta(int diasComItem) {
        int atraso = diasComItem - getPrazoDias();
        return atraso > 0 ? atraso * getMultaPorDia() : 0.0;
    }

    // Sem setDisponivel() público: só a Biblioteca (mesmo pacote) muda isso.
    void marcarEmprestado() { disponivel = false; }
    void marcarDevolvido()  { disponivel = true; }

    /** Autor (só livros têm). Padrão: nenhum. Usado ao gravar no banco. */
    public String getAutor()  { return null; }

    /** Edição (só revistas têm). Padrão: nenhuma. Usado ao gravar no banco. */
    public String getEdicao() { return null; }

    public String getCodigo()     { return codigo; }
    public String getTitulo()     { return titulo; }
    public boolean isDisponivel() { return disponivel; }

    @Override
    public String toString() {
        return String.format("[%s] %-8s %-32s prazo %2d dias | multa R$ %.2f/dia | %s",
                codigo, getTipo(), titulo, getPrazoDias(), getMultaPorDia(),
                disponivel ? "disponível" : "emprestado");
    }
}
