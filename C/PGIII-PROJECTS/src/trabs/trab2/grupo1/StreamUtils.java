package trabs.trab2.grupo1;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamUtils {
    public static int copyWithFormat(Reader in, Writer pw) throws IOException {
        BufferedReader bf = new BufferedReader(in);
        StringBuilder sb = new StringBuilder();
        int TotalDots = 0;
        int current;
        int count = 0;

        while ((current = bf.read()) != -1) sb.append((char) current);

        for(int i = sb.length() - 1; i >= 0; i--) {
            if(Character.isDigit(sb.charAt(i))) {
                if(count == 3) {
                    sb.insert(i + 1, '.');
                    count = 1;
                    TotalDots++;
                } else {
                    count++;
                }
            } else count = 0;
        }

        pw.write(sb.toString());

        return TotalDots;
    }

    public static int copyWithFormat(String pathIn, String pathOut) throws IOException {
        try (BufferedReader bf = new BufferedReader(new FileReader(pathIn));
             BufferedWriter bw = new BufferedWriter(new FileWriter(pathOut))) {
            return copyWithFormat(bf, bw);
        }
    }


    public static String formatNumber(int number) {
        Writer w = new StringWriter();

        try {
            copyWithFormat(new StringReader(String.valueOf(number)), w);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return w.toString();
    }

    public static <V> int forEachIf(BufferedReader br, Function<String, V> buildValue, Predicate<V> pred, Consumer<V> action) throws IOException {
        String str;
        int count = 0;

        while((str = br.readLine()) != null) {
            V value = buildValue.apply(str);

            if(pred.test(value)) {
                action.accept(value);
                count++;
            }
        }

        return count;
    }
}
