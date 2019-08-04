package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.Player;
import net.sothatsit.royalurserver.util.Checks;

public class PacketOutGame {

    public static PacketOut create(GameID gameID, Player yourPlayer, String opponentName) {
        Checks.ensureNonNull(gameID, "gameID");
        Checks.ensureNonNull(yourPlayer, "yourPlayer");
        Checks.ensureNonNull(opponentName, "opponentName");

        PacketOut packet = new PacketOut(PacketOut.Type.GAME);

        packet.write(gameID);
        packet.write(yourPlayer);
        packet.writeVarString(opponentName);

        return packet;
    }
}
