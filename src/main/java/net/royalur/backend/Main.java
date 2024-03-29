package net.royalur.backend;

import org.slf4j.simple.SimpleLogger;

import java.util.Scanner;

/**
 * The entry point to this application.
 *
 * @author Paddy Lamont
 */
public class Main {

    public static void main(String[] args) {
        // Set SLF4J-Simple to have the INFO log level.
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");


        RoyalUr.logger.info("Starting RoyalUr.net server v" + RoyalUr.VERSION);
        RoyalUr game = new RoyalUr();

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
