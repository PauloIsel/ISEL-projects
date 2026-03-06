package trabs.trab1.grupo4;

import org.junit.jupiter.api.Test;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHotelRoom {
    @Test
    public void testName() {
        HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
        assertEquals( "Hotel Altis", altis.getName());
        HotelRoom ibis = new HotelRoom("Ibis",60, 4);
        assertEquals( "Hotel Ibis", ibis.getName());
    }

    @Test
    public void testStars() {
        HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
        assertEquals( "*****", altis.stars);
        HotelRoom ibis = new HotelRoom("Ibis",60, 4);
        assertEquals( "****", ibis.stars);
    }

    @Test
    public void testDates() {
        HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
        assertEquals( LocalDate.of(2024, 4, 12), altis.getStartDate());
        assertEquals( LocalDate.of(2024, 4, 14), altis.getEndDate());
        HotelRoom ibis = new HotelRoom("Ibis",60, 4);
        assertEquals( LocalDate.now(), ibis.getStartDate());
        assertEquals( LocalDate.now().plusDays(1), ibis.getEndDate());
    }

    @Test
    public void testPrice() {
        HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
        assertEquals( 250, altis.getPrice());
        HotelRoom ibis = new HotelRoom("Ibis",60, 4);
        assertEquals( 60, ibis.getPrice());
    }

    @Test
    public void testToStringAndDescription(){
        HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
        assertEquals( "***** Hotel Altis from 2024-04-12 to 2024-04-14 (€250)", altis.getDescription(""));
        assertEquals( "***** Hotel Altis from 2024-04-12 to 2024-04-14 (€250)", altis.toString());
        HotelRoom ibis = new HotelRoom("Ibis",60, 4);
        LocalDate today = LocalDate.now();
        assertEquals( "\t- **** Hotel Ibis from "+today+" to " +today.plusDays(1)+" (€60)", ibis.getDescription("\t- "));
     }

}
