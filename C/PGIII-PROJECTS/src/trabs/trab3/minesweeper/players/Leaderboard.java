package trabs.trab3.minesweeper.players;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

public class Leaderboard {
    private final String fileName = "leaderboard.txt";
    private final HashMap<String, PlayerTimes> leaderboard = new HashMap<>();
    private boolean isInitialized = false;

    private void processFile(BufferedReader reader) throws IOException {
        String line;
        String currentDifficulty = null;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.endsWith(":")) {
                currentDifficulty = line.substring(0, line.length() - 1);
            } else if (currentDifficulty != null) {
                String[] parts = line.split(" - ");
                if (parts.length == 2) {
                    int time = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    addTime(currentDifficulty, name, time);
                }
            }
        }
    }

    private void writeToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (String difficulty : new String[]{"beginner", "intermediate", "advanced"}) {
                writer.println(difficulty + ":");

                for (var entry : leaderboard.entrySet()) {
                    String name = entry.getKey();
                    PlayerTimes playerTimes = entry.getValue();
                    int time = playerTimes.getTime(difficulty);
                    if (time > 0) {
                        writer.println(time + " - " + name);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public Leaderboard() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            processFile(reader);
            isInitialized = true;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void addTime(String difficulty, String name, int time) {
        if (!leaderboard.containsKey(name)) {
            leaderboard.put(name, new PlayerTimes(name));
        }

        if (leaderboard.get(name).getTime(difficulty) == 0 || leaderboard.get(name).getTime(difficulty) > time) {
            leaderboard.get(name).setTime(difficulty, time);
            if (isInitialized) writeToFile();
        }
    }

    public LinkedHashMap<String, Integer> getTop10(String difficulty) {
        PriorityQueue<PlayerTimes> top10Players = new PriorityQueue<>((playerTime1, playerTime2) -> {
            int time1 = playerTime1.getTime(difficulty);
            int time2 = playerTime2.getTime(difficulty);
            return time2 - time1;
        });

        for (PlayerTimes playerTimes : leaderboard.values()) {
            int time = playerTimes.getTime(difficulty);
            if (time > 0) {
                top10Players.add(playerTimes);
            }

            if (top10Players.size() > 10) {
                top10Players.poll();
            }
        }

        LinkedHashMap<String, Integer> top10 = new LinkedHashMap<>();
        while (!top10Players.isEmpty()) {
            PlayerTimes playerTimes = top10Players.poll();
            top10.putFirst(playerTimes.getName(), playerTimes.getTime(difficulty));
        }

        return top10;
    }

    public int getPlayerTime(String name, String difficulty) {
        if (leaderboard.containsKey(name)) {
            return leaderboard.get(name).getTime(difficulty);
        }

        return -1;
    }
}
