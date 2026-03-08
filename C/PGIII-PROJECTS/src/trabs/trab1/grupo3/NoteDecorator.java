package trabs.trab1.grupo3;
import java.time.LocalDate;

public abstract class NoteDecorator implements Note {
    protected Note nt;

    protected NoteDecorator(Note nt) {
        this.nt = nt;
    }

    public LocalDate getDate() {
        return this.nt.getDate();
    }
}
