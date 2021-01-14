package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.Player;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to indicate the connection status of a player.
 *
 * @author Paddy Lamont
 */
public class PacketOutPlayerStatus extends PacketOut {

    private final Player player;
    private final boolean connected;

    public PacketOutPlayerStatus(Player player, boolean connected) {
        super(Type.PLAYER_STATUS);
        Checks.ensureNonNull(player, "player");
        this.player = player;
        this.connected = connected;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushValue(player);
        writer.pushBool(connected);
    }

    @Override
    public String toString() {
        return "PacketOutPlayerStatus(player=" + player + ", connected=" + connected + ")";
    }
}
