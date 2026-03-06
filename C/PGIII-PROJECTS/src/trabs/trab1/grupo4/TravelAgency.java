package trabs.trab1.grupo4;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TravelAgency {
    private final List<Item> items = new ArrayList<>();

    public List<Item> getItens(Predicate<Item> filter) {
        List<Item> result = new ArrayList<>();

        for (Item item : items) {
            if (filter.test(item)) {
                result.add(item);
            }
        }

        return result;
    }

    public TravelAgency append(Item p) throws ItensException {
        Predicate<Item> sameName = item -> item.getName().equals(p.getName());
        if (!this.getItens(sameName).isEmpty()) {
            throw new ItensException("\"" + p.getName() + "\" - this name already exists");
        }

        this.items.add(p);

        return this;
    }

    public List<Item> getBetween(LocalDate min, LocalDate max) {
        Predicate<Item> betweenDates = item -> !item.getStartDate().isBefore(min) && !item.getStartDate().isAfter(max);
        List<Item> result = this.getItens(betweenDates);

        result.sort(Comparator.comparing(Item::getStartDate));
        return result;
    }

    public int getUpperPrice(Comparator<Integer> cmp) {
        int upperPrice = this.items.getFirst().getPrice();

        for (Item item : this.items) {
            if (cmp.compare(item.getPrice(), upperPrice) > 0) {
                upperPrice = item.getPrice();
            }
        }

        return upperPrice;
    }

    public int getLowerPrice() {
        return getUpperPrice(Comparator.reverseOrder());
    }

    private void forEach(Comparator<Item> cmp, Consumer<Item> action) {
        List<Item> sortedItems = new ArrayList<>(this.items);
        sortedItems.sort(cmp);

        for (Item item : sortedItems) {
            action.accept(item);
        }
    }

    public void forEachPerDate(Consumer<Item> action) {
        forEach(Comparator.comparing(Item::getStartDate).thenComparing(Item::getName), action);
    }

    public void forEachByPrice(Consumer<Item> action) {
        forEach(Comparator.comparing(Item::getPrice), action);
    }
}