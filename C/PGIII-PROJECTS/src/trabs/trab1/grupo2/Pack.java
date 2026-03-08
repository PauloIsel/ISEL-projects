package trabs.trab1.grupo2;

public class Pack extends Product {
    public final int units;

    public Pack(String nm, double pu, int u) {
        super(nm, pu * u);
        this.units = u;
    }

    @Override
    protected String getDescription(String prefix) {
        return super.getDescription(prefix) + " (" + this.units + " units)";
    }
}
