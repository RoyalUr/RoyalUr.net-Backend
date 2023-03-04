package net.royalur.backend;

import net.royalur.backend.discord.DiscordBot;
import net.royalur.backend.game.GameID;
import net.royalur.backend.management.GameManager;
import net.royalur.backend.management.GameRepository;
import net.royalur.backend.management.ManagedGame;
import net.royalur.backend.management.MatchMaker;
import net.royalur.backend.network.Client;
import net.royalur.backend.network.RoyalUrServer;
import net.royalur.backend.network.incoming.*;
import net.royalur.backend.network.outgoing.PacketOutGameInvalid;
import net.royalur.backend.ssl.KeyInfo;
import net.royalur.backend.ssl.LetsEncryptSSL;
import net.royalur.backend.util.Checks;

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
    private final GameRepository gameRepository;
    private final GameManager gameManager;
    private final MatchMaker matchmaker;
    private final @Nullable DiscordBot bot;

    public RoyalUr() {
        this.config = Config.read();
        this.server = new RoyalUrServer(this, maybeLoadSSLKey());
        this.gameRepository = new GameRepository();
        this.gameManager = new GameManager(gameRepository);
        this.matchmaker = new MatchMaker(gameRepository, gameManager);

        this.server.start();
        this.gameManager.start();
        this.bot = maybeStartDiscordBot();
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

    /**
     * Handle the timeout of a client.
     */
    public void onReconnectTimeout(Client client) {
        Checks.ensureNonNull(client, "client");

        logger.info(client + " timed out");
        gameManager.onClientTimeout(client);
    }

    /**
     * Handle a message from a client.
     */
    public void onMessage(Client client, PacketIn packet) {
        Checks.ensureNonNull(client, "client");
        Checks.ensureNonNull(packet, "packet");

        if (packet instanceof GamePacketIn) {
            GamePacketIn gamePacket = (GamePacketIn) packet;
            ManagedGame game = gameManager.getGameOrNull(gamePacket.gameID);
            if (game == null) {
                client.error("Unable to find the game " + gamePacket.gameID);
                throw new IllegalStateException(
                        "Unable to find game " + gamePacket.gameID + " for " + client + " who sent " + packet
                );
            }

            game.onPacket(client, packet);
            return;
        }

        if (matchmaker.isFindingMatchFor(client)) {
            client.error("Unexpected packet " + packet.type + " while in the match-making queue");
            return;
        }

        switch (packet.type) {
            case JOIN_GAME -> {
                PacketInJoinGame joinGamePacket = (PacketInJoinGame) packet;
                GameID gameID = joinGamePacket.gameID;

                if (gameManager.containsGame(gameID)) {
                    gameManager.joinGame(gameID, client, true);
                } else if (matchmaker.isGameReserved(gameID)) {
                    matchmaker.joinReservedGame(gameID, client);
                } else {
                    client.send(new PacketOutGameInvalid(gameID));
                }
            }
            case FIND_GAME -> matchmaker.findMatchFor(client, (PacketInFindGame) packet);
            case CREATE_GAME -> matchmaker.createPendingGame(client, (PacketInCreateGame) packet);
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
