package biblioteca;

public class Revista extends ItemBiblioteca {
    private final String edicao;

    public Revista(String codigo, String titulo) { this(codigo, titulo, null); }

    public Revista(String codigo, String titulo, String edicao) {
        super(codigo, titulo);
        this.edicao = edicao;
    }

    @Override public int getPrazoDias()      { return 7; }
    @Override public double getMultaPorDia() { return 1.00; }
    @Override public String getTipo()        { return "Revista"; }
    @Override public String getEdicao()      { return edicao; }
}
