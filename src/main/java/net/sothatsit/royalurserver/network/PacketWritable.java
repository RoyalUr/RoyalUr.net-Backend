package net.sothatsit.royalurserver.network;

import net.sothatsit.royalurserver.network.outgoing.PacketWriter;

/**
 * An interface that indicates sub-classes can be written to a packet.
 *
 * @author Paddy Lamont
 */
public interface PacketWritable {

    public void writeTo(PacketWriter packet);
}
