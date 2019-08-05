package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to indicate an error occurred between them and the game.
 *
 * @author Paddy Lamont
 */
public class PacketOutError {

    public static PacketOut create(String error) {
        Checks.ensureNonNull(error, "error");

        PacketOut packet = new PacketOut(PacketOut.Type.ERROR);

        packet.writeString(error);

        return packet;
    }

}
