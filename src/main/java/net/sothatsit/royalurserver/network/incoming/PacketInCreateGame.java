package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to request that a match be created that they can get a friend to join.
 *
 * @author Paddy Lamont
 */
public class PacketInCreateGame {

    public static PacketInCreateGame read(PacketIn packet) {
        return Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.CREATE_GAME);

            packet.expectEmpty();

            return new PacketInCreateGame();
        }, "exception reading game packet " + packet);
    }
}
