package domain;

public enum GameResult {
    PLAYER1_WINS("Победил игрок 1"),
    PLAYER2_WINS("Победил игрок 2"),
    DRAW("Ничья");

    private final String description;

    GameResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}