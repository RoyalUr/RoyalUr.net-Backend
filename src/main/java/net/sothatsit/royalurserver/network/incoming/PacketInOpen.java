package net.sothatsit.royalurserver.network.incoming;

/**
 * A packet sent after opening a connection to indicate the client does not already have a unique ID.
 *
 * @author Paddy Lamont
 */
public class PacketInOpen extends PacketIn {

    public int protocolVersion;
    public String name;

    public PacketInOpen() {
        super(Type.OPEN);
    }

    @Override
    protected void readContents(PacketReader reader) {
        this.protocolVersion = reader.nextInt(4);
        this.name = reader.nextVarString(2);
    }

    @Override
    public String toString() {
        return "PacketInReOpen("
                + "protocolVersion=" + protocolVersion
                + ", name=\"" + name + "\"" + ")";
    }
}
