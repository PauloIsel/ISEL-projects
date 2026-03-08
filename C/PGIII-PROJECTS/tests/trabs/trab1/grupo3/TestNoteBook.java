package trabs.trab1.grupo3;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class TestNoteBook {
    @Test
    public void testAddOneNote() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3", LocalDate.of(2024, 1, 9), 19);

            nb.addNote(nt1);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testAddNotes() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3", LocalDate.of(2024, 1, 9), 19);
            Note nt2 = new Location("E1.12", new TextNote("Class PG3", "2023-09-11"));
            nb.addNote(nt1).addNote(nt2);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testNotesBetweenEmppty() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            nb.addNote(nt1).addNote(nt2);

            List<Note> l = nb.notesBetween(LocalDate.parse("2023-01-01"),
                                           LocalDate.parse("2023-08-01"));
            assertEquals(0, l.size());

            l = nb.notesBetween(LocalDate.parse("2024-01-10"),
                    LocalDate.parse("2024-08-01"));
            assertEquals(0, l.size());

        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }
    @Test
    public void testNotesBetweenAll() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            nb.addNote(nt1).addNote(nt2);

            List<Note> l = nb.notesBetween(LocalDate.parse("2023-01-01"),
                                           LocalDate.parse("2024-12-31"));
            assertEquals(2, l.size());
            assertEquals(Arrays.asList(nt2, nt1), l);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testNotesBetweenOnlyLast() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            Note nt3 = new TextNote("Trab 1", "2023-09-21");
            nb.addNote(nt1).addNote(nt2).addNote(nt3);

            List<Note> l = nb.notesBetween(LocalDate.parse("2023-09-12"),
                                           LocalDate.parse("2024-01-08"));
            assertEquals(1, l.size());
            assertEquals(l.get(0), nt3);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }
    @Test
    public void testNotesBetweenOnlyFirst() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            Note nt3 = new TextNote("Trab 1", "2023-09-30");
            nb.addNote(nt1).addNote(nt2).addNote(nt3);

            List<Note> l = nb.notesBetween(LocalDate.parse("2024-01-01"),
                    LocalDate.parse("2024-12-31"));
            assertEquals(1, l.size());
            assertEquals(l.get(0), nt1);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testNotesBetweenOnly() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            Note nt3 = new TextNote("Trab 1", "2023-09-30");

            nb.addNote(nt1).addNote(nt2).addNote(nt3);

            List<Note> l = nb.notesBetween(LocalDate.parse("2023-01-01"),
                    LocalDate.parse("2023-09-29"));
            assertEquals(1, l.size());
            assertEquals(l.get(0), nt2);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testGetNotesEmpty() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            nb.addNote(nt1).addNote(nt2);

            Predicate<Note> alwaysFalse = new Predicate<Note>() {
                @Override
                public boolean test(Note note) {
                    return false;
                }
            };
            List<Note> l = nb.getNotes(alwaysFalse);
            assertEquals(0, l.size());
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }
    @Test
    public void testGetNotesAll() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            nb.addNote(nt1).addNote(nt2);

            Predicate<Note> allNotes = new Predicate<Note>() {
                @Override
                public boolean test(Note note) {
                    return true;
                }
            };
            List<Note> l = nb.getNotes(allNotes);
            assertEquals(2, l.size());
            assertEquals(Arrays.asList(nt1, nt2), l);

        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testGetNotesOnlyLast() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            Note nt3 = new TextNote("Trab 1", "2023-09-21");
            nb.addNote(nt1).addNote(nt2).addNote(nt3);

            List<Note> l = nb.getNotes(n -> n.getDate().equals(LocalDate.parse("2023-09-21")));
            assertEquals(1, l.size());
            assertEquals(l.get(0), nt3);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }
    @Test
    public void testGetNotesOnlyFirst() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            Note nt3 = new TextNote("Trab 1", "2023-09-30");
            nb.addNote(nt1).addNote(nt2).addNote(nt3);

            List<Note> l = nb.getNotes(n -> n.toString().contains("Test"));
            assertEquals(1, l.size());
            assertEquals(l.get(0), nt1);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testGetNotes() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new Repeating(10, new TextNote("Class PG3", "2023-09-11"));
            Note nt3 = new TextNote("Trab 1", "2023-09-30");
            nb.addNote(nt1).addNote(nt2).addNote(nt3);

            List<Note> l = nb.getNotes(n -> n instanceof Repeating);
            assertEquals(1, l.size());
            assertEquals(l.get(0), nt2);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

    @Test
    public void testIterableEmpty() {
            NoteBook nb = new NoteBook();
            assertFalse(nb.iterator().hasNext());
    }
    @Test
    public void testIterable() {
        try {
            NoteBook nb = new NoteBook();
            Note nt1 = new TextNote("Test PG3","2024-01-09");
            Note nt2 = new TextNote("Class PG3", "2023-09-11");
            nb.addNote(nt1).addNote(nt2);
            List<Note> l = new ArrayList<>();
            for (Note n: nb) {
                l.add(n);
            }
            assertEquals(Arrays.asList(nt1, nt2), l);
        } catch (Exception ex) {
            fail("Should not have thrown any exception", ex);
        }
    }

}
