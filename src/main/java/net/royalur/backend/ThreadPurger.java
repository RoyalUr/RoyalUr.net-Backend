package net.royalur.backend;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Purges stalled threads on shutdown.
 *
 * @author Paddy Lamont
 */
public class ThreadPurger {

    private static final int INTERRUPT_TIME = 5 * 1000;
    private static final int KILL_TIME = 10 * 1000;
    private static final int KILL_WAIT_TIME = 5 * 1000;

    private static final Logger logger = Logging.getLogger("ThreadPurger");

    private final Thread purgeThread;
    private long startPurgeTime;

    public ThreadPurger() {
        this.purgeThread = new Thread(this::purgeLoop, "stalled-thread-purger");
        this.purgeThread.setDaemon(true);
    }

    public void start() {
        purgeThread.start();
        this.startPurgeTime = System.nanoTime();
    }

    public double getTimeSincePurgeStartMs() {
        return (System.nanoTime() - startPurgeTime) / 1e6d;
    }

    private Set<Thread> getOtherRunningThreads() {
        Set<Thread> filtered = new HashSet<>();

        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread == purgeThread)
                continue;

            filtered.add(thread);
        }

        return filtered;
    }

    private void purgeLoop() {
        while (true) {
            double time = getTimeSincePurgeStartMs();

            if (time < INTERRUPT_TIME) {
                try {
                    Thread.sleep((long) (INTERRUPT_TIME - time));
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            Set<Thread> threads = getOtherRunningThreads();
            if (threads.size() == 0)
                break;

            if (time < KILL_TIME) {
                for (Thread thread : threads) {
                    logger.log(Level.WARNING, "ThreadPurger: Interrupting thread " + thread);
                    thread.interrupt();
                }

                try {
                    Thread.sleep((long) (KILL_TIME - time));
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            // Force all threads to stop
            for (Thread thread : threads) {
                logger.log(Level.WARNING, "ThreadPurger: Forcing thread " + thread + " to stop");
                thread.stop();
            }

            try {
                Thread.sleep(KILL_WAIT_TIME);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
