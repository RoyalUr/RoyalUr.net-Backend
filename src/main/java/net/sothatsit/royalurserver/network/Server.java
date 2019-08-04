package net.sothatsit.royalurserver.network;

import net.sothatsit.royalurserver.Logging;
import net.sothatsit.royalurserver.Main;
import net.sothatsit.royalurserver.RoyalUr;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInOpen;
import net.sothatsit.royalurserver.network.incoming.PacketInReOpen;
import net.sothatsit.royalurserver.network.outgoing.PacketOutSetID;
import net.sothatsit.royalurserver.scheduler.RepeatingTask;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends WebSocketServer {

    private static final int PURGE_TIMER_INTERVAL_SECS = 10;

    private final RoyalUr game;
    private final Logger logger;
    private final Scheduler scheduler;

    private final Map<WebSocket, Client> clients;
    private final Map<UUID, Client> disconnected;
    private RepeatingTask clientPurgerTask;
    private boolean started = false;

    public Server(int port, RoyalUr game) {
        super(new InetSocketAddress(port));

        Checks.ensureNonNull(game, "game");

        this.game = game;
        this.logger = Logging.getLogger("server " + port);
        this.scheduler = new Scheduler("server " + port, 1, TimeUnit.SECONDS);

        this.clients = new ConcurrentHashMap<>();
        this.disconnected = new ConcurrentHashMap<>();
        this.clientPurgerTask = null;
    }

    public void purgeDisconnected() {
        Iterator<Client> clients = disconnected.values().iterator();

        while(clients.hasNext()) {
            Client client = clients.next();

            if(client.isTimedOut()) {
                clients.remove();
                game.onReconnectTimeout(client);
            }
        }
    }

    @Override
    public void onStart() {
        started = true;

        scheduler.scheduleRepeating(
                "server client purger", this::purgeDisconnected,
                PURGE_TIMER_INTERVAL_SECS, TimeUnit.SECONDS);

        System.out.println("Listening on " + getPort());
    }

    public void shutdown() {
        if(clientPurgerTask != null) {
            clientPurgerTask.cancel();
        }

        try {
            stop();
        } catch (IOException | InterruptedException e) {
            System.err.println("Exception stopping server");
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake clientHandshake) {
        // TODO: Timeout connections that never send an open message
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        Client client = clients.get(socket);

        if(client == null)
            return;

        clients.remove(socket);
        disconnected.put(client.id, client);

        client.onDisconnect();
        game.onDisconnect(client);
    }

    @Override
    public void onMessage(WebSocket socket, String message) {
        PacketIn packet;

        try {
            packet = new PacketIn(message);
        } catch(Exception exception) {
            logger.log(Level.SEVERE, "exception reading packet message " + message, exception);
            socket.close();
            return;
        }

        Client client = clients.get(socket);

        if(client == null) {
            connectClient(socket, packet);
            return;
        }

        try {
            game.onMessage(client, packet);
        } catch(Exception exception) {
            logger.log(Level.SEVERE, "exception handling packet " + packet + " for " + client, exception);
            socket.close();
        }
    }

    private void connectClient(WebSocket socket, PacketIn packet) {
        Checks.ensureNonNull(socket, "socket");
        Checks.ensureNonNull(packet, "packet");

        Client client = null;
        boolean isReconnect = false;

        switch(packet.type) {
            case OPEN:
                PacketInOpen.read(packet);
                break;
            case REOPEN:
                PacketInReOpen reopen = PacketInReOpen.read(packet);

                client = disconnected.get(reopen.previousID);

                if(client != null && !client.isTimedOut()) {
                    disconnected.remove(reopen.previousID);
                    isReconnect = true;
                } else {
                    client = null;
                }

                break;
            default:
                new IllegalStateException("Expected open or reopen packet, recieved " + packet).printStackTrace();
                socket.close();
                return;
        }

        if(client == null) {
            client = new Client(UUID.randomUUID(), socket);
        }

        clients.put(socket, client);

        client.onConnect(socket);
        client.send(PacketOutSetID.create(client.id));

        game.onConnect(client, isReconnect);
    }

    @Override
    public void onError(WebSocket socket, Exception error) {
        if(!started) {
            logger.log(Level.SEVERE, "error starting server", error);
            System.exit(1);
            return;
        }

        Client client = clients.get(socket);

        if(socket == null || client == null) {
            game.onError(error);
        } else {
            game.onError(client, error);
        }
    }

}
