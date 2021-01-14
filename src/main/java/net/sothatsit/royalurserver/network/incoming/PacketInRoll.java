package net.sothatsit.royalurserver.network.incoming;

/**
 * A packet sent to request the dice be rolled.
 *
 * @author Paddy Lamont
 */
public class PacketInRoll extends PacketIn {

    public PacketInRoll() {
        super(Type.ROLL);
    }

    @Override
    public String toString() {
        return "PacketInRoll()";
    }
}
