package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

public enum Player implements PacketWritable {

    DARK(1, "dark"),
    LIGHT(2, "light");

    public final int id;
    public final String name;

    private Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void writeTo(PacketOut packet) {
        packet.writeDigit(id);
    }

    @Override
    public String toString() {
        return name + " player";
    }

    public static Player getOtherPlayer(Player player) {
        Checks.ensureNonNull(player, "player");

        return Player.values()[(player.ordinal() + 1) % 2];
    }

    public static int toClientID(Player player) {
        if(player == null)  return 0;
        if(player == DARK)  return 1;
        if(player == LIGHT) return 2;

        throw new IllegalArgumentException("Unknown player " + player);
    }
}
