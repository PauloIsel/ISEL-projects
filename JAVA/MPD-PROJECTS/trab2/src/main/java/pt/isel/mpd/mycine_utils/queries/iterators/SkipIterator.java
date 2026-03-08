package pt.isel.mpd.mycine_utils.queries.iterators;

import java.util.Iterator;

public class SkipIterator<T> implements Iterator<T> {
    private final Iterator<T> srcIt;
    private int remaining;

    public SkipIterator(Iterable<T> sr, int n) {
        this.srcIt = sr.iterator();
        this.remaining = n;

        while (remaining > 0 && srcIt.hasNext()) {
            srcIt.next();
            remaining--;
        }
    }

    @Override
    public boolean hasNext() {
        return srcIt.hasNext();
    }

    @Override
    public T next() {
        return srcIt.next();
    }
}
