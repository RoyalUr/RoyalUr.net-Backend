package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.network.Server;

import java.util.Scanner;
import java.util.logging.*;

public class Main {

    public static final int DEFAULT_PORT = 9113;

    public static void main(String[] args) {
        Logger logger = Logging.getLogger("main");

        RoyalUr game = new RoyalUr();
        Server server = new Server(DEFAULT_PORT, game);

        int retryCount = 0;

        while(true) {
            try {
                server.start();
                break;
            } catch(Exception exception) {
                if(retryCount < 10) {
                    logger.log(Level.WARNING, "error starting server, retrying...");

                    retryCount += 1;
                    try {
                        Thread.sleep(retryCount * 1000);
                    } catch (InterruptedException ignored) {}
                    continue;
                }

                logger.log(Level.SEVERE, "error starting server", exception);
                System.exit(1);
                return;
            }
        }

        try(Scanner scanner = new Scanner(System.in)) {
            while(true) {
                String input = scanner.nextLine();

                if(input.equalsIgnoreCase("stop")) {
                    game.shutdown();
                    server.shutdown();
                    break;
                }

                game.onConsoleInput(input);
            }
        }
    }
}
