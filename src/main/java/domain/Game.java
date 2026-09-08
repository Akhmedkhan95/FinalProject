package domain;

public class Game {
    private final Player player1;
    private final Player player2;
    private final int totalRounds;
    private int currentRound;
    private Round currentRoundObj;

    public Game(Player player1, Player player2, int totalRounds) {
        if (totalRounds <= 0) {
            throw new IllegalArgumentException("Количество раундов должно быть больше 0");
        }
        this.player1 = player1;
        this.player2 = player2;
        this.totalRounds = totalRounds;
        this.currentRound = 0;
        this.currentRoundObj = new Round(player1, player2);
    }

    public synchronized void makeMove(Player player, Move move) {
        currentRoundObj.makeMove(player, move);
    }

    public boolean isCurrentRoundComplete() {
        return currentRoundObj.isComplete();
    }

    public synchronized GameResult finishCurrentRound() {
        if (!isCurrentRoundComplete()) {
            throw new IllegalStateException("Раунд ещё не завершён");
        }

        GameResult result = currentRoundObj.play();
        currentRound++;

        if (currentRound < totalRounds) {
            currentRoundObj.reset();
        }

        return result;
    }

    public boolean isGameOver() {
        return currentRound >= totalRounds;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public Round getCurrentRoundObj() {
        return currentRoundObj;
    }
}