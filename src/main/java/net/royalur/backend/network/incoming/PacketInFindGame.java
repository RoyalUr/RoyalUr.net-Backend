package net.royalur.backend.network.incoming;

/**
 * A packet sent by the client to request they be entered in for match making.
 *
 * @author Paddy Lamont
 */
public class PacketInFindGame extends PacketIn {

    public PacketInFindGame() {
        super(Type.FIND_GAME);
    }

    @Override
    public String toString() {
        return "PacketInFindGame()";
    }
}
