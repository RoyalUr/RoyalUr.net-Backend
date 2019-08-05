package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.game.Game;
import net.sothatsit.royalurserver.management.GameManager;
import net.sothatsit.royalurserver.management.MatchMaker;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInFindGame;
import net.sothatsit.royalurserver.network.incoming.PacketInJoinGame;
import net.sothatsit.royalurserver.util.Checks;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the RoyalUr application.
 *
 * @author Paddy Lamont
 */
public class RoyalUr {

    public static final Logger logger = Logging.getLogger("main");

    private final GameManager gameManager;
    private final MatchMaker matchmaker;

    public RoyalUr() {
        this.gameManager = new GameManager();
        this.matchmaker = new MatchMaker(gameManager);

        this.gameManager.start();
    }

    /**
     * Shutdown the RoyalUr application.
     */
    public void shutdown() {
        gameManager.stopAll();
    }

    /**
     * Handle the console input {@param input}.
     */
    public void onConsoleInput(String input) {
        Checks.ensureNonNull(input, "input");

        logger.info("Input: " + input);
    }

    /**
     * Handle the connection of the client {@param client}.
     */
    public void onConnect(Client client, boolean isReconnect) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " " + (isReconnect ? "reopen" : "open"));
    }

    /**
     * Handle the disconnection of the client {@param client}.
     */
    public void onDisconnect(Client client) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " close");

        gameManager.onClientDisconnect(client);
        matchmaker.onClientDisconnect(client);
    }

    /**
     * Handle the timeout of the client {@param client}.
     */
    public void onReconnectTimeout(Client client) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " timed out");

        gameManager.onClientTimeout(client);
    }

    /**
     * Handle the message {@param packet} from the client {@param client}.
     */
    public void onMessage(Client client, PacketIn packet) {
        Checks.ensureNonNull(client, "client");
        Checks.ensureNonNull(packet, "packet");

        logger.info(client + " -> " + packet);

        Game game = gameManager.findActiveGame(client);
        if (game != null) {
            game.onMessage(client, packet);
            return;
        }

        if (matchmaker.isFindingMatchFor(client)) {
            client.error("Unexpected packet " + packet.type + " while in the match-making queue");
            return;
        }

        switch (packet.type) {
            case JOIN_GAME:
                PacketInJoinGame joinGamePacket = PacketInJoinGame.read(packet);
                gameManager.onJoinGame(client, joinGamePacket.gameID);
                break;

            case FIND_GAME:
                PacketInFindGame.read(packet);
                matchmaker.findMatchFor(client);
                break;

            case CREATE_GAME:
                client.error("Cannot currently create games");
                break;

            default:
                client.error("Unexpected packet " + packet.type + " while not in game");
                throw new IllegalStateException(client + " not in game but sent " + packet);
        }
    }

    /**
     * Report the error {@param error}.
     */
    public void onError(Exception error) {
        Checks.ensureNonNull(error, "error");

        logger.log(Level.SEVERE, "there was an error", error);
    }

    /**
     * Report the error {@param error} caused by {@param client}.
     */
    public void onError(Client client, Exception error) {
        Checks.ensureNonNull(client, "client");
        Checks.ensureNonNull(error, "error");

        logger.log(Level.SEVERE, "there was an error with " + client, error);
    }
}
