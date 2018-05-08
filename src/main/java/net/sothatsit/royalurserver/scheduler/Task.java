package net.sothatsit.royalurserver.scheduler;

import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.ExceptionDetailer;

public abstract class Task implements Runnable {

    private final ExceptionDetailer exceptionDetailer;
    private final String name;
    private boolean cancelled;

    public Task(String name) {
        Checks.ensureNonNull(name, "name");

        this.exceptionDetailer = ExceptionDetailer.constructorDetailer();
        this.name = name;
        this.cancelled = false;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public abstract boolean shouldRun();

    public abstract boolean shouldRepeat();

    public abstract void run();

    public final void runSafe() throws Exception {
        try {
            run();
        } catch(Exception exception) {
            throw exceptionDetailer.detail(exception);
        }
    }

    @Override
    public String toString() {
        return "task " + name;
    }

    private static final class RunnableTask extends Task {
        private Runnable runnable;

        public RunnableTask(String name, Runnable runnable) {
            super(name);

            Checks.ensureNonNull(runnable, "runnable");

            this.runnable = runnable;
        }

        @Override
        public boolean shouldRun() {
            return true;
        }

        @Override
        public boolean shouldRepeat() {
            return false;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }

    public static Task create(String name, Runnable runnable) {
        return new RunnableTask(name, runnable);
    }

}
