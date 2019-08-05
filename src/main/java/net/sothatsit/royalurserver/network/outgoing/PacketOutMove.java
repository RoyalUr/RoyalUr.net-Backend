package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.Move;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to indicate a move has been made.
 *
 * @author Paddy Lamont
 */
public class PacketOutMove {

    public static PacketOut create(Move move) {
        Checks.ensureNonNull(move, "move");

        PacketOut packet = new PacketOut(PacketOut.Type.MOVE);

        packet.write(move.from);
        packet.write(move.to);

        return packet;
    }

}
