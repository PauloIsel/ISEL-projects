package trabs.trab2.grupo2;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trabs.trab2.grupo2.AlgorithmUtils.reverseUnique;

public class TestReverseUnique {

    @Test
    public void testEmptyCollection() {
        List<String> result = reverseUnique(new LinkedList<>());
        assertEquals(new LinkedList<>(), result);
    }

    @Test
    public void testSingleElement() {
        List<String> result = reverseUnique(Arrays.asList("apple"));
        assertEquals(new LinkedList<>(Arrays.asList("apple")), result);
    }

    @Test
    public void testMultipleElements() {
        List<String> result = reverseUnique(Arrays.asList("apple", "banana", "cherry"));
        assertEquals(new LinkedList<>(Arrays.asList("cherry", "banana", "apple")), result);
    }

    @Test
    public void testWithDuplicates() {
        List<String> result = reverseUnique(Arrays.asList("apple", "apple", "banana", "banana", "cherry", "cherry"));
        assertEquals(new LinkedList<>(Arrays.asList("cherry", "banana", "apple")), result);
    }

    @Test
    public void testAllDuplicates() {
        List<String> result = reverseUnique(Arrays.asList("apple", "apple", "apple"));
        assertEquals(new LinkedList<>(Arrays.asList("apple")), result);
    }
}