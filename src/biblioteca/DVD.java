package biblioteca;

/** Categoria nova criada só para o teste do item 10: nenhum método existente foi alterado. */
public class DVD extends ItemBiblioteca {
    public DVD(String codigo, String titulo) { super(codigo, titulo); }

    @Override public int getPrazoDias()      { return 3; }
    @Override public double getMultaPorDia() { return 2.00; }
    @Override public String getTipo()        { return "DVD"; }
}
