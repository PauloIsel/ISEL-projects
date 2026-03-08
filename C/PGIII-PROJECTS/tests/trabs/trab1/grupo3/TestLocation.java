package trabs.trab1.grupo3;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocation {
    @Test
    public void testConstruct() throws NoteException {
        Location nt1 = new Location("E1.33", new TextNote("Class PG3", "2024-09-09"));
        assertEquals(LocalDate.of(2024,9,9), nt1.getDate());
        nt1 = new Location("G0.14", new TextNote("Trabalho 1", "2024-09-16"));
        assertEquals(LocalDate.of(2024,9,16), nt1.getDate());
    }
    @Test
    public void testToString() throws NoteException {
        Location nt1 = new Location("E1.33", new TextNote("Class PG3", "2024-09-09"));
        assertEquals("\"Class PG3\" 2024-09-09, @ E1.33", nt1.toString());
        nt1 = new Location("G0.15", new TextNote("Trabalho 1", LocalDate.of(2024,9,16), 14));
        assertEquals("\"Trabalho 1\" 2024-09-16 14:00, @ G0.15", nt1.toString());
    }
}
