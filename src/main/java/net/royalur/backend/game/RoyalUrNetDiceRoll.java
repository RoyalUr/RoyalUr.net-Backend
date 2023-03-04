package net.royalur.backend.game;

import net.royalur.model.Roll;

import javax.annotation.Nonnull;

/**
 * A roll to display on RoyalUr.net.
 */
public class RoyalUrNetDiceRoll extends Roll {

    /**
     * The value of each dice.
     */
    public final @Nonnull DiceValue[] values;

    /**
     * @param values The value of each dice.
     */
    public RoyalUrNetDiceRoll(@Nonnull DiceValue[] values) {
        super(DiceValue.count(values));
        this.values = values;
    }
}
