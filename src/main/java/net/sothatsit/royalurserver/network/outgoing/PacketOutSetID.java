package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * A packet sent to set the unique ID of a client.
 *
 * @author Paddy Lamont
 */
public class PacketOutSetID extends PacketOut {

    private final @Nonnull UUID id;

    public PacketOutSetID(@Nonnull UUID id) {
        super(Type.SET_ID);
        Checks.ensureNonNull(id, "id");
        this.id = id;
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);
        writer.pushUUID(id);
    }

    @Override
    public @Nonnull String toString() {
        return "PacketOutSetID(id=" + id + ")";
    }
}
