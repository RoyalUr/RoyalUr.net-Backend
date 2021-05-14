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
    private final String lightName;
    private final String darkName;
    private final boolean lightConnected;
    private final boolean darkConnected;

    public PacketOutGame(
            GameID gameID,
            Player yourPlayer,
            String lightName,
            String darkName,
            boolean lightConnected,
            boolean darkConnected) {

        super(Type.GAME);
        Checks.ensureNonNull(gameID, "gameID");
        Checks.ensureNonNull(yourPlayer, "yourPlayer");
        Checks.ensureNonNull(lightName, "lightName");
        Checks.ensureNonNull(darkName, "darkName");
        this.gameID = gameID;
        this.yourPlayer = yourPlayer;
        this.lightName = lightName;
        this.darkName = darkName;
        this.lightConnected = lightConnected;
        this.darkConnected = darkConnected;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushValue(gameID)
                .pushValue(yourPlayer)
                .pushVarString(lightName)
                .pushVarString(darkName)
                .pushBool(lightConnected)
                .pushBool(darkConnected);
    }

    @Override
    public String toString() {
        return "PacketOutGame(gameID=" + gameID + ", "
                + "yourPlayer=" + yourPlayer + ", "
                + "lightName=\"" + lightName + "\", "
                + "darkName=\"" + darkName + "\", "
                + "lightConnected=" + lightConnected + ", "
                + "darkConnected=" + darkConnected + ")";
    }
}
