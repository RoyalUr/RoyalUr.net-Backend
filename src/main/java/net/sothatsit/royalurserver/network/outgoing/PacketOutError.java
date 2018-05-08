package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

public class PacketOutError {

    public static PacketOut create(String error) {
        Checks.ensureNonNull(error, "error");

        PacketOut packet = new PacketOut(PacketOut.Type.ERROR);

        packet.write(error);

        return packet;
    }

}
