package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Location implements PacketWritable {

    public static final List<Location> LOTUSES = list(
            0, 0,
            2, 0,
            1, 3,
            0, 6,
            2, 6
    );

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

    public Location(int x, int y) {
        Checks.ensure(x >= 0, "x must be >= 0");
        Checks.ensure(y >= 0, "y must be >= 0");
        Checks.ensure(x < Board.WIDTH, "x must be less than Board.WIDTH (" + Board.WIDTH + ")");
        Checks.ensure(y < Board.HEIGHT, "y must be less than Board.HEIGHT (" + Board.HEIGHT + ")");

        this.x = x;
        this.y = y;
        this.index = y * Board.WIDTH + x;
    }

    public Location add(int dx, int dy) {
        return new Location(x + dx, y + dy);
    }

    public boolean isLotus() {
        for(Location lotus : LOTUSES) {
            if(equals(lotus))
                return true;
        }
        return false;
    }

    public boolean isStart(Player player) {
        return equals(player == Player.LIGHT ? LIGHT_START : DARK_START);
    }

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
    public void writeTo(PacketOut packet) {
        packet.writeDigit(x);
        packet.writeDigit(y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public static Location read(PacketIn packet) {
        int x = packet.nextInt(1);
        int y = packet.nextInt(1);

        return new Location(x, y);
    }

    public static List<Location> getPath(Player player) {
        return (player == Player.LIGHT ? LIGHT_PATH : DARK_PATH);
    }

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

    private static int signum(int num) {
        return (num < 0 ? -1 : (num > 0 ? 1 : 0));
    }
}
