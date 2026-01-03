import java.io.PrintStream;
import java.util.*;

public class LocalRockPaperScissors {
    private static final Scanner scanner = new Scanner(System.in);
    private static String player1Name;
    private static String player2Name;
    private static int player1Score = 0;
    private static int player2Score = 0;
    private static int totalRounds = 0;

    public static void main(String[] args) {

        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Не удалось установить UTF-8, используем простые символы");
        }

        System.out.println("=== КАМЕНЬ, НОЖНИЦЫ, БУМАГА ===");

        setupGame();
        playGame();

        scanner.close();
    }

    private static void setupGame() {
        System.out.print("Введите количество раундов: ");
        totalRounds = scanner.nextInt();
        scanner.nextLine(); // очистка буфера

        System.out.print("Введите имя первого игрока: ");
        player1Name = scanner.nextLine();

        System.out.print("Введите имя второго игрока: ");
        player2Name = scanner.nextLine();

        System.out.println("\nИгра начинается: " + player1Name + " vs " + player2Name);
        System.out.println("Всего раундов: " + totalRounds);
        System.out.println("========================");
    }

    private static void playGame() {

        for (int round = 1; round <= totalRounds; round++) {
            clearConsole();
            System.out.println("=== РАУНД " + round + " ===");
            System.out.println("Счет: " + player1Name + " [" + player1Score + "] - [" + player2Score + "] " + player2Name);
            System.out.println();


            String playerChoice1 = getPlayerChoice(player1Name);
            String playerChoice2 = getPlayerChoice(player2Name);


            try {
                displayChoices(playerChoice1, playerChoice2);
                determineRoundWinner(playerChoice1, playerChoice2);

                if (round < totalRounds) {
                    System.out.println("\nНажмите Enter для следующего раунда...");
                    scanner.nextLine();
                }

            } catch (RuntimeException e) {
                System.out.println("Ошибка в игре: " + e.getMessage());
            }
        }

        //   executor.shutdown();
        displayFinalResults();
        askForReplay();
    }

    private static String getPlayerChoice(String playerName) {
        System.out.println(playerName + ", сделайте ваш выбор:");
        System.out.println("1. Камень");
        System.out.println("2. Ножницы");
        System.out.println("3. Бумага");
        System.out.print("Введите номер (1-3): ");

        int choice;
        while (true) {
            try {
                choice = scanner.nextInt();
                if (choice >= 1 && choice <= 3) {
                    break;
                }
            } catch (InputMismatchException e) {
                scanner.next(); // очистка некорректного ввода
            }
            System.out.print("Некорректный ввод. Введите 1, 2 или 3: ");
        }
        scanner.nextLine(); // очистка буфера

        return switch (choice) {
            case 1 -> "КАМЕНЬ";
            case 2 -> "НОЖНИЦЫ";
            case 3 -> "БУМАГА";
            default -> "";
        };
    }

    private static void displayChoices(String choice1, String choice2) {
        clearConsole();
        System.out.println("=== ВЫБОР ИГРОКОВ ===");
        System.out.println(player1Name + " выбрал(а): " + choice1);
        System.out.println(player2Name + " выбрал(а): " + choice2);
        System.out.println("===================");
    }

    private static void determineRoundWinner(String choice1, String choice2) {
        if (choice1.equals(choice2)) {
            System.out.println("\nРЕЗУЛЬТАТ: НИЧЬЯ!");
            return;
        }

        Map<String, String> rules = new HashMap<>();
        rules.put("КАМЕНЬ", "НОЖНИЦЫ");
        rules.put("НОЖНИЦЫ", "БУМАГА");
        rules.put("БУМАГА", "КАМЕНЬ");

        if (rules.get(choice1).equals(choice2)) {
            System.out.println("\nРЕЗУЛЬТАТ: " + player1Name + " ВЫИГРАЛ(А) РАУНД!");
            player1Score++;
        } else {
            System.out.println("\nРЕЗУЛЬТАТ: " + player2Name + " ВЫИГРАЛ(А) РАУНД!");
            player2Score++;
        }
    }

    private static void displayFinalResults() {
        clearConsole();
        System.out.println("=== ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ ===");
        System.out.println("Игроки: " + player1Name + " vs " + player2Name);
        System.out.println("Сыграно раундов: " + totalRounds);
        System.out.println("\nФИНАЛЬНЫЙ СЧЕТ:");
        System.out.println(player1Name + ": " + player1Score + " очков");
        System.out.println(player2Name + ": " + player2Score + " очков");
        System.out.println();

        if (player1Score > player2Score) {
            System.out.println("🏆 ПОБЕДИТЕЛЬ: " + player1Name + "! 🏆");
        } else if (player2Score > player1Score) {
            System.out.println("🏆 ПОБЕДИТЕЛЬ: " + player2Name + "! 🏆");
        } else {
            System.out.println("🤝 НИЧЬЯ ПО ИТОГАМ ВСЕХ РАУНДОВ! 🤝");
        }
        System.out.println("==========================");
    }

    private static void askForReplay() {
        System.out.print("\nХотите сыграть еще раз? (да/нет): ");
        String response = scanner.nextLine().toLowerCase();

        if (response.equals("да") || response.equals("д") || response.equals("yes") || response.equals("y")) {
            player1Score = player2Score = 0;
            setupGame();
            playGame();
        } else {
            System.out.println("Спасибо за игру! До свидания!");
        }
    }

    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("\n\n\n\n\n"); // Простой способ очистки
        }
    }
}