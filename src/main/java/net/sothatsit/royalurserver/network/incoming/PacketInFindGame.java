package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent by the client to request they be entered in for match making.
 *
 * @author Paddy Lamont
 */
public class PacketInFindGame {

    public static PacketInFindGame read(PacketIn packet) {
        return Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.FIND_GAME);

            packet.expectEmpty();

            return new PacketInFindGame();
        }, "exception reading game packet " + packet);
    }
}
