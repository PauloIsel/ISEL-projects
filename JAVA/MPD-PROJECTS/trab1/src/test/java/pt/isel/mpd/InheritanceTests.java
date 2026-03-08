package pt.isel.mpd;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.part3.PlantUmlBuilder;

import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InheritanceTests {

    @Test
    public void testEmmitInheritanceAssociations() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);

        builder.addTypes(SubClass.class, SuperClass.class)
                .emmitInheritanceAssociations();

        String expectedOutput = "SuperClass <|-- SubClass\n";
        assertEquals(expectedOutput, writer.toString());
    }

    @Test
    public void testEmmitInheritanceAssociationsWithMultipleClasses() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);

        builder.addTypes(SubClass.class, SuperClass.class, AnotherClass.class)
                .emmitInheritanceAssociations();

        String expectedOutput = "SuperClass <|-- SubClass\nSuperClass <|-- AnotherClass\n";
        assertEquals(expectedOutput, writer.toString());
    }

    @Test
    public void testEmmitInheritanceAssociationsWithNoInheritance() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);

        builder.addTypes(UnrelatedClass.class)
                .emmitInheritanceAssociations();

        String expectedOutput = "";
        assertEquals(expectedOutput, writer.toString());
    }

    static class SuperClass {}
    static class SubClass extends SuperClass {}
    static class AnotherClass extends SuperClass {}
    static class UnrelatedClass {}
}