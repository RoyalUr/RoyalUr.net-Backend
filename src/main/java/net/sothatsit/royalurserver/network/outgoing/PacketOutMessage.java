package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to give an arbitrary message to the client.
 *
 * @author Paddy Lamont
 */
public class PacketOutMessage {

    public static PacketOut create(String message) {
        Checks.ensureNonNull(message, "message");

        PacketOut packet = new PacketOut(PacketOut.Type.MESSAGE);

        packet.writeVarString(message);

        return packet;
    }
}
