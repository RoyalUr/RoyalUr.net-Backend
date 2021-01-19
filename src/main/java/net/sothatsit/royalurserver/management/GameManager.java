package net.sothatsit.royalurserver.management;

import net.sothatsit.royalurserver.game.Game;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.GameState;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.outgoing.PacketOutInvalidGame;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages all the games currently running.
 *
 * @author Paddy Lamont
 */
public class GameManager {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Scheduler scheduler = new Scheduler("game-manager", 1, TimeUnit.SECONDS);

    private final Object lock = new Object();
    private final Map<GameID, Game> games = new ConcurrentHashMap<>();
    private final Map<Client, Game> clientActiveGames = new ConcurrentHashMap<>();

    public GameManager() {
        scheduler.scheduleRepeating("game-purger", this::purgeInactiveGames, 5, TimeUnit.SECONDS);
    }

    public void start() {
        scheduler.start();
    }

    public void stop() {
        scheduler.stop();
    }

    public void purgeInactiveGames() {
        synchronized (lock) {
            List<Game> inactive = new ArrayList<>();
            for(Game game : games.values()) {
                if (!game.isInactive())
                    continue;

                inactive.add(game);
            }
            for(Game game : inactive) {
                stopGame(game, "Game is inactive");
            }
        }
    }

    public Game findActiveGame(Client client) {
        Game game = clientActiveGames.get(client);
        if(game != null && game.getState() == GameState.DONE) {
            stopGame(game, "Game is done");
            return null;
        }
        return game;
    }

    public void startGame(Client light, Client dark) {
        GameID id = GameID.random(RANDOM);
        Game game;

        synchronized (lock) {
            // This is kinda dodgy, but repeats should only occur _very_ rarely
            while (games.containsKey(id)) {
                id = GameID.random(RANDOM);
            }
            game = new Game(id, light, dark);
            games.put(id, game);
        }

        onJoinGame(light, id, false);
        onJoinGame(dark, id, false);
    }

    public void onJoinGame(Client client, GameID gameID, boolean isReconnect) {
        synchronized (lock) {
            Game activeGame = findActiveGame(client);
            if (activeGame != null) {
                if (activeGame.id.equals(gameID)) {
                    client.error("Already part of game " + gameID);
                    return;
                }

                activeGame.onDisconnect(client);
                clientActiveGames.remove(client);
            }

            Game joinGame = games.get(gameID);
            if (joinGame == null) {
                client.send(new PacketOutInvalidGame(gameID));
                return;
            }

            if (isReconnect) {
                joinGame.onReconnect(client);
            } else {
                joinGame.onJoin(client);
            }
            clientActiveGames.put(client, joinGame);
        }
    }

    public void onClientDisconnect(Client client) {
        synchronized (lock) {
            Game game = findActiveGame(client);
            if (game != null) {
                game.onDisconnect(client);
            }
            clientActiveGames.remove(client);
        }
    }

    public void onClientTimeout(Client client) {
        synchronized (lock) {
            Set<Game> toStop = new HashSet<>();
            for (Game game : games.values()) {
                if (game.isPlayer(client)) {
                    toStop.add(game);
                }
            }
            for (Game game : toStop) {
                game.onTimeout(client);
                stopGame(game, "Opponent left the game");
            }
        }
    }

    public void stopAll(String reason) {
        synchronized (lock) {
            List<Game> games = new ArrayList<>(this.games.values());
            for(Game game : games) {
                stopGame(game, reason);
            }
            scheduler.stop();
        }
    }

    public void stopGame(Game game, String reason) {
        Checks.ensureNonNull(game, "game");

        synchronized (lock) {
            clientActiveGames.remove(game.darkClient);
            clientActiveGames.remove(game.lightClient);
            games.remove(game.id);
            game.stop(reason);
        }
    }

    public List<Game> getGames() {
        return Collections.unmodifiableList(new ArrayList<>(games.values()));
    }
}
