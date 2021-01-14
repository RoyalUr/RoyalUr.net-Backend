package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.Move;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to indicate a move has been made.
 *
 * @author Paddy Lamont
 */
public class PacketOutMove extends PacketOut {

    private final Move move;

    public PacketOutMove(Move move) {
        super(Type.MOVE);
        Checks.ensureNonNull(move, "move");
        this.move = move;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushValue(move);
    }

    @Override
    public String toString() {
        return "PacketOutMove(move=" + move + ")";
    }
}
