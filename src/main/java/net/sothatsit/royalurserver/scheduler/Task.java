package net.sothatsit.royalurserver.scheduler;

import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.ExceptionDetailer;

/**
 * A task to be ran by a Scheduler.
 *
 * @author Paddy Lamont
 */
public abstract class Task {

    private final ExceptionDetailer exceptionDetailer;
    private final String name;
    private boolean cancelled;

    public Task(String name) {
        Checks.ensureNonNull(name, "name");

        this.exceptionDetailer = ExceptionDetailer.constructorDetailer();
        this.name = name;
        this.cancelled = false;
    }

    /**
     * Cancel this task such that it no longer be ran.
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * @return Whether this task has been cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @return Whether this task should be ran on the current cycle.
     */
    public abstract boolean shouldRun();

    /**
     * @return Whether this task will repeat on any future cycle.
     */
    public abstract boolean willRepeat();

    /**
     * Should be overridden in sub-classes to implement the behaviour desired when this task is ran.
     */
    protected abstract void runImpl();

    /**
     * Run this task.
     */
    public final void run() throws Exception {
        try {
            runImpl();
        } catch(Exception exception) {
            throw exceptionDetailer.detail(exception);
        }
    }

    @Override
    public String toString() {
        return "Task(" + name + ")";
    }
}
