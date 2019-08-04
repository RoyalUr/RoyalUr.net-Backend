package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.game.Game;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoyalUr {

    public static final Logger logger = Logging.getLogger("main");

    private final Scheduler scheduler;
    private final Matchmaker matchmaker;

    public RoyalUr() {
        this.scheduler = new Scheduler("general scheduler", 100, TimeUnit.MILLISECONDS);
        this.scheduler.start();

        this.matchmaker = new Matchmaker();
    }

    public void shutdown() {
        scheduler.stop();
        matchmaker.stopAll();
    }

    public void onConsoleInput(String input) {
        Checks.ensureNonNull(input, "input");

        logger.info("Input: " + input);
    }

    public void onConnect(Client client, boolean isReconnect) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " " + (isReconnect ? "reopen" : "open"));

        matchmaker.connectClient(client);
    }

    public void onDisconnect(Client client) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " close");

        matchmaker.disconnectClient(client);
    }

    public void onReconnectTimeout(Client client) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " timed out");

        matchmaker.timeoutClient(client);
    }

    public void onMessage(Client client, PacketIn packet) {
        Checks.ensureNonNull(client, "client");
        Checks.ensureNonNull(packet, "packet");

        logger.info(client + " -> " + packet);

        Game game = matchmaker.findGame(client);

        if(game == null) {
            client.error("unexpected packet when not in game");
            throw new IllegalStateException(client + " not in game but sent " + packet);
        }

        game.onMessage(client, packet);
    }

    public void onError(Exception error) {
        Checks.ensureNonNull(error, "error");

        logger.log(Level.SEVERE, "there was an error", error);
    }

    public void onError(Client client, Exception error) {
        Checks.ensureNonNull(client, "client");
        Checks.ensureNonNull(error, "error");

        logger.log(Level.SEVERE, "there was an error with " + client, error);
    }
}
