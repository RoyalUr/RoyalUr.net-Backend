package net.royalur.backend.management;

import net.royalur.backend.game.GameID;
import net.royalur.backend.game.GameSettings;
import net.royalur.backend.game.SavedGame;
import net.royalur.backend.network.Client;
import net.royalur.backend.network.outgoing.PacketOutGameInvalid;
import net.royalur.backend.scheduler.Scheduler;
import net.royalur.backend.util.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Manages the connection of clients to games.
 *
 * @author Paddy Lamont
 */
public class GameManager {

    private final Scheduler scheduler = new Scheduler("game-manager", 1, TimeUnit.SECONDS);

    private final GameRepository repository;

    private final Object lock = new Object();
    private final Map<GameID, ManagedGame> games = new HashMap<>();

    public GameManager(GameRepository repository) {
        this.repository = repository;
        scheduler.scheduleRepeating("game-purger", this::purgeInactiveGames, 5, TimeUnit.SECONDS);
    }

    public void start() {
        scheduler.start();
    }

    public void stop() {
        stopAll("Server is shutting down");
        scheduler.stop();
    }

    public boolean containsGame(@Nonnull GameID gameID) {
        synchronized (lock) {
            if (games.containsKey(gameID))
                return true;
        }

        GameRepositoryEntry entry = repository.get(gameID);
        return entry != null && entry.isGame();
    }

    public @Nullable ManagedGame getGameOrNull(@Nonnull GameID gameID) {
        synchronized (lock) {
            return games.get(gameID);
        }
    }

    /**
     * Note: The returned games are not thread-safe. Users beware!
     */
    public List<SavedGame> getActiveGames() {
        List<SavedGame> gameSnapshots = new ArrayList<>();
        synchronized (lock) {
            for (ManagedGame game : games.values()) {
                gameSnapshots.add(game.savedGame.copy());
            }
        }
        return gameSnapshots;
    }

    private List<ManagedGame> findActiveGames(@Nonnull Client client) {
        List<ManagedGame> activeGames = new ArrayList<>();
        synchronized (lock) {
            for (ManagedGame game : games.values()) {
                if (game.isPlayer(client)) {
                    activeGames.add(game);
                }
            }
        }
        return activeGames;
    }

    public void purgeInactiveGames() {
        List<ManagedGame> inactive = new ArrayList<>();
        synchronized (lock) {
            for (ManagedGame game : games.values()) {
                if (game.isInactive()) {
                    inactive.add(game);
                }
            }
            for (ManagedGame game : inactive) {
                stopGame(game, "Game is inactive");
            }
        }
    }

    public @Nonnull GameID reserveGameID(@Nonnull GameSettings settings, @Nonnull Client client) {
        return repository.reserveGameID(settings, client.getIdentity());
    }

    public void startGame(@Nonnull GameID id, @Nonnull Client light, @Nonnull Client dark) {
        ManagedGame game;
        synchronized (lock) {
            SavedGame savedGame = repository.createGame(id, light.getIdentity(), dark.getIdentity());
            game = new ManagedGame(savedGame, light, dark);
            games.put(id, game);
        }

        joinGame(id, light, false);
        joinGame(id, dark, false);
    }

    public void joinGame(GameID gameID, Client client, boolean isReconnect) {
        synchronized (lock) {
            ManagedGame game = games.get(gameID);
            if (game == null) {
                client.send(new PacketOutGameInvalid(gameID));
                return;
            }

            if (isReconnect) {
                game.onReconnect(client);
            } else {
                game.onJoin(client);
            }
        }
    }

    public void onClientDisconnect(Client client) {
        List<ManagedGame> games = findActiveGames(client);
        for (ManagedGame game : games) {
            game.onDisconnect(client);
        }
    }

    public void onClientTimeout(Client client) {
        List<ManagedGame> games = findActiveGames(client);
        for (ManagedGame game : games) {
            stopGame(game, "Your opponent abandoned the game");
        }
    }

    public void stopAll(String reason) {
        List<ManagedGame> games;
        synchronized (lock) {
            games = new ArrayList<>(this.games.values());
            for (ManagedGame game : games) {
                stopGame(game, reason);
            }
        }
    }

    public void stopGame(ManagedGame game, String reason) {
        Checks.ensureNonNull(game, "game");

        synchronized (lock) {
            games.remove(game.getID());
        }
        game.stop(reason);
    }
}
