import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    private static final int PORT = 8888;
    private static final int MAX_PLAYERS = 2;
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static List<GameRoom> gameRooms = new ArrayList<>();

    public static void main(String[] args) {

        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Не удалось установить UTF-8, используем простые символы");
        }

        System.out.println("Сервер игры 'Камень, ножницы, бумага' запущен...");
        System.out.println("Порт: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ExecutorService pool = Executors.newFixedThreadPool(MAX_PLAYERS);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                pool.execute(clientHandler);
                System.out.println("Новый клиент подключен: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String playerName;
        private GameRoom currentRoom;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {




            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Добро пожаловать в игру 'Камень, ножницы, бумага'!");
                out.println("Введите ваше имя: ");

                playerName = in.readLine();
                out.println("Привет, " + playerName + "! Ожидаем второго игрока...");

                synchronized (gameRooms) {
                    GameRoom availableRoom = null;
                    for (GameRoom room : gameRooms) {
                        if (!room.isFull()) {
                            availableRoom = room;
                            break;
                        }
                    }

                    if (availableRoom == null) {
                        availableRoom = new GameRoom();
                        gameRooms.add(availableRoom);
                    }

                    availableRoom.addPlayer(this);
                    currentRoom = availableRoom;
                }

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    currentRoom.processMove(this, inputLine);
                }

            } catch (IOException e) {
                System.out.println("Игрок отключен: " + playerName);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                if (currentRoom != null) {
                    currentRoom.removePlayer(this);
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    static class GameRoom {
        private ClientHandler[] players = new ClientHandler[2];
        private int playerCount = 0;
        private String[] choices = new String[2];
        private int moveCount = 0;

        public synchronized void addPlayer(ClientHandler player) {
            if (playerCount < 2) {
                players[playerCount] = player;
                playerCount++;

                if (playerCount == 2) {
                    broadcast("Оба игрока подключены! Игра начинается.");
                    broadcast("Доступные ходы: КАМЕНЬ, НОЖНИЦЫ, БУМАГА");
                    broadcast("Сделайте ваш выбор:");
                }
            }
        }

        public synchronized void removePlayer(ClientHandler player) {
            for (int i = 0; i < playerCount; i++) {
                if (players[i] == player) {
                    players[i] = null;
                    broadcast(player.getPlayerName() + " покинул игру.");
                    break;
                }
            }
        }

        public synchronized void processMove(ClientHandler player, String move) {
            move = move.toUpperCase();

            if (!move.equals("КАМЕНЬ") && !move.equals("НОЖНИЦЫ") && !move.equals("БУМАГА")) {
                player.sendMessage("Некорректный ход! Используйте: КАМЕНЬ, НОЖНИЦЫ, БУМАГА");
                return;
            }

            int playerIndex = -1;
            for (int i = 0; i < playerCount; i++) {
                if (players[i] == player) {
                    playerIndex = i;
                    break;
                }
            }

            if (playerIndex != -1) {
                choices[playerIndex] = move;
                moveCount++;

                broadcast(player.getPlayerName() + " сделал(а) выбор.");

                if (moveCount == 2) {
                    String result = determineWinner();
                    broadcast("\n=== РЕЗУЛЬТАТЫ ===");
                    broadcast(players[0].getPlayerName() + ": " + choices[0]);
                    broadcast(players[1].getPlayerName() + ": " + choices[1]);
                    broadcast("Результат: " + result);
                    broadcast("==================\n");

                    // Сброс для следующего раунда
                    choices[0] = choices[1] = null;
                    moveCount = 0;

                    broadcast("Новый раунд! Сделайте ваш выбор:");
                } else {
                    broadcast("Ждем выбора второго игрока...");
                }
            }
        }

        private String determineWinner() {
            if (choices[0].equals(choices[1])) {
                return "НИЧЬЯ!";
            }

            Map<String, String> rules = new HashMap<>();
            rules.put("КАМЕНЬ", "НОЖНИЦЫ");
            rules.put("НОЖНИЦЫ", "БУМАГА");
            rules.put("БУМАГА", "КАМЕНЬ");

            if (rules.get(choices[0]).equals(choices[1])) {
                return players[0].getPlayerName() + " ПОБЕДИЛ(А)!";
            } else {
                return players[1].getPlayerName() + " ПОБЕДИЛ(А)!";
            }
        }

        private void broadcast(String message) {
            for (int i = 0; i < playerCount; i++) {
                if (players[i] != null) {
                    players[i].sendMessage(message);
                }
            }
        }

        public boolean isFull() {
            return playerCount >= 2;
        }
    }
}