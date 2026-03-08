package trabs.trab3.minesweeper.players;

public class PlayerStats {
    private String name;
    private int gamesWon;
    private int gamesLost;

    public PlayerStats(String name) {
        this.name = name;
        this.gamesWon = 0;
        this.gamesLost = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public int getWinRate() {
        if (gamesWon + gamesLost == 0) {
            return 0;
        }

        return (gamesWon * 100) / (gamesWon + gamesLost);
    }

    public void incrementGamesWon() {
        gamesWon++;
    }

    public void incrementGamesLost() {
        gamesLost++;
    }

    @Override
    public String toString() {
        return (name != null ? "Player: " + name + "\n" : "") +
               "Games won: " + gamesWon + "\n" +
               "Games lost: " + gamesLost + "\n" +
               "Win rate: " + getWinRate() + "%\n";
    }
}
