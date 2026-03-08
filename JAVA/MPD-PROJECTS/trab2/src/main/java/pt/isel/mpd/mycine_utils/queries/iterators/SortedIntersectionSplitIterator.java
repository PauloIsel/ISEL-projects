package pt.isel.mpd.mycine_utils.queries.iterators;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class SortedIntersectionSplitIterator<T> extends Spliterators.AbstractSpliterator<T> {
    private final Comparator<T> cmp;
    private final Spliterator<T> spliterator1;
    private final Spliterator<T> spliterator2;
    private T next1;
    private T next2;
    private boolean initialized = false;

    public SortedIntersectionSplitIterator(Spliterator<T> s1, Spliterator<T> s2, Comparator<T> cmp) {
        super(Long.MAX_VALUE, ORDERED | NONNULL);
        this.cmp = cmp;
        this.spliterator1 = s1;
        this.spliterator2 = s2;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (!initialized) {
            next1 = spliterator1.tryAdvance(e -> next1 = e) ? next1 : null;
            next2 = spliterator2.tryAdvance(e -> next2 = e) ? next2 : null;
            initialized = true;
        }

        while (next1 != null && next2 != null) {
            int comparison = cmp.compare(next1, next2);

            if (comparison == 0) {
                action.accept(next1);
                next1 = spliterator1.tryAdvance(e -> next1 = e) ? next1 : null;
                next2 = spliterator2.tryAdvance(e -> next2 = e) ? next2 : null;
                return true;
            } else if (comparison < 0) {
                next1 = spliterator1.tryAdvance(e -> next1 = e) ? next1 : null;
            } else {
                next2 = spliterator2.tryAdvance(e -> next2 = e) ? next2 : null;
            }
        }
        
        return false;
    }
}

