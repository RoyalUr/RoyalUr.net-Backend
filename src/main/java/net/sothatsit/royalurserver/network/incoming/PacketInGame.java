package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.util.Checks;

public class PacketInGame {

    public final GameID gameID;

    public PacketInGame(GameID gameID) {
        Checks.ensureNonNull(gameID, "gameID");

        this.gameID = gameID;
    }

    public static PacketInGame read(PacketIn packet) {
        return Checks.detailThrown(() -> {
            Checks.ensureNonNull(packet, "packet");
            packet.expectType(PacketIn.Type.GAME);

            GameID gameID = GameID.read(packet);

            packet.expectEmpty();

            return new PacketInGame(gameID);
        }, "exception reading game packet " + packet);
    }
}
