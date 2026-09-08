package protocol;

public enum Command {
    JOIN,
    MOVE,
    QUIT,
    UNKNOWN;

    public static Command fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UNKNOWN;
        }

        try {
            return Command.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}