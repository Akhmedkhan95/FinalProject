import java.io.PrintStream;
import java.util.Scanner;

public class SimpleRockPaperScissors {
    private static Scanner scanner = new Scanner(System.in);

    // Используем текстовые константы вместо эмодзи
    private static final String[] CHOICE_NAMES = {"Камень", "Ножницы", "Бумага"};
    private static final String[] CHOICE_SYMBOLS = {"[К]", "[Н]", "[Б]"};

    public static void main(String[] args) {

        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Не удалось установить UTF-8, используем простые символы");
        }

        System.out.println("=================================");
        System.out.println("  КАМЕНЬ, НОЖНИЦЫ, БУМАГА");
        System.out.println("=================================");

        int playerWins = 0;
        int computerWins = 0;
        int ties = 0;

        while (true) {
            System.out.println("\n=== НОВЫЙ РАУНД ===");
            System.out.println("Счет: Игрок " + playerWins + " - " + computerWins + " Компьютер");
            System.out.println("Ничьих: " + ties);

            playRound();

            System.out.print("\nХотите сыграть еще раунд? (да/нет): ");
            String answer = scanner.nextLine().toLowerCase();

            if (!answer.equals("да") && !answer.equals("д") && !answer.equals("yes") && !answer.equals("y")) {
                break;
            }
        }

        displayFinalResults(playerWins, computerWins, ties);
        System.out.println("\nСпасибо за игру!");
        scanner.close();
    }

    private static void playRound() {
        System.out.println("\nВыберите ваш ход:");
        for (int i = 0; i < CHOICE_NAMES.length; i++) {
            System.out.println((i + 1) + ". " + CHOICE_SYMBOLS[i] + " " + CHOICE_NAMES[i]);
        }
        System.out.print("Ваш выбор (1-3): ");

        int playerChoice = getValidChoice();
        int computerChoice = (int) (Math.random() * 3) + 1;

        System.out.println("\n--- РЕЗУЛЬТАТЫ ---");
        System.out.println("Ваш выбор: " + CHOICE_SYMBOLS[playerChoice - 1] + " " + CHOICE_NAMES[playerChoice - 1]);
        System.out.println("Выбор компьютера: " + CHOICE_SYMBOLS[computerChoice - 1] + " " + CHOICE_NAMES[computerChoice - 1]);

        String result = determineWinner(playerChoice, computerChoice);
        System.out.println("\n>>> " + result + " <<<");
    }

    private static int getValidChoice() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 3) {
                    return choice;
                } else {
                    System.out.print("Пожалуйста, введите число от 1 до 3: ");
                }
            } catch (NumberFormatException e) {
                // Проверяем, если пользователь ввел букву
                String input = scanner.nextLine().toLowerCase();
                if (input.equals("к") || input.equals("камень")) {
                    return 1;
                } else if (input.equals("н") || input.equals("ножницы")) {
                    return 2;
                } else if (input.equals("б") || input.equals("бумага")) {
                    return 3;
                } else {
                    System.out.print("Некорректный ввод. Введите 1, 2, 3 или букву (К/Н/Б): ");
                }
            }
        }
    }

    private static String determineWinner(int player, int computer) {
        if (player == computer) {
            return "НИЧЬЯ!";
        }

        // Правила: 1 > 2, 2 > 3, 3 > 1
        if ((player == 1 && computer == 2) ||
                (player == 2 && computer == 3) ||
                (player == 3 && computer == 1)) {
            return "ВЫ ПОБЕДИЛИ!";
        } else {
            return "КОМПЬЮТЕР ПОБЕДИЛ!";
        }
    }

    private static void displayFinalResults(int playerWins, int computerWins, int ties) {
        System.out.println("\n=================================");
        System.out.println("       ИТОГИ ИГРЫ");
        System.out.println("=================================");
        System.out.println("Побед игрока: " + playerWins);
        System.out.println("Побед компьютера: " + computerWins);
        System.out.println("Ничьих: " + ties);
        System.out.println("Всего сыграно раундов: " + (playerWins + computerWins + ties));
        System.out.println();

        if (playerWins > computerWins) {
            System.out.println(">>> ВЫ ВЫИГРАЛИ ИГРУ! <<<");
        } else if (computerWins > playerWins) {
            System.out.println(">>> КОМПЬЮТЕР ВЫИГРАЛ ИГРУ! <<<");
        } else {
            System.out.println(">>> ИГРА ЗАКОНЧИЛАСЬ ВНИЧЬЮ! <<<");
        }
        System.out.println("=================================");
    }
}