package trabs.trab1.grupo4;

import java.time.LocalDate;

public class HotelRoom extends SimpleItem {
    public final String stars;
    private final int nights;

    public HotelRoom(String nm, int pDay, int nights, LocalDate checkIn, int stars) {
        super("Hotel " + nm, pDay * nights, checkIn);
        this.stars = "*".repeat(stars);
        this.nights = nights;
    }

    public HotelRoom(String nm, int pDay, int stars) {
        this(nm, pDay, 1, LocalDate.now(), stars);
    }

    @Override
    public LocalDate getEndDate() {
        return start.plusDays(nights);
    }

    @Override
    public String getDescription(String prefix) {
        return super.getDescription(prefix + stars + " ");
    }
}
