package trabs.trab2.grupo1;

import java.io.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestStudents {
    @Test
    public void testCopyStudents() {
        String input = """
                LEIRT 1234 John Doe
                LEIRT 1235 Jane Doe
                LEIRT 1236 John Smith
                LEETC 1237 Jane Smith
                """;

        StringReader sr = new StringReader(input);
        StringWriter sw = new StringWriter();
        try {
            int c = Student.copyStudents(new BufferedReader(sr), new PrintWriter(sw), "LEETC");
            assertEquals(1, c);
            assertEquals("LEETC 1237 Jane Smith\n", sw.toString());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testprintStudents() {
        String inputFilePath = "alunos.txt";
        String classId = "LT53N";

        try {
            int c = Student.printStudents(inputFilePath, classId);
            assertEquals(2, c);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testCopyStudentsWithPath() {
        String inputFilePath = "alunos.txt";
        String classId = "LT52N";
        File outputFile = new File(classId + ".txt");

        try {
            int count = Student.copyStudents(inputFilePath, classId);

            assertEquals(2, count);

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            assertEquals("""
                    LT52N 10002 Maria Alves
                    LT52N 10003 Manuel Alves
                    """, output.toString());

        } catch (IOException e) {
            fail(e);
        }
    }
}
