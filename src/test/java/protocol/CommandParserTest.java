package protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    @Test
    void testParseJoin() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("JOIN Иван");
        assertEquals(Command.JOIN, cmd.getCommand());
        assertEquals("Иван", cmd.getArgument());
    }

    @Test
    void testParseMove() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("MOVE КАМЕНЬ");
        assertEquals(Command.MOVE, cmd.getCommand());
        assertEquals("КАМЕНЬ", cmd.getArgument());
    }

    @Test
    void testParseQuit() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("QUIT");
        assertEquals(Command.QUIT, cmd.getCommand());
        assertNull(cmd.getArgument());
    }

    @Test
    void testParseCaseInsensitive() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("join Иван");
        assertEquals(Command.JOIN, cmd.getCommand());
    }

    @Test
    void testParseUnknownCommand() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("ATTACK");
        assertEquals(Command.UNKNOWN, cmd.getCommand());
    }

    @Test
    void testParseEmptyString() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("");
        assertEquals(Command.UNKNOWN, cmd.getCommand());
    }

    @Test
    void testParseNull() {
        CommandParser.ParsedCommand cmd = CommandParser.parse(null);
        assertEquals(Command.UNKNOWN, cmd.getCommand());
    }

    @Test
    void testParseWithExtraSpaces() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("  JOIN   Иван Петров  ");
        assertEquals(Command.JOIN, cmd.getCommand());
        assertEquals("Иван Петров", cmd.getArgument());
    }
}