package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

/**
 * Allows the construction of outgoing packets.
 *
 * @author Paddy Lamont
 */
public abstract class PacketOut {

    public final Type type;

    public PacketOut(Type type) {
        Checks.ensureNonNull(type, "type");
        this.type = type;
    }

    /** @return this packet written out to a String. **/
    public String write() {
        PacketWriter writer = new PacketWriter(type);
        writeContents(writer);
        return writer.toString();
    }

    /** Write the contents of the packet. **/
    protected void writeContents(PacketWriter writer) {
        // Some packets contain no contents.
    }

    @Override
    public abstract String toString();

    /** The type of outgoing packets. **/
    public enum Type {
        ERROR("error"),
        SET_ID("set_id"),
        INVALID_GAME("invalid_game"),
        GAME_PENDING("game_pending"),
        GAME("game"),
        GAME_END("game_end"),
        MESSAGE("message"),
        PLAYER_STATUS("player_status"),
        STATE("state"),
        MOVE("move");

        private final String name;

        private Type(String name) {
            Checks.ensureNonNull(name, "name");

            this.name = name;
        }

        /** @return A unique ID used to represent this packet type. **/
        public int getId() {
            return ordinal();
        }

        /** @return A human-readable name for this packet type. **/
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "PACKET_OUT_" + name();
        }
    }
}
