package trabs.trab1.grupo3;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRepeating {
    @Test
    public void testConstruct() throws NoteException {
        Repeating nt1 = new Repeating(15, new TextNote("Class PG3", "2024-09-09"));
        assertEquals(LocalDate.of(2024,9,9), nt1.getDate());
        nt1 = new Repeating(6, new TextNote("Trabalho 1", "2024-09-16"));
        assertEquals(LocalDate.of(2024,9,16), nt1.getDate());
    }
    @Test
    public void testToString() throws NoteException {
        Repeating nt1 = new Repeating(15, new TextNote("Class PG3", "2024-09-09"));
        assertEquals("\"Class PG3\" 2024-09-09, repeating for 15 weeks", nt1.toString());
        nt1 = new Repeating(6, new TextNote("Trabalho 1", LocalDate.of(2024,9,16), 14));
        assertEquals("\"Trabalho 1\" 2024-09-16 14:00, repeating for 6 weeks", nt1.toString());
    }
}
