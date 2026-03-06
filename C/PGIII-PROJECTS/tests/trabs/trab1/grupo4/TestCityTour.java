package trabs.trab1.grupo4;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCityTour {

    @Test
    public void testName() {
        CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
        assertEquals( "Lisbon Tour from Alcantara to Castelo", tour.getName());
        tour = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
        assertEquals( "Lisbon Tour at night from Restauradores to Belem", tour.getName());
    }

    @Test
    public void testDates() {
        CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
        LocalDate date = LocalDate.of(2024, 4, 13);
        assertEquals( date, tour.getStartDate());
        assertEquals( date, tour.getEndDate());
        tour = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
        date = LocalDate.of(2024, 4, 12);
        assertEquals( date, tour.getStartDate());
        assertEquals( date, tour.getEndDate());
    }

    @Test
    public void testOriAndDest() {
        CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
        assertEquals( "Alcantara", tour.getOrigin());
        assertEquals( "Castelo", tour.getDestination());
        tour = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
        assertEquals( "Restauradores", tour.getOrigin());
        assertEquals( "Belem", tour.getDestination());
    }

    @Test
    public void testPrice() {
        CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
        assertEquals( 50, tour.getPrice());
        tour = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
        assertEquals( 75, tour.getPrice());
    }

    @Test
    public void testToStringAndDescription(){
        CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
        assertEquals( "Lisbon Tour from Alcantara to Castelo in 2024-04-13 (€50)", tour.getDescription(""));
        assertEquals( "Lisbon Tour from Alcantara to Castelo in 2024-04-13 (€50)", tour.toString());
        tour = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
        assertEquals( " - Lisbon Tour at night from Restauradores to Belem in 2024-04-12 (€75)", tour.getDescription(" - "));
    }

    @Test
    public void testSumPrice( ){
        assertEquals( 0, Item.sumPrice() );

        CityTour tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2024-04-13");
        assertEquals( 50, Item.sumPrice(tour));

        CityTour tourNight = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2024-04-12");
        assertEquals( 125, Item.sumPrice(tour, tourNight));

        assertEquals( 50+75*2, Item.sumPrice(tourNight, tour, tourNight));
    }

}
