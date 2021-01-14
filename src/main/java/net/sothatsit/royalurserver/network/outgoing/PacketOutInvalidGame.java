package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to the client when they attempt to join an unknown game.
 *
 * @author Paddy Lamont
 */
public class PacketOutInvalidGame extends PacketOut {

    private final GameID gameID;

    public PacketOutInvalidGame(GameID gameID) {
        super(Type.INVALID_GAME);
        Checks.ensureNonNull(gameID, "gameID");
        this.gameID = gameID;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushValue(gameID);
    }

    @Override
    public String toString() {
        return "PacketOutInvalidGame(gameID=" + gameID + ")";
    }
}
