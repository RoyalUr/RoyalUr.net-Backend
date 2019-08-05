package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to request to join a game.
 *
 * @author Paddy Lamont
 */
public class PacketInJoinGame {

    public final GameID gameID;

    public PacketInJoinGame(GameID gameID) {
        Checks.ensureNonNull(gameID, "gameID");

        this.gameID = gameID;
    }

    public static PacketInJoinGame read(PacketIn packet) {
        return Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.JOIN_GAME);

            GameID gameID = GameID.read(packet);

            packet.expectEmpty();

            return new PacketInJoinGame(gameID);
        }, "exception reading game packet " + packet);
    }
}
