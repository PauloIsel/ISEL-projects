package trabs.trab2.grupo2;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trabs.trab2.grupo2.AlgorithmUtils.getDecrescent;

public class TestGetDecrescent {

    @Test
    public void testEmptyCollection() {
        LinkedList<String> result = getDecrescent(new LinkedList<>());
        assertEquals(new LinkedList<>(), result);
    }

    @Test
    public void testSingleElement() {
        LinkedList<String> result = getDecrescent(Arrays.asList("apple"));
        assertEquals(new LinkedList<>(Arrays.asList("apple")), result);
    }

    @Test
    public void testMultipleElements() {
        LinkedList<String> result = getDecrescent(Arrays.asList("banana", "apple", "cherry"));
        assertEquals(new LinkedList<>(Arrays.asList("banana", "apple")), result);
    }

    @Test
    public void testWithDuplicates() {
        LinkedList<String> result = getDecrescent(Arrays.asList("banana", "apple", "banana", "cherry", "a", "apple"));
        assertEquals(new LinkedList<>(Arrays.asList("banana", "apple", "a")), result);
    }
}