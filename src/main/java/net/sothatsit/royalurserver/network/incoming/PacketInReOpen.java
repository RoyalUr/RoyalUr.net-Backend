package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

import java.util.UUID;

/**
 * A packet sent by the client to indicate they are re-opening their connection and already have a unique ID.
 *
 * @author Paddy Lamont
 */
public class PacketInReOpen {

    public final UUID previousID;

    public PacketInReOpen(UUID previousID) {
        Checks.ensureNonNull(previousID, "previousId");

        this.previousID = previousID;
    }

    public static PacketInReOpen read(PacketIn packet) {
        return Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.REOPEN);

            UUID uuid = packet.nextUUID();

            packet.expectEmpty();

            return new PacketInReOpen(uuid);
        }, "exception reading re-open packet " + packet);
    }
}
