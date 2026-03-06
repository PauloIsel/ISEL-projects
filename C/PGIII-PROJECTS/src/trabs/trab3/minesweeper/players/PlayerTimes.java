package trabs.trab3.minesweeper.players;

public class PlayerTimes {
    private final String name;
    private int beginnerTime;
    private int intermediateTime;
    private int advancedTime;

    public PlayerTimes(String name, int beginnerTime, int intermediateTime, int advancedTime) {
        this.name = name;
        this.beginnerTime = beginnerTime;
        this.intermediateTime = intermediateTime;
        this.advancedTime = advancedTime;
    }

    public PlayerTimes(String name) {
        this(name, 0, 0, 0);
    }

    public String getName() {
        return name;
    }

    public int getTime(String difficulty) {
        return switch (difficulty) {
            case "beginner" -> beginnerTime;
            case "intermediate" -> intermediateTime;
            case "advanced" -> advancedTime;
            default -> 0;
        };
    }

    public void setTime(String difficulty, int time) {
        switch (difficulty) {
            case "beginner":
                beginnerTime = time;
                break;
            case "intermediate":
                intermediateTime = time;
                break;
            case "advanced":
                advancedTime = time;
                break;
        }
    }
}
