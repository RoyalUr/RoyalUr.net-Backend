package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketWriter;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

/**
 * Stores the state of tiles on the game board.
 *
 * @author Paddy Lamont
 */
public class Board implements PacketWritable {

    public static final int WIDTH = 3;
    public static final int HEIGHT = 8;
    public static final int TILE_COUNT = WIDTH * HEIGHT;

    private final Player[] tileOwners = new Player[TILE_COUNT];

    /** @return Whether {@param location} has an owner. **/
    public boolean hasOwner(Location location) {
        return getOwner(location) != null;
    }

    /** @return The owner of {@param location}, or null. **/
    public Player getOwner(Location location) {
        Checks.ensureNonNull(location, "location");

        return tileOwners[location.index];
    }

    /** Clear the owner of {@param location}. **/
    public void clearOwner(Location location) {
        setOwner(location, null);
    }

    /**
     * Set the owner of {@param location} to {@param owner}.
     * @param owner can be null.
     */
    public void setOwner(Location location, Player owner) {
        Checks.ensureNonNull(location, "location");

        tileOwners[location.index] = owner;
    }

    @Override
    public void writeTo(PacketWriter out) {
        for(Player owner : tileOwners) {
            out.pushDigit(Player.toClientID(owner));
        }
    }
}
