package net.sothatsit.royalurserver.management;

import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketInCreateGame;
import net.sothatsit.royalurserver.network.incoming.PacketInFindGame;
import net.sothatsit.royalurserver.network.outgoing.PacketOutGamePending;
import net.sothatsit.royalurserver.network.outgoing.PacketOutInvalidGame;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    public MatchMaker(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /** @return whether {@param client} is currently waiting for a match. **/
    public boolean isFindingMatchFor(Client client) {
        return waitingClient == client;
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
        client.setName(packet.name);

        GameID gameID = gameManager.nextGameID();
        synchronized (lock) {
            pendingGames.put(gameID, client);
            if (waitingClient == client) {
                waitingClient = null;
            }
        }
        client.send(new PacketOutGamePending(gameID));
    }

    /** Attempts to find a match for {@param client} to play. **/
    public void findMatchFor(Client client, PacketInFindGame packet) {
        client.setName(packet.name);

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
