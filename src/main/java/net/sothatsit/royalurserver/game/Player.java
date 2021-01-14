package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketWriter;
import net.sothatsit.royalurserver.network.PacketWritable;

/**
 * Represent a player in a game.
 *
 * @author Paddy Lamont
 */
public enum Player implements PacketWritable {

    DARK(1, "dark") {
        @Override
        public Player getOtherPlayer() {
            return LIGHT;
        }
    },

    LIGHT(2, "light") {
        @Override
        public Player getOtherPlayer() {
            return DARK;
        }
    };

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
        return name + " player";
    }

    /**
     * @return The other Player in a game.
     */
    public abstract Player getOtherPlayer();

    /**
     * @return An ID representing {@param player} to the client.
     */
    public static int toClientID(Player player) {
        if(player == null)  return 0;
        if(player == DARK)  return 1;
        if(player == LIGHT) return 2;

        throw new IllegalArgumentException("Unknown player " + player);
    }
}
