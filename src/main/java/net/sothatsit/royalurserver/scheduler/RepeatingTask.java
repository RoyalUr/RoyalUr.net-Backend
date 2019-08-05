package net.sothatsit.royalurserver.scheduler;

import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;

import java.util.concurrent.TimeUnit;

/**
 * A task that repeats on a given period.
 *
 * @author Paddy Lamont
 */
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
        return lastRun.getNanosSince() > periodNanos;
    }

    @Override
    public boolean willRepeat() {
        return true;
    }

    @Override
    public void runImpl() {
        this.lastRun = Time.now();

        runnable.run();
    }
}
