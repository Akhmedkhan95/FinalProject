package domain;

public enum Move {
    ROCK("КАМЕНЬ"),
    PAPER("БУМАГА"),
    SCISSORS("НОЖНИЦЫ");

    private final String displayName;

    Move(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Move fromString(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Ход не может быть null");
        }

        String normalized = text.trim().toUpperCase();

        switch (normalized) {
            case "КАМЕНЬ":
            case "ROCK":
            case "1":
                return ROCK;
            case "БУМАГА":
            case "PAPER":
            case "3":
                return PAPER;
            case "НОЖНИЦЫ":
            case "SCISSORS":
            case "2":
                return SCISSORS;
            default:
                throw new IllegalArgumentException("Неизвестный ход: " + text);
        }
    }

    public static Move fromNumber(int number) {
        switch (number) {
            case 1: return ROCK;
            case 2: return SCISSORS;
            case 3: return PAPER;
            default: throw new IllegalArgumentException("Номер должен быть 1, 2 или 3");
        }
    }
}