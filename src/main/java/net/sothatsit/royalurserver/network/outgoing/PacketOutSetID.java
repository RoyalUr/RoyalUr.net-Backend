package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

import java.util.UUID;

/**
 * A packet sent to set the unique ID of a client.
 *
 * @author Paddy Lamont
 */
public class PacketOutSetID {

    public static PacketOut create(UUID id) {
        Checks.ensureNonNull(id, "id");

        PacketOut packet = new PacketOut(PacketOut.Type.SET_ID);

        packet.writeUUID(id);

        return packet;
    }

}
