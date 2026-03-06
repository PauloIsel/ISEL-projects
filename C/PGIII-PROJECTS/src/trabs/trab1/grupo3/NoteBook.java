package trabs.trab1.grupo3;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class NoteBook implements Iterable<Note> {
    private final List<Note> notes;

    public NoteBook() {
        this.notes = new ArrayList<>();
    }

    public List<Note> getNotes(Predicate<Note> pred) {
        List<Note> result = new ArrayList<>();

        for(Note n : this.notes) {
            if(pred.test(n)) {
                result.add(n);
            }
        }

        return result;
    }

    public List<Note> notesBetween(LocalDate d1, LocalDate d2) {
        Predicate<Note> pred = n -> n.getDate().isAfter(d1) && n.getDate().isBefore(d2);
        List<Note> result = this.getNotes(pred);

        result.sort(Comparator.comparing(Note::getDate));
        return result;
    }

    public NoteBook addNote(Note nt) throws NoteException {
        Predicate<Note> pred = n -> n.getDate().equals(nt.getDate());
        if (!this.getNotes(pred).isEmpty()) {
            throw new NoteException(nt);
        }

        this.notes.add(nt);

        return this;
    }

    @Override
    public Iterator<Note> iterator() {
        return this.notes.iterator();
    }
}