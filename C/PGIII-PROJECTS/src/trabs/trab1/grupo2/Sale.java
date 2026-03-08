package trabs.trab1.grupo2;

public class Sale extends Item implements Reduction {
    private final Item itemBase;
    private final int percentage;

    private static int calculateTotalPercentage(Item iBase, int perc) {
        if (iBase instanceof Reduction reduction) {
            return perc + reduction.getPercentage();
        } else {
            return perc;
        }
    }

    public Sale(Item iBase, int perc) {
        super("desconto de " + calculateTotalPercentage(iBase, perc) + "%");

        if (iBase instanceof Reduction reduction) {
            this.itemBase = reduction.getItemBase();
            this.percentage = calculateTotalPercentage(iBase, perc);
        } else {
            this.itemBase = iBase;
            this.percentage = perc;
        }
    }

    @Override
    public Item getItemBase() {
        return this.itemBase;
    }

    @Override
    public int getPercentage() {
        return this.percentage;
    }

    @Override
    public double getPrice() {
        return this.itemBase.getPrice() * (1 - this.percentage / 100.0);
    }

    @Override
    protected String getDescription(String prefix) {
        return this.itemBase.getDescription(prefix) + super.getDescription(" com ");
    }
}