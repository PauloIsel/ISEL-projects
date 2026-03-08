package trabs.trab1.grupo4;

import java.time.LocalDate;

public class CityTour extends SimpleItem {
    private final String origin;
    private final String destination;

    public CityTour(String nm, String ori, String dest, int p, String date) {
        super(nm + " from " + ori + " to " + dest, p, LocalDate.parse(date));
        this.origin = ori;
        this.destination = dest;
    }

    @Override
    public LocalDate getEndDate() {
        return start;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }
}
