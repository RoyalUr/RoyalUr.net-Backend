package net.sothatsit.royalurserver.management;

import net.sothatsit.royalurserver.RoyalUrNetIdentity;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.GameSettings;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketInCreateGame;
import net.sothatsit.royalurserver.network.incoming.PacketInFindGame;
import net.sothatsit.royalurserver.network.outgoing.PacketOutGamePending;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates games between players that are searching for a game.
 *
 * @author Paddy Lamont
 */
public class MatchMaker {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final GameRepository gameRepository;
    private final GameManager gameManager;

    private final Object lock = new Object();
    private Client waitingClient = null;
    private final Map<GameID, Client> pendingGames = new HashMap<>();

    public MatchMaker(GameRepository gameRepository, GameManager gameManager) {
        this.gameRepository = gameRepository;
        this.gameManager = gameManager;
    }

    /** @return whether {@param client} is currently waiting for a match. **/
    public boolean isFindingMatchFor(Client client) {
        return waitingClient == client;
    }

    /** @return whether {@param gameID} corresponds to a generated game which has had no one connect. **/
    public boolean isGameReserved(GameID gameID) {
        GameRepositoryEntry entry = gameRepository.get(gameID);
        return entry != null && entry.isReservation();
    }

    /**
     * Creates or starts a pending game for the given reserved game ID.
     */
    public void joinReservedGame(GameID gameID, Client client) {
        GameRepositoryEntry entry = gameRepository.get(gameID);
        if (entry == null || !entry.isReservation())
            throw new IllegalArgumentException("Game is not reserved: " + gameID);

        synchronized (lock) {
            Client pendingClient = pendingGames.get(gameID);

            if (pendingClient == null) {
                createPendingGame(gameID, client);
                return;
            }

            if (pendingClient == client) {
                client.send(new PacketOutGamePending(gameID));
                return;
            }

            startPendingGame(gameID, client, pendingClient);
        }
    }

    /**
     * Reserves a game ID for the given client.
     */
    public void createPendingGame(Client client, PacketInCreateGame packet) {
        GameID gameID = gameManager.reserveGameID(GameSettings.STANDARD, client);
        createPendingGame(gameID, client);
    }

    /**
     * Creates a pending game for the given client.
     */
    private void createPendingGame(GameID gameID, Client client) {
        synchronized (lock) {
            pendingGames.put(gameID, client);
            if (waitingClient == client) {
                waitingClient = null;
            }
        }
        client.send(new PacketOutGamePending(gameID));
    }

    /**
     * Starts a pending game when an opponent joins.
     **/
    private void startPendingGame(GameID gameID, Client client1, Client client2) {
        if (client1 == client2)
            throw new IllegalArgumentException("A client cannot play themselves!");

        synchronized (lock) {
            Client known = pendingGames.remove(gameID);
            if (known == null)
                throw new IllegalArgumentException("The game ID is not pending: " + gameID);
        }
        startGame(gameID, client1, client2);
    }

    private void startGame(GameID gameID, Client client1, Client client2) {
        boolean flag = RANDOM.nextBoolean();
        Client lightClient = (flag ? client1 : client2);
        Client darkClient = (flag ? client2 : client1);
        gameManager.startGame(gameID, lightClient, darkClient);

    }

    /**
     * Places the given client into the match-making queue.
     */
    public void findMatchFor(Client client, PacketInFindGame packet) {
        Client opponentClient;
        synchronized (lock) {
            if(waitingClient == null) {
                waitingClient = client;
                return;
            }
            if (waitingClient == client)
                return;

            opponentClient = waitingClient;
            waitingClient = null;
        }

        GameID gameID = gameManager.reserveGameID(GameSettings.STANDARD, client);
        startGame(gameID, opponentClient, client);
    }

    /**
     * Reserves a game ID for the bot.
     */
    public @Nonnull GameID reserveBotGame(RoyalUrNetIdentity identity) {
        return gameRepository.reserveGameID(GameSettings.STANDARD, identity);
    }

    public void onClientDisconnect(Client client) {
        synchronized (lock) {
            if (waitingClient == client) {
                waitingClient = null;
            }
        }
    }

    public void onClientTimeout(Client client) {
        synchronized (lock) {
            pendingGames.values().removeIf(c -> c == client);
        }
    }
}
