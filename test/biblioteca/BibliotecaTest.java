package biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BibliotecaTest {

    private Biblioteca bib;
    private ItemBiblioteca livro1, livro2, livro3, revista1;
    private Usuario aluno, professor;

    @BeforeEach
    void preparar() {
        bib = new Biblioteca(10);
        livro1 = new Livro("L01", "Dom Casmurro");
        livro2 = new Livro("L02", "O Cortiço");
        livro3 = new Livro("L03", "Vidas Secas");
        revista1 = new Revista("R01", "Superinteressante");
        bib.cadastrar(livro1);
        bib.cadastrar(livro2);
        bib.cadastrar(livro3);
        bib.cadastrar(revista1);
        aluno = new Aluno("Ana");
        professor = new Professor("Carlos");
    }

    @Test
    @DisplayName("Empréstimo bem-sucedido deixa o item indisponível e soma 1 ao usuário")
    void emprestimoBemSucedido() {
        assertTrue(bib.emprestar("L01", aluno));
        assertFalse(livro1.isDisponivel());
        assertEquals(1, aluno.getQuantidadeEmprestada());
    }

    @Test
    @DisplayName("Empréstimo recusado quando o aluno atinge o limite de 3 itens")
    void emprestimoRecusadoPorLimite() {
        bib.emprestar("L01", aluno);
        bib.emprestar("L02", aluno);
        bib.emprestar("L03", aluno);

        assertFalse(bib.emprestar("R01", aluno));
        assertTrue(revista1.isDisponivel(), "item recusado continua disponível");
        assertEquals(3, aluno.getQuantidadeEmprestada());
    }

    @Test
    @DisplayName("Empréstimo recusado quando o item já está emprestado")
    void emprestimoRecusadoItemIndisponivel() {
        bib.emprestar("L01", aluno);
        assertFalse(bib.emprestar("L01", professor));
        assertEquals(0, professor.getQuantidadeEmprestada());
    }

    @Test
    @DisplayName("Empréstimo recusado para código inexistente")
    void emprestimoRecusadoCodigoInexistente() {
        assertFalse(bib.emprestar("X99", aluno));
        assertEquals(0, aluno.getQuantidadeEmprestada());
    }

    @Test
    @DisplayName("Devolução no prazo: sem multa, item volta a ficar disponível")
    void devolucaoNoPrazo() {
        bib.emprestar("L01", aluno);
        assertEquals(0.0, bib.devolver("L01", 10), 0.001);
        assertTrue(livro1.isDisponivel());
        assertEquals(0, aluno.getQuantidadeEmprestada());
    }

    @Test
    @DisplayName("Devolução atrasada de revista: 3 dias = R$ 3,00")
    void devolucaoAtrasadaRevista() {
        bib.emprestar("R01", aluno);
        assertEquals(3.00, bib.devolver("R01", 10), 0.001);
    }

    @Test
    @DisplayName("Devolver item que não está emprestado não altera nada")
    void devolucaoInvalida() {
        assertEquals(0.0, bib.devolver("L01", 20), 0.001);
        assertTrue(livro1.isDisponivel());
    }

    @Test
    @DisplayName("Depois de devolver, o aluno no limite pode emprestar de novo")
    void devolverLiberaNovoEmprestimo() {
        bib.emprestar("L01", aluno);
        bib.emprestar("L02", aluno);
        bib.emprestar("L03", aluno);
        bib.devolver("L02", 5);
        assertTrue(bib.emprestar("R01", aluno));
    }

    @Test
    @DisplayName("Acervo cheio recusa novo cadastro")
    void acervoCheio() {
        Biblioteca pequena = new Biblioteca(1);
        assertTrue(pequena.cadastrar(new Livro("A", "A")));
        assertFalse(pequena.cadastrar(new Livro("B", "B")));
    }
}
