package net.sothatsit.royalurserver;

import net.sothatsit.royalurserver.discord.DiscordBot;
import net.sothatsit.royalurserver.game.Game;
import net.sothatsit.royalurserver.management.GameManager;
import net.sothatsit.royalurserver.management.MatchMaker;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.RoyalUrServer;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInCreateGame;
import net.sothatsit.royalurserver.network.incoming.PacketInFindGame;
import net.sothatsit.royalurserver.network.incoming.PacketInJoinGame;
import net.sothatsit.royalurserver.ssl.KeyInfo;
import net.sothatsit.royalurserver.ssl.LetsEncryptSSL;
import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the RoyalUr.net backend server.
 *
 * @author Paddy Lamont
 */
public class RoyalUr {

    public static final String VERSION = "2.0.0-SNAPSHOT";
    public static final Logger logger = Logging.getLogger("main");

    private final Config config;
    private final RoyalUrServer server;
    private final GameManager gameManager;
    private final MatchMaker matchmaker;
    private final @Nullable DiscordBot bot;

    public RoyalUr() {
        this.config = Config.read();
        this.server = new RoyalUrServer(this, maybeLoadSSLKey());
        this.gameManager = new GameManager();
        this.matchmaker = new MatchMaker(gameManager);

        this.server.start();
        this.gameManager.start();
        this.bot = maybeStartDiscordBot();
        if (bot != null) {
            gameManager.addGameListener(bot);
        }
    }

    private @Nullable KeyInfo maybeLoadSSLKey() {
        return config.useSSL() ? loadSSLKey() : null;
    }

    private KeyInfo loadSSLKey() {
        File certFile = new File(config.getSSLCertFile());
        File privateKeyFile = new File(config.getSSLPrivateKeyFile());
        String password = config.getSSLPassword();
        return LetsEncryptSSL.loadKey(certFile, privateKeyFile, password);
    }

    public void reloadSSL() {
        server.reloadSSL(loadSSLKey());
        logger.info("SSL encryption has been reloaded");
    }

    /** Starts the Discord bot if it is enabled. **/
    private DiscordBot maybeStartDiscordBot() {
        if (!config.runDiscordBot())
            return null;

        try {
            DiscordBot bot = new DiscordBot(config.getDiscordToken(), matchmaker, gameManager);
            System.out.println("Started Discord Bot");
            return bot;
        } catch (LoginException e) {
            new RuntimeException("Error starting Discord bot", e).printStackTrace();
            return null;
        }
    }

    /** Shutdown the RoyalUr application. **/
    public void shutdown() {
        try {
            gameManager.stopAll("Server is restarting");
        } finally {
            try {
                if (bot != null) {
                    bot.shutdown();
                }
            } finally {
                server.stop();
            }
        }
    }

    /** Handle the console input {@param input}. **/
    public void onConsoleInput(String input) {
        Checks.ensureNonNull(input, "input");

        // Remove all non-ascii characters.
        input = input.replaceAll("[^\\x00-\\x7F]", "");
        logger.info("Input: " + input);
    }

    /** Handle the connection of the client {@param client}. **/
    public void onConnect(Client client, boolean isReconnect) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " " + (isReconnect ? "reopen" : "open"));
    }

    /** Handle the disconnection of the client {@param client}. **/
    public void onDisconnect(Client client) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " close");
        gameManager.onClientDisconnect(client);
        matchmaker.onClientDisconnect(client);
    }

    /** Handle the timeout of the client {@param client}. **/
    public void onReconnectTimeout(Client client) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " timed out");
        gameManager.onClientTimeout(client);
    }

    /** Handle the message {@param packet} from the client {@param client}. **/
    public void onMessage(Client client, PacketIn packet) {
        Checks.ensureNonNull(client, "client");
        Checks.ensureNonNull(packet, "packet");

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
            case JOIN_GAME -> {
                PacketInJoinGame joinGamePacket = (PacketInJoinGame) packet;
                if (matchmaker.isGameGenerated(joinGamePacket.gameID)) {
                    matchmaker.joinGeneratedGame(joinGamePacket.gameID, client);
                } else if (matchmaker.isGamePending(joinGamePacket.gameID)) {
                    matchmaker.startPendingGame(joinGamePacket.gameID, client);
                } else {
                    gameManager.onJoinGame(client, joinGamePacket.gameID, true);
                }
            }
            case FIND_GAME -> matchmaker.findMatchFor(client, (PacketInFindGame) packet);
            case CREATE_GAME -> matchmaker.createPendingMatch(client, (PacketInCreateGame) packet);
            default -> {
                client.error("Unexpected packet " + packet.type + " while not in game");
                throw new IllegalStateException(client + " not in game but sent " + packet);
            }
        }
    }

    /** Report the error {@param error}. **/
    public void onError(Exception error) {
        Checks.ensureNonNull(error, "error");

        logger.log(Level.SEVERE, "there was an error", error);
    }

    /** Report the error {@param error} caused by {@param client}. **/
    public void onError(Client client, Exception error) {
        Checks.ensureNonNull(client, "client");
        Checks.ensureNonNull(error, "error");

        logger.log(Level.SEVERE, "there was an error with " + client, error);
    }
}
