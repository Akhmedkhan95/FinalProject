import domain.Game;
import domain.GameResult;
import domain.Move;
import domain.Player;

import java.io.PrintStream;
import java.util.Scanner;

public class LocalRockPaperScissors {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Не удалось установить UTF-8");
        }

        System.out.println("=== КАМЕНЬ, НОЖНИЦЫ, БУМАГА ===");

        boolean playAgain = true;
        while (playAgain) {
            playAgain = playOneGame();
        }

        System.out.println("Спасибо за игру! До свидания!");
        scanner.close();
    }

    private static boolean playOneGame() {
        Game game = setupGame();
        if (game == null) {
            return false;
        }

        playGame(game);
        return askForReplay();
    }

    private static Game setupGame() {
        int totalRounds = getValidRoundsCount();
        if (totalRounds <= 0) {
            return null;
        }

        System.out.print("Введите имя первого игрока: ");
        String name1 = scanner.nextLine();
        while (name1.trim().isEmpty()) {
            System.out.print("Имя не может быть пустым. Введите имя первого игрока: ");
            name1 = scanner.nextLine();
        }

        System.out.print("Введите имя второго игрока: ");
        String name2 = scanner.nextLine();
        while (name2.trim().isEmpty()) {
            System.out.print("Имя не может быть пустым. Введите имя второго игрока: ");
            name2 = scanner.nextLine();
        }

        Player player1 = new Player(name1.trim());
        Player player2 = new Player(name2.trim());

        Game game = new Game(player1, player2, totalRounds);

        System.out.println("\nИгра начинается: " + name1 + " vs " + name2);
        System.out.println("Всего раундов: " + totalRounds);
        System.out.println("========================");

        return game;
    }

    private static int getValidRoundsCount() {
        while (true) {
            System.out.print("Введите количество раундов (1-100): ");
            String input = scanner.nextLine().trim();

            try {
                int rounds = Integer.parseInt(input);
                if (rounds >= 1 && rounds <= 100) {
                    return rounds;
                } else {
                    System.out.println("Количество раундов должно быть от 1 до 100.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Пожалуйста, введите число.");
            }
        }
    }

    private static void playGame(Game game) {
        while (!game.isGameOver()) {
            int roundNum = game.getCurrentRound() + 1;
            clearConsole();
            System.out.println("=== РАУНД " + roundNum + " ===");
            System.out.println("Счет: " + game.getPlayer1() + " - " + game.getPlayer2());
            System.out.println();

            Move move1 = getPlayerChoice(game.getPlayer1().getName());
            game.makeMove(game.getPlayer1(), move1);

            Move move2 = getPlayerChoice(game.getPlayer2().getName());
            game.makeMove(game.getPlayer2(), move2);

            displayChoices(game, move1, move2);
            GameResult result = game.finishCurrentRound();
            displayRoundResult(game, result);

            if (!game.isGameOver()) {
                System.out.println("\nНажмите Enter для следующего раунда...");
                scanner.nextLine();
            }
        }

        displayFinalResults(game);
    }

    private static Move getPlayerChoice(String playerName) {
        System.out.println(playerName + ", сделайте ваш выбор:");
        System.out.println("1. Камень");
        System.out.println("2. Ножницы");
        System.out.println("3. Бумага");
        System.out.print("Введите номер (1-3) или название: ");

        while (true) {
            String input = scanner.nextLine().trim();

            try {
                // Пробуем распарсить как число
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 3) {
                    return Move.fromNumber(choice);
                }
            } catch (NumberFormatException e) {
                // Если не число, пробуем распарсить как слово
                try {
                    return Move.fromString(input);
                } catch (IllegalArgumentException ex) {
                    // Игнорируем, чтобы вывести сообщение об ошибке ниже
                }
            }

            System.out.print("Некорректный ввод. Введите 1, 2, 3 или название хода: ");
        }
    }

    private static void displayChoices(Game game, Move move1, Move move2) {
        clearConsole();
        System.out.println("=== ВЫБОР ИГРОКОВ ===");
        System.out.println(game.getPlayer1().getName() + " выбрал(а): " + move1.getDisplayName());
        System.out.println(game.getPlayer2().getName() + " выбрал(а): " + move2.getDisplayName());
        System.out.println("===================");
    }

    private static void displayRoundResult(Game game, GameResult result) {
        System.out.println("\nРЕЗУЛЬТАТ: " + result.getDescription());
        if (result == GameResult.PLAYER1_WINS) {
            System.out.println(game.getPlayer1().getName() + " выиграл(а) раунд!");
        } else if (result == GameResult.PLAYER2_WINS) {
            System.out.println(game.getPlayer2().getName() + " выиграл(а) раунд!");
        }
    }

    private static void displayFinalResults(Game game) {
        clearConsole();
        System.out.println("=== ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ ===");
        System.out.println("Игроки: " + game.getPlayer1().getName() + " vs " + game.getPlayer2().getName());
        System.out.println("Сыграно раундов: " + game.getTotalRounds());
        System.out.println("\nФИНАЛЬНЫЙ СЧЕТ:");
        System.out.println(game.getPlayer1());
        System.out.println(game.getPlayer2());
        System.out.println();

        if (game.getPlayer1().getScore() > game.getPlayer2().getScore()) {
            System.out.println("🏆 ПОБЕДИТЕЛЬ: " + game.getPlayer1().getName() + "! 🏆");
        } else if (game.getPlayer2().getScore() > game.getPlayer1().getScore()) {
            System.out.println("🏆 ПОБЕДИТЕЛЬ: " + game.getPlayer2().getName() + "! 🏆");
        } else {
            System.out.println("🤝 НИЧЬЯ ПО ИТОГАМ ВСЕХ РАУНДОВ! 🤝");
        }
        System.out.println("==========================");
    }

    private static boolean askForReplay() {
        System.out.print("\nХотите сыграть еще раз? (да/нет): ");
        String response = scanner.nextLine().toLowerCase().trim();
        return response.equals("да") || response.equals("д") ||
                response.equals("yes") || response.equals("y");
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
            System.out.println("\n\n\n\n\n"); // Фоллбек для очистки
        }
    }
}