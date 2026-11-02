package biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    @DisplayName("Aluno pode ter até 3 itens")
    void limiteDoAluno() {
        assertEquals(3, new Aluno("Ana").getLimiteItens());
    }

    @Test
    @DisplayName("Professor pode ter até 5 itens")
    void limiteDoProfessor() {
        assertEquals(5, new Professor("Carlos").getLimiteItens());
    }

    @Test
    @DisplayName("Usuário novo começa com 0 itens e pode emprestar")
    void usuarioNovoPodeEmprestar() {
        Usuario ana = new Aluno("Ana");
        assertEquals(0, ana.getQuantidadeEmprestada());
        assertTrue(ana.podeEmprestar());
    }

    @Test
    @DisplayName("Aluno não pode emprestar ao chegar em 3 itens")
    void alunoNoLimiteNaoPodeEmprestar() {
        Usuario ana = new Aluno("Ana");
        for (int i = 0; i < 3; i++) ana.registrarEmprestimo();
        assertFalse(ana.podeEmprestar());
    }

    @Test
    @DisplayName("Professor com 3 itens ainda pode emprestar")
    void professorComTresItensAindaPode() {
        Usuario carlos = new Professor("Carlos");
        for (int i = 0; i < 3; i++) carlos.registrarEmprestimo();
        assertTrue(carlos.podeEmprestar());
    }
}
