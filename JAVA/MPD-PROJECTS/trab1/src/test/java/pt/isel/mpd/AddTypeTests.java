package pt.isel.mpd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isel.mpd.part3.PlantUmlBuilder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AddTypeTests {

    private PlantUmlBuilder builder;

    @BeforeEach
    void setUp() {
        StringWriter writer = new StringWriter();
        builder = new PlantUmlBuilder(writer);
    }

    @Test
    void addTypes_shouldAddSingleClass() {
        builder.addTypes(String.class);
        assertTrue(builder.processesType.contains(String.class));
    }

    @Test
    void addTypes_shouldAddClassAndSuperclass() {
        builder.addTypes(Integer.class);
        assertTrue(builder.processesType.contains(Integer.class));
        assertTrue(builder.processesType.contains(Number.class));
    }

    @Test
    void addTypes_shouldAddClassAndInterfaces() {
        builder.addTypes(ArrayList.class);
        assertTrue(builder.processesType.contains(ArrayList.class));
        assertTrue(builder.processesType.contains(List.class));
        assertTrue(builder.processesType.contains(Collection.class));
    }

    @Test
    void addTypes_shouldHandleNullInput() {
        builder.addTypes((Class<?>[]) null);
        assertTrue(builder.processesType.isEmpty());
    }

    @Test
    void addTypes_shouldHandleEmptyInput() {
        builder.addTypes();
        assertTrue(builder.processesType.isEmpty());
    }

    @Test
    void addTypes_shouldNotAddObjectClass() {
        builder.addTypes(Object.class);
        assertFalse(builder.processesType.contains(Object.class));
    }
}
