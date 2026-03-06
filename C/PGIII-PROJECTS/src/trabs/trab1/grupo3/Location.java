package trabs.trab1.grupo3;

public class Location extends NoteDecorator {
    public final String location;

    public Location(String l, Note nt) {
        super(nt);
        this.location = l;
    }

    public String toString() {
        return super.nt.toString() + ", @ " + this.location;
    }
}
