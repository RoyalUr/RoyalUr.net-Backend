package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class DiceRoll implements PacketWritable {

    public static final SecureRandom random = new SecureRandom();

    public static final int DICE_COUNT = 4;

    private final DiceValue[] values;
    private final int value;

    public DiceRoll(DiceValue[] values) {
        Checks.ensureNonNull(values, "values");
        Checks.ensure(values.length == DICE_COUNT, "values must be of length 4");

        this.values = values;
        this.value = countUp(values);
    }

    public DiceValue[] getValues() {
        return values;
    }

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

    public static DiceRoll roll() {
        DiceValue[] values = new DiceValue[4];

        for(int index = 0; index < values.length; ++index) {
            values[index] = DiceValue.random(random);
        }

        return new DiceRoll(values);
    }

    private enum DiceValue {
        UP_1("up 1", 1, true),
        UP_2("up 2", 2, true),
        UP_3("up 3", 3, true),

        DOWN_1("down 1", 4, false),
        DOWN_2("down 2", 5, false),
        DOWN_3("down 3", 6, false);

        public static final DiceValue[] UP = {UP_1, UP_2, UP_3};
        public static final DiceValue[] DOWN = {DOWN_1, DOWN_2, DOWN_3};

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

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public boolean isUp() {
            return isUp;
        }

        @Override
        public String toString() {
            return "dice " + name;
        }

        public static DiceValue random(Random random) {
            Checks.ensureNonNull(random, "random");

            if(random.nextBoolean()) {
                return UP[random.nextInt(UP.length)];
            } else {
                return DOWN[random.nextInt(DOWN.length)];
            }
        }

    }

}
