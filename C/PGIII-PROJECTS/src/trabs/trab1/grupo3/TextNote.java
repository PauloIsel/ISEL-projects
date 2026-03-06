package trabs.trab1.grupo3;
import java.time.LocalDate;

public class TextNote implements Note {
    private final String text;
    private final LocalDate date;
    private final int hour;

    public TextNote(String t, LocalDate dt, int h) {
        this.text = t;
        this.date = dt;
        this.hour = h;
    }

    public TextNote(String t, String dt) {
        this(t, LocalDate.parse(dt), 0 );
    }

    public LocalDate getDate() {
        return this.date;
    }

    @Override
    public String toString() {
        return this.hour == 0
                ? String.format("\"%s\" %s", this.text, this.date)
                : String.format("\"%s\" %s %02d:00", this.text, this.date, this.hour);
    }
}
