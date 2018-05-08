package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.game.Game;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.util.Checks;

import java.security.SecureRandom;
import java.util.*;

public class Matchmaker {

    private static final SecureRandom random = new SecureRandom();

    private final Object lock = new Object();

    private final List<Game> games = new ArrayList<>();
    private final Map<Client, Game> gamesByClientId = new HashMap<>();
    private Client waitingClient = null;

    public Game findGame(Client client) {
        synchronized (lock) {
            return gamesByClientId.get(client);
        }
    }

    public void connectClient(Client client) {
        synchronized (lock) {
            Game currentGame = gamesByClientId.get(client);

            if(currentGame != null) {
                currentGame.onReconnect(client);
                return;
            }

            if(waitingClient == null) {
                waitingClient = client;
                return;
            }

            boolean flag = random.nextBoolean();

            Client light = (flag ? waitingClient : client);
            Client dark = (flag ? client : waitingClient);

            Game game = new Game(light, dark);

            gamesByClientId.put(light, game);
            gamesByClientId.put(dark, game);
            games.add(game);
        }
    }

    public void disconnectClient(Client client) {
        synchronized (lock) {
            Game currentGame = gamesByClientId.get(client);

            if(currentGame != null) {
                currentGame.onDisconnect(client);
            }

            if(waitingClient == client) {
                waitingClient = null;
            }
        }
    }

    public void timeoutClient(Client client) {
        synchronized (lock) {
            Game game = gamesByClientId.get(client);

            if(game != null) {
                game.onTimeout(client);
                stopGame(game);
            }

            if(waitingClient == client) {
                waitingClient = null;
            }
        }
    }

    public void stopAll() {
        synchronized (lock) {
            List<Game> games = new ArrayList<>(this.games);

            for(Game game : games) {
                stopGame(game);
            }
        }
    }

    public void stopGame(Game game) {
        Checks.ensureNonNull(game, "game");

        synchronized (lock) {
            gamesByClientId.remove(game.darkClient, game);
            gamesByClientId.remove(game.lightClient, game);
            games.remove(game);
            game.stop();
        }
    }

    public List<Game> getGames() {
        synchronized (lock) {
            return Collections.unmodifiableList(games);
        }
    }
}
