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
    private static final int MAX_THREADS = 10;
    private static final List<GameRoom> gameRooms = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Не удалось установить UTF-8");
        }

        System.out.println("Сервер игры 'Камень, ножницы, бумага' запущен...");
        System.out.println("Порт: " + PORT);

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
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