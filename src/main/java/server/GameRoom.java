package server;

import domain.GameResult;
import domain.Move;
import domain.Player;
import domain.Round;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private final List<ClientSession> players = new ArrayList<>();
    private final List<Player> domainPlayers = new ArrayList<>();
    private Round round;

    public synchronized void addPlayer(ClientSession player) {
        if (players.size() >= 2) {
            player.sendMessage("Комната заполнена!");
            return;
        }

        players.add(player);
        domainPlayers.add(new Player(player.getPlayerName()));

        if (players.size() == 1) {
            player.sendMessage("Ожидание второго игрока...");
        } else if (players.size() == 2) {
            round = new Round(domainPlayers.get(0), domainPlayers.get(1));
            broadcast("Оба игрока подключены! Игра начинается.");
            broadcast("Игроки: " + domainPlayers.get(0).getName() + " vs " + domainPlayers.get(1).getName());
            broadcast("Доступные ходы: КАМЕНЬ, НОЖНИЦЫ, БУМАГА (или 1, 2, 3)");
            broadcast("Сделайте ваш выбор: MOVE <ход>");
        }
    }

    public synchronized void removePlayer(ClientSession player) {
        int index = players.indexOf(player);
        if (index != -1) {
            players.remove(index);
            domainPlayers.remove(index);

            broadcast(player.getPlayerName() + " покинул игру.");

            if (players.size() < 2 && round != null) {
                broadcast("Игра прервана из-за выхода игрока.");
                round = null;
            }

            // Если комната пуста, помечаем для удаления
            if (players.isEmpty()) {
                GameServer.removeEmptyRoom(this);
            }
        }
    }

    public synchronized void processMove(ClientSession player, String moveStr) {
        if (round == null) {
            player.sendMessage("Игра ещё не началась или была прервана.");
            return;
        }

        try {
            Move move = Move.fromString(moveStr);

            int playerIndex = players.indexOf(player);
            if (playerIndex == -1) {
                player.sendMessage("Вы не участвуете в этой игре.");
                return;
            }

            Player domainPlayer = domainPlayers.get(playerIndex);

            try {
                round.makeMove(domainPlayer, move);
            } catch (IllegalStateException e) {
                player.sendMessage("Вы уже сделали ход в этом раунде! Ожидайте второго игрока.");
                return;
            }

            broadcast(domainPlayer.getName() + " сделал(а) выбор.");

            if (round.isComplete()) {
                GameResult result = round.play();

                broadcast("\n=== РЕЗУЛЬТАТЫ РАУНДА ===");
                broadcast(domainPlayers.get(0).getName() + ": " + round.getPlayer1Move().getDisplayName());
                broadcast(domainPlayers.get(1).getName() + ": " + round.getPlayer2Move().getDisplayName());
                broadcast("Результат: " + result.getDescription());
                broadcast("Счёт: " + domainPlayers.get(0) + " - " + domainPlayers.get(1));
                broadcast("==========================\n");

                round.reset();
                broadcast("Новый раунд! Сделайте ваш выбор: MOVE <ход>");
            } else {
                broadcast("Ждем выбора второго игрока...");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("Некорректный ход! Используйте: КАМЕНЬ, НОЖНИЦЫ, БУМАГА (или 1, 2, 3)");
        }
    }

    private void broadcast(String message) {
        for (ClientSession player : players) {
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    public synchronized boolean isFull() {
        return players.size() >= 2;
    }

    public synchronized boolean isEmpty() {
        return players.isEmpty();
    }
}