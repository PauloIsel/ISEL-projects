package trabs.trab1.grupo2;

public class Product extends Item {
    private final double price;

    public Product(String nm, double price) {
        super(nm);
        this.price = price;
    }

    @Override
    public double getPrice() {
        return this.price;
    }
}
