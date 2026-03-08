package pt.isel.mpd.mycine_utils.queries.iterators;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DistinctIterator<T> implements Iterator<T> {
    private final Iterator<T> srcIt;
    private final Set<T> seen = new HashSet<>();
    private T nextElem;
    private boolean nextEvaluated = false;

    public DistinctIterator(Iterable<T> src) {
        this.srcIt = src.iterator();
    }

    @Override
    public boolean hasNext() {
        if (nextEvaluated) return nextElem != null;

        while (srcIt.hasNext()) {
            T elem = srcIt.next();
            if (seen.add(elem)) {
                nextElem = elem;
                nextEvaluated = true;
                return true;
            }
        }
        nextElem = null;
        nextEvaluated = true;
        return false;
    }

    @Override
    public T next() {
        if (!hasNext() && nextElem == null) {
            throw new java.util.NoSuchElementException();
        }
        nextEvaluated = false;
        return nextElem;
    }
}