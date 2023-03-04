package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nonnull;

/**
 * Allows the construction of outgoing packets.
 *
 * @author Paddy Lamont
 */
public abstract class PacketOut {

    public final Type type;

    public PacketOut(@Nonnull Type type) {
        Checks.ensureNonNull(type, "type");
        this.type = type;
    }

    /** @return this packet written out to a String. **/
    public @Nonnull String write() {
        PacketWriter writer = new PacketWriter(type);
        writeContents(writer);
        return writer.toString();
    }

    /** Write the contents of the packet. **/
    protected void writeContents(@Nonnull PacketWriter writer) {
        // Some packets contain no contents.
    }

    @Override
    public abstract String toString();

    /** The type of outgoing packets. **/
    public enum Type {
        ERROR("error"),
        SET_ID("set_id"),
        GAME_INVALID("invalid_game"),
        GAME_PENDING("game_pending"),
        GAME_METADATA("game"),
        GAME_END("game_end"),
        GAME_MESSAGE("message"),
        GAME_PLAYER_STATUS("player_status"),
        GAME_STATE("state"),
        GAME_MOVE("move");

        private final String name;

        Type(@Nonnull String name) {
            Checks.ensureNonNull(name, "name");
            this.name = name;
        }

        /** @return A unique ID used to represent this packet type. **/
        public int getId() {
            return ordinal();
        }

        /** @return A human-readable name for this packet type. **/
        public @Nonnull String getName() {
            return name;
        }

        @Override
        public @Nonnull String toString() {
            return "PACKET_OUT_" + name();
        }
    }
}
