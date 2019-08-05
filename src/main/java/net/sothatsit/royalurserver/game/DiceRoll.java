package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

import java.util.Arrays;
import java.util.Random;

/**
 * Represents the state of a dice roll.
 *
 * @author Paddy Lamont
 */
public class DiceRoll implements PacketWritable {

    public static final int DICE_COUNT = 4;

    private final DiceValue[] values;
    private final int value;

    /**
     * Construct a dice roll with the given dice values.
     */
    public DiceRoll(DiceValue[] values) {
        Checks.ensureArrayNonNull(values, "values");
        Checks.ensure(values.length == DICE_COUNT, "values must be of length 4");

        this.values = values;
        this.value = countUp(values);
    }

    /**
     * @return The states of all dice.
     */
    public DiceValue[] getValues() {
        return values;
    }

    /**
     * @return The number of dice that are face up.
     */
    public int getValue() {
        return value;
    }

    @Override
    public void writeTo(PacketOut packet) {
        for(DiceValue value : values) {
            packet.writeDigit(value.getId());
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    private static int countUp(DiceValue[] values) {
        int value = 0;

        for(DiceValue diceValue : values) {
            if(!diceValue.isUp())
                continue;

            value += 1;
        }

        return value;
    }

    /**
     * @return A random DiceRoll containing all random dice values.
     */
    public static DiceRoll roll(Random random) {
        DiceValue[] values = new DiceValue[4];

        for(int index = 0; index < values.length; ++index) {
            values[index] = DiceValue.random(random);
        }

        return new DiceRoll(values);
    }

}
