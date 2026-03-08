package pt.isel.mpd.mycine_async.sequences;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.*;

public interface Sequence<T> {
    boolean tryAdvance(Consumer<T> action);
    
    static <T> Sequence<T> from(Iterable<T> src) {
        var it = src.iterator();
        return action -> {
            if (it.hasNext()) {
                action.accept(it.next());
                return true;
            }
            else {
                return false;
            }
        };
    }
    
    static <T> Sequence<T> empty() {
        return action -> false;
    }
    
    default <U> Sequence<U> map(Function<T,U> mapper) {
        return action -> tryAdvance(t ->
                            action.accept(mapper.apply(t))
                         );
    }

    @SuppressWarnings("unchecked")
    default Sequence<T> iterate(T seed, UnaryOperator<T> oper) {
        Object[] current = {seed};
        return action -> {
            action.accept((T) current[0]);
            current[0] = oper.apply((T)current[0]);
            return true;
        };
    }

    default Sequence<T> limit(int max) {
        int[] count = {0};
        return action -> {
            if (count[0] < max) {
                count[0]++;
                return tryAdvance(action);
            }
            return false;
        };
    }

    default Sequence<T> interleave(Sequence<T> other) {
        boolean[] hasnext = {true};
        return action -> {
            if (hasnext[0] ? tryAdvance(action) : other.tryAdvance(action)) {
                hasnext[0] = !hasnext[0];
                return true;
            }
            return false;
        };
    }

   default Sequence<T> skipWhile(Predicate<T> pred) {
       return new Sequence<>() {
           private boolean skipping = true;

           @Override
           public boolean tryAdvance(Consumer<T> action) {
               return Sequence.this.tryAdvance(t -> {
                   if (skipping && !pred.test(t)) {
                       skipping = false;
                   }
                   if (!skipping) {
                       action.accept(t);
                   }
               });
           }
       };
    }

    @SuppressWarnings("unchecked")
    default <U> U reduce(U initial, BiFunction<U, T, U> accum) {
        Object[] result = {initial};
        while (tryAdvance(t -> result[0] = accum.apply((U) result[0], t)));
        return (U) result[0];
    }

    default Optional<T> min(Comparator<T> cmp) {
        return Optional.ofNullable(reduce(null, (acc, t) ->
                acc == null || cmp.compare(t, acc) < 0 ? t : acc));
    }
}
