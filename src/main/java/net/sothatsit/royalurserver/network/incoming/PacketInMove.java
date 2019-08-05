package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.game.Location;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to move a tile.
 *
 * @author Paddy Lamont
 */
public class PacketInMove {

    public final Location from;

    public PacketInMove(Location from) {
        Checks.ensureNonNull(from, "from");

        this.from = from;
    }

    public static PacketInMove read(PacketIn packet) {
        return Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.MOVE);

            Location tile = Location.read(packet);

            packet.expectEmpty();

            return new PacketInMove(tile);
        }, "exception reading move packet " + packet);
    }
}
