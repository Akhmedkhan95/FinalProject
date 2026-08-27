package server;

import domain.GameResult;
import domain.Move;
import domain.Player;
import domain.Round;

public class GameRoom {
    private final ClientSession[] players = new ClientSession[2];
    private int playerCount = 0;
    private Round round;
    private final Player[] domainPlayers = new Player[2];

    public synchronized void addPlayer(ClientSession player) {
        if (playerCount >= 2) {
            player.sendMessage("Комната заполнена!");
            return;
        }

        players[playerCount] = player;
        domainPlayers[playerCount] = new Player(player.getPlayerName());
        playerCount++;

        if (playerCount == 1) {
            player.sendMessage("Ожидание второго игрока...");
        } else if (playerCount == 2) {
            round = new Round(domainPlayers[0], domainPlayers[1]);
            broadcast("Оба игрока подключены! Игра начинается.");
            broadcast("Игроки: " + domainPlayers[0].getName() + " vs " + domainPlayers[1].getName());
            broadcast("Доступные ходы: КАМЕНЬ, НОЖНИЦЫ, БУМАГА (или 1, 2, 3)");
            broadcast("Сделайте ваш выбор: MOVE <ход>");
        }
    }

    public synchronized void removePlayer(ClientSession player) {
        for (int i = 0; i < playerCount; i++) {
            if (players[i] == player) {
                players[i] = null;
                playerCount--;

                broadcast(player.getPlayerName() + " покинул игру.");

                if (playerCount < 2 && round != null) {
                    broadcast("Игра прервана из-за выхода игрока.");
                    round = null;
                }
                break;
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

            int playerIndex = getPlayerIndex(player);
            if (playerIndex == -1) {
                player.sendMessage("Вы не участвуете в этой игре.");
                return;
            }

            Player domainPlayer = domainPlayers[playerIndex];

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
                broadcast(domainPlayers[0].getName() + ": " + round.getPlayer1Move().getDisplayName());
                broadcast(domainPlayers[1].getName() + ": " + round.getPlayer2Move().getDisplayName());
                broadcast("Результат: " + result.getDescription());
                broadcast("Счёт: " + domainPlayers[0] + " - " + domainPlayers[1]);
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

    private int getPlayerIndex(ClientSession player) {
        for (int i = 0; i < playerCount; i++) {
            if (players[i] == player) {
                return i;
            }
        }
        return -1;
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

    public synchronized boolean isEmpty() {
        return playerCount == 0;
    }
}