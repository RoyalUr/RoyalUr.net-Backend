package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An incoming packet from the client.
 *
 * @author Paddy Lamont
 */
public class PacketIn {

    public final Type type;
    private final String data;
    private int index;

    /**
     * Construct a packet to read in the packet data {@param data}.
     */
    public PacketIn(String data) {
        Checks.ensureNonNull(data, "data");
        Checks.ensure(data.length() > 0, "data cannot be empty, expected type character");

        this.data = data;
        this.index = 0;

        char typeChar = nextChar();
        this.type = Type.fromId(typeChar - '0');

        Checks.ensure(type != null, "Unknown incoming packet type " + typeChar + " (ucs " + ((int) typeChar) + ")");
    }

    /**
     * @return A copy of this packet, with its position reset to the beginning of the packet.
     */
    public PacketIn copy() {
        return new PacketIn(data);
    }

    /**
     * @throws IllegalStateException if the type of this packet is not {@param type}.
     */
    public void expectType(Type type) {
        Checks.ensureState(this.type == type, "expected " + type + " packet");
    }

    /**
     * @return The next character in this packet.
     */
    public char nextChar() {
        Checks.ensureState(index < data.length(), "there are no characters left in this packet");

        return data.charAt(index++);
    }

    /**
     * @return The next String of length {@param length} in this packet.
     */
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

    /**
     * @return The next integer of length {@param digits} digits in this packet.
     */
    public int nextInt(int digits) {
        String string = nextString(digits);

        return Checks.detailThrown(() -> Integer.parseInt(string), "expected integer but found " + string);
    }

    /**
     * @return The next UUID in the packet.
     */
    public UUID nextUUID() {
        String string = nextString(36);

        return Checks.detailThrown(UUID::fromString, string, "expected uuid but found " + string);
    }

    /**
     * @throws IllegalStateException if there is any data left unread in this packet.
     */
    public void expectEmpty() {
        Checks.ensureState(index == data.length(), "expected packet " + type + " to be fully read");
    }

    @Override
    public String toString() {
        String dataInfo = (data.length() > 1 ? ", " + data.substring(1) : "");
        return "PacketIn(" + type.getId() + ":" + type.getName() + dataInfo + ")";
    }

    /**
     * The type of an incoming packet.
     */
    public enum Type {

        OPEN("open"),
        REOPEN("reopen"),
        JOIN_GAME("join_game"),
        FIND_GAME("find_game"),
        CREATE_GAME("create_game"),
        ROLL("roll"),
        MOVE("move");

        private final String name;

        private Type(String name) {
            Checks.ensureNonNull(name, "name");

            this.name = name;
        }

        /**
         * @return A unique ID used to represent each packet type.
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
            return "PacketIn.Type(" + getId() + ":" + getName() + ")";
        }

        private static Map<Integer, Type> byId = new HashMap<>();
        static {
            for(Type type : values()) {
                byId.put(type.ordinal(), type);
            }
        }

        public static Type fromId(int id) {
            return byId.get(id);
        }

    }
}
