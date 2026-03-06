package trabs.trab2.grupo1;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static trabs.trab2.grupo1.StreamUtils.copyWithFormat;

public class TestCopyWithFormat {

    @Test
    public void testCopyWithFormatNoDots() {
        StringReader r = new StringReader("123");
        StringWriter w = new StringWriter();
        try {
            int dots = copyWithFormat(r, w);
            assertEquals(0, dots);
            assertEquals(w.toString(), "123");
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testCopyWithFormatOneDot() {
        try {
            StringReader r = new StringReader("1234");
            StringWriter w = new StringWriter();
            assertEquals(1, copyWithFormat(r, w));
            assertEquals("1.234", w.toString());
            r = new StringReader("12345");
            w = new StringWriter();
            assertEquals(1, copyWithFormat(r, w));
            assertEquals(w.toString(), "12.345");
            r = new StringReader("123456");
            w = new StringWriter();
            assertEquals(1, copyWithFormat(r, w));
            assertEquals(w.toString(), "123.456");
        } catch (IOException e) {
            fail(e);
        }
    }
    @Test
    public void testCopyWithFormat() {
        try {
            StringReader r = new StringReader(
                    "aa: 1234567 bbb: 12345678 ... 123456789 ...");
            StringWriter w = new StringWriter();
            assertEquals(6, copyWithFormat(r, w));
            assertEquals("aa: 1.234.567 bbb: 12.345.678 ... 123.456.789 ...", w.toString());
         } catch (IOException e) {
            fail(e);
        }
    }

}

