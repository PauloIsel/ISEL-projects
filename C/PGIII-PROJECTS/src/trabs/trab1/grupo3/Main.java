package trabs.trab1.grupo3;

public class Main {
    public static void main(String[] args) throws NoteException {
        Note nt = new Repeating(4,
                    new Location("beach, countryside, ...",
                        new TextNote("Playday", "2024-08-01")));

        System.out.println(nt);
    }
}
