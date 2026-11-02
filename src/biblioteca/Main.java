package biblioteca;

public class Main {
    public static void main(String[] args) {
        Biblioteca bib = new Biblioteca(10);

        // Cadastro
        bib.cadastrar(new Livro("L01", "Dom Casmurro"));
        bib.cadastrar(new Livro("L02", "O Cortiço"));
        bib.cadastrar(new Livro("L03", "Vidas Secas"));
        bib.cadastrar(new Revista("R01", "Superinteressante - Out/2026"));
        bib.cadastrar(new Revista("R02", "Ciência Hoje - Set/2026"));
        bib.cadastrar(new DVD("D01", "Central do Brasil")); // categoria nova, nada alterado

        Usuario ana = new Aluno("Ana");
        Usuario carlos = new Professor("Prof. Carlos");

        bib.listarAcervo();

        // Empréstimos bem-sucedidos
        bib.emprestar("L01", ana);
        bib.emprestar("R01", ana);
        bib.emprestar("D01", ana);
        System.out.println(ana);

        // Recusado por limite atingido (aluno: 3 itens)
        bib.emprestar("L02", ana);

        // Professor tem limite maior
        bib.emprestar("L02", carlos);
        System.out.println(carlos);

        bib.listarAcervo();

        // Devoluções: no prazo e com atraso
        bib.devolver("L01", 10); // livro, dentro dos 14 dias -> R$ 0,00
        bib.devolver("R01", 10); // revista, 3 dias de atraso -> R$ 3,00
        bib.devolver("D01", 5);  // DVD, 2 dias de atraso -> R$ 4,00
        System.out.println(ana);

        // Agora Ana pode pegar de novo
        bib.emprestar("L03", ana);

        bib.listarAcervo();
    }
}
