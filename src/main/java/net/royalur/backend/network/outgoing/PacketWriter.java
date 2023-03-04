package net.royalur.backend.network.outgoing;

import net.royalur.backend.network.PacketWritable;
import net.royalur.backend.util.Checks;

import java.util.UUID;

/**
 * A helper that allows the writing of packets.
 *
 * @author Paddy Lamont
 */
public class PacketWriter {

    public final PacketOut.Type type;
    private final StringBuilder dataBuilder;

    /** Create a packet of type {@param type}. **/
    public PacketWriter(PacketOut.Type type) {
        Checks.ensureNonNull(type, "type");

        this.type = type;
        this.dataBuilder = new StringBuilder();
        dataBuilder.append((char) (type.getId() + '0'));
    }

    /** @return A String encoded version of this packet. **/
    @Override
    public String toString() {
        return dataBuilder.toString();
    }

    /** Write the boolean value {@param value} to the packet. **/
    public PacketWriter pushBool(boolean value) {
        dataBuilder.append(value ? "t" : "f");
        return this;
    }

    /** Write the value {@param value} to the packet. **/
    public PacketWriter pushValue(PacketWritable value) {
        Checks.ensureNonNull(value, "value");
        value.writeTo(this);
        return this;
    }

    /**
     * Write the single digit {@param digit} to the packet.
     * @param digit must be in the range 0 -> 9 inclusive.
     */
    public PacketWriter pushDigit(int digit) {
        Checks.ensureSingleDigit(digit, "digit");
        dataBuilder.append(digit);
        return this;
    }

    /**
     * Write the variable length String {@param value} to the packet, where
     * the length of the string can fall anywhere between 0 and 99 inclusive.
     */
    public PacketWriter pushVarString(String value) {
        return pushVarString(value, 2);
    }

    /**
     * Write the variable length String {@param value} to the packet, where the
     * length of the string can have a maximum of {@param lengthDigits} digits.
     */
    public PacketWriter pushVarString(String value, int lengthDigits) {
        Checks.ensureNonNull(value, "value");
        pushInt(value.length(), lengthDigits);
        dataBuilder.append(value);
        return this;
    }

    /**
     * Write the integer {@param value} to the packet, encoding it using {@param digits} digits.
     */
    public PacketWriter pushInt(int value, int digits) {
        Checks.ensure(value >= 0, "value must be >= 0");
        Checks.ensure(digits > 0, "digits must be positive");

        String string = Integer.toString(value);
        Checks.ensure(string.length() <= digits, "value has too many digits");

        for (int index = string.length(); index < digits; ++index) {
            dataBuilder.append('0');
        }
        dataBuilder.append(string);
        return this;
    }

    /** Write the UUID {@param uuid} to the packet. **/
    public PacketWriter pushUUID(UUID uuid) {
        Checks.ensureNonNull(uuid, "uuid");

        String stringUUID = uuid.toString();
        Checks.ensure(stringUUID.length() == 36, "A UUID should be of length 36 characters");

        dataBuilder.append(stringUUID);
        return this;
    }

    /** Write the String {@param value} to the packet. **/
    public PacketWriter pushRaw(String value) {
        Checks.ensureNonNull(value, "value");
        dataBuilder.append(value);
        return this;
    }
}
