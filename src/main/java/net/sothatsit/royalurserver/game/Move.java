package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.util.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a move of a tile from one location to another.
 *
 * @author Paddy Lamont
 */
public final class Move implements PacketWritable {

    public final Location from;
    public final Location to;

    /**
     * Create a Move from location {@param from} to location {@param to}.
     */
    public Move(Location from, Location to) {
        Checks.ensureNonNull(from, "from");
        Checks.ensureNonNull(to, "to");

        this.from = from;
        this.to = to;
    }

    @Override
    public void writeTo(PacketOut packet) {
        packet.write(from);
        packet.write(to);
    }

    @Override
    public int hashCode() {
        return from.hashCode() ^ to.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Move))
            return false;

        Move other = (Move) object;

        return from.equals(other.from) && to.equals(other.to);
    }

    @Override
    public String toString() {
        return from + " -> " + to;
    }

    /**
     * @return A List of all possible moves for the player {@param playerState} on the
     *         given board {@param board} with the dice value {@param diceValue}.
     */
    public static List<Move> getMoves(Board board, PlayerState playerState, int diceValue) {
        Checks.ensureNonNull(board, "board");
        Checks.ensureNonNull(playerState, "playerState");
        Checks.ensure(diceValue >= 0 && diceValue <= 4, "invalid dice value " + diceValue);

        if(diceValue == 0)
            return Collections.emptyList();

        List<Move> moves = new ArrayList<>();

        List<Location> path = Location.getPath(playerState.player);
        List<Location> fromTiles = new ArrayList<>();

        for(int x = 0; x < Board.WIDTH; ++x) {
            for(int y = 0; y < Board.HEIGHT; ++y) {
                Location location = new Location(x, y);
                Player owner = board.getOwner(location);

                if(owner != playerState.player)
                    continue;

                fromTiles.add(location);
            }
        }

        if(playerState.getTiles() > 0) {
            fromTiles.add(path.get(0));
        }

        for(Location from : fromTiles) {
            int index = path.indexOf(from);

            if(index == -1)
                throw new IllegalStateException("tile on board, but not on player's path");

            int toIndex = index + diceValue;

            if(toIndex >= path.size())
                continue;

            Location to = path.get(toIndex);
            Player toOwner = board.getOwner(to);

            if(toOwner == playerState.player || (toOwner != null && to.isLotus()))
                continue;

            moves.add(new Move(from, to));
        }

        return moves;
    }
}
