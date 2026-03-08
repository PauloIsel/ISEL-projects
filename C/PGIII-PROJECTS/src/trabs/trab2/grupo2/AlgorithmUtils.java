package trabs.trab2.grupo2;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AlgorithmUtils {
    public static <E, S extends Collection<E>> S getSorted(Iterable<E> seq, Comparator<E> cmp, Supplier<S> ss) {
        S sortedCollection = ss.get();
        E lastAdded = null;

        for (E e : seq) {
            if (lastAdded == null || cmp.compare(e, lastAdded) > 0) {
                sortedCollection.add(e);
                lastAdded = e;
            }
        }

        return sortedCollection;
    }

    public static LinkedList<String> getDecrescent(Collection<String> words) {
        return getSorted(words, String.CASE_INSENSITIVE_ORDER.reversed(), LinkedList::new);
    }

    public static <E> List<E> reverseUnique(Collection<E> sortedSeq) {
        List<E> reversed = new LinkedList<>();
        E lastAdded = null;

        for (E e : sortedSeq) {
            if (lastAdded == null || !lastAdded.equals(e)) {
                reversed.addFirst(e);
                lastAdded = e;
            }
        }

        return reversed;
    }

    public static <K, V> int forEachKey(Map<K,V> map, Predicate<V> filter, BiConsumer<K,V> action) {
        int count = 0;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (filter.test(entry.getValue())) {
                action.accept(entry.getKey(), entry.getValue());
                count++;
            }
        }

        return count;
    }
}
