package net.royalur.backend.network.outgoing;

import net.royalur.model.Player;
import net.royalur.backend.game.GameID;
import net.royalur.backend.util.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A packet sent to the client containing the details about a game they've been added to.
 *
 * @author Paddy Lamont
 */
public class PacketOutGameMetadata extends GamePacketOut {

    private final @Nullable Player yourPlayer;
    private final @Nonnull String lightName;
    private final @Nonnull String darkName;
    private final boolean lightConnected;
    private final boolean darkConnected;

    public PacketOutGameMetadata(
            @Nonnull GameID gameID,
            @Nullable Player yourPlayer,
            @Nonnull String lightName,
            @Nonnull String darkName,
            boolean lightConnected,
            boolean darkConnected
    ) {
        super(Type.GAME_METADATA, gameID);
        Checks.ensureNonNull(yourPlayer, "yourPlayer");
        Checks.ensureNonNull(lightName, "lightName");
        Checks.ensureNonNull(darkName, "darkName");
        this.yourPlayer = yourPlayer;
        this.lightName = lightName;
        this.darkName = darkName;
        this.lightConnected = lightConnected;
        this.darkConnected = darkConnected;
    }

    public static void writePlayer(@Nonnull PacketWriter writer, @Nullable Player player, int emptyID) {
        // IDs used for backwards compatibility.
        int playerType;
        if (player != null) {
            switch (player) {
                case LIGHT -> playerType = 2;
                case DARK -> playerType = 1;
                default -> throw new IllegalStateException("Unknown player type " + player);
            }
        } else {
            // Spectator or empty tile.
            playerType = emptyID;
        }
        writer.pushDigit(playerType);
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);
        writePlayer(writer, yourPlayer, 3);
        writer.pushVarString(lightName)
                .pushVarString(darkName)
                .pushBool(lightConnected)
                .pushBool(darkConnected);
    }

    @Override
    public @Nonnull String toString() {
        return "PacketOutGameMetadata("
                + "gameID=" + gameID + ", "
                + "yourPlayer=" + yourPlayer + ", "
                + "lightName=\"" + lightName + "\", "
                + "darkName=\"" + darkName + "\", "
                + "lightConnected=" + lightConnected + ", "
                + "darkConnected=" + darkConnected + ")";
    }
}
