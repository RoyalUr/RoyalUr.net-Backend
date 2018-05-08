package net.sothatsit.royalurserver.scheduler;

import net.sothatsit.royalurserver.Main;
import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        this.logger = Main.getLogger("scheduler " + name);
        this.tickLength = tickLengthUnits.toMillis(tickLength);
        this.tasks = new ArrayList<>();
    }

    public void start() {
        new Thread(this, name).start();
    }

    public void stop() {
        this.running = false;
    }

    public void addTask(Task task) {
        synchronized(tasks) {
            tasks.add(task);
        }
    }

    public void schedule(String name, Runnable runnable) {
        addTask(Task.create(name, runnable));
    }

    public void scheduleAt(String name, Runnable runnable, Time at) {
        addTask(new ScheduledTask(name, runnable, at));
    }

    public void scheduleIn(String name, Runnable runnable, long duration, TimeUnit units) {
        scheduleAt(name, runnable, Time.in(duration, units));
    }

    public void scheduleRepeating(String name, Runnable runnable, long period, TimeUnit units) {
        addTask(new RepeatingTask(name, runnable, period, units));
    }

    @Override
    public void run() {
        this.nextTickTime = Time.now();
        this.running = true;

        while(running) {
            try {
                long sleepTime = nextTickTime.getTimeToMillis();

                if(sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch(InterruptedException e) {
                continue;
            }

            doTick();
        }
    }

    public void doTick() {
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

                    if(!task.shouldRepeat()) {
                        iterator.remove();
                    }
                }
            }
        }

        toRun.forEach(task -> {
            try {
                task.runSafe();
            } catch(Exception exception) {
                logger.log(Level.SEVERE, "exception running " + task, exception);
            }
        });
    }

}
