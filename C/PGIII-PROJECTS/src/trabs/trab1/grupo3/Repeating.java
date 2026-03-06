package trabs.trab1.grupo3;


public class Repeating extends NoteDecorator {
    private final int numberOfWeeks;

    public Repeating(int nw, Note nt) throws NoteException {
        super(nt);

        if(nw < 2) throw new NoteException("Invalid number of weeks");

        this.numberOfWeeks = nw;
    }

    @Override
    public String toString() {
        return String.format(this.nt.toString() + ", repeating for %d weeks", this.numberOfWeeks);
    }
}
