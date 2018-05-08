package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

import java.util.UUID;

public class PacketOutSetID {

    public static PacketOut create(UUID id) {
        Checks.ensureNonNull(id, "id");

        PacketOut packet = new PacketOut(PacketOut.Type.SETID);

        packet.write(id);

        return packet;
    }

}
