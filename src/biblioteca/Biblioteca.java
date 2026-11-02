package biblioteca;

public class Biblioteca {
    private final ItemBiblioteca[] acervo;
    private int totalItens = 0;

    // Quem está com cada item: emprestadoPara[i] corresponde a acervo[i].
    private final Usuario[] emprestadoPara;

    public Biblioteca(int capacidade) {
        acervo = new ItemBiblioteca[capacidade];
        emprestadoPara = new Usuario[capacidade];
    }

    public boolean cadastrar(ItemBiblioteca item) {
        if (totalItens == acervo.length) {
            System.out.println("Acervo cheio, não foi possível cadastrar " + item.getTitulo());
            return false;
        }
        acervo[totalItens++] = item;
        return true;
    }

    public boolean emprestar(String codigo, Usuario usuario) {
        int i = buscarIndice(codigo);
        if (i < 0) {
            System.out.println("RECUSADO: item " + codigo + " não existe.");
            return false;
        }
        ItemBiblioteca item = acervo[i];
        if (!item.isDisponivel()) {
            System.out.println("RECUSADO: \"" + item.getTitulo() + "\" já está emprestado.");
            return false;
        }
        if (!usuario.podeEmprestar()) {
            System.out.println("RECUSADO: " + usuario.getNome() + " atingiu o limite de "
                    + usuario.getLimiteItens() + " itens (" + usuario.getTipo() + ").");
            return false;
        }
        item.marcarEmprestado();
        usuario.registrarEmprestimo();
        emprestadoPara[i] = usuario;
        System.out.printf("OK: \"%s\" emprestado para %s. Devolver em %d dias.%n",
                item.getTitulo(), usuario.getNome(), item.getPrazoDias());
        return true;
    }

    /** Devolve o item e retorna a multa devida (0 se dentro do prazo). */
    public double devolver(String codigo, int diasComItem) {
        int i = buscarIndice(codigo);
        if (i < 0 || acervo[i].isDisponivel()) {
            System.out.println("Devolução inválida: item " + codigo + " não está emprestado.");
            return 0.0;
        }
        ItemBiblioteca item = acervo[i];
        Usuario usuario = emprestadoPara[i];
        double multa = item.calcularMulta(diasComItem);

        item.marcarDevolvido();
        usuario.registrarDevolucao();
        emprestadoPara[i] = null;

        System.out.printf("Devolvido: \"%s\" por %s após %d dias. Multa: R$ %.2f%n",
                item.getTitulo(), usuario.getNome(), diasComItem, multa);
        return multa;
    }

    /** Um único laço, sem nenhum if sobre o tipo: o polimorfismo resolve. */
    public void listarAcervo() {
        System.out.println("---- Acervo ----");
        for (int i = 0; i < totalItens; i++) {
            System.out.println(acervo[i]);
        }
        System.out.println("----------------");
    }

    private int buscarIndice(String codigo) {
        for (int i = 0; i < totalItens; i++) {
            if (acervo[i].getCodigo().equals(codigo)) return i;
        }
        return -1;
    }
}
