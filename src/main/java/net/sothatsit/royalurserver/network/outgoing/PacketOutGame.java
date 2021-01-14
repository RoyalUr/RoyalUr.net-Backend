package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.Player;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to the client containing the details about a game they've been added to.
 *
 * @author Paddy Lamont
 */
public class PacketOutGame extends PacketOut {

    private final GameID gameID;
    private final Player yourPlayer;
    private final String yourName;
    private final String opponentName;

    public PacketOutGame(GameID gameID, Player yourPlayer, String yourName, String opponentName) {
        super(Type.GAME);
        Checks.ensureNonNull(gameID, "gameID");
        Checks.ensureNonNull(yourPlayer, "yourPlayer");
        Checks.ensureNonNull(opponentName, "opponentName");
        this.gameID = gameID;
        this.yourPlayer = yourPlayer;
        this.yourName = yourName;
        this.opponentName = opponentName;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushValue(gameID).pushValue(yourPlayer).pushVarString(yourName).pushVarString(opponentName);
    }

    @Override
    public String toString() {
        return "PacketOutGame(gameID=" + gameID + ", "
                + "yourPlayer=" + yourPlayer + ", "
                + "opponentName=\"" + opponentName + "\")";
    }
}
