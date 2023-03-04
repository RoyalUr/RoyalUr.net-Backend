package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nonnull;

/**
 * A packet sent to clients when a game they are taking part in is stopped.
 *
 * @author Paddy Lamont
 */
public class PacketOutGameEnd extends GamePacketOut {

    private final @Nonnull String reason;

    public PacketOutGameEnd(@Nonnull GameID gameID, @Nonnull String reason) {
        super(Type.GAME_END, gameID);
        Checks.ensureNonNull(reason, "reason");
        this.reason = reason;
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);
        writer.pushRaw(reason);
    }

    @Override
    public String toString() {
        return "PacketOutGameEnd(reason=\"" + reason + "\")";
    }
}
