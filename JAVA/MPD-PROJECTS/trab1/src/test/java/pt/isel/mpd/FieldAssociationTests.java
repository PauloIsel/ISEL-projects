package pt.isel.mpd;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.part3.PlantUmlBuilder;

import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldAssociationTests {

    @Test
    public void testEmmitFieldAssociations() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);
        builder.addTypes(TestClass.class, AnotherClass.class);

        builder.emmitFieldAssociations();

        String output = writer.toString();
        String expected = "TestClass \"1\" o----- \"1\" AnotherClass : field\n";
        assertEquals(expected, output);
    }

    static class TestClass {
        private AnotherClass field;
    }

    static class AnotherClass {
    }

    @Test
    public void testEmmitFieldAssociationsWithOneToMany() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);
        builder.addTypes(ClassWithCollection.class, OutraClass.class);

        builder.emmitFieldAssociations();

        String output = writer.toString();
        String expected = "ClassWithCollection \"1\" o----- \"*\" List : fields\n";
        assertEquals(expected, output);
    }

    static class ClassWithCollection {
        private List<OutraClass> fields;
    }

    static class OutraClass {
    }
}