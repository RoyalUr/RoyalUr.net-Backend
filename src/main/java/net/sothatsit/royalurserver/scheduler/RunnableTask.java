package net.sothatsit.royalurserver.scheduler;

import net.sothatsit.royalurserver.util.Checks;

/**
 * A task to be ran once next scheduler cycle.
 *
 * @author Paddy Lamont
 */
public class RunnableTask extends Task {

    private final Runnable runnable;

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
    public boolean willRepeat() {
        return false;
    }

    @Override
    public void runImpl() {
        runnable.run();
    }
}
