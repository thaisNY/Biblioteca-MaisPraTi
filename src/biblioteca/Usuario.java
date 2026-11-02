package biblioteca;

public abstract class Usuario {
    private final String nome;
    private int quantidadeEmprestada = 0;

    protected Usuario(String nome) { this.nome = nome; }

    /** Máximo de itens simultâneos. Cada tipo de usuário define o seu. */
    public abstract int getLimiteItens();

    public abstract String getTipo();

    public boolean podeEmprestar() {
        return quantidadeEmprestada < getLimiteItens();
    }

    // Contador muda só como consequência de emprestar()/devolver().
    void registrarEmprestimo() { quantidadeEmprestada++; }
    void registrarDevolucao()  { quantidadeEmprestada--; }

    public String getNome()               { return nome; }
    public int getQuantidadeEmprestada()  { return quantidadeEmprestada; }

    @Override
    public String toString() {
        return String.format("%s (%s) %d/%d itens", nome, getTipo(),
                quantidadeEmprestada, getLimiteItens());
    }
}
