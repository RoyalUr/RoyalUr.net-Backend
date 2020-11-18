package net.sothatsit.royalurserver;

import java.util.Scanner;

/**
 * The entry point to this application.
 *
 * @author Paddy Lamont
 */
public class Main {

    public static final int DEFAULT_PORT = 9113;

    public static void main(String[] args) {
        // Set SLF4J-Simple to have the INFO log level.
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

        RoyalUr game = new RoyalUr(DEFAULT_PORT);

        try(Scanner scanner = new Scanner(System.in)) {
            while(true) {
                String input = scanner.nextLine();

                if(input.equalsIgnoreCase("stop"))
                    break;

                game.onConsoleInput(input);
            }
        } finally {
            try {
                game.shutdown();
            } finally {
                new ThreadPurger().start();
            }
        }
    }
}
