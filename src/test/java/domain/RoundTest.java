package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundTest {

    private Player player1;
    private Player player2;
    private Round round;

    @BeforeEach
    void setUp() {
        player1 = new Player("Игрок1");
        player2 = new Player("Игрок2");
        round = new Round(player1, player2);
    }

    @Test
    void testInitialState() {
        assertFalse(round.isComplete());
        assertNull(round.getPlayer1Move());
        assertNull(round.getPlayer2Move());
    }

    @Test
    void testBothPlayersMustMove() {
        round.makeMove(player1, Move.ROCK);
        assertFalse(round.isComplete());

        round.makeMove(player2, Move.SCISSORS);
        assertTrue(round.isComplete());
    }

    @Test
    void testCannotMoveTwice_SamePlayer() {
        round.makeMove(player1, Move.ROCK);

        // Повторный ход того же игрока — ошибка
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> round.makeMove(player1, Move.PAPER));
        assertTrue(exception.getMessage().contains("уже сделал ход"));
    }

    @Test
    void testCannotMoveTwice_OtherPlayer() {
        round.makeMove(player2, Move.ROCK);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> round.makeMove(player2, Move.SCISSORS));
        assertTrue(exception.getMessage().contains("уже сделал ход"));
    }

    @Test
    void testInvalidPlayerCannotMove() {
        Player stranger = new Player("Чужой");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> round.makeMove(stranger, Move.ROCK));
        assertTrue(exception.getMessage().contains("не участвует"));
    }

    @Test
    void testCannotPlayWithoutBothMoves() {
        round.makeMove(player1, Move.ROCK);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> round.play());
        assertTrue(exception.getMessage().contains("Не все игроки"));
    }

    @Test
    void testPlayUpdatesScore_Player1Wins() {
        round.makeMove(player1, Move.ROCK);
        round.makeMove(player2, Move.SCISSORS);

        GameResult result = round.play();

        assertEquals(GameResult.PLAYER1_WINS, result);
        assertEquals(1, player1.getScore());
        assertEquals(0, player2.getScore());
    }

    @Test
    void testPlayUpdatesScore_Player2Wins() {
        round.makeMove(player1, Move.ROCK);
        round.makeMove(player2, Move.PAPER);

        GameResult result = round.play();

        assertEquals(GameResult.PLAYER2_WINS, result);
        assertEquals(0, player1.getScore());
        assertEquals(1, player2.getScore());
    }

    @Test
    void testPlayUpdatesScore_Draw() {
        round.makeMove(player1, Move.ROCK);
        round.makeMove(player2, Move.ROCK);

        GameResult result = round.play();

        assertEquals(GameResult.DRAW, result);
        assertEquals(0, player1.getScore());
        assertEquals(0, player2.getScore());
    }

    @Test
    void testReset() {
        round.makeMove(player1, Move.ROCK);
        round.makeMove(player2, Move.SCISSORS);
        round.play();

        round.reset();

        assertFalse(round.isComplete());
        assertNull(round.getPlayer1Move());
        assertNull(round.getPlayer2Move());
        // Счёт игроков не сбрасывается
        assertEquals(1, player1.getScore());
    }

    @Test
    void testMultipleRoundsAccumulateScore() {
        // Раунд 1: игрок 1 выигрывает
        round.makeMove(player1, Move.ROCK);
        round.makeMove(player2, Move.SCISSORS);
        round.play();

        round.reset();

        // Раунд 2: ничья
        round.makeMove(player1, Move.PAPER);
        round.makeMove(player2, Move.PAPER);
        round.play();

        round.reset();

        // Раунд 3: игрок 2 выигрывает
        round.makeMove(player1, Move.SCISSORS);
        round.makeMove(player2, Move.ROCK);
        round.play();

        assertEquals(1, player1.getScore());
        assertEquals(1, player2.getScore());
    }
}