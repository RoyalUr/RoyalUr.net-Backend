package net.sothatsit.royalurserver.network;

import net.sothatsit.royalurserver.Logging;
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
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A server that accepts web socket connections, and manages
 * those connections as clients, each with their own unique IDs.
 *
 * @author Paddy Lamont
 */
public class Server extends WebSocketServer {

    private static final Field SERVER_FIELD;
    static {
        try {
            SERVER_FIELD = WebSocketServer.class.getDeclaredField("server");
            SERVER_FIELD.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static final int PURGE_TIMER_INTERVAL_SECS = 10;
    private static final int PURGE_LIMBO_SECS = 10;

    private final RoyalUr game;
    private final Logger logger;
    private final Scheduler scheduler;

    private final Map<WebSocket, Client> clients;
    private final Map<WebSocket, Long> limboConnections;
    private final Map<UUID, Client> disconnected;
    private final RepeatingTask clientPurgerTask;

    private final Object startMonitor = new Object();
    private Thread serverThread;
    private boolean started = false;
    private Exception startException;

    public Server(int port, RoyalUr game) {
        super(new InetSocketAddress(port));

        Checks.ensureNonNull(game, "game");

        this.game = game;
        this.logger = Logging.getLogger("server " + port);
        this.scheduler = new Scheduler("server " + port, 1, TimeUnit.SECONDS);

        this.clients = new ConcurrentHashMap<>();
        this.limboConnections = new ConcurrentHashMap<>();
        this.disconnected = new ConcurrentHashMap<>();
        this.clientPurgerTask = new RepeatingTask(
                "server client purger", this::purgeDisconnected,
                PURGE_TIMER_INTERVAL_SECS, TimeUnit.SECONDS
        );
    }

    public void setupSSL(SSLContext context) {
        setWebSocketFactory(new DefaultSSLWebSocketServerFactory(context));
        setConnectionLostTimeout(120);
    }

    @Override
    public void start() {
        this.started = false;
        this.startException = null;
        this.serverThread = new Thread(this);

        try {
            serverThread.start();

            // Wait for the server to start
            synchronized (startMonitor) {
                startMonitor.wait();
            }
        } catch (Exception exception) {
            RuntimeException toThrow = new RuntimeException("Exception starting thread", exception);

            try {
                stop();
            } catch (Exception stopException) {
                toThrow.addSuppressed(stopException);
            }

            throw toThrow;
        }

        if (startException != null)
            throw new RuntimeException("Exception starting server", startException);
    }

    /**
     * Properly close the socket. The standard WebSocketServer#close doesn't work.
     */
    private void killChannel() {
        ServerSocketChannel channel;
        try {
            channel = (ServerSocketChannel) SERVER_FIELD.get(this);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Exception getting WebSocketServer's channel", exception);
        }

        if (channel == null || !channel.isOpen())
            return;

        try {
            channel.close();
        } catch (IOException exception) {
            throw new RuntimeException("Exception closing channel", exception);
        }
    }

    @SuppressWarnings("deprecation")
    private void killServerThread() {
        serverThread.stop();
    }

    @Override
    public void stop(int timeout) {
        try {
            super.stop(timeout);
            serverThread.interrupt();
        } catch (InterruptedException exception) {
            throw new RuntimeException("Interrupted waiting for server to stop", exception);
        } finally {
            try {
                killChannel();
            } finally {
                killServerThread();
            }
        }
    }

    public void stop() {
        this.stop(0);
    }

    public void purgeDisconnected() {
        // Remove timed out limbo connections
        Iterator<Map.Entry<WebSocket, Long>> limbo = limboConnections.entrySet().iterator();

        while (limbo.hasNext()) {
            Map.Entry<WebSocket, Long> entry = limbo.next();

            long connectTime = entry.getValue();
            long timeInLimbo = (System.nanoTime() - connectTime) / 1000000;

            if (timeInLimbo >= PURGE_LIMBO_SECS) {
                limbo.remove();
                entry.getKey().close();
            }
        }

        // Remove timed out clients
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
        scheduler.addTask(clientPurgerTask);

        synchronized (startMonitor) {
            startMonitor.notifyAll();
        }

        System.out.println("Listening on " + getPort());
    }

    public void shutdown() {
        if(clientPurgerTask != null) {
            clientPurgerTask.cancel();
        }

        stop();
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake clientHandshake) {
        limboConnections.put(socket, System.currentTimeMillis());
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        limboConnections.remove(socket);
        Client client = clients.remove(socket);

        if(client == null)
            return;

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
                new IllegalStateException("Expected open or reopen packet, received " + packet).printStackTrace();
                socket.close();
                return;
        }

        if(client == null) {
            client = new Client(UUID.randomUUID(), socket);
        }

        clients.put(socket, client);
        limboConnections.remove(socket);

        client.onConnect(socket);
        client.send(PacketOutSetID.create(client.id));

        game.onConnect(client, isReconnect);
    }

    @Override
    public void onError(WebSocket socket, Exception exception) {
        if(!started) {
            if (startException == null) {
                startException = exception;
            } else {
                startException.addSuppressed(exception);
            }

            synchronized (startMonitor) {
                startMonitor.notifyAll();
            }
            return;
        }

        Client client = clients.get(socket);

        if(socket == null || client == null) {
            game.onError(exception);
        } else {
            game.onError(client, exception);
        }
    }

}
