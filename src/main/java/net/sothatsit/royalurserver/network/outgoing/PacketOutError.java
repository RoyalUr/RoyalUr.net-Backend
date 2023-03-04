package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nonnull;

/**
 * A packet sent to indicate an error occurred between them and the game.
 *
 * @author Paddy Lamont
 */
public class PacketOutError extends PacketOut {

    private final @Nonnull String error;

    public PacketOutError(@Nonnull String error) {
        super(Type.ERROR);
        Checks.ensureNonNull(error, "error");
        this.error = error;
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);
        writer.pushRaw(error);
    }

    @Override
    public String toString() {
        return "PacketOutError(error=\"" + error + "\")";
    }
}
