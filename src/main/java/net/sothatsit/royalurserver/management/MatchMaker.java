package net.sothatsit.royalurserver.management;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketInCreateGame;
import net.sothatsit.royalurserver.network.incoming.PacketInFindGame;
import net.sothatsit.royalurserver.network.outgoing.PacketOutGamePending;
import net.sothatsit.royalurserver.network.outgoing.PacketOutInvalidGame;

import java.security.SecureRandom;
import java.util.*;

/**
 * Creates games between players that are searching for a game.
 *
 * @author Paddy Lamont
 */
public class MatchMaker {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final GameManager gameManager;

    private final Object lock = new Object();
    private Client waitingClient = null;
    private final Map<GameID, Client> pendingGames = new HashMap<>();
    private final Set<GameID> generatedGames = new HashSet<>();

    public MatchMaker(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /** @return whether {@param client} is currently waiting for a match. **/
    public boolean isFindingMatchFor(Client client) {
        return waitingClient == client;
    }

    /** @return whether {@param gameID} corresponds to a generated game which has had no one connect. **/
    public boolean isGameGenerated(GameID gameID) {
        synchronized (lock) {
            return generatedGames.contains(gameID);
        }
    }

    /** Converts a generated game into a pending game with the given client. **/
    public void joinGeneratedGame(GameID gameID, Client client) {
        synchronized (lock) {
            if (!generatedGames.remove(gameID)) {
                client.send(new PacketOutInvalidGame(gameID));
                return;
            }
            createPendingMatch(gameID, client);
        }
    }

    /** @return whether {@param gameID} corresponds to a currently pending game. **/
    public boolean isGamePending(GameID gameID) {
        synchronized (lock) {
            return pendingGames.containsKey(gameID);
        }
    }

    /** Starts a pending game when an opponent joins. **/
    public void startPendingGame(GameID gameID, Client opponentClient) {
        synchronized (lock) {
            Client client = pendingGames.get(gameID);
            if (client == null) {
                opponentClient.send(new PacketOutInvalidGame(gameID));
                return;
            }

            // The client just refreshed their browser while waiting for a pending game.
            if (client == opponentClient) {
                opponentClient.send(new PacketOutGamePending(gameID));
                return;
            }

            pendingGames.remove(gameID);
            boolean flag = RANDOM.nextBoolean();
            Client light = (flag ? client : opponentClient);
            Client dark = (flag ? opponentClient : client);
            gameManager.startGame(gameID, light, dark);
        }
    }

    /** Creates a pending match for an opponent to join by link. **/
    public void createPendingMatch(Client client, PacketInCreateGame packet) {
        GameID gameID = gameManager.nextGameID();
        createPendingMatch(gameID, client);
    }

    /** Creates a pending match for an opponent to join by link. **/
    public void createPendingMatch(GameID gameID, Client client) {
        synchronized (lock) {
            pendingGames.put(gameID, client);
            if (waitingClient == client) {
                waitingClient = null;
            }
        }
        client.send(new PacketOutGamePending(gameID));
    }

    /** Generates a new game for two players to connect to from a link. **/
    public GameID generateGame() {
        GameID gameID = gameManager.nextGameID();
        synchronized (lock) {
            generatedGames.add(gameID);
        }
        return gameID;
    }

    /** Attempts to find a match for {@param client} to play. **/
    public void findMatchFor(Client client, PacketInFindGame packet) {
        synchronized (lock) {
            if(waitingClient == null) {
                waitingClient = client;
                return;
            }

            if (waitingClient == client) {
                waitingClient = null;
                client.error("You cannot request to be found two games");
                return;
            }

            boolean flag = RANDOM.nextBoolean();
            Client light = (flag ? waitingClient : client);
            Client dark = (flag ? client : waitingClient);

            gameManager.startGame(light, dark);
            waitingClient = null;
        }
    }

    public void onClientDisconnect(Client client) {
        synchronized (lock) {
            if(waitingClient == client) {
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
