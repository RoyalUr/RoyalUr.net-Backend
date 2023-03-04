package net.sothatsit.royalurserver.game;

import net.royalur.Game;
import net.royalur.builder.BoardType;
import net.royalur.builder.PathType;
import net.royalur.model.PlayerState;
import net.royalur.rules.simple.SimplePiece;
import net.sothatsit.royalurserver.RoyalUrNetIdentity;

import javax.annotation.Nonnull;

/**
 * The settings for a game.
 *
 * @param boardType The type of board for the game.
 * @param pathType  The type of path for the game.
 */
public record GameSettings(
        @Nonnull BoardType boardType,
        @Nonnull PathType pathType
) {

    public static final @Nonnull GameSettings STANDARD = new GameSettings(BoardType.STANDARD, PathType.BELL);

    /**
     * Creates a new game using these settings.
     *
     * @return A new game using these settings.
     */
    public @Nonnull Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> create(
            @Nonnull RoyalUrNetIdentity lightIdentity,
            @Nonnull RoyalUrNetIdentity darkIdentity
    ) {
        return Game.builder()
                .simpleRules()
                .boardShape(boardType)
                .paths(pathType)
                .dice(new RoyalUrNetDice())
                .players(lightIdentity, darkIdentity)
                .build();
    }
}
