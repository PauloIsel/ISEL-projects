package trabs.trab2.grupo2;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static trabs.trab2.grupo2.AlgorithmUtils.forEachKey;

public class TestForEachKey {

    @Test
    public void testEmptyMap() {
        Map<Integer, String> map = new HashMap<>();
        int result = forEachKey(map, s -> s.length() > 3, (k, v) -> {});
        assertEquals(0, result);
    }

    @Test
    public void testSingleElement() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "apple");
        int result = forEachKey(map, s -> true, (k, v) -> {});
        assertEquals(1, result);
    }

    @Test
    public void testMultipleElements() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "banana");
        map.put(2, "apple");
        map.put(3, "cherry");
        int result = forEachKey(map, s -> s.length() > 4, (k, v) -> {});
        assertEquals(3, result);
    }

    @Test
    public void testRepeatedElements() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "banana");
        map.put(2, "apple");
        map.put(3, "cherry");
        map.put(4, "apple");
        int result = forEachKey(map, s -> s.equals("apple"), (k, v) -> {});
        assertEquals(2, result);
    }

    @Test
    public void testBiconsumer() {
        Map<Integer, String> map = new HashMap<>();
        Map<Integer, String> newMap = new HashMap<>();
        map.put(1, "banana");
        map.put(2, "apple");
        map.put(3, "cherry");
        forEachKey(map, s -> s.length() > 5, newMap::put);
        assertEquals(2, newMap.size());
        assertEquals("banana", newMap.get(1));
        assertEquals("cherry", newMap.get(3));
    }
}
