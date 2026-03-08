package trabs.trab1.grupo3;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTextNote  {

    @Test
    public void testConstructWithHour() {
        Note nt1 = new TextNote("Test PG3",
                LocalDate.of(2024, 10, 28), 9);
        assertEquals(LocalDate.of(2024, 10, 28), nt1.getDate());
    }
    @Test
    public void testConstructWithoutHour() {
        Note nt1 = new TextNote("Class PG3","2024-09-05");
        assertEquals(LocalDate.of(2024,9,5), nt1.getDate());
    }

    @Test
    public void testToStringWithHour() {
        Note nt1 = new TextNote("Test PG3",
                LocalDate.of(2024, 10, 9), 9);
        assertEquals("\"Test PG3\" 2024-10-09 09:00", nt1.toString());
    }
    @Test
    public void testToStringWithoutHour() {
        Note nt1 = new TextNote("Class PG3","2024-09-09");
        assertEquals("\"Class PG3\" 2024-09-09", nt1.toString());
    }

}


