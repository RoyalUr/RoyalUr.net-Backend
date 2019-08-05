package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent after opening a connection to indicate the client does not already have a unique ID.
 *
 * @author Paddy Lamont
 */
public class PacketInOpen {

    public static void read(PacketIn packet) {
        Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.OPEN);

            packet.expectEmpty();
        }, "exception reading open packet " + packet);
    }
}
