package net.royalur.backend.scheduler;

import net.royalur.backend.Logging;
import net.royalur.backend.util.Checks;
import net.royalur.backend.util.Time;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows the scheduling of tasks to be ran on a single thread.
 *
 * @author Paddy Lamont
 */
public class Scheduler implements Runnable {

    private final String name;
    private final Logger logger;
    private final long tickLength;
    private final List<Task> tasks;

    private Time nextTickTime;
    private boolean running;

    public Scheduler(String name, long tickLength, TimeUnit tickLengthUnits) {
        Checks.ensureNonNull(name, "name");
        Checks.ensure(tickLength >= 0, "tickLength must be >= 0");
        Checks.ensureNonNull(tickLengthUnits, "tickLengthUnits");

        this.name = name;
        this.logger = Logging.getLogger("scheduler " + name);
        this.tickLength = tickLengthUnits.toMillis(tickLength);
        this.tasks = new ArrayList<>();
    }

    /**
     * Start this Scheduler such that it starts processing tasks.
     */
    public void start() {
        // TODO : Store this Thread and make sure only one is made
        new Thread(this, name).start();
    }

    /**
     * Stop this Scheduler after the current cycle of tasks.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Add the task {@param task} to be processed by this Scheduler.
     */
    public void addTask(Task task) {
        synchronized(tasks) {
            tasks.add(task);
        }
    }

    /**
     * Schedule that the task {@param runnable} be ran on the next cycle.
     */
    public void schedule(String name, Runnable runnable) {
        addTask(new RunnableTask(name, runnable));
    }

    /**
     * Schedule that the task {@param runnable} be ran at the given time {@param at}.
     */
    public void scheduleAt(String name, Runnable runnable, Time at) {
        addTask(new ScheduledTask(name, runnable, at));
    }

    /**
     * Schedule that the task {@param runnable} be ran in the given duration, {@param duration} {@param units}.
     */
    public void scheduleIn(String name, Runnable runnable, long duration, TimeUnit units) {
        scheduleAt(name, runnable, Time.in(duration, units));
    }

    /**
     * Schedule that the task {@param runnable} be ran every period, {@param period} {@param units}.
     */
    public void scheduleRepeating(String name, Runnable runnable, long period, TimeUnit units) {
        addTask(new RepeatingTask(name, runnable, period, units));
    }

    @Override
    public void run() {
        this.nextTickTime = Time.now();
        this.running = true;

        while(running) {
            doTick();

            try {
                long sleepTime = nextTickTime.getMillisUntil();

                if(sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch(InterruptedException ignored) {
                // We only care about the interrupt if running was set to false
            }
        }
    }

    /**
     * Cycle through and run all tasks that request to be ran.
     */
    private void doTick() {
        this.nextTickTime = Time.in(tickLength, TimeUnit.MILLISECONDS);

        List<Task> toRun = new ArrayList<>();

        synchronized(tasks) {
            Iterator<Task> iterator = tasks.iterator();

            while(iterator.hasNext()) {
                Task task = iterator.next();

                if(task.isCancelled()) {
                    iterator.remove();
                } else if(task.shouldRun()) {
                    toRun.add(task);

                    if(!task.willRepeat()) {
                        iterator.remove();
                    }
                }
            }
        }

        toRun.forEach(task -> {
            try {
                task.run();
            } catch(Exception exception) {
                logger.log(Level.SEVERE, "exception running " + task, exception);
            }
        });
    }

}
