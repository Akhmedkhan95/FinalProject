package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Не удалось установить UTF-8");
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Подключение к серверу...");

            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Соединение с сервером разорвано");
                }
            });
            readerThread.start();

            System.out.println("\nВведите команду (или HELP для списка команд):");

            String userInput;
            while ((userInput = scanner.nextLine()) != null) {
                String trimmed = userInput.trim();

                if (trimmed.equalsIgnoreCase("HELP")) {
                    System.out.println("\nДоступные команды:");
                    System.out.println("  JOIN <имя> - присоединиться к игре");
                    System.out.println("  MOVE <ход> - сделать ход (КАМЕНЬ, НОЖНИЦЫ, БУМАГА)");
                    System.out.println("  QUIT - выйти из игры");
                    System.out.println("  HELP - показать эту справку\n");
                } else {
                    out.println(trimmed);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }
}