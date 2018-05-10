package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.game.DiceRoll;
import net.sothatsit.royalurserver.game.Player;
import net.sothatsit.royalurserver.game.Board;
import net.sothatsit.royalurserver.game.PlayerState;
import net.sothatsit.royalurserver.util.Checks;

public class PacketOutState {

    private static PacketOut create(
            PlayerState lightPlayer,
            PlayerState darkPlayer,
            Board board,
            Player currentPlayer) {

        Checks.ensureNonNull(lightPlayer, "lightPlayer");
        Checks.ensureNonNull(darkPlayer, "darkPlayer");
        Checks.ensureNonNull(board, "board");
        Checks.ensureNonNull(currentPlayer, "currentPlayer");

        PacketOut packet = new PacketOut(PacketOut.Type.STATE);

        packet.write(lightPlayer);
        packet.write(darkPlayer);
        packet.write(board);

        packet.write(currentPlayer);

        return packet;
    }

    public static PacketOut createAwaitingRoll(
            PlayerState lightPlayer,
            PlayerState darkPlayer,
            Board board,
            Player currentPlayer) {

        PacketOut packet = create(lightPlayer, darkPlayer, board, currentPlayer);

        packet.write(false);

        return packet;
    }

    public static PacketOut createRolled(
            PlayerState lightPlayer,
            PlayerState darkPlayer,
            Board board,
            Player currentPlayer,
            DiceRoll diceRoll,
            boolean hasMoves) {

        Checks.ensureNonNull(diceRoll, "diceRoll");

        PacketOut packet = create(lightPlayer, darkPlayer, board, currentPlayer);

        packet.write(true);
        packet.write(diceRoll);
        packet.write(hasMoves);

        return packet;
    }
}
