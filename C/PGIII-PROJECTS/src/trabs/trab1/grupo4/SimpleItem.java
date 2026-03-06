package trabs.trab1.grupo4;

import java.time.LocalDate;

public abstract class SimpleItem extends Item {
    protected LocalDate start;

    public SimpleItem(String nm, int p, LocalDate dt) {
        super(nm, p);
        this.start = dt;
    }

    @Override
    public LocalDate getStartDate() {
        return this.start;
    }
}
