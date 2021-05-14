package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;
import java.util.UUID;

/**
 * A helper to read values from a packet.
 *
 * @author Paddy Lamont
 */
public class PacketReader {

    public final PacketIn.Type type;
    private final String data;
    private int index;

    /** Construct a packet to read in the packet data {@param data}. **/
    public PacketReader(String data) {
        Checks.ensureNonNull(data, "data");
        Checks.ensure(data.length() > 0, "data cannot be empty, expected type character");

        this.data = data;
        this.index = 0;

        char typeChar = nextChar();
        this.type = PacketIn.Type.fromId(typeChar - '0');

        Checks.ensure(type != null, "Unknown incoming packet type " + typeChar + " (ucs " + ((int) typeChar) + ")");
    }

    /** @return A copy of this packet, with its position reset to the beginning of the packet. **/
    public PacketReader copy() {
        return new PacketReader(data);
    }

    /** @throws IllegalStateException if the type of this packet is not {@param type}. **/
    public void assertType(PacketIn.Type type) {
        Checks.ensureState(this.type == type, "expected " + type + " packet");
    }

    /** @throws IllegalStateException if there is any data left unread in this packet. **/
    public void assertEmpty() {
        Checks.ensureState(index == data.length(), "expected packet " + type + " to be fully read");
    }

    /** @return The next character in this packet. **/
    public char nextChar() {
        Checks.ensureState(index < data.length(), "there are no characters left in this packet");
        return data.charAt(index++);
    }

    /** @return The next String of length {@param length} in this packet. **/
    public String nextString(int length) {
        Checks.ensure(length >= 0, "length must be >= 0");
        Checks.ensureState(
            index + length <= data.length(),
            "there are not " + length + " characters left in this packet"
        );

        int from = index;
        index += length;
        return data.substring(from, index);
    }

    /** @return The next integer of length {@param digits} digits in this packet. **/
    public int nextInt(int digits) {
        String string = nextString(digits);
        return Checks.detailThrown(() -> Integer.parseInt(string), "expected integer but found " + string);
    }

    /** @return The next String with length encoded in the next {@param lengthDigits} digits. **/
    public String nextVarString(int lengthDigits) {
        int length = nextInt(lengthDigits);
        return nextString(length);
    }

    /** @return The next UUID in the packet. **/
    public UUID nextUUID() {
        String string = nextString(36);
        return Checks.detailThrown(UUID::fromString, string, "expected uuid but found " + string);
    }

    @Override
    public String toString() {
        String dataInfo = (data.length() > 1 ? ", " + data.substring(1) : "");
        return "PacketIn(" + type.getId() + ":" + type.getName() + dataInfo + ")";
    }
}
