package net.sothatsit.royalurserver.network.incoming;

import java.util.UUID;

/**
 * A packet sent by the client to indicate they are re-opening their connection and already have a unique ID.
 *
 * @author Paddy Lamont
 */
public class PacketInReOpen extends PacketIn {

    public int protocolVersion;
    public UUID previousID;

    public PacketInReOpen() {
        super(Type.REOPEN);
    }

    @Override
    public void readContents(PacketReader reader) {
        this.protocolVersion = reader.nextInt(4);
        this.previousID = reader.nextUUID();
    }

    @Override
    public String toString() {
        return "PacketInReOpen(previousID=" + previousID + ")";
    }
}
