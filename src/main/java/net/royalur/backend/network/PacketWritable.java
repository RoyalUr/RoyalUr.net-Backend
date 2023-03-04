package net.royalur.backend.network;

import net.royalur.backend.network.outgoing.PacketWriter;

/**
 * An interface that indicates sub-classes can be written to a packet.
 *
 * @author Paddy Lamont
 */
public interface PacketWritable {

    public void writeTo(PacketWriter packet);
}
