package trabs.trab2.grupo1;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static trabs.trab2.grupo1.StreamUtils.forEachIf;

public class TestForEachIf {

    @Test
    public void testCopyAll() {
        try {
            StringBuilder output = new StringBuilder();
            String input = """
                    Faca um método leia o stream de texto in e, para cada linha,
                    obtenha o valor através da aplicação da função buildValue.
                    Caso o valor obedeça ao predicado definido em pred, execute
                    a ação especificada em action passando-lhe por parâmetro
                    o valor. Retorna o número de valores que obedeceram ao
                    predicado.
                    """;
            StringReader sr = new StringReader(input);
            int c = forEachIf(new BufferedReader(sr),
                    (s) -> s,
                    (s) -> true,
                    (s) -> output.append(s + '\n')
            );
            assertEquals(6, c);
            assertEquals(input, output.toString());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    public void testDoNothing() {
        try {
            StringBuilder output = new StringBuilder();
            String input = """
                    Faca um método leia o stream de texto in e, para cada linha,
                    obtenha o valor através da aplicação da função buildValue.
                    Caso o valor obedeça ao predicado definido em pred, execute
                    a ação especificada em action passando-lhe por parâmetro
                    o valor. Retorna o número de valores que obedeceram ao
                    predicado.
                    """;
            StringReader sr = new StringReader(input);
            int c = forEachIf(new BufferedReader(sr),
                    (s) -> s,
                    (s) -> false,
                    (s) -> output.append(s + '\n')
            );
            assertEquals(0, c);
            assertEquals("", output.toString());
        } catch (IOException ex) {
            fail(ex);
        }

    }

    @Test
    public void TestBuildValue() {
        try {
            ArrayList<Integer> output = new ArrayList<>();
            String input = """
                    10
                    -200
                    3000
                    -40000
                    500000
                    -6000000
                    """;
            StringReader sr = new StringReader(input);
            int c = forEachIf(new BufferedReader(sr),
                    (s) -> Integer.parseInt( s),
                    (i) -> true,
                    (i) -> output.add(i)
            );
            assertEquals(6, c);
            assertEquals(6, output.size());
            assertEquals(List.of(10, -200, 3000, -40000, 500000, -6000000 ),
                         output);
        } catch (IOException ex) {
            fail(ex);
        }

    }

    @Test
    public void TestPredicate() {
        try {
            ArrayList<Integer> output = new ArrayList<>();
            String input = """
                    10
                    -200
                    3000
                    -40000
                    500000
                    -6000000
                    """;
            StringReader sr = new StringReader(input);
            int c = forEachIf(new BufferedReader(sr),
                    (s) -> Integer.parseInt( s),
                    (i) -> i > 0,
                    (i) -> output.add(i)
            );
            assertEquals(3, c);
            assertEquals(3, output.size());
            assertEquals(List.of(10, 3000, 500000 ), output);
        } catch (IOException ex) {
            fail(ex);
        }

    }
}
