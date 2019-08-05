package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to request the dice be rolled.
 *
 * @author Paddy Lamont
 */
public class PacketInRoll {

    public static void read(PacketIn packet) {
        Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.ROLL);

            packet.expectEmpty();
        }, "exception reading roll packet " + packet);
    }
}
