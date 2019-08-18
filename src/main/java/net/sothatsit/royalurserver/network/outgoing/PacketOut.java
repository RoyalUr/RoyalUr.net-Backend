package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

import java.util.UUID;

/**
 * Allows the construction of an outgoing packet.
 *
 * @author Paddy Lamont
 */
public class PacketOut {

    public final Type type;
    private final StringBuilder dataBuilder;

    /**
     * Create a packet of type {@param type}.
     */
    public PacketOut(Type type) {
        Checks.ensureNonNull(type, "type");

        this.type = type;
        this.dataBuilder = new StringBuilder();

        dataBuilder.append((char) (type.getId() + '0'));
    }

    /**
     * @return A String encoded version of this packet.
     */
    public String encode() {
        return dataBuilder.toString();
    }

    /**
     * Write the boolean value {@param value} to the packet.
     */
    public void write(boolean value) {
        dataBuilder.append(value ? "t" : "f");
    }

    /**
     * Write the value {@param value} to the packet.
     */
    public void write(PacketWritable value) {
        Checks.ensureNonNull(value, "value");

        value.writeTo(this);
    }

    /**
     * Write the single digit {@param digit} to the packet.
     *
     * @param digit must be in the range 0 -> 9 inclusive.
     */
    public void writeDigit(int digit) {
        Checks.ensureSingleDigit(digit, "digit");

        dataBuilder.append(digit);
    }

    /**
     * Write the variable length String {@param value} to the packet, where
     * the length of the string can fall anywhere between 0 and 99 inclusive.
     */
    public void writeVarString(String value) {
        writeVarString(value, 2);
    }

    /**
     * Write the variable length String {@param value} to the packet, where the
     * length of the string can have a maximum of {@param lengthDigits} digits.
     */
    public void writeVarString(String value, int lengthDigits) {
        Checks.ensureNonNull(value, "value");

        writeInt(value.length(), lengthDigits);
        dataBuilder.append(value);
    }

    /**
     * Write the integer {@param value} to the packet, encoding it using {@param digits} digits.
     */
    public void writeInt(int value, int digits) {
        Checks.ensure(value >= 0, "value must be >= 0");
        Checks.ensure(digits > 0, "digits must be positive");

        String string = Integer.toString(value);

        Checks.ensure(string.length() <= digits, "value has too many characters");

        for(int index = string.length(); index < digits; ++index) {
            dataBuilder.append('0');
        }

        dataBuilder.append(string);
    }

    /**
     * Write the UUID {@param uuid} to the packet.
     */
    public void writeUUID(UUID uuid) {
        Checks.ensureNonNull(uuid, "uuid");

        String stringUUID = uuid.toString();
        Checks.ensure(stringUUID.length() == 36, "A UUID should be of length 36 characters");

        dataBuilder.append(stringUUID);
    }

    /**
     * Write the String {@param value} to the packet.
     */
    public void writeString(String value) {
        Checks.ensureNonNull(value, "value");

        dataBuilder.append(value);
    }

    /**
     * The type of an outgoing packet.
     */
    public enum Type {

        ERROR("error"),
        SET_ID("set_id"),
        INVALID_GAME("invalid_game"),
        GAME("game"),
        MESSAGE("message"),
        STATE("state"),
        MOVE("move");

        private final String name;

        private Type(String name) {
            Checks.ensureNonNull(name, "name");

            this.name = name;
        }

        /**
         * @return A unique ID used to represent this packet type.
         */
        public int getId() {
            return ordinal();
        }

        /**
         * @return A human-readable name for this packet type.
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "PacketOut.Type(" + getId() + ":" + getName() + ")";
        }
    }
}
