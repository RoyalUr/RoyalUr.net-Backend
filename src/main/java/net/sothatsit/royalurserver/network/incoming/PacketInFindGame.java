package net.sothatsit.royalurserver.network.incoming;

/**
 * A packet sent by the client to request they be entered in for match making.
 *
 * @author Paddy Lamont
 */
public class PacketInFindGame extends PacketIn {

    public String name;

    public PacketInFindGame() {
        super(Type.FIND_GAME);
    }

    @Override
    protected void readContents(PacketReader reader) {
        this.name = reader.nextVarString();
    }

    @Override
    public String toString() {
        return "PacketInFindGame(name=\"" + name + "\")";
    }
}
