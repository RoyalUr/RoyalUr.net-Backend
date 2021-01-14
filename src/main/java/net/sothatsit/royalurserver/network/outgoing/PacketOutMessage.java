package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to give an arbitrary message to the client.
 *
 * @author Paddy Lamont
 */
public class PacketOutMessage extends PacketOut {

    private final String text;
    private final String subText;

    public PacketOutMessage(String text, String subText) {
        super(Type.MESSAGE);
        Checks.ensureNonNull(text, "text");
        Checks.ensureNonNull(subText, subText);
        this.text = text;
        this.subText = subText;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushVarString(text);
        writer.pushVarString(subText);
    }

    @Override
    public String toString() {
        return "PacketOutMessage(text=\"" + text + "\", subText=\"" + subText + "\")";
    }
}
