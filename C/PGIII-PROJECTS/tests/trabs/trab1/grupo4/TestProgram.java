package trabs.trab1.grupo4;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TestProgram {
    @Test
    public void testName() {
        try {
            CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
            HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024, 4, 12), 5);
            Item lisbon = new Program("Lisbon", 2, tour, altis);
            assertEquals("Lisbon", lisbon.getName());

            CityTour tourNight = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
            Item lisbonDayNight = new Program("Lisbon day and night", 2,
                    new Program("Lisbon", 2, tour, altis),
                    tourNight);
            assertEquals("Lisbon day and night", lisbonDayNight.getName());
        }
        catch (ItensException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void testPrice() {
        try {
            CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
            HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024, 4, 12), 5);
            Item lisbon = new Program("Lisbon", 2, tour, altis);
            assertEquals(300, lisbon.getPrice());

            CityTour tourNight = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
            Item lisbonDayNight = new Program("Lisbon day and night", 2,
                    new Program("Lisbon", 2, tour, altis),
                    tourNight);
            assertEquals(375, lisbonDayNight.getPrice());
        }
        catch (ItensException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void Dates() {
        try {
            CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
            HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024, 4, 12), 5);
            Item lisbon = new Program("Lisbon", 2, tour, altis);
            assertEquals(LocalDate.of(2024,4,12), lisbon.getStartDate());
            assertEquals(LocalDate.of(2024,4,14), lisbon.getEndDate());
            CityTour tourNight = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
            Item lisbonDayNight = new Program("Lisbon day and night", 2,
                    new Program("Lisbon", 2, tour, altis),
                    tourNight);
            assertEquals(LocalDate.of(2024,4,12), lisbonDayNight.getStartDate());
            assertEquals(LocalDate.of(2024,4,14), lisbonDayNight.getEndDate());
        }
        catch (ItensException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void testToStringAndDescriptionSimple() {
        try {
            CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
            HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
            Item lisbon = new Program("Lisbon", 2, tour, altis);
            assertEquals("""
                    Travel Program: Lisbon from 2024-04-12 to 2024-04-14 (€300)
                      ***** Hotel Altis from 2024-04-12 to 2024-04-14 (€250)
                      Lisbon Tour from Alcantara to Castelo in 2024-04-13 (€50)""", lisbon.toString());
        } catch (ItensException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void ToStringAndDescription() {
        try {
            CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13"),
                     tourNight = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
            HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
            Item lisbonDayNight = new Program("Lisbon day and night", 2,
                    new Program("Lisbon", 2, tour, altis),
                    tourNight);
            assertEquals("""
                    Travel Program: Lisbon day and night from 2024-04-12 to 2024-04-14 (€375)
                      Travel Program: Lisbon from 2024-04-12 to 2024-04-14 (€300)
                        ***** Hotel Altis from 2024-04-12 to 2024-04-14 (€250)
                        Lisbon Tour from Alcantara to Castelo in 2024-04-13 (€50)
                      Lisbon Tour at night from Restauradores to Belem in 2024-04-12 (€75)""", lisbonDayNight.toString());
        } catch (ItensException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void testException() {
        CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
        HotelRoom altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2024,4,12), 5);
        ItensException ex = assertThrows(
                ItensException.class,
                () -> new Program("Lisbon", 1, tour, altis)
        );
        assertEquals("Hotel Altis - invalid item", ex.getMessage());
    }
}
