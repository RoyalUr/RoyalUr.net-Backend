package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.network.Server;

import java.util.Scanner;

/**
 * The entry point to this application.
 *
 * @author Paddy Lamont
 */
public class Main {

    public static final int DEFAULT_PORT = 9113;

    public static void main(String[] args) {
        RoyalUr game = new RoyalUr();

        // TODO : Its weird that this isn't handled by RoyalUr itself
        Server server = new Server(DEFAULT_PORT, game);

        try(Scanner scanner = new Scanner(System.in)) {
            server.start();

            while(true) {
                String input = scanner.nextLine();

                if(input.equalsIgnoreCase("stop"))
                    break;

                game.onConsoleInput(input);
            }
        } finally {
            game.shutdown();
            server.shutdown();
        }
    }
}
