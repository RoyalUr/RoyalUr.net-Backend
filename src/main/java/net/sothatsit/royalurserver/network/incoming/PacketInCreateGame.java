package net.sothatsit.royalurserver.network.incoming;

/**
 * A packet sent to request that a match be created that they can get a friend to join.
 *
 * @author Paddy Lamont
 */
public class PacketInCreateGame extends PacketIn {

    public PacketInCreateGame() {
        super(Type.CREATE_GAME);
    }

    @Override
    public String toString() {
        return "PacketInCreateGame()";
    }
}
