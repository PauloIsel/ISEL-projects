package trabs.trab3.minesweeper.players;

import javax.swing.*;
import java.util.Map;

public class TestLeaderboard {
    public static void main(String[] args) {
        Leaderboard leaderboard = new Leaderboard();
        System.out.println("Beginner:");
        for (var entry : leaderboard.getTop10("beginner").entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }

        System.out.println("Intermediate:");
        for (var entry : leaderboard.getTop10("intermediate").entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }

        System.out.println("Advanced:");
        for (var entry : leaderboard.getTop10("advanced").entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }

//        leaderboard.addTime("beginner", "John Doe", 100);

        showTop10Leaderboard("beginner");
    }

    private static String formatTime(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static void showTop10Leaderboard(String difficulty) {
        StringBuilder message = new StringBuilder();

        Leaderboard leaderboard = new Leaderboard();

        if (leaderboard.getTop10(difficulty).isEmpty()) {
            message.append("No entries yet.");
        } else {
            for (Map.Entry<String, Integer> entry : leaderboard.getTop10(difficulty).entrySet()) {
                message.append(entry.getKey()).append(" - ").append(formatTime(entry.getValue())).append("\n");
            }
        }

        JOptionPane.showMessageDialog(null, message.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
    }
}
