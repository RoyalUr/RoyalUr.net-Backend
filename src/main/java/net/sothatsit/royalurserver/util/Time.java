package net.sothatsit.royalurserver.util;

import java.util.concurrent.TimeUnit;

/**
 * Represents a certain time.
 *
 * @author Paddy Lamont
 */
public class Time {

    public final long time;

    /** A time at the given milliseconds {@param time} since epoch. **/
    private Time(long time) {
        this.time = time;
    }

    /** @return Whether this time is in the past. **/
    public boolean isPast() {
        return time < System.nanoTime();
    }

    /** @return Whether this time is in the future. **/
    public boolean isFuture() {
        return time > System.nanoTime();
    }

    /** @return Whether this time is before {@param other}. **/
    public boolean isBefore(Time other) {
        return time < other.time;
    }

    /** @return Whether this time is after {@param other}. **/
    public boolean isAfter(Time other) {
        return time > other.time;
    }

    /** @return The number of nanoseconds since this time. **/
    public long getNanosSince() {
        return System.nanoTime() - time;
    }

    /** @return The number of milliseconds since this time. **/
    public long getMillisSince() {
        return getNanosSince() / 1000000;
    }

    /** @return The number of nanoseconds until this time. **/
    public long getNanosUntil() {
        return time - System.nanoTime();
    }

    /** @return The number of milliseconds until this time. **/
    public long getMillisUntil() {
        return getNanosUntil() / 1000000;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Time && time == ((Time) obj).time;

    }

    /** @return The current time. **/
    public static Time now() {
        return new Time(System.nanoTime());
    }

    /** @return The time in the given duration, {@param duration} {@param units}. **/
    public static Time in(long duration, TimeUnit units) {
        Checks.ensure(duration >= 0, "duration must be >= 0");
        Checks.ensureNonNull(units, "units");

        return new Time(System.nanoTime() + units.toNanos(duration));
    }

    /** @return The time in the past by the given duration, {@param duration} {@param units}. **/
    public static Time ago(long duration, TimeUnit units) {
        Checks.ensure(duration >= 0, "duration must be >= 0");
        Checks.ensureNonNull(units, "units");

        return new Time(System.nanoTime() - units.toNanos(duration));
    }

    /** @return The earliest time of {@param one} and {@param two}. **/
    public static Time earliest(Time one, Time two) {
        return one.isAfter(two) ? two : one;
    }

    /** @return The latest time of {@param one} and {@param two}. **/
    public static Time latest(Time one, Time two) {
        return one.isAfter(two) ? one : two;
    }
}
