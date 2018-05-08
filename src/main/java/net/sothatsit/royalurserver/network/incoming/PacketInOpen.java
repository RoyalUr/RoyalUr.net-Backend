package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

public class PacketInOpen {

    public static void read(PacketIn packet) {
        Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.OPEN);

            packet.expectEmpty();
        }, "exception reading open packet " + packet);
    }
}
