package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.GameID;

import javax.annotation.Nonnull;

/**
 * A packet sent to the client when they attempt to join an unknown game.
 *
 * @author Paddy Lamont
 */
public class PacketOutGameInvalid extends GamePacketOut {

    public PacketOutGameInvalid(@Nonnull GameID gameID) {
        super(Type.GAME_INVALID, gameID);
    }

    @Override
    public @Nonnull String toString() {
        return "PacketOutGameInvalid(gameID=" + gameID + ")";
    }
}
