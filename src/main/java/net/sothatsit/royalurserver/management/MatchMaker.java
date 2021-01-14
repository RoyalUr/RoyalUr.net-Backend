package net.sothatsit.royalurserver.management;

import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketInFindGame;

import java.security.SecureRandom;

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

    public MatchMaker(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public boolean isFindingMatchFor(Client client) {
        return waitingClient == client;
    }

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
}
