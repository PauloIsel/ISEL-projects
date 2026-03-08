package pt.isel.mpd.mycine_utils.streams;

import pt.isel.mpd.mycine_utils.queries.iterators.CacheSpliterator;
import pt.isel.mpd.mycine_utils.queries.iterators.SortedIntersectionSplitIterator;

import java.util.*;

import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {
	public static <T> Supplier<Stream<T>> cache(Stream<T> src) {
		ArrayList<T> buffer = new ArrayList<>();
		Spliterator<T> spliterator = src.spliterator();
		return () -> StreamSupport.stream(new CacheSpliterator<>(spliterator, buffer), false);
	}
	
 	public static <T> Stream<T> sortedIntersection(Comparator<T> cmp, Stream<T> s1, Stream<T> s2) {
		Spliterator<T> spliterator1 = s1.spliterator();
		Spliterator<T> spliterator2 = s2.spliterator();
		return StreamSupport.stream(new SortedIntersectionSplitIterator<>(spliterator1, spliterator2, cmp),
				false);
	}

	public static <T> Stream<T> sortedIntersection(Comparator<T> cmp, Stream<T>... streams) {
		if (streams == null || streams.length == 0) {
			return Stream.empty();
		}
		if (streams.length == 1) {
			return streams[0];
		}

		Spliterator<T> resultSpliterator = Arrays.stream(streams)
				.map(Stream::spliterator)
				.reduce((spl1, spl2) -> new SortedIntersectionSplitIterator<>(spl1, spl2, cmp))
				.orElseThrow();

		return StreamSupport.stream(resultSpliterator, false);
	}
}
