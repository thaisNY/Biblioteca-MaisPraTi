package biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemBibliotecaTest {

    @Test
    @DisplayName("Livro: prazo de 14 dias e multa de R$ 0,50/dia")
    void livroTemPrazoEMultaCorretos() {
        ItemBiblioteca livro = new Livro("L01", "Dom Casmurro");
        assertEquals(14, livro.getPrazoDias());
        assertEquals(0.50, livro.getMultaPorDia(), 0.001);
    }

    @Test
    @DisplayName("Revista: prazo de 7 dias e multa de R$ 1,00/dia")
    void revistaTemPrazoEMultaCorretos() {
        ItemBiblioteca revista = new Revista("R01", "Superinteressante");
        assertEquals(7, revista.getPrazoDias());
        assertEquals(1.00, revista.getMultaPorDia(), 0.001);
    }

    @Test
    @DisplayName("Item novo começa disponível")
    void itemNovoComecaDisponivel() {
        assertTrue(new Livro("L01", "Dom Casmurro").isDisponivel());
    }

    @Test
    @DisplayName("Sem multa quando devolvido dentro do prazo ou no último dia")
    void semMultaDentroDoPrazo() {
        ItemBiblioteca livro = new Livro("L01", "Dom Casmurro");
        assertEquals(0.0, livro.calcularMulta(10), 0.001);
        assertEquals(0.0, livro.calcularMulta(14), 0.001);
    }

    @Test
    @DisplayName("Multa do livro: 4 dias de atraso = R$ 2,00")
    void multaLivroComAtraso() {
        assertEquals(2.00, new Livro("L01", "X").calcularMulta(18), 0.001);
    }

    @Test
    @DisplayName("Multa da revista: 3 dias de atraso = R$ 3,00")
    void multaRevistaComAtraso() {
        assertEquals(3.00, new Revista("R01", "X").calcularMulta(10), 0.001);
    }

    @Test
    @DisplayName("Categoria nova (DVD) funciona sem alterar código existente")
    void dvdFuncionaPeloPolimorfismo() {
        ItemBiblioteca dvd = new DVD("D01", "Central do Brasil");
        assertEquals(3, dvd.getPrazoDias());
        assertEquals(4.00, dvd.calcularMulta(5), 0.001);
    }
}
