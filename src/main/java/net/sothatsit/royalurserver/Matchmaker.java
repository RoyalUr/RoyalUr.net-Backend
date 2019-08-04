package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.game.Game;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.GameState;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Matchmaker {

    private static final SecureRandom random = new SecureRandom();

    private final Object lock = new Object();

    private final Scheduler scheduler = new Scheduler("matchmaker", 1, TimeUnit.SECONDS);
    private final List<Game> games = new ArrayList<>();
    private final Map<Client, Game> gamesByClientId = new HashMap<>();
    private Client waitingClient = null;

    public Matchmaker() {
        scheduler.scheduleRepeating("game-purger", () -> {
            synchronized (lock) {
                List<Game> done = new ArrayList<>();

                for(Game game : games) {
                    if(game.getState() != GameState.DONE)
                        continue;

                    done.add(game);
                }

                for(Game game : done) {
                    stopGame(game);
                }
            }
        }, 5, TimeUnit.SECONDS);
    }

    public Game findGame(Client client) {
        synchronized (lock) {
            Game game = gamesByClientId.get(client);

            if(game != null && game.getState() == GameState.DONE) {
                stopGame(game);
                return null;
            }

            return game;
        }
    }

    public void connectClient(Client client) {
        synchronized (lock) {
            Game currentGame = findGame(client);

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

            Game game = new Game(GameID.random(), light, dark);

            gamesByClientId.put(light, game);
            gamesByClientId.put(dark, game);
            games.add(game);
        }
    }

    public void disconnectClient(Client client) {
        synchronized (lock) {
            Game currentGame = findGame(client);

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
            Game game = findGame(client);

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
            return Collections.unmodifiableList(new ArrayList<>(games));
        }
    }
}
