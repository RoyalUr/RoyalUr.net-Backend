package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.util.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the state of a single dice.
 *
 * @author Paddy Lamont
 */
public enum DiceValue {

    UP_1("up 1", 1, true),
    UP_2("up 2", 2, true),
    UP_3("up 3", 3, true),

    DOWN_1("down 1", 4, false),
    DOWN_2("down 2", 5, false),
    DOWN_3("down 3", 6, false);

    public static final DiceValue[] UP;
    public static final DiceValue[] DOWN;
    static {
        List<DiceValue> up = new ArrayList<>();
        List<DiceValue> down = new ArrayList<>();
        for (DiceValue value : values()) {
            if (value.isUp) {
                up.add(value);
            } else {
                down.add(value);
            }
        }

        Checks.ensure(up.size() > 0, "There must be at least one up DiceValue");
        Checks.ensure(down.size() > 0, "There must be at least one down DiceValue");

        UP = up.toArray(new DiceValue[0]);
        DOWN = down.toArray(new DiceValue[0]);
    }

    private final String name;
    private final int id;
    private final boolean isUp;

    private DiceValue(String name, int id, boolean isUp) {
        Checks.ensureNonNull(name, "name");
        Checks.ensureSingleDigit(id, "id");

        this.name = name;
        this.id = id;
        this.isUp = isUp;
    }

    /**
     * @return A name representing the given dice value.
     */
    public String getName() {
        return name;
    }

    /**
     * @return An ID that represents this dice value.
     */
    public int getId() {
        return id;
    }

    /**
     * @return Whether this dice value is up.
     */
    public boolean isUp() {
        return isUp;
    }

    @Override
    public String toString() {
        return "DiceValue(" + name + ")";
    }

    /**
     * @return A random dice value. Has a 50% change of being up, 50% chance down.
     */
    public static DiceValue random(Random random) {
        Checks.ensureNonNull(random, "random");

        if(random.nextBoolean()) {
            return UP[random.nextInt(UP.length)];
        } else {
            return DOWN[random.nextInt(DOWN.length)];
        }
    }

}
