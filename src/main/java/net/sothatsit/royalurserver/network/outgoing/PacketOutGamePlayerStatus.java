package net.sothatsit.royalurserver.network.outgoing;

import net.royalur.model.Player;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nonnull;

/**
 * A packet sent to indicate the connection status of a player.
 *
 * @author Paddy Lamont
 */
public class PacketOutGamePlayerStatus extends GamePacketOut {

    private final @Nonnull Player player;
    private final boolean connected;

    public PacketOutGamePlayerStatus(@Nonnull GameID gameID, @Nonnull Player player, boolean connected) {
        super(Type.GAME_PLAYER_STATUS, gameID);
        Checks.ensureNonNull(player, "player");
        this.player = player;
        this.connected = connected;
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);
        PacketOutGameMetadata.writePlayer(writer, player, 3);
        writer.pushBool(connected);
    }

    @Override
    public String toString() {
        return "PacketOutGamePlayerStatus(gameID=" + gameID + ", player=" + player + ", connected=" + connected + ")";
    }
}
