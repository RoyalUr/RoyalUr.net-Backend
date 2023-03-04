package net.royalur.backend.ssl;

import net.royalur.backend.Logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.ProcessHandle;

/**
 * This class handles hooking into CertBot to receive
 * SIGHUP signals whenever a new LetsEncrypt certificate
 * is loaded. The certificates can then be live-reloaded
 * without restarting the server.
 */
public class CertbotHook {

    private static final Logger logger = Logging.getLogger("certbot-hook");

    public static void setupHook(Runnable reloadCertificate) {
        writePID();

        sun.misc.Signal.handle(new sun.misc.Signal("HUP"), sig -> {
            reloadCertificate.run();
        });
    }

    /**
     * The CertBot renewal handler reads the PID of the
     * server from a file, which this updates.
     */
    private static void writePID() {
        String pidFileLocation = System.getenv("ROYAL_UR_SERVER_PID_FILE");
        if (pidFileLocation == null || pidFileLocation.trim().isEmpty()) {
            logger.log(
                    Level.WARNING,
                    "No PID file set via the ROYAL_UR_SERVER_PID_FILE env variable. " +
                    "Not writing the PID of the server."
            );
            return;
        }

        long pid = ProcessHandle.current().pid();

        File file = new File(pidFileLocation);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.write(Long.toString(pid));
        } catch (IOException e) {
            throw new RuntimeException("Unable to write PID to PID file " + file, e);
        }
    }
}
