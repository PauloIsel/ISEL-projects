package trabs.trab2.grupo2;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static trabs.trab2.grupo2.AlgorithmUtils.forEachKey;

public class Document {
    public static class OcurrencesCounter {
        private final String word;
        private int file1;
        private int file2;

        public OcurrencesCounter(String word) {
            this.word = word;
            this.file1 = 0;
            this.file2 = 0;
        }

        public String getWord() {
            return this.word;
        }

        public int getFile1() {
            return this.file1;
        }

        public int getFile2() {
            return this.file2;
        }

        public int getTotal() {
            return this.file1 + this.file2;
        }

        public void incrementFile1() {
            this.file1++;
        }

        public void incrementFile2() {
            this.file2++;
        }

        public boolean areSame() {
            return this.file1 == this.file2;
        }

        public boolean existsInBothFiles() {
            return this.file1 > 0 && this.file2 > 0;
        }
    }

    private final Map<String, OcurrencesCounter> wordCount = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public Document(String pathname1, String pathname2, JTextField infoField) {
        try (BufferedReader br1 = new BufferedReader(new FileReader(pathname1));
             BufferedReader br2 = new BufferedReader(new FileReader(pathname2))) {

            processFile(br1, OcurrencesCounter::incrementFile1);
            processFile(br2, OcurrencesCounter::incrementFile2);
        } catch (IOException e) {
            infoField.setText("Error: " + e.getMessage());
        }
    }

    private void processFile(BufferedReader reader, Consumer<OcurrencesCounter> incrementFunction) throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            String[] words = line.split(" ");
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z0-9]", "");
                if (!word.isEmpty()) {
                    this.wordCount.putIfAbsent(word, new OcurrencesCounter(word));
                    incrementFunction.accept(this.wordCount.get(word));
                }
            }
        }
    }

    public int calculateSimilarity() {
        int similarity = 0;

        for (OcurrencesCounter ocurrencesCounter : this.wordCount.values()) {
            if (!ocurrencesCounter.areSame()) {
                similarity++;
            }
        }

        return similarity;
    }

    public void getAllWords(Consumer<OcurrencesCounter> consumer) {
        ArrayList<OcurrencesCounter> allWords = new ArrayList<>(this.wordCount.values());
        allWords.sort(Comparator.comparing(OcurrencesCounter::getTotal).reversed().thenComparing(OcurrencesCounter::getWord));
        allWords.forEach(consumer);
    }

    public void getTop20Words(Consumer<OcurrencesCounter> consumer) {
        PriorityQueue<OcurrencesCounter> top20Words = new PriorityQueue<>(Comparator.comparingInt(OcurrencesCounter::getTotal).thenComparing(OcurrencesCounter::getWord, Comparator.reverseOrder()));

        forEachKey(this.wordCount, ocurrencesCounter -> true, (word, ocurrencesCounter) -> {
            top20Words.add(ocurrencesCounter);
            if (top20Words.size() > 20) {
                top20Words.poll();
            }
        });

        while (!top20Words.isEmpty()) {
            consumer.accept(top20Words.poll());
        }
    }

    private void execForEach(Predicate<OcurrencesCounter> filter, Consumer<String> consumer) {
        forEachKey(this.wordCount, filter, (word, ocurrencesCounter) -> consumer.accept(word));
    }

    public void getSameOcurrencesWords(Consumer<String> consumer, int k) {
        execForEach(ocurrencesCounter -> ocurrencesCounter.existsInBothFiles() && ocurrencesCounter.getTotal() == k, consumer);
    }

    public void getOnlyInOneFileWords(Consumer<String> consumer, int fileN) {
        execForEach(ocurrencesCounter -> fileN == 1 ? ocurrencesCounter.getFile2() == 0 : ocurrencesCounter.getFile1() == 0, consumer);
    }
}
