package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketIn {

    public final Type type;
    private final String data;
    private int index;

    public PacketIn(String data) {
        Checks.ensureNonNull(data, "data");
        Checks.ensure(data.length() > 0, "data cannot be empty, expected type character");

        this.data = data;
        this.index = 0;

        char typeChar = nextChar();
        this.type = Type.fromId(typeChar - '0');

        Checks.ensure(type != null, "Unknown incoming packet type " + typeChar + " (ucs " + ((int) typeChar) + ")");
    }

    public PacketIn copy() {
        return new PacketIn(data);
    }

    public void expectType(Type type) {
        Checks.ensureState(this.type == type, "expected " + type + " packet");
    }

    public char nextChar() {
        Checks.ensureState(index < data.length(), "there are no characters left in the packet");

        return data.charAt(index++);
    }

    public String nextString(int length) {
        Checks.ensure(length >= 0, "length must be >= 0");
        Checks.ensureState(index + length <= data.length(), "there are not " + length + " characters left in the packet");

        int from = index;
        index += length;

        return data.substring(from, index);
    }

    public int nextInt(int length) {
        String string = nextString(length);

        return Checks.detailThrown(() -> Integer.parseInt(string), "expected integer but found " + string);
    }

    public UUID nextUUID() {
        String string = nextString(36);

        return Checks.detailThrown(UUID::fromString, string, "expected uuid but found " + string);
    }

    public void expectEmpty() {
        Checks.ensureState(index == data.length(), "expected packet " + type + " to be fully read");
    }

    @Override
    public String toString() {
        return type + " " + data;
    }

    public enum Type {

        OPEN("open"),
        REOPEN("reopen"),
        ROLL("roll"),
        MOVE("move");

        private final String name;

        private Type(String name) {
            Checks.ensureNonNull(name, "name");

            this.name = name;
        }

        public int getId() {
            return ordinal();
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "incoming " + getId() + ":" + getName();
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
