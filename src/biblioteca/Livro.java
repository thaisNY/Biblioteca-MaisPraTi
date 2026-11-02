package biblioteca;

public class Livro extends ItemBiblioteca {
    private final String autor;

    public Livro(String codigo, String titulo) { this(codigo, titulo, null); }

    public Livro(String codigo, String titulo, String autor) {
        super(codigo, titulo);
        this.autor = autor;
    }

    @Override public int getPrazoDias()      { return 14; }
    @Override public double getMultaPorDia() { return 0.50; }
    @Override public String getTipo()        { return "Livro"; }
    @Override public String getAutor()       { return autor; }
}
