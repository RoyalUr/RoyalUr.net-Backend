package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.util.Checks;

import java.security.SecureRandom;

public final class GameID implements PacketWritable {

    private static final SecureRandom random = new SecureRandom();

    public static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int ID_LENGTH = 5;
    public static final int MAX_ID = (int) Math.pow(ID_CHARS.length(), ID_LENGTH);

    private final int numericID;

    public GameID(int numericID) {
        Checks.ensure(numericID >= 0 && numericID < MAX_ID, "Numeric game ID " + numericID + " is out of the valid range");

        this.numericID = numericID;
    }

    @Override
    public String toString() {
        char[] chars = new char[ID_LENGTH];

        int num = this.numericID;

        for(int index = 0; index < ID_LENGTH; ++index) {
            int charOrdinal = num % ID_CHARS.length();
            num /= ID_CHARS.length();

            chars[index] = ID_CHARS.charAt(charOrdinal);
        }

        return new String(chars);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GameID
                && ((GameID) obj).numericID == numericID;

    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numericID);
    }

    @Override
    public void writeTo(PacketOut packet) {
        packet.write(toString());
    }

    public static GameID read(PacketIn packet) {
        return fromString(packet.nextString(ID_LENGTH));
    }

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

    public static GameID random() {
        return new GameID(random.nextInt(MAX_ID));
    }
}
