package net.royalur.backend.network.outgoing;

import net.royalur.backend.game.GameID;
import net.royalur.backend.util.Checks;

import javax.annotation.Nonnull;

/**
 * A packet sent to give an arbitrary message to the client.
 *
 * @author Paddy Lamont
 */
public class PacketOutGameMessage extends GamePacketOut {

    private final @Nonnull String text;
    private final @Nonnull String subText;

    public PacketOutGameMessage(@Nonnull GameID gameID, @Nonnull String text, @Nonnull String subText) {
        super(Type.GAME_MESSAGE, gameID);
        Checks.ensureNonNull(text, "text");
        Checks.ensureNonNull(subText, subText);
        this.text = text;
        this.subText = subText;
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);
        writer.pushVarString(text);
        writer.pushVarString(subText);
    }

    @Override
    public String toString() {
        return "PacketOutGameMessage(text=\"" + text + "\", subText=\"" + subText + "\")";
    }
}
