package net.sothatsit.royalurserver.network.incoming;

/**
 * A packet sent to request the dice be rolled.
 *
 * @author Paddy Lamont
 */
public class PacketInGameRoll extends GamePacketIn {

    public PacketInGameRoll() {
        super(Type.ROLL);
    }

    @Override
    public String toString() {
        return "PacketInRoll(gameID=" + gameID + ")";
    }
}
