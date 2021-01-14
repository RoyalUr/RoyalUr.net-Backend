package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.network.incoming.PacketReader;
import net.sothatsit.royalurserver.network.outgoing.PacketWriter;

import java.util.Random;

/**
 * Represents the unique ID given to a Game.
 *
 * @author Paddy Lamont
 */
public final class GameID implements PacketWritable {

    public static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int ID_LENGTH = 6;

    private final int numericID;

    /**
     * Create a new GameID backed by the given numerical ID {@param numericID}.
     */
    public GameID(int numericID) {
        this.numericID = numericID;
    }

    @Override
    public String toString() {
        char[] chars = new char[ID_LENGTH];

        int num = this.numericID;
        for(int index = 0; index < ID_LENGTH; ++index) {
            int charOrdinal = Math.toIntExact(num % ID_CHARS.length());
            num /= ID_CHARS.length();
            chars[index] = ID_CHARS.charAt(charOrdinal);
        }
        return new String(chars);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GameID && ((GameID) obj).numericID == numericID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numericID);
    }

    @Override
    public void writeTo(PacketWriter packet) {
        packet.pushRaw(toString());
    }

    /**
     * @return The next GameID read from {@param packet}.
     */
    public static GameID read(PacketReader packet) {
        return fromString(packet.nextString(ID_LENGTH));
    }

    /**
     * @return The GameID represented by {@param string}.
     */
    public static GameID fromString(String string) {
        if(string.length() != ID_LENGTH)
            throw new IllegalArgumentException("Game ID \"" + string + "\" is not of the expected length");

        int numericId = 0;

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
     * @return A random new GameID.
     */
    public static GameID random(Random random) {
        int gameID = random.nextInt();

        // We only want positive game IDs.
        if (gameID < 0) {
            gameID = -(gameID + 1);
        }

        return new GameID(gameID);
    }
}
