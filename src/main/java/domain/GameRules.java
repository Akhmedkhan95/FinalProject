package domain;

import java.util.Map;

public class GameRules {
    private static final Map<Move, Move> WINNING_RULES = Map.of(
            Move.ROCK, Move.SCISSORS,
            Move.SCISSORS, Move.PAPER,
            Move.PAPER, Move.ROCK
    );

    public static GameResult determineWinner(Move move1, Move move2) {
        if (move1 == null || move2 == null) {
            throw new IllegalArgumentException("Ходы не могут быть null");
        }

        if (move1 == move2) {
            return GameResult.DRAW;
        }

        if (WINNING_RULES.get(move1) == move2) {
            return GameResult.PLAYER1_WINS;
        }

        return GameResult.PLAYER2_WINS;
    }
}