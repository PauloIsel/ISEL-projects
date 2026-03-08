package trabs.trab1.grupo2;

public class Group extends Item {
    protected Item[] items;

    public Group(String nm, Item... items) {
        super(nm);
        this.items = items;
    }

    public void putInSale(String nm, int perc) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].sameName(nm)) {
                items[i] = new Sale(items[i], perc);
            }
        }
    }

    @Override
    public double getPrice() {
        double price = 0;

        for (Item item : items) {
            price += item.getPrice();
        }

        return price;
    }

    @Override
    protected String getDescription(String prefix) {
        StringBuilder str = new StringBuilder(super.getDescription(prefix));

        for (Item item : items) {
            str.append("\n").append(item.getDescription(prefix + "  "));
        }

        return str.toString();
    }
}
