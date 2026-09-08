package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 8888;
    private static final int MAX_THREADS = 50;
    private static final int CLIENT_TIMEOUT_MS = 300000; // 5 минут
    private static final List<GameRoom> gameRooms = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Не удалось установить UTF-8");
        }

        System.out.println("Сервер игры 'Камень, ножницы, бумага' запущен...");
        System.out.println("Порт: " + PORT);
        System.out.println("Таймаут клиента: " + (CLIENT_TIMEOUT_MS / 1000) + " секунд");

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Устанавливаем timeout на сокет
                clientSocket.setSoTimeout(CLIENT_TIMEOUT_MS);

                System.out.println("Новый клиент подключен: " + clientSocket.getInetAddress());

                ClientSession clientSession = new ClientSession(clientSocket, gameRooms);
                pool.execute(clientSession);
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    public static void removeEmptyRoom(GameRoom room) {
        gameRooms.remove(room);
    }
}