package pt.isel.mpd.mycine_utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pt.isel.mpd.mycine_utils.streams.StreamUtils.*;


public class StreamUtilsTests {

	@Test
	public void testCachePreservesAllElements() {
		Stream<Integer> original = Stream.of(1, 2, 3, 4, 5);
		List<Integer> expected = List.of(1, 2, 3, 4, 5);

		Supplier<Stream<Integer>> cached = cache(original);
		List<Integer> result = cached.get().collect(Collectors.toList());
		result.forEach(System.out::println);

		assertEquals(expected, result);
	}

	@Test
	public void testCacheAllowsMultipleConsumption() {
		Stream<String> original = Stream.of("a", "b", "c");

		Supplier<Stream<String>> cached = cache(original);

		assertEquals(3, cached.get().count());
		assertEquals(3, cached.get().count());
		assertEquals("abc", cached.get().collect(Collectors.joining()));

	}

	@Test
	public void testCacheWithEmptyStream() {
		Stream<Object> empty = Stream.empty();

		Supplier<Stream<Object>> cached = cache(empty);

		assertEquals(0, cached.get().count());
	}

	@Test
	public void testCacheWithTransformations() {
		Stream<Integer> original = Stream.of(1, 2, 3, 4, 5);

		Supplier<Stream<Integer>> cached = cache(original);

		assertEquals(15, cached.get().mapToInt(i -> i).sum());
		assertEquals(2, cached.get().filter(i -> i % 2 == 0).count());
	}

	@Test
	public void testCacheSavesOperationsFromBeingExecutedMultipleTimes() {
		int[] counter = {0};
		List<Integer> result = new ArrayList<>();

		Stream<Integer> original = Stream.of(1, 2, 3, 4, 5)
				.map(i -> {
					counter[0]++;
					result.add(i * 2);
					return i * 2;
				});
		result.forEach(System.out::println);

		Supplier<Stream<Integer>> cached = cache(original);

		assertEquals(0, counter[0]);

		cached.get().forEach(i -> {});
		cached.get().forEach(i -> {});

		assertEquals(5, counter[0]);
	}
	
	@Test
	public void intersectionTest() {
		Comparator<Integer> cmp = Comparator.comparing((Integer v) -> v);

		Stream<Integer> ints1 = Stream.of(3, 1, 5, 9, 7, 7).sorted(cmp);
		Stream<Integer> ints2 = Stream.of(5, 3, 4, 7, 10, 12).sorted(cmp);
		var expected = List.of(3, 5, 7);

		Stream<Integer> res = sortedIntersection(cmp, ints1, ints2);
		var list = res.collect(Collectors.toList());
		System.out.println(list);

		assertEquals(expected, list);
	}

	@Test
	public void intersectionWithNoCommonElementsTest() {
		Comparator<Integer> cmp = Comparator.comparing((Integer v) -> v);

		Stream<Integer> ints1 = Stream.of(1, 2, 3).sorted(cmp);
		Stream<Integer> ints2 = Stream.of(4, 5, 6).sorted(cmp);
		var expected = List.of();

		Stream<Integer> res = sortedIntersection(cmp, ints1, ints2);
		var list = res.collect(Collectors.toList());
		System.out.println(list);

		assertEquals(expected, list);
	}

	@Test
	public void intersectionWithDuplicatesTest() {
		Comparator<Integer> cmp = Comparator.comparing((Integer v) -> v);

		Stream<Integer> ints1 = Stream.of(1, 2, 2, 4, 6).sorted(cmp);
		Stream<Integer> ints2 = Stream.of(2, 2, 4, 6, 8, 10).sorted(cmp);
		var expected = List.of(2, 2, 4, 6);

		Stream<Integer> res = sortedIntersection(cmp, ints1, ints2);
		var list = res.collect(Collectors.toList());
		System.out.println(list);

		assertEquals(expected, list);
	}

    @Test
    public void sortedIntersectionWithThreeStreamsTest() {
        Comparator<Integer> cmp = Comparator.comparingInt(i -> i);

        Stream<Integer> s1 = Stream.of(1, 2, 3, 4, 5);
        Stream<Integer> s2 = Stream.of(2, 3, 4, 6);
        Stream<Integer> s3 = Stream.of(0, 2, 4, 8);

        var expected = List.of(2, 4);

        var result = sortedIntersection(cmp, s1.sorted(cmp), s2.sorted(cmp), s3.sorted(cmp));
        var list = result.collect(Collectors.toList());

        assertEquals(expected, list);
    }

    @Test
    public void sortedIntersectionWithNoCommonElementsTest() {
        Comparator<Integer> cmp = Comparator.comparingInt(i -> i);

        Stream<Integer> s1 = Stream.of(1, 2, 3);
        Stream<Integer> s2 = Stream.of(4, 5, 6);
        Stream<Integer> s3 = Stream.of(7, 8, 9);

        var expected = List.of();

        var result = sortedIntersection(cmp, s1.sorted(cmp), s2.sorted(cmp), s3.sorted(cmp));
        var list = result.collect(Collectors.toList());

        assertEquals(expected, list);
    }
}
