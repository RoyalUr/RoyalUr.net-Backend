package net.sothatsit.royalurserver.scheduler;

import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;

import java.util.concurrent.TimeUnit;

public class RepeatingTask extends Task {

    private final Runnable runnable;
    private final long periodNanos;
    private Time lastRun;

    public RepeatingTask(String name, Runnable runnable, long period, TimeUnit units) {
        super(name);

        Checks.ensureNonNull(runnable, "runnable");
        Checks.ensure(period >= 0, "period must be >= 0");
        Checks.ensureNonNull(units, "units");

        this.runnable = runnable;
        this.periodNanos = units.toNanos(period);
        this.lastRun = Time.now();
    }

    @Override
    public boolean shouldRun() {
        return lastRun.getTimeSinceNanos() > periodNanos;
    }

    @Override
    public boolean shouldRepeat() {
        return true;
    }

    @Override
    public void run() {
        this.lastRun = Time.now();

        runnable.run();
    }
}
