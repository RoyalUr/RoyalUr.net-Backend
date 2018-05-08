package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

public class Board implements PacketWritable {

    public static final int WIDTH = 3;
    public static final int HEIGHT = 8;
    public static final int TILE_COUNT = WIDTH * HEIGHT;

    static {
        // WIDTH and HEIGHT should be such that all coordinate x/y's are single digits
        Checks.ensureSingleDigit(WIDTH, "WIDTH");
        Checks.ensureSingleDigit(HEIGHT, "HEIGHT");
    }

    private final Player[] tileOwners = new Player[TILE_COUNT];

    public Player getOwner(Location location) {
        Checks.ensureNonNull(location, "location");

        return tileOwners[location.index];
    }

    public void clearOwner(Location location) {
        Checks.ensureNonNull(location, "location");

        tileOwners[location.index] = null;
    }

    public void setOwner(Location location, Player owner) {
        Checks.ensureNonNull(location, "location");
        Checks.ensureNonNull(owner, "owner");

        tileOwners[location.index] = owner;
    }

    @Override
    public void writeTo(PacketOut out) {
        for(Player owner : tileOwners) {
            out.writeDigit(Player.toClientID(owner));
        }
    }
}
