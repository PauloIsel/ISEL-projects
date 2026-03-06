package trabs.trab1.grupo4;

import java.time.LocalDate;

public abstract class Item {
    private final String name;
    private final int price;

    protected Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public final String getName() { return name; }
    public final String toString() { return getDescription(""); }

    public String getDescription( String prefix ){
        LocalDate s = getStartDate(), e = getEndDate();
        return prefix +name+( s.equals( e )? " in " + s: " from "+s+" to "+e)+ " (€"+price+')';
    }

    public final int getPrice() { return price; }

    public abstract LocalDate getStartDate();
    public abstract LocalDate getEndDate();

    public static int sumPrice(Item... items) {
        int sum = 0;
        for (Item i : items) sum += i.getPrice();
        return sum;
    }
}
