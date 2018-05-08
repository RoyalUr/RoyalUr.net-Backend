package net.sothatsit.royalurserver.network;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;

public interface PacketWritable {

    public void writeTo(PacketOut packet);

}
