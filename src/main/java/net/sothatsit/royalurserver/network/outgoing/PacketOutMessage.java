package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

public class PacketOutMessage {

    public static PacketOut create(String message) {
        Checks.ensureNonNull(message, "message");

        PacketOut packet = new PacketOut(PacketOut.Type.MESSAGE);

        packet.writeVarString(message);

        return packet;
    }
}
