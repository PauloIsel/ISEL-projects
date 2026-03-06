package trabs.trab1.grupo4;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTravelAgency {
    private static int[] allPrices;
    private static TravelAgency travel;

    private TravelAgency getTravelAgencyInstance() throws ItensException {
        if (travel == null) {
            Item altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2050, 4, 12), 5),
                 ibis = new HotelRoom("Ibis", 60, 4),
                 tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2050-04-13"),
                 tourNight = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2050-04-12"),
                 lisbon = new Program("Lisbon", 2, tour, altis);
            travel = new TravelAgency();
            travel.append(altis).append(tour).append(tourNight).append(lisbon).append(ibis);
            allPrices = new int[]{altis.getPrice(), tour.getPrice(), tourNight.getPrice(), lisbon.getPrice(), ibis.getPrice()};
            Arrays.sort(allPrices);
        }
        return travel;
    }

    @Test
    public void testAppendOne() {
        try {
            Item altis = new HotelRoom("Altis", 125, 2, LocalDate.of(2050, 4, 12), 5);
            TravelAgency travel = new TravelAgency();
            travel.append(altis);
        }
        catch (ItensException e) {
            fail("unexpected exception", e);
        }
    }

    @Test
    public void testAppendTwo()  {
        try {
            Item tour = new CityTour("Lisbon Tour", "Alcantara", "Castelo", 50, "2050-04-13"),
                 tourNight = new CityTour("Lisbon Tour at night", "Restauradores", "Belem", 75, "2050-04-12");
            TravelAgency travel = new TravelAgency();
            travel.append(tour).append(tourNight);
        }
        catch (ItensException e) {
            fail("unexpected exception", e);
        }
    }

    @Test
    public void testGetBetween() throws ItensException {
        TravelAgency travel = getTravelAgencyInstance();
        List<Item> res = travel.getBetween(LocalDate.of(2050, 4, 12), LocalDate.of(2050, 4, 13));
        assertEquals(4, res.size());

        Item lastItem = null;
        for (Item i : res) {
            if (lastItem != null) {
                assertTrue(lastItem.getStartDate().isBefore(i.getStartDate()) ||
                        lastItem.getStartDate().isEqual(i.getStartDate()));
            }
            lastItem = i;
        }
    }

    @Test
    public void testforEachByPrice() throws ItensException {
        TravelAgency travel = getTravelAgencyInstance();
        ArrayList<Item> res = new ArrayList<>();
        travel.forEachByPrice((item) -> res.add(item));
        int index = 0;
        assertEquals(allPrices.length, res.size());
        Item lastItem = null;
        for (Item i : res) {
            assertEquals(i.getPrice(), allPrices[index++]);
            if (lastItem != null) {
                assertTrue(lastItem.getPrice() < i.getPrice() ||
                        lastItem.getPrice() == i.getPrice() &&
                                lastItem.getName().compareTo(i.getName()) < 0);
            }
            lastItem = i;
        }
    }

    @Test
    public void testforEachByDate() throws ItensException {
        TravelAgency travel = getTravelAgencyInstance();
        List<String> expected = Arrays.asList("Hotel Ibis", // Hoje
                                              "Hotel Altis", //12-04-2050
                                              "Lisbon",      // 12-04-2050
                                              "Lisbon Tour at night from Restauradores to Belem", //12-04-2050
                                              "Lisbon Tour from Alcantara to Castelo"); // 13-04-2050

        ArrayList<String> res = new ArrayList<>();
        travel.forEachPerDate((item) -> res.add(item.getName()));
        assertEquals(expected, res);
    }

    @Test
    public void testUpperAndLowerPrice() throws ItensException {
        TravelAgency travel = getTravelAgencyInstance();
        assertEquals(allPrices[allPrices.length - 1], travel.getUpperPrice(Integer::compareTo));
        assertEquals(allPrices[0], travel.getLowerPrice());
    }
}