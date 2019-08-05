package net.sothatsit.royalurserver.network.outgoing;

/**
 * A packet sent to the client when they attempt to join an unknown game.
 *
 * @author Paddy Lamont
 */
public class PacketOutInvalidGame {

    public static PacketOut create() {
        return new PacketOut(PacketOut.Type.INVALID_GAME);
    }
}
