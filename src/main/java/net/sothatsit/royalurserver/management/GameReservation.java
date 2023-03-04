package net.sothatsit.royalurserver.management;

import net.sothatsit.royalurserver.RoyalUrNetIdentity;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.GameSettings;

import javax.annotation.Nonnull;

/**
 * A reservation for a game ID.
 */
public class GameReservation {

    /**
     * The game ID of this reservation.
     */
    public final @Nonnull GameID id;

    /**
     * The settings for this game reservation.
     */
    public final @Nonnull GameSettings settings;

    /**
     * The identity of the user that made the reservation.
     */
    public final @Nonnull RoyalUrNetIdentity identity;

    public GameReservation(@Nonnull GameID id, @Nonnull GameSettings settings, @Nonnull RoyalUrNetIdentity identity) {
        this.id = id;
        this.settings = settings;
        this.identity = identity;
    }
}
