package pt.isel.mpd.mycine_utils;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.mycine_utils.queries.PipeIterable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class IteratorTests {

    @Test
    public void testOfMethod() {
        Integer[] input = {1, 2, 3, 4, 5};
        PipeIterable<Integer> pipe = PipeIterable.of(input);

        Iterator<Integer> iterator = pipe.iterator();
        for (Integer integer : input) {
            assertTrue(iterator.hasNext());
            Integer next = iterator.next();
            System.out.println("Iterated value: " + next);
            assertEquals(integer, next);
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    void testSkip() {
        PipeIterable<Integer> numbers = PipeIterable.of(1, 2, 3, 4, 5);
        PipeIterable<Integer> skipped = numbers.skip(2);

        List<Integer> result = new ArrayList<>();
        for (int num : skipped) {
            result.add(num);
        }

        assertEquals(List.of(3, 4, 5), result);
    }

    @Test
    void testTakeWhile() {
        PipeIterable<Integer> numbers = PipeIterable.of(1, 2, 3, 4, 5);
        PipeIterable<Integer> taken = numbers.takeWhile(n -> n < 4);

        List<Integer> result = new ArrayList<>();
        for (int num : taken) {
            result.add(num);
        }

        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void testDistinct() {
        PipeIterable<Integer> numbers = PipeIterable.of(1, 2, 2, 3, 4, 4, 5);
        PipeIterable<Integer> distinctNumbers = numbers.distinct();

        List<Integer> result = new ArrayList<>();
        for (int num : distinctNumbers) {
            result.add(num);
        }

        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    void testReduce() {
        PipeIterable<Integer> pipe = PipeIterable.of(1, 2, 3, 4, 5);

        Optional<Integer> result = pipe.reduce((acc, elem) -> acc == null ? elem : acc + elem);

        assertTrue(result.isPresent());
        assertEquals(15, result.get());
    }

    @Test
    void testReduceEmptySequence() {
        PipeIterable<Integer> emptyPipe = PipeIterable.of();

        Optional<Integer> result = emptyPipe.reduce((acc, elem) -> acc == null ? elem : acc + elem);

        assertFalse(result.isPresent());
    }

    @Test
    void testLast() {
        PipeIterable<Integer> pipe = PipeIterable.of(1, 2, 3, 4, 5);

        Optional<Integer> lastElement = pipe.last();

        assertTrue(lastElement.isPresent());
        assertEquals(5, lastElement.get());
    }

    @Test
    void testLastEmpty() {
        PipeIterable<Integer> pipe = PipeIterable.of();
        Optional<Integer> lastElement = pipe.last();
        assertTrue(lastElement.isEmpty());
    }

    @Test
    void testMax() {
        PipeIterable<Integer> numbers = PipeIterable.of(3, 1, 4, 1, 5, 9, 2, 6, 5);

        Optional<Integer> max = numbers.max(Comparator.naturalOrder());

        assertTrue(max.isPresent());
        assertEquals(9, max.get());
    }

    @Test
    void testMaxEmptyIterable() {
        PipeIterable<Integer> empty = PipeIterable.of();

        Optional<Integer> max = empty.max(Comparator.naturalOrder());

        assertTrue(max.isEmpty());
    }
}
