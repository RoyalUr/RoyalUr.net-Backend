package net.sothatsit.royalurserver.network.outgoing;

import net.sothatsit.royalurserver.network.PacketWritable;
import net.sothatsit.royalurserver.util.Checks;

public class PacketOut {

    public final Type type;
    private final StringBuilder dataBuilder;

    public PacketOut(Type type) {
        Checks.ensureNonNull(type, "type");

        this.type = type;
        this.dataBuilder = new StringBuilder();

        write((char) (type.getId() + '0'));
    }

    public String encode() {
        return dataBuilder.toString();
    }

    public void write(boolean value) {
        write(value ? "t" : "f");
    }

    public void write(PacketWritable value) {
        Checks.ensureNonNull(value, "value");

        value.writeTo(this);
    }

    public void writeDigit(int digit) {
        Checks.ensureSingleDigit(digit, "digit");

        write(digit);
    }

    public void writeVarString(String value) {
        writeVarString(value, 2);
    }

    public void writeVarString(String value, int lengthCharacters) {
        Checks.ensureNonNull(value, "value");

        write(value.length(), lengthCharacters);
        dataBuilder.append(value);
    }

    public void write(int value, int characters) {
        Checks.ensure(value >= 0, "value must be >= 0");
        Checks.ensure(characters > 0, "digits must be positive");

        String string = Integer.toString(value);

        Checks.ensure(string.length() <= characters, "value has too many characters");

        for(int index = string.length(); index < characters; ++index) {
            dataBuilder.append('0');
        }

        dataBuilder.append(string);
    }

    public void write(Object value) {
        Checks.ensureNonNull(value, "value");

        dataBuilder.append(value);
    }

    public enum Type {

        ERROR("error"),
        SETID("setid"),
        GAME("game"),
        MESSAGE("message"),
        STATE("state"),
        MOVE("move"),
        WIN("win");

        private final String name;

        private Type(String name) {
            Checks.ensureNonNull(name, "name");

            this.name = name;
        }

        public int getId() {
            return ordinal();
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "outgoing " + getId() + ":" + getName();
        }
    }
}
