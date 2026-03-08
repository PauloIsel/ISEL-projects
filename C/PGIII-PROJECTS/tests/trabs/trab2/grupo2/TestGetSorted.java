package trabs.trab2.grupo2;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trabs.trab2.grupo2.AlgorithmUtils.getSorted;

public class TestGetSorted {

    @Test
    public void testEmptyList() {
        List<Integer> emptyList = Collections.emptyList();
        List<Integer> l =getSorted(emptyList, Comparator.naturalOrder(),ArrayList::new);
        assertEquals(0, l.size());
    }

    @Test
    public void testOnlyOneElement() {
        List<Integer> l =getSorted(List.of(150), Comparator.naturalOrder(),ArrayList::new);
        assertEquals(List.of(150), l);
        assertEquals(1, l.size());
        l =getSorted(List.of(150, 150, 150), Comparator.naturalOrder(),ArrayList::new);
        assertEquals(List.of(150), l);
        assertEquals(1, l.size());
    }

    @Test
    public void testOnlyFirst() {
        List<Integer> l =getSorted(List.of(30,2,1,4,10,15,20), Comparator.naturalOrder(),ArrayList::new);
        assertEquals(List.of(30), l);
         l =getSorted(List.of(30,30,2,1,4,30, 10,15,20), Comparator.naturalOrder(),ArrayList::new);
        assertEquals(List.of(30), l);
    }
    @Test
    public void testAllElements() {
        List<Integer> l =getSorted(List.of(7,10,15,20), Comparator.naturalOrder(),ArrayList::new);
        assertEquals(List.of(7, 10, 15, 20), l);
    }

    @Test
    public void testRepetitions() {
        List<Integer> l =getSorted(List.of(7,7,10,10,10,15, 15,20, 20), Comparator.naturalOrder(),ArrayList::new);
        assertEquals(List.of(7, 10, 15, 20), l);
    }

    @Test
    public void testWithRemotions() {
        List<Integer> l =getSorted(List.of(5,5,8,2,9,3,9,70,70), Comparator.naturalOrder(),ArrayList::new);
        assertEquals(List.of(5,8,9,70), l);
    }
}
