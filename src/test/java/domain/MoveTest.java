package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void testFromStringRussian() {
        assertEquals(Move.ROCK, Move.fromString("КАМЕНЬ"));
        assertEquals(Move.PAPER, Move.fromString("БУМАГА"));
        assertEquals(Move.SCISSORS, Move.fromString("НОЖНИЦЫ"));
    }

    @Test
    void testFromStringRussianLowerCase() {
        assertEquals(Move.ROCK, Move.fromString("камень"));
        assertEquals(Move.PAPER, Move.fromString("бумага"));
        assertEquals(Move.SCISSORS, Move.fromString("ножницы"));
    }

    @Test
    void testFromStringEnglish() {
        assertEquals(Move.ROCK, Move.fromString("ROCK"));
        assertEquals(Move.PAPER, Move.fromString("PAPER"));
        assertEquals(Move.SCISSORS, Move.fromString("SCISSORS"));
    }

    @Test
    void testFromStringNumbers() {
        assertEquals(Move.ROCK, Move.fromString("1"));
        assertEquals(Move.SCISSORS, Move.fromString("2"));
        assertEquals(Move.PAPER, Move.fromString("3"));
    }

    @Test
    void testFromStringWithSpaces() {
        assertEquals(Move.ROCK, Move.fromString("  КАМЕНЬ  "));
    }

    @Test
    void testFromStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Move.fromString("ПИСТОЛЕТ"));
        assertThrows(IllegalArgumentException.class, () -> Move.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Move.fromString("4"));
    }

    @Test
    void testFromStringNull() {
        assertThrows(IllegalArgumentException.class, () -> Move.fromString(null));
    }

    @Test
    void testFromNumber() {
        assertEquals(Move.ROCK, Move.fromNumber(1));
        assertEquals(Move.SCISSORS, Move.fromNumber(2));
        assertEquals(Move.PAPER, Move.fromNumber(3));
    }

    @Test
    void testFromNumberInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Move.fromNumber(0));
        assertThrows(IllegalArgumentException.class, () -> Move.fromNumber(4));
        assertThrows(IllegalArgumentException.class, () -> Move.fromNumber(-1));
    }

    @Test
    void testDisplayName() {
        assertEquals("КАМЕНЬ", Move.ROCK.getDisplayName());
        assertEquals("БУМАГА", Move.PAPER.getDisplayName());
        assertEquals("НОЖНИЦЫ", Move.SCISSORS.getDisplayName());
    }
}