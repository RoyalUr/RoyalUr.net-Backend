package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.DiceRoll;
import net.sothatsit.royalurserver.game.Player;
import net.sothatsit.royalurserver.game.Board;
import net.sothatsit.royalurserver.game.PlayerState;
import net.sothatsit.royalurserver.util.Checks;

/**
 * A packet sent to the client indicating the state of the game.
 *
 * @author Paddy Lamont
 */
public class PacketOutState extends PacketOut {

    private final PlayerState lightPlayer;
    private final PlayerState darkPlayer;
    private final Board board;
    private final boolean isGameWon;
    private final Player currentPlayer;
    private final DiceRoll diceRoll;
    private final boolean hasMoves;

    public PacketOutState(
            PlayerState lightPlayer, PlayerState darkPlayer,
            Board board, boolean isGameWon, Player currentPlayer) {

        this(lightPlayer, darkPlayer, board, isGameWon, currentPlayer, null, false);
    }

    public PacketOutState(
            PlayerState lightPlayer, PlayerState darkPlayer,
            Board board, boolean isGameWon, Player currentPlayer,
            DiceRoll diceRoll, boolean hasMoves) {

        super(Type.STATE);
        Checks.ensureNonNull(lightPlayer, "lightPlayer");
        Checks.ensureNonNull(darkPlayer, "darkPlayer");
        Checks.ensureNonNull(board, "board");
        Checks.ensureNonNull(currentPlayer, "currentPlayer");
        this.lightPlayer = lightPlayer;
        this.darkPlayer = darkPlayer;
        this.board = board;
        this.isGameWon = isGameWon;
        this.currentPlayer = currentPlayer;
        this.diceRoll = diceRoll;
        this.hasMoves = hasMoves;
    }

    @Override
    protected void writeContents(PacketWriter writer) {
        writer.pushValue(lightPlayer).pushValue(darkPlayer).pushValue(board).pushBool(isGameWon).pushValue(currentPlayer);
        writer.pushBool(diceRoll != null);
        if (diceRoll != null) {
            writer.pushValue(diceRoll);
            writer.pushBool(hasMoves);
        }
    }

    @Override
    public String toString() {
        String optionalDiceProperties = "";
        if (diceRoll != null) {
            optionalDiceProperties = ", diceRoll=" + diceRoll + ", hasMoves=" + hasMoves;
        }
        return "PacketOutState(lightPlayer=" + lightPlayer + ", "
                + "darkPlayer=" + darkPlayer + ", "
                + "board=" + board + ", "
                + "isGameWon=" + isGameWon + ", "
                + "currentPlayer=" + currentPlayer + optionalDiceProperties + ")";
    }
}
