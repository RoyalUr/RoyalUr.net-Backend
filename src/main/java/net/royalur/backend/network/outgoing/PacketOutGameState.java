package net.royalur.backend.network.outgoing;

import net.royalur.Game;
import net.royalur.model.*;
import net.royalur.model.state.WaitingForMoveGameState;
import net.royalur.rules.simple.SimplePiece;
import net.royalur.backend.game.DiceValue;
import net.royalur.backend.game.GameID;
import net.royalur.backend.game.RoyalUrNetDiceRoll;

import javax.annotation.Nonnull;

/**
 * A packet sent to the client indicating the state of the game.
 *
 * @author Paddy Lamont
 */
public class PacketOutGameState extends GamePacketOut {

    private final @Nonnull Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> game;

    public PacketOutGameState(
            @Nonnull GameID gameID,
            @Nonnull Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> game
    ) {
        super(Type.GAME_STATE, gameID);
        this.game = game.copy();
    }

    private static void writePlayerState(@Nonnull PacketWriter writer, @Nonnull PlayerState state) {
        writer.pushDigit(state.pieceCount);
        writer.pushDigit(state.score);
    }

    private static void writeBoard(@Nonnull PacketWriter writer, @Nonnull Board<SimplePiece> board) {
        int width = board.shape.width;
        int height = board.shape.height;
        for (int iy = 0; iy < height; ++iy) {
            for (int ix = 0; ix < width; ++ix) {

                Player player = null;
                if (board.contains(ix, iy)) {
                    Piece piece = board.get(ix, iy);
                    if (piece != null) {
                        player = piece.owner;
                    }
                }

                PacketOutGameMetadata.writePlayer(writer, player, 0);
            }
        }
    }

    @Override
    protected void writeContents(@Nonnull PacketWriter writer) {
        super.writeContents(writer);

        writePlayerState(writer, game.getLightPlayer());
        writePlayerState(writer, game.getDarkPlayer());
        writeBoard(writer, game.getBoard());

        writer.pushBool(game.isFinished());
        PacketOutGameMetadata.writePlayer(writer, game.getTurnPlayer().player, 3);

        boolean hasDiceRoll = (game.getCurrentState() instanceof WaitingForMoveGameState);
        writer.pushBool(hasDiceRoll);
        if (hasDiceRoll) {
            // Write the dice roll to the packet.
            WaitingForMoveGameState<?, ?, RoyalUrNetDiceRoll> state = (
                    (WaitingForMoveGameState<?, ?, RoyalUrNetDiceRoll>) game.getCurrentState()
            );
            RoyalUrNetDiceRoll roll = state.roll;
            for (DiceValue value : roll.values) {
                writer.pushDigit(value.getId());
            }

            // Write whether there are moves available.
            boolean hasMoves = (game.findAvailableMoves().size() > 0);
            writer.pushBool(hasMoves);
        }
    }

    @Override
    public String toString() {
        return "PacketOutGameState(gameID=" + gameID + ", game=" + game + ")";
    }
}
