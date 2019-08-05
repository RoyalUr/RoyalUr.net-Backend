package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.Player;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to indicate that a game has been won!
 *
 * @author Paddy Lamont
 */
public class PacketOutWin {

    public static PacketOut create(Player winner) {
        Checks.ensureNonNull(winner, "winner");

        PacketOut packet = new PacketOut(PacketOut.Type.WIN);

        packet.write(winner);

        return packet;
    }
}
