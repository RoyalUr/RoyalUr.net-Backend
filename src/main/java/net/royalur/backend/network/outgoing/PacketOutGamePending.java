package net.royalur.backend.network.outgoing;

import net.royalur.backend.game.GameID;

import javax.annotation.Nonnull;

/**
 * A packet sent to the client telling them that a game is pending.
 *
 * @author Paddy Lamont
 */
public class PacketOutGamePending extends GamePacketOut {

    public PacketOutGamePending(@Nonnull GameID gameID) {
        super(Type.GAME_PENDING, gameID);
    }

    @Override
    public @Nonnull String toString() {
        return "PacketOutGamePending(gameID=" + gameID + ")";
    }
}
