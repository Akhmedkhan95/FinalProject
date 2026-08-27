package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {

    @Test
    void testRockBeatsScissors() {
        assertEquals(GameResult.PLAYER1_WINS,
                GameRules.determineWinner(Move.ROCK, Move.SCISSORS));
    }

    @Test
    void testScissorsBeatPaper() {
        assertEquals(GameResult.PLAYER1_WINS,
                GameRules.determineWinner(Move.SCISSORS, Move.PAPER));
    }

    @Test
    void testPaperBeatsRock() {
        assertEquals(GameResult.PLAYER1_WINS,
                GameRules.determineWinner(Move.PAPER, Move.ROCK));
    }

    @ParameterizedTest
    @CsvSource({
            "ROCK, SCISSORS, PLAYER1_WINS",
            "SCISSORS, PAPER, PLAYER1_WINS",
            "PAPER, ROCK, PLAYER1_WINS",
            "SCISSORS, ROCK, PLAYER2_WINS",
            "PAPER, SCISSORS, PLAYER2_WINS",
            "ROCK, PAPER, PLAYER2_WINS"
    })
    void testAllCombinations(Move move1, Move move2, GameResult expected) {
        assertEquals(expected, GameRules.determineWinner(move1, move2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROCK", "PAPER", "SCISSORS"})
    void testDraw(Move move) {
        assertEquals(GameResult.DRAW, GameRules.determineWinner(move, move));
    }

    @Test
    void testNullFirstMove() {
        assertThrows(IllegalArgumentException.class,
                () -> GameRules.determineWinner(null, Move.ROCK));
    }

    @Test
    void testNullSecondMove() {
        assertThrows(IllegalArgumentException.class,
                () -> GameRules.determineWinner(Move.ROCK, null));
    }

    @Test
    void testSymmetry() {
        // Если игрок 1 выигрывает с X против Y, то игрок 2 выигрывает с Y против X
        for (Move m1 : Move.values()) {
            for (Move m2 : Move.values()) {
                GameResult result1 = GameRules.determineWinner(m1, m2);
                GameResult result2 = GameRules.determineWinner(m2, m1);

                if (result1 == GameResult.PLAYER1_WINS) {
                    assertEquals(GameResult.PLAYER2_WINS, result2);
                } else if (result1 == GameResult.PLAYER2_WINS) {
                    assertEquals(GameResult.PLAYER1_WINS, result2);
                } else {
                    assertEquals(GameResult.DRAW, result2);
                }
            }
        }
    }
}