package pt.isel.mpd.mycine_utils.queries.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class OfIterator<T> implements Iterator<T> {

    private final T[] elements;
    private int index = 0;

    public OfIterator(T[] elements) {
        this.elements = elements;
    }

    @Override
    public boolean hasNext() {
        return index < elements.length;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return elements[index++];
    }
}