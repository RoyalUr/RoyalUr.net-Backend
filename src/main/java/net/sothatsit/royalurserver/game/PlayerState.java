package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

/**
 * Represents the state of a player in a game.
 *
 * @author Paddy Lamont
 */
public class PlayerState implements PacketWritable {

    public static final int MAX_TILES = 7;

    public final Player player;
    public final String name;
    private int tiles;
    private int score;

    public PlayerState(Player player, String name) {
        this(player, name, 7, 0);
    }

    public PlayerState(Player player, String name, int tiles, int score) {
        Checks.ensureNonNull(player, "player");
        Checks.ensureNonNull(name, "name");
        Checks.ensure(tiles >= 0, "tiles cannot be less than 0");
        Checks.ensure(score >= 0, "score cannot be less than 0");
        Checks.ensure(tiles <= MAX_TILES, "tiles must be less than MAX_TILES (" + MAX_TILES + ")");
        Checks.ensure(score <= MAX_TILES, "score must be less than MAX_TILES (" + MAX_TILES + ")");

        this.player = player;
        this.name = name;
        this.tiles = tiles;
        this.score = score;
    }

    /**
     * @return The number of un-played tiles this player has.
     */
    public int getTiles() {
        return tiles;
    }

    /**
     * @return The number of tiles this player has taken around and off the board.
     */
    public int getScore() {
        return score;
    }

    /**
     * Add another un-played tile to this player.
     */
    public void addTile() {
        Checks.ensure(tiles < MAX_TILES, "tiles must remain lower than MAX_TILES (" + MAX_TILES + ")");

        this.tiles += 1;
    }

    /**
     * Remove an un-played tile from this player.
     */
    public void useTile() {
        Checks.ensure(tiles > 0, "tiles must remain greater than 0");

        this.tiles -= 1;
    }

    /**
     * Add a scored tile to this player.
     */
    public void addScore() {
        Checks.ensure(score < MAX_TILES, "score must remain lower than MAX_TILES (" + MAX_TILES + ")");

        this.score += 1;
    }

    /**
     * @return Whether this player has reached the maximum score (i.e. won the game).
     */
    public boolean isMaxScore() {
        return score == MAX_TILES;
    }

    @Override
    public void writeTo(PacketOut packet) {
        packet.writeDigit(tiles);
        packet.writeDigit(score);
    }
}
