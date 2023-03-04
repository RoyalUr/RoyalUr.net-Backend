package net.royalur.backend.network.outgoing;

import net.royalur.backend.game.GameID;
import net.royalur.backend.util.Checks;

import javax.annotation.Nonnull;

/**
 * An outgoing packet related to a specific game.
 *
 * @author Paddy Lamont
 */
public abstract class GamePacketOut extends PacketOut {

    protected final @Nonnull GameID gameID;

    public GamePacketOut(@Nonnull Type type, @Nonnull GameID gameID) {
        super(type);
        Checks.ensureNonNull(gameID, "gameID");
        this.gameID = gameID;
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        writer.pushValue(gameID);
    }
}
