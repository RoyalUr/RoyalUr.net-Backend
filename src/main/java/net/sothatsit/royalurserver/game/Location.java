package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.incoming.PacketReader;
import net.sothatsit.royalurserver.network.outgoing.PacketWriter;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A location on the game board.
 *
 * @author Paddy Lamont
 */
public final class Location implements PacketWritable {

    /**
     * The locations of all rosette tiles.
     */
    public static final List<Location> ROSETTES = list(
            0, 0,
            2, 0,
            1, 3,
            0, 6,
            2, 6
    );

    /**
     * The path that all light tiles must take around the board.
     */
    public static List<Location> LIGHT_PATH = path(
            0, 4,
            0, 0,
            1, 0,
            1, 7,
            0, 7,
            0, 5
    );
    public static final Location LIGHT_START = LIGHT_PATH.get(0);
    public static final Location LIGHT_END = LIGHT_PATH.get(LIGHT_PATH.size() - 1);

    /**
     * The path that all dark tiles must take around the board.
     */
    public static List<Location> DARK_PATH = path(
            2, 4,
            2, 0,
            1, 0,
            1, 7,
            2, 7,
            2, 5
    );
    public static final Location DARK_START = DARK_PATH.get(0);
    public static final Location DARK_END = DARK_PATH.get(DARK_PATH.size() - 1);

    public final int x;
    public final int y;
    public final int index;

    /**
     * Create a location at {@param x}, {@param y} on the board.
     */
    public Location(int x, int y) {
        Checks.ensure(x >= 0, "x must be >= 0");
        Checks.ensure(y >= 0, "y must be >= 0");
        Checks.ensure(x < Board.WIDTH, "x must be less than the width of the board");
        Checks.ensure(y < Board.HEIGHT, "y must be less than the height of the board");

        this.x = x;
        this.y = y;
        this.index = y * Board.WIDTH + x;
    }

    /**
     * @return This location shifted by {@param dx}, {@param dy}.
     */
    public Location add(int dx, int dy) {
        return new Location(x + dx, y + dy);
    }

    /**
     * @return Whether this location is a rosette tile.
     */
    public boolean isRosette() {
        for(Location rosette : ROSETTES) {
            if(equals(rosette))
                return true;
        }
        return false;
    }

    /**
     * @return Whether this location is the start for {@param player}.
     */
    public boolean isStart(Player player) {
        return equals(player == Player.LIGHT ? LIGHT_START : DARK_START);
    }

    /**
     * @return Whether this location is the end for {@param player}.
     */
    public boolean isEnd(Player player) {
        return equals(player == Player.LIGHT ? LIGHT_END : DARK_END);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(index);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Location))
            return false;

        Location other = (Location) obj;

        return other.index == index;
    }

    @Override
    public void writeTo(PacketWriter packet) {
        packet.pushDigit(x);
        packet.pushDigit(y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    /**
     * @return The next Location read from {@param packet}.
     */
    public static Location read(PacketReader packet) {
        int x = packet.nextInt(1);
        int y = packet.nextInt(1);

        return new Location(x, y);
    }

    /**
     * @return The path that {@param player}'s tiles must take.
     */
    public static List<Location> getPath(Player player) {
        return (player == Player.LIGHT ? LIGHT_PATH : DARK_PATH);
    }

    /**
     * Utility function to tersely construct a list of locations.
     */
    private static List<Location> list(int... coordinates) {
        Checks.ensureNonNull(coordinates, "coordinates");
        Checks.ensure(coordinates.length % 2 == 0, "coordinates must have an even length");

        int length = coordinates.length / 2;
        List<Location> locations = new ArrayList<>(length);

        for(int index = 0; index < length; ++index) {
            locations.add(new Location(coordinates[index * 2], coordinates[index * 2 + 1]));
        }

        return locations;
    }

    /**
     * Utility function to tersely construct a path list of locations.
     */
    private static List<Location> path(int... coordinates) {
        Checks.ensureNonNull(coordinates, "coordinates");
        Checks.ensure(coordinates.length % 2 == 0, "coordinates must have an even length");

        if(coordinates.length == 0)
            return Collections.emptyList();

        List<Location> locations = new ArrayList<>();

        Location from = new Location(coordinates[0], coordinates[1]);
        for(int index = 2; index < coordinates.length; index += 2) {
            Location to = new Location(coordinates[index], coordinates[index + 1]);

            while(!from.equals(to)) {
                int dx = signum(to.x - from.x);
                int dy = signum(to.y - from.y);

                locations.add(from);
                from = from.add(dx, dy);
            }
        }

        locations.add(from);

        return locations;
    }

    /**
     * @return -1 if {@param num} is negative, 1 if {@param num} is positive, else 0.
     */
    private static int signum(int num) {
        return Integer.compare(num, 0);
    }
}
