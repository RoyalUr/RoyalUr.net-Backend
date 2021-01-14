package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

import java.util.UUID;

/**
 * A packet sent to set the unique ID of a client.
 *
 * @author Paddy Lamont
 */
public class PacketOutSetID extends PacketOut {

    private final UUID id;

    public PacketOutSetID(UUID id) {
        super(Type.SET_ID);
        Checks.ensureNonNull(id, "id");
        this.id = id;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushUUID(id);
    }

    @Override
    public String toString() {
        return "PacketOutSetID(id=" + id + ")";
    }
}
