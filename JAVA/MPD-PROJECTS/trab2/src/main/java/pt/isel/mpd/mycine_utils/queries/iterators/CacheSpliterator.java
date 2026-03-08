package pt.isel.mpd.mycine_utils.queries.iterators;

import java.util.ArrayList;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class CacheSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

    private final ArrayList<T> buffer;
    private final Spliterator<T> spliterator;
    private int currentIndex = 0;

    public CacheSpliterator(Spliterator<T> spliterator, ArrayList<T> buffer) {
        super(buffer.size(), ORDERED | NONNULL);
        this.spliterator = spliterator;
        this.buffer = buffer;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (currentIndex < buffer.size()) {
            action.accept(buffer.get(currentIndex));
            currentIndex++;
            return true;
        } else {
            boolean advanced = spliterator.tryAdvance(elem -> {
                buffer.add(elem);
                action.accept(elem);
            });

            if (advanced) {
                currentIndex++;
            }

            return advanced;
        }
    }

}
