package net.royalur.backend.network.incoming;

import net.royalur.model.BoardShape;
import net.royalur.model.Tile;
import net.royalur.model.shape.StandardBoardShape;

import javax.annotation.Nullable;

/**
 * A packet sent to move a piece.
 *
 * @author Paddy Lamont
 */
public class PacketInGameMove extends GamePacketIn {

    private static final BoardShape temporaryBoardShapeForCompatibility = new StandardBoardShape();

    public @Nullable Tile from;

    public PacketInGameMove() {
        super(Type.MOVE);
    }

    @Override
    public void readContents(PacketReader reader) {
        super.readContents(reader);
        int x = reader.nextInt(2);
        int y = reader.nextInt(2);
        Tile tile = Tile.fromIndices(x, y);

        // TODO : This only supports the standard board shape...
        // This should not be the way this is supported.
        if (temporaryBoardShapeForCompatibility.contains(tile)) {
            this.from = tile;
        } else {
            this.from = null;
        }
    }

    @Override
    public String toString() {
        return "PacketInMove(gameID=" + gameID + ", from=" + from + ")";
    }
}
