package pt.isel.mpd.mycine_utils.queries.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class TakeWhileIterator<T> implements Iterator<T> {
    private final Iterator<T> srcIt;
    private final Predicate<T> pred;
    private T nextElement;
    private boolean nextEvaluated = false;
    private boolean hasNextElement;

    public TakeWhileIterator(Iterable<T> src, Predicate<T> pred) {
        this.srcIt = src.iterator();
        this.pred = pred;
    }

    @Override
    public boolean hasNext() {
        if (!nextEvaluated) {
            nextEvaluated = true;
            if (srcIt.hasNext()) {
                nextElement = srcIt.next();
                hasNextElement = pred.test(nextElement);
            } else {
                hasNextElement= false;
            }
        }
        return hasNextElement;
    }

    @Override
    public T next() {
        if (!hasNextElement) {
            throw new NoSuchElementException();
        }
        nextEvaluated = false;
        return nextElement;
    }
}