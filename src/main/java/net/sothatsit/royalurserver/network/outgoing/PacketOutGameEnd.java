package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to clients when a game they are taking part in is stopped.
 *
 * @author Paddy Lamont
 */
public class PacketOutGameEnd extends PacketOut {

    private final String reason;

    public PacketOutGameEnd(String reason) {
        super(Type.GAME_END);
        Checks.ensureNonNull(reason, "reason");
        this.reason = reason;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushRaw(reason);
    }

    @Override
    public String toString() {
        return "PacketOutGameEnd(reason=\"" + reason + "\")";
    }
}
