package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

public class PlayerState implements PacketWritable {

    public static final int MAX_TILES = 7;

    public final Player player;
    public final String name;
    private int tiles;
    private int score;

    public PlayerState(Player player, String name) {
        this(player, name, MAX_TILES, 0);
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

    public int getTiles() {
        return tiles;
    }

    public int getScore() {
        return score;
    }

    public void addTile() {
        Checks.ensure(tiles < MAX_TILES, "tiles must remain lower than MAX_TILES (" + MAX_TILES + ")");

        this.tiles += 1;
    }

    public void useTile() {
        Checks.ensure(tiles > 0, "tiles must remain greater than 0");

        this.tiles -= 1;
    }

    public void addScore() {
        Checks.ensure(score < MAX_TILES, "score must remain lower than MAX_TILES (" + MAX_TILES + ")");

        this.score += 1;
    }

    public boolean isMaxScore() {
        return score == MAX_TILES;
    }

    @Override
    public void writeTo(PacketOut packet) {
        packet.writeDigit(tiles);
        packet.writeDigit(score);
    }
}
