package server;

import protocol.Command;
import protocol.CommandParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientSession implements Runnable {
    private final Socket socket;
    private final List<GameRoom> gameRooms;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private GameRoom currentRoom;

    public ClientSession(Socket socket, List<GameRoom> gameRooms) {
        this.socket = socket;
        this.gameRooms = gameRooms;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            sendMessage("Добро пожаловать в игру 'Камень, ножницы, бумага'!");
            sendMessage("Доступные команды:");
            sendMessage("  JOIN <имя> - присоединиться к игре");
            sendMessage("  MOVE <ход> - сделать ход (КАМЕНЬ, НОЖНИЦЫ, БУМАГА)");
            sendMessage("  QUIT - выйти из игры");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                CommandParser.ParsedCommand parsed = CommandParser.parse(inputLine);

                switch (parsed.getCommand()) {
                    case JOIN:
                        handleJoin(parsed.getArgument());
                        break;
                    case MOVE:
                        handleMove(parsed.getArgument());
                        break;
                    case QUIT:
                        handleQuit();
                        return;
                    case UNKNOWN:
                    default:
                        sendMessage("Неизвестная команда. Используйте: JOIN <имя>, MOVE <ход>, QUIT");
                }
            }
        } catch (IOException e) {
            System.out.println("Игрок отключен: " + (playerName != null ? playerName : "неизвестный"));
        } finally {
            cleanup();
        }
    }

    private void handleJoin(String name) {
        if (name == null || name.trim().isEmpty()) {
            sendMessage("Ошибка: укажите имя. Пример: JOIN Иван");
            return;
        }

        if (currentRoom != null) {
            sendMessage("Вы уже в игре. Используйте MOVE для хода или QUIT для выхода.");
            return;
        }

        this.playerName = name.trim();

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
    }

    private void handleMove(String move) {
        if (currentRoom == null) {
            sendMessage("Сначала присоединитесь к игре: JOIN <имя>");
            return;
        }

        if (move == null || move.trim().isEmpty()) {
            sendMessage("Ошибка: укажите ход. Пример: MOVE КАМЕНЬ");
            return;
        }

        currentRoom.processMove(this, move.trim());
    }

    private void handleQuit() {
        sendMessage("До свидания!");
        if (currentRoom != null) {
            currentRoom.removePlayer(this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getPlayerName() {
        return playerName;
    }

    private void cleanup() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (currentRoom != null) {
            currentRoom.removePlayer(this);
            if (currentRoom.isEmpty()) {
                GameServer.removeEmptyRoom(currentRoom);
            }
        }
    }
}