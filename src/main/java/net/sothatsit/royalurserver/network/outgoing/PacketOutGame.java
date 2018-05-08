package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.Player;
import net.sothatsit.royalurserver.util.Checks;

public class PacketOutGame {

    public static PacketOut create(Player yourPlayer, String opponentName) {
        Checks.ensureNonNull(yourPlayer, "yourPlayer");
        Checks.ensureNonNull(opponentName, "opponentName");

        PacketOut packet = new PacketOut(PacketOut.Type.GAME);

        packet.write(yourPlayer);
        packet.writeVarString(opponentName);

        return packet;
    }
}
