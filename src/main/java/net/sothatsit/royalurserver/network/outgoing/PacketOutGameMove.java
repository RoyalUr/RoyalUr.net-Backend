package net.sothatsit.royalurserver.network.outgoing;

import net.royalur.model.Move;
import net.royalur.model.Tile;
import net.royalur.model.path.BellPathPair;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A packet sent to indicate that a move has been made in a game.
 *
 * @author Paddy Lamont
 */
public class PacketOutGameMove extends GamePacketOut {

    private static final BellPathPair temporaryPathForCompatibility = new BellPathPair();

    private final @Nonnull Move<?> move;

    public PacketOutGameMove(@Nonnull GameID gameID, @Nonnull Move<?> move) {
        super(Type.GAME_MOVE, gameID);
        Checks.ensureNonNull(move, "move");
        this.move = move;
    }

    private void writeTile(@Nonnull PacketWriter writer, @Nullable Tile tile, @Nonnull Tile nullTile) {
        if (tile != null) {
            writer.pushInt(tile.ix, 2);
            writer.pushInt(tile.iy, 2);
        } else {
            writer.pushInt(nullTile.ix, 2);
            writer.pushInt(nullTile.iy, 2);
        }
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);

        Tile source = (move.isIntroducingPiece() ? null : move.getSource());
        Tile destination = (move.isScoringPiece() ? null : move.getDestination());

        writeTile(writer, source, temporaryPathForCompatibility.get(move.player).startTile);
        writeTile(writer, destination, temporaryPathForCompatibility.get(move.player).endTile);
    }

    @Override
    public @Nonnull String toString() {
        return "PacketOutGameMove(gameID=" + gameID + ", move=" + move + ")";
    }
}
