package trabs.trab1.grupo2;

public abstract class Item {
    private final String name;

    protected Item(String nm) {
        this.name = nm;
    }

    public boolean sameName(String otherName) {
        return this.name.equals(otherName);
    }

    public abstract double getPrice();

    public final String toString() { return this.getDescription(""); }

    protected String getDescription( String prefix ) {
        return prefix + this.name + ": €" + this.getPrice();
    }
}
