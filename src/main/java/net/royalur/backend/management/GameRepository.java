package net.royalur.backend.management;

import net.royalur.Game;
import net.royalur.model.PlayerState;
import net.royalur.rules.simple.SimplePiece;
import net.royalur.backend.RoyalUrNetIdentity;
import net.royalur.backend.game.GameID;
import net.royalur.backend.game.GameSettings;
import net.royalur.backend.game.RoyalUrNetDiceRoll;
import net.royalur.backend.game.SavedGame;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * A repository for all RoyalUr.net games. These games do not contain
 * any connection information. For now, this only stores games in-memory,
 * but a database is planned.
 */
public class GameRepository {

    private final Object lock = new Object();

    /**
     * The source of randomness to use to generate game IDs.
     */
    private final @Nonnull Random random;

    /**
     * An in-memory cache of games.
     */
    private final @Nonnull Map<GameID, GameRepositoryEntry> gameCache = new HashMap<>();

    public GameRepository(@Nonnull Random random) {
        this.random = random;
    }

    public GameRepository() {
        this(new SecureRandom());
    }

    /**
     * Generates and reserves a new game ID for the given identity.
     * @return A reserved game ID.
     */
    public @Nonnull GameID reserveGameID(@Nonnull GameSettings settings, @Nonnull RoyalUrNetIdentity identity) {
        GameID gameID;
        synchronized (lock) {
            do {
                gameID = GameID.random(random);
            } while (gameCache.containsKey(gameID));

            GameReservation reservation = new GameReservation(gameID, settings, identity);
            gameCache.put(gameID, GameRepositoryEntry.create(reservation));
        }
        return gameID;
    }

    /**
     * Generates a new game and adds it to the repository.
     * @param lightIdentity The identity of the light player.
     * @param darkIdentity The identity of the dark player.
     * @return A new game.
     */
    public @Nonnull SavedGame createGame(
            @Nonnull GameSettings settings,
            @Nonnull RoyalUrNetIdentity lightIdentity,
            @Nonnull RoyalUrNetIdentity darkIdentity
    ) {
        synchronized (lock) {
            GameID gameID = reserveGameID(settings, lightIdentity);
            return createGame(gameID, lightIdentity, darkIdentity);
        }
    }

    /**
     * Generates a new game and adds it to the repository.
     * @param gameID The identifier to use for the game.
     * @param lightIdentity The identity of the light player.
     * @param darkIdentity The identity of the dark player.
     * @return A new game.
     */
    public @Nonnull SavedGame createGame(
            @Nonnull GameID gameID,
            @Nonnull RoyalUrNetIdentity lightIdentity,
            @Nonnull RoyalUrNetIdentity darkIdentity
    ) {
        synchronized (lock) {
            GameRepositoryEntry entry = gameCache.get(gameID);
            if (entry == null)
                throw new IllegalArgumentException("The given game ID has not been reserved: " + gameID);
            if (!entry.isReservation())
                throw new IllegalArgumentException("The game has already been created: " + gameID);

            GameSettings settings = entry.getReservation().settings;
            Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> game = settings.create(lightIdentity, darkIdentity);

            SavedGame savedGame = new SavedGame(gameID, game);
            gameCache.put(gameID, GameRepositoryEntry.create(savedGame));
            return savedGame.copy();
        }
    }

    /**
     * Updates the state of a game in the repository.
     * @param game The new game state.
     */
    public void update(@Nonnull SavedGame game) {
        synchronized (lock) {
            gameCache.put(game.id, GameRepositoryEntry.create(game.copy()));
        }
    }

    /**
     * Attempts to find a game or reservation with the given ID.
     * @param id The ID of the game.
     * @return The game associated with the ID, or else {@code null}.
     */
    public @Nullable GameRepositoryEntry get(@Nonnull GameID id) {
        synchronized (lock) {
            return gameCache.get(id);
        }
    }
}
