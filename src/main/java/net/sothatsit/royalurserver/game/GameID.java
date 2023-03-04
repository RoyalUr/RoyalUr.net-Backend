package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.network.incoming.PacketReader;
import net.sothatsit.royalurserver.network.outgoing.PacketWriter;
import net.sothatsit.royalurserver.util.MathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Represents a unique ID given to a game.
 *
 * @author Paddy Lamont
 */
public final class GameID implements PacketWritable {

    /**
     * The allowed characters within a game ID.
     */
    public static final String ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * The length of each ID.
     */
    public static final int ID_LENGTH = 8;

    /**
     * The maximum possible ID that could be represented as text.
     */
    public static final long ID_MAX = MathHelper.power(ID_CHARS.length(), ID_LENGTH);

    /**
     * The numeric ID that this ID corresponds to.
     */
    private final long numericID;

    /**
     * Create a new GameID backed by the given numerical ID {@param numericID}.
     */
    public GameID(long numericID) {
        this.numericID = numericID;
    }

    @Override
    public String toString() {
        char[] chars = new char[ID_LENGTH];

        long num = this.numericID;

        for(int index = 0; index < ID_LENGTH; ++index) {
            int charOrdinal = Math.toIntExact(num % ID_CHARS.length());
            num /= ID_CHARS.length();
            chars[index] = ID_CHARS.charAt(charOrdinal);
        }
        return new String(chars);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof GameID && ((GameID) obj).numericID == numericID;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(numericID);
    }

    @Override
    public void writeTo(@Nonnull PacketWriter packet) {
        packet.pushRaw(toString());
    }

    /**
     * Read a game ID from a packet.
     * @param packet The packet to read from.
     * @return The next GameID read from {@param packet}.
     */
    public static @Nonnull GameID read(@Nonnull PacketReader packet) {
        return fromString(packet.nextString(ID_LENGTH));
    }

    /**
     * Convert a game ID from text to a numerical ID.
     * @param string The text ID to convert to a numerical ID.
     * @return The GameID represented by {@param string}.
     */
    public static @Nonnull GameID fromString(@Nonnull String string) {
        if(string.length() != ID_LENGTH)
            throw new IllegalArgumentException("Game ID \"" + string + "\" is not of the expected length");

        long numericId = 0;

        for(int index = ID_LENGTH - 1; index >= 0; --index) {
            char ch = string.charAt(index);
            int ordinal = ID_CHARS.indexOf(ch);

            if(ordinal == -1)
                throw new IllegalArgumentException("Game ID \"" + string + "\" contains invalid characters");

            numericId *= ID_CHARS.length();
            numericId += ordinal;
        }
        return new GameID(numericId);
    }

    /**
     * Generate a new random game ID.
     * @param random The random number generator to use.
     * @return A random new GameID.
     */
    public static @Nonnull GameID random(@Nonnull Random random) {
        long gameID = random.nextLong(0, ID_MAX);
        if (gameID > ID_MAX)
            throw new RuntimeException("Unexpectedly generated game ID larger than ID_MAX");

        return new GameID(gameID);
    }
}
