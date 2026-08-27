package domain;

public class Round {
    private final Player player1;
    private final Player player2;
    private Move player1Move;
    private Move player2Move;

    public Round(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public synchronized void makeMove(Player player, Move move) {
        if (player == player1) {
            if (player1Move != null) {
                throw new IllegalStateException("Игрок " + player.getName() + " уже сделал ход");
            }
            player1Move = move;
        } else if (player == player2) {
            if (player2Move != null) {
                throw new IllegalStateException("Игрок " + player.getName() + " уже сделал ход");
            }
            player2Move = move;
        } else {
            throw new IllegalArgumentException("Игрок " + player.getName() + " не участвует в этом раунде");
        }
    }

    public boolean isComplete() {
        return player1Move != null && player2Move != null;
    }

    public GameResult play() {
        if (!isComplete()) {
            throw new IllegalStateException("Не все игроки сделали ходы");
        }

        GameResult result = GameRules.determineWinner(player1Move, player2Move);

        if (result == GameResult.PLAYER1_WINS) {
            player1.incrementScore();
        } else if (result == GameResult.PLAYER2_WINS) {
            player2.incrementScore();
        }

        return result;
    }

    public Move getPlayer1Move() {
        return player1Move;
    }

    public Move getPlayer2Move() {
        return player2Move;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void reset() {
        player1Move = null;
        player2Move = null;
    }
}