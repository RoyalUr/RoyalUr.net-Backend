package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.game.Location;

/**
 * A packet sent to move a tile.
 *
 * @author Paddy Lamont
 */
public class PacketInMove extends PacketIn {

    public Location from;

    public PacketInMove() {
        super(Type.MOVE);
    }

    @Override
    public void readContents(PacketReader reader) {
        this.from = Location.read(reader);
    }

    @Override
    public String toString() {
        return "PacketInMove(from=" + from + ")";
    }
}
