package trabs.trab1.grupo4;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

public class Program extends Item {
    private final LocalDate startDate, endDate;
    private final Item[] items;

    public Program(String nm, int days, Item... items) throws ItensException {
        super(nm, sumPrice(items));
        this.items = items;

        if (items.length == 0) {
            throw new IllegalArgumentException("Items list cannot be empty");
        }

        Arrays.sort(this.items, Comparator.comparing(Item::getStartDate));

        this.startDate = this.items[0].getStartDate();
        this.endDate = this.startDate.plusDays(days);

        for (Item item : items) {
            if (item.getEndDate().isAfter(this.endDate)) {
                throw new ItensException(item.getName() + " - invalid item");
            }
        }
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public String getDescription(String prefix) {
        StringBuilder str = new StringBuilder(super.getDescription(prefix + "Travel Program: "));

        for (Item item : items) {
            str.append("\n").append(item.getDescription(prefix + "  "));
        }

        return str.toString();
    }
}