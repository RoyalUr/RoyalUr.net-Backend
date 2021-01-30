package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to the client telling them that a game is pending.
 *
 * @author Paddy Lamont
 */
public class PacketOutGamePending extends PacketOut {

    public final GameID gameID;

    public PacketOutGamePending(GameID gameID) {
        super(Type.GAME_PENDING);
        Checks.ensureNonNull(gameID, "gameID");
        this.gameID = gameID;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushValue(gameID);
    }

    @Override
    public String toString() {
        return "PacketOutGamePending(gameID=" + gameID + ")";
    }
}
