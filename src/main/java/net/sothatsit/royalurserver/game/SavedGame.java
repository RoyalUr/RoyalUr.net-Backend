package net.sothatsit.royalurserver.game;

import net.royalur.Game;
import net.royalur.model.PlayerIdentity;
import net.royalur.model.PlayerState;
import net.royalur.rules.simple.SimplePiece;
import net.sothatsit.royalurserver.RoyalUrNetIdentity;

import javax.annotation.Nonnull;

/**
 * A game that is being played, or was played, on this server.
 */
public class SavedGame {

    /**
     * The ID of this game.
     */
    public final @Nonnull GameID id;

    /**
     * The game of the Royal Game of Ur.
     */
    public final @Nonnull Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> game;

    /**
     * The identity of the light player of the game.
     */
    public final @Nonnull RoyalUrNetIdentity lightIdentity;

    /**
     * The identity of the dark player of the game.
     */
    public final @Nonnull RoyalUrNetIdentity darkIdentity;

    /**
     * @param game The game of the Royal Game of Ur.
     */
    public SavedGame(@Nonnull GameID id, @Nonnull Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> game) {
        this.id = id;
        this.game = game;

        PlayerIdentity lightIdentity = game.lightIdentity;
        PlayerIdentity darkIdentity = game.darkIdentity;

        if (!(lightIdentity instanceof RoyalUrNetIdentity)) {
            throw new IllegalArgumentException(
                    "The game's light player identity should be of type " + RoyalUrNetIdentity.class.getSimpleName() +
                            ", not " + lightIdentity.getClass().getSimpleName()
            );
        }
        if (!(darkIdentity instanceof RoyalUrNetIdentity)) {
            throw new IllegalArgumentException(
                    "The game's dark player identity should be of type " + RoyalUrNetIdentity.class.getSimpleName() +
                            ", not " + darkIdentity.getClass().getSimpleName()
            );
        }

        this.lightIdentity = (RoyalUrNetIdentity) lightIdentity;
        this.darkIdentity = (RoyalUrNetIdentity) darkIdentity;
    }

    private SavedGame(@Nonnull SavedGame template) {
        this(template.id, template.game.copy());
    }

    /**
     * Generates a copy of this saved game.
     * @return A copy of {@code this}.
     */
    public @Nonnull SavedGame copy() {
        return new SavedGame(this);
    }
}
