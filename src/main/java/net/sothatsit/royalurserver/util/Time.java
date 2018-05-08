package net.sothatsit.royalurserver.util;

import java.util.concurrent.TimeUnit;

public class Time {

    public final long time;

    private Time(long time) {
        this.time = time;
    }

    public boolean isPast() {
        return time < System.nanoTime();
    }

    public boolean isFuture() {
        return time > System.nanoTime();
    }

    public long getTimeSinceNanos() {
        return System.nanoTime() - time;
    }

    public long getTimeSinceMillis() {
        return getTimeSinceNanos() / 1000000;
    }

    public long getTimeToNanos() {
        return time - System.nanoTime();
    }

    public long getTimeToMillis() {
        return getTimeToNanos() / 1000000;
    }

    public static Time now() {
        return new Time(System.nanoTime());
    }

    public static Time in(long duration, TimeUnit units) {
        Checks.ensure(duration >= 0, "duration must be >= 0");
        Checks.ensureNonNull(units, "units");

        return new Time(System.nanoTime() + units.toNanos(duration));
    }

    public static Time ago(long duration, TimeUnit units) {
        Checks.ensure(duration >= 0, "duration must be >= 0");
        Checks.ensureNonNull(units, "units");

        return new Time(System.nanoTime() - units.toNanos(duration));
    }

}
