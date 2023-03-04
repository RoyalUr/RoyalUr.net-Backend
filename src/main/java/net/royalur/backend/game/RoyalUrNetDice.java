package net.royalur.backend.game;

import net.royalur.rules.Dice;

import javax.annotation.Nonnull;
import java.util.Random;

public class RoyalUrNetDice extends Dice<RoyalUrNetDiceRoll> {

    public static final String ID = "RoyalUr.net";

    /**
     * The number of dice to roll.
     */
    public static final int DICE_COUNT = 4;

    /**
     * The random number generator used to generate dice rolls.
     */
    private final @Nonnull Random random;

    /**
     * @param random The random number generator used to generate dice rolls.
     */
    protected RoyalUrNetDice(@Nonnull Random random) {
        super(DICE_COUNT);
        this.random = random;
    }

    protected RoyalUrNetDice() {
        this(new Random());
    }

    @Override
    public @Nonnull String getIdentifier() {
        return ID;
    }

    @Override
    public @Nonnull RoyalUrNetDiceRoll roll() {
        DiceValue[] values = new DiceValue[DICE_COUNT];
        for (int index = 0; index < DICE_COUNT; ++index) {
            values[index] = DiceValue.random(random);
        }
        return new RoyalUrNetDiceRoll(values);
    }

    @Override
    public @Nonnull RoyalUrNetDiceRoll roll(int value) {
        DiceValue[] values = new DiceValue[DICE_COUNT];
        for (int index = 0; index < DICE_COUNT; ++index) {
            values[index] = (index < value ? DiceValue.UP_1 : DiceValue.DOWN_1);
        }
        return new RoyalUrNetDiceRoll(values);
    }
}
