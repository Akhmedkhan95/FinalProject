package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testInvalidRoundsCount() {
        Player p1 = new Player("Игрок1");
        Player p2 = new Player("Игрок2");

        assertThrows(IllegalArgumentException.class, () -> new Game(p1, p2, 0));
        assertThrows(IllegalArgumentException.class, () -> new Game(p1, p2, -5));
    }

    @Test
    void testInitialState() {
        Game game = new Game(new Player("P1"), new Player("P2"), 3);

        assertEquals(0, game.getCurrentRound());
        assertEquals(3, game.getTotalRounds());
        assertFalse(game.isGameOver());
        assertFalse(game.isCurrentRoundComplete());
    }

    @Test
    void testGameFlow_ThreeRounds() {
        Player p1 = new Player("Игрок1");
        Player p2 = new Player("Игрок2");
        Game game = new Game(p1, p2, 3);

        // Раунд 1: игрок 1 выигрывает
        game.makeMove(p1, Move.ROCK);
        game.makeMove(p2, Move.SCISSORS);
        assertTrue(game.isCurrentRoundComplete());
        GameResult r1 = game.finishCurrentRound();
        assertEquals(GameResult.PLAYER1_WINS, r1);
        assertEquals(1, game.getCurrentRound());

        // Раунд 2: игрок 2 выигрывает
        game.makeMove(p1, Move.PAPER);
        game.makeMove(p2, Move.SCISSORS);
        GameResult r2 = game.finishCurrentRound();
        assertEquals(GameResult.PLAYER2_WINS, r2);
        assertEquals(2, game.getCurrentRound());

        // Раунд 3: ничья
        game.makeMove(p1, Move.ROCK);
        game.makeMove(p2, Move.ROCK);
        GameResult r3 = game.finishCurrentRound();
        assertEquals(GameResult.DRAW, r3);
        assertEquals(3, game.getCurrentRound());
        assertTrue(game.isGameOver());

        assertEquals(1, p1.getScore());
        assertEquals(1, p2.getScore());
    }

    @Test
    void testCannotFinishRoundWithoutMoves() {
        Game game = new Game(new Player("P1"), new Player("P2"), 1);

        assertThrows(IllegalStateException.class, () -> game.finishCurrentRound());
    }

    @Test
    void testCannotFinishRoundWithOneMove() {
        Game game = new Game(new Player("P1"), new Player("P2"), 1);
        game.makeMove(game.getPlayer1(), Move.ROCK);

        assertThrows(IllegalStateException.class, () -> game.finishCurrentRound());
    }

    @Test
    void testMultipleParallelGames() {
        // Две независимые игры одновременно
        Game game1 = new Game(new Player("A"), new Player("B"), 2);
        Game game2 = new Game(new Player("C"), new Player("D"), 2);

        // Играем в первой игре
        game1.makeMove(game1.getPlayer1(), Move.ROCK);
        game1.makeMove(game1.getPlayer2(), Move.SCISSORS);
        game1.finishCurrentRound();

        // Во второй игре счёт должен быть 0
        assertEquals(0, game2.getPlayer1().getScore());
        assertEquals(0, game2.getPlayer2().getScore());
        assertEquals(0, game2.getCurrentRound());

        // Играем во второй игре
        game2.makeMove(game2.getPlayer1(), Move.PAPER);
        game2.makeMove(game2.getPlayer2(), Move.ROCK);
        game2.finishCurrentRound();

        // Счёт первой игры не изменился
        assertEquals(1, game1.getPlayer1().getScore());
        assertEquals(0, game1.getPlayer2().getScore());

        // Счёт второй игры
        assertEquals(1, game2.getPlayer1().getScore());
        assertEquals(0, game2.getPlayer2().getScore());
    }
}