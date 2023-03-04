package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.ExceptionDetailer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A packet that has been received from the client.
 *
 * @author Paddy Lamont
 */
public abstract class PacketIn {

    public final Type type;

    public PacketIn(Type type) {
        this.type = type;
    }

    /** Populates the packet with the contents from {@param reader}. **/
    public void read(PacketReader reader) {
        try {
            Checks.ensureNonNull(reader, "reader");
            reader.assertType(type);
            readContents(reader);
            reader.assertEmpty();
        } catch (RuntimeException exception) {
            throw ExceptionDetailer.detail(exception, "exception while reading " + type.getName() + " packet");
        }
    }

    /** Read the contents from the packet. **/
    protected void readContents(PacketReader reader) {
        // Some packets don't contain anything.
    }

    @Override
    public abstract String toString();

    /** The type of incoming packets. **/
    public enum Type {
        OPEN("open", PacketInOpen::new),
        REOPEN("reopen", PacketInReOpen::new),
        JOIN_GAME("join_game", PacketInJoinGame::new),
        FIND_GAME("find_game", PacketInFindGame::new),
        CREATE_GAME("create_game", PacketInCreateGame::new),
        ROLL("roll", PacketInGameRoll::new),
        MOVE("move", PacketInGameMove::new);

        private final String name;
        private final Supplier<PacketIn> packetConstructor;

        Type(String name, Supplier<PacketIn> packetConstructor) {
            Checks.ensureNonNull(name, "name");
            Checks.ensureNonNull(packetConstructor, "packetConstructor");
            this.name = name;
            this.packetConstructor = packetConstructor;
        }

        /** @return A unique ID used to represent each packet type. **/
        public int getId() {
            return ordinal();
        }

        /** @return A human-readable name for this packet type. **/
        public String getName() {
            return name;
        }

        /** @return A packet object that holds the data of a packet with this type. **/
        public PacketIn newPacket() {
            return packetConstructor.get();
        }

        @Override
        public String toString() {
            return "PACKET_IN_" + name();
        }

        private static final Map<Integer, Type> byId = new HashMap<>();
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
