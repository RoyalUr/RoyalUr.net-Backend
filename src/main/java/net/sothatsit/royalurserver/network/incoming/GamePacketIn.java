package net.sothatsit.royalurserver.network.incoming;

import net.sothatsit.royalurserver.game.GameID;

/**
 * An incoming packet relating to a specific game.
 */
public abstract class GamePacketIn extends PacketIn {

    public GameID gameID;

    public GamePacketIn(Type type) {
        super(type);
    }

    @Override
    public void readContents(PacketReader reader) {
        super.readContents(reader);
        this.gameID = GameID.read(reader);
    }
}
