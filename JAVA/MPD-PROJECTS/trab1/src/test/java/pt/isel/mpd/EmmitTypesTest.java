package pt.isel.mpd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isel.mpd.part3.PlantUmlBuilder;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class EmmitTypesTest {

    private PlantUmlBuilder builder;
    private StringWriter writer;

    @BeforeEach
    void setUp() {
        writer = new StringWriter();
        builder = new PlantUmlBuilder(writer);
    }

    @Test
    void emmitTypes_shouldEmitSingleClass() {
        builder.typeFilter(type -> true)
                .memberFilter(member -> true)
                .addTypes(Dummy.class)
                .emmitTypes();
        String expected = "class Dummy {\n  {static} data : String\n  Dummy()\n  +{static} getData() : String\n  " +
                "+setData(data : String) : void\n  +{abstract} methods() : void\n}\n";
        assertEquals(expected, writer.toString());
    }

    @Test
    void emmitTypes_shouldEmitClassWithFields() {
        builder.typeFilter(type -> true)
                .memberFilter(member -> true)
                .addTypes(Flight.class)
                .emmitTypes();
        String expected = "class Flight {\n  {static} flightNumber : Integer\n  {static} departureTime : Date\n  " +
                "Flight()\n  +{static} getFlightNumber() : Integer\n  +{static} setFlightNumber(flightNumber : Integer) : void\n  " +
                "+{static} getDepartureTime() : Date\n  +{static} setDepartureTime(departureTime : Date) : void\n  " +
                "+{abstract} fly() : void\n}\n";
        assertEquals(expected, writer.toString());
    }

    @Test
    void emmitTypes_shouldHandleEmptyTypes() {
        builder.typeFilter(type -> true)
                .memberFilter(member -> true)
                .emmitTypes();
        assertEquals("", writer.toString());
    }

    @Test
    void emmitTypes_shouldHandleNullTypes() {
        builder.typeFilter(type -> true)
                .memberFilter(member -> true)
                .addTypes((Class<?>[]) null)
                .emmitTypes();
        assertEquals("", writer.toString());
    }
}