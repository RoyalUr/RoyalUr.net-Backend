package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketWriter;
import net.sothatsit.royalurserver.network.PacketWritable;

/**
 * Represent a player in a game.
 *
 * @author Paddy Lamont
 */
public enum Player implements PacketWritable {
    DARK(1, "dark"),
    LIGHT(2, "light"),
    SPECTATOR(3, "spectator");

    public final int id;
    public final String name;

    private Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void writeTo(PacketWriter packet) {
        packet.pushDigit(id);
    }

    @Override
    public String toString() {
        return "Player." + name();
    }

    /** @return The other Player in a game. **/
    public Player getOtherPlayer() {
        switch (this) {
            case DARK: return LIGHT;
            case LIGHT: return DARK;
            case SPECTATOR: throw new IllegalStateException(this + " does not have an other player");
            default: throw new IllegalStateException("Unknown player " + this);
        }
    }
}
