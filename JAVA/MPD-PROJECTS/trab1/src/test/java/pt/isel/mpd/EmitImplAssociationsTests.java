package pt.isel.mpd;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.part3.PlantUmlBuilder;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmitImplAssociationsTests {

    @Test
    public void testEmitImplAssociations() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);

        builder.addTypes(SingleClass.class, AnotherInterface.class)
                .emmitImplAssociations();

        String expectedOutput = "AnotherInterface <|.. SingleClass\n";
        assertEquals(expectedOutput, writer.toString());
    }

    @Test
    public void testEmitImplAssociationsWithMultipleInterfaces() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);

        builder.addTypes(ConcreteClass.class, Interface.class, AnotherInterface.class)
                .emmitImplAssociations();

        String expectedOutput = "Interface <|.. ConcreteClass\nAnotherInterface <|.. ConcreteClass\n";
        assertEquals(expectedOutput, writer.toString());
    }

    @Test
    public void testEmitImplAssociationsWithNoInterfaces() {
        StringWriter writer = new StringWriter();
        PlantUmlBuilder builder = new PlantUmlBuilder(writer);

        builder.addTypes(UnrelatedClass.class)
                .emmitImplAssociations();

        String expectedOutput = "";
        assertEquals(expectedOutput, writer.toString());
    }

    interface Interface {}
    interface AnotherInterface {}
    static class SingleClass implements AnotherInterface{}
    static class ConcreteClass implements Interface, AnotherInterface {}
    static class UnrelatedClass {}
}
