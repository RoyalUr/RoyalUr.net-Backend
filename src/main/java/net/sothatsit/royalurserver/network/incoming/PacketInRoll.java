package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

public class PacketInRoll {

    public static void read(PacketIn packet) {
        Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.ROLL);

            packet.expectEmpty();
        }, "exception reading roll packet " + packet);
    }
}
