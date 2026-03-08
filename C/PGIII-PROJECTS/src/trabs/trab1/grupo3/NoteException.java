package trabs.trab1.grupo3;

public class NoteException extends Exception {
    public NoteException(Note nt) {
        super("Duplicate info: " + nt.toString());
    }
    public NoteException(String msg) {
        super(msg);
    }
}
