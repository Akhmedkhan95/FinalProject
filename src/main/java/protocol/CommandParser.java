package protocol;

public class CommandParser {
    public static ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParsedCommand(Command.UNKNOWN, null);
        }

        String[] parts = input.trim().split("\\s+", 2);
        if (parts.length == 0) {
            return new ParsedCommand(Command.UNKNOWN, null);
        }

        Command command = Command.fromString(parts[0]);
        String argument = parts.length > 1 ? parts[1].trim() : null;

        return new ParsedCommand(command, argument);
    }

    public static class ParsedCommand {
        private final Command command;
        private final String argument;

        public ParsedCommand(Command command, String argument) {
            this.command = command;
            this.argument = argument;
        }

        public Command getCommand() {
            return command;
        }

        public String getArgument() {
            return argument;
        }

        @Override
        public String toString() {
            return "Command: " + command + ", Argument: " + argument;
        }
    }
}