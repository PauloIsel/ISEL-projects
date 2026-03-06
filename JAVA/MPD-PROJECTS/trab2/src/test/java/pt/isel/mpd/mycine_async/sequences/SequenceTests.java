package pt.isel.mpd.mycine_async.sequences;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static pt.isel.mpd.mycine_async.sequences.Sequence.empty;
import static pt.isel.mpd.mycine_async.sequences.Sequence.from;

public class SequenceTests {
    @Test
    public void emptySequenceTest() {
        var seq = empty();
        
        assertFalse(seq.tryAdvance((t) -> {
            System.out.println("Não é suposto chegar aqui!");
        }));
    }
    
    @Test
    public void fromIterableSequenceTest() {
        var elems = List.of(1,2,3);
        var seq = from(elems);
        var result = new ArrayList<Integer>();
        while(seq.tryAdvance(result::add));
        assertEquals(elems, result);
    }

    @Test
    public void iterateIntegerSequenceTest() {
        // Cria uma sequência que incrementa em 1 a cada elemento
        var seq = Sequence.<Integer>empty().iterate(1, i -> i + 1);

        var result = new ArrayList<Integer>();
        int count = 0;
        // Limita a 5 elementos para evitar um loop infinito
        while(count < 5 && seq.tryAdvance(result::add)) {
            count++;
        }

        // Verifica os primeiros 5 elementos da sequência
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    public void limitSequenceTest() {
        var elems = List.of(1,2,3,4,5,6);

        var seq = from(elems).limit(3);
        var result = new ArrayList<Integer>();

        while(seq.tryAdvance(result::add));
        assertEquals(List.of(1,2,3), result);
    }

    @Test
    public void interleaveEqualSizeSequencesTest() {
        var seq1 = from(List.of(1, 3, 5));
        var seq2 = from(List.of(2, 4, 6));

        var interleaved = seq1.interleave(seq2);
        var result = new ArrayList<Integer>();

        while(interleaved.tryAdvance(result::add));

        result.forEach(System.out::println);
        // Deve intercalar os elementos: 1,2,3,4,5,6
        assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    public void interleaveFirstShorterTest() {
        var seq1 = from(List.of(1, 3));
        var seq2 = from(List.of(2, 4, 6, 8));

        var interleaved = seq1.interleave(seq2);
        var result = new ArrayList<Integer>();

        while(interleaved.tryAdvance(result::add));

        result.forEach(System.out::println);
        // Deve parar quando a primeira sequência terminar
        assertEquals(List.of(1, 2, 3, 4), result);
    }

    @Test
    public void interleaveSecondShorterTest() {
        var seq1 = from(List.of(1, 3, 5, 7));
        var seq2 = from(List.of(2, 4));

        var interleaved = seq1.interleave(seq2);
        var result = new ArrayList<Integer>();

        while(interleaved.tryAdvance(result::add));

        result.forEach(System.out::println);
        // Deve parar quando a segunda sequência terminar
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    public void skipWhileTest() {
        var elems = List.of(1, 2, 3, 4, 5);
        var seq = from(elems).skipWhile(i -> i < 3);
        var result = new ArrayList<Integer>();
        while(seq.tryAdvance(result::add));
        assertEquals(List.of(3, 4, 5), result);
    }

    @Test
    public void reduceSumTest() {
        var elems = List.of(1, 2, 3, 4);
        var seq = from(elems);
        Integer sum = seq.reduce(0, Integer::sum);
        assertEquals(10, sum);
    }

    @Test
    public void minTest() {
        var elems = List.of(5, 2, 8, 1, 4);
        var seq = from(elems);
        var min = seq.min(Integer::compareTo);
        assertEquals(Optional.of(1), min);
    }
}
