package net.sothatsit.royalurserver.network;

import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoSocket;
import net.sothatsit.royalurserver.Logging;
import net.sothatsit.royalurserver.RoyalUr;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInOpen;
import net.sothatsit.royalurserver.network.incoming.PacketInReOpen;
import net.sothatsit.royalurserver.network.incoming.PacketReader;
import net.sothatsit.royalurserver.network.outgoing.PacketOutSetID;
import net.sothatsit.royalurserver.scheduler.RepeatingTask;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;
import org.eclipse.jetty.server.handler.HandlerCollection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
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
public class GameServer {

    private static final int PURGE_TIMER_INTERVAL_SECS = 10;
    private static final int PURGE_LIMBO_SECS = 10;

    private final RoyalUr game;
    private final Logger logger;
    private final Scheduler scheduler;

    private final SocketIoServlet servlet;
    private final SocketIoNamespace servletNamespace;

    private final Map<SocketIoSocket, Client> clients;
    private final Map<SocketIoSocket, Long> limboConnections;
    private final Map<UUID, Client> disconnected;
    private final RepeatingTask clientPurgerTask;

    public GameServer(RoyalUr game) {
        Checks.ensureNonNull(game, "game");

        this.game = game;
        this.logger = Logging.getLogger(GameServer.class.getName());
        this.scheduler = new Scheduler(GameServer.class.getName(), 1, TimeUnit.SECONDS);

        this.clients = new ConcurrentHashMap<>();
        this.limboConnections = new ConcurrentHashMap<>();
        this.disconnected = new ConcurrentHashMap<>();
        this.clientPurgerTask = new RepeatingTask(
                "server client purger", this::purgeDisconnected,
                PURGE_TIMER_INTERVAL_SECS, TimeUnit.SECONDS
        );

        this.servlet = new SocketIoServlet(EngineIoServerOptions.ALLOWED_CORS_ORIGIN_ALL);
        this.servletNamespace = servlet.namespace("/");

        servletNamespace.on("connection", this::acceptConnection);
    }

    public void addJettyHandlers(HandlerCollection handlerList) {
        handlerList.addHandler(servlet.createContextHandler());
    }

    public void start() {
        scheduler.start();
        scheduler.addTask(clientPurgerTask);
    }

    public void stop() {
        if(clientPurgerTask != null) {
            clientPurgerTask.cancel();
        }
        scheduler.stop();
    }

    public void purgeDisconnected() {
        // Remove timed out limbo connections
        Iterator<Map.Entry<SocketIoSocket, Long>> limbo = limboConnections.entrySet().iterator();

        while (limbo.hasNext()) {
            Map.Entry<SocketIoSocket, Long> entry = limbo.next();

            long connectTime = entry.getValue();
            long timeInLimbo = (System.nanoTime() - connectTime) / 1000000;

            if (timeInLimbo >= PURGE_LIMBO_SECS) {
                limbo.remove();
                entry.getKey().disconnect(true);
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

    /**
     * This function is called when a new connection is accepted.
     * @param args The arguments describing the new connection.
     */
    private void acceptConnection(Object... args) {
        if (args.length == 0)
            throw new IllegalArgumentException("No arguments provided");

        if (args[0] == null) {
            throw new IllegalArgumentException(
                    "Expected the first argument to be of type " + SocketIoSocket.class + ", but it is null"
            );
        }
        if (!(args[0] instanceof SocketIoSocket)) {
            throw new IllegalArgumentException(
                    "Expected the first argument to be of type " + SocketIoSocket.class +
                            ", but it is of type " + args[0].getClass()
            );
        }

        SocketIoSocket socket = (SocketIoSocket) args[0];
        acceptConnection(socket);
    }

    /**
     * This function is called when a new connection is accepted.
     * @param socket The socket for the connection.
     */
    private void acceptConnection(SocketIoSocket socket) {
        limboConnections.put(socket, System.currentTimeMillis());

        socket.on("message", args -> acceptMessage(socket, args));
        socket.on("disconnect", args -> onDisconnect(socket));
        socket.on("error", args -> {
            System.err.println("ERROR: " + Arrays.toString(args));
        });
    }

    public void onDisconnect(SocketIoSocket socket) {
        limboConnections.remove(socket);
        Client client = clients.remove(socket);
        if(client == null)
            return;

        disconnected.put(client.id, client);

        client.onDisconnect();
        game.onDisconnect(client);
    }

    /**
     * This function is called when a new message is received from a socket.
     * @param args The arguments containing the message.
     */
    private void acceptMessage(SocketIoSocket socket, Object... args) {
        if (args.length == 0)
            throw new IllegalArgumentException("No arguments provided");

        if (args[0] == null) {
            throw new IllegalArgumentException(
                    "Expected the first argument to be of type " + String.class + ", but it is null"
            );
        }
        if (!(args[0] instanceof String)) {
            throw new IllegalArgumentException(
                    "Expected the first argument to be of type " + String.class +
                            ", but it is of type " + args[0].getClass()
            );
        }

        String message = (String) args[0];
        acceptMessage(socket, message);
    }

    /**
     * This function is called when a new connection is accepted.
     * @param socket The socket for the connection.
     */
    private void acceptMessage(SocketIoSocket socket, String message) {
        Client client = clients.get(socket);
        PacketIn packet;
        try {
            PacketReader reader = new PacketReader(message);
            packet = reader.type.newPacket();
            packet.read(reader);
        } catch(Exception exception) {
            String exceptionName = exception.getClass().getSimpleName();
            logger.log(
                    Level.WARNING,
                    exceptionName + " reading packet \"" + message + "\": " + exception.getMessage()
            );
            Client errorClient;
            if (client == null) {
                errorClient = new Client("unknown", UUID.randomUUID(), socket);
            } else {
                errorClient = client;
            }
            errorClient.error("invalid packet");
            return;
        }

        // Check if we need to initialise this client.
        if(client == null) {
            connectClient(socket, packet);
            return;
        }

        try {
            game.onMessage(client, packet);
        } catch(Exception exception) {
            String exceptionName = exception.getClass().getSimpleName();
            logger.log(Level.SEVERE, exceptionName + " handling packet " + packet + " for " + client, exception);
            client.error("internal error");
            return;
        }
    }

    private void connectClient(SocketIoSocket socket, PacketIn packet) {
        Checks.ensureNonNull(socket, "socket");
        Checks.ensureNonNull(packet, "packet");

        Client client;
        int protocolVersion;
        boolean isReconnect = false;

        switch(packet.type) {
            // When a client first connects.
            case OPEN:
                PacketInOpen open = (PacketInOpen) packet;
                client = new Client(open.name, UUID.randomUUID(), socket);
                protocolVersion = open.protocolVersion;
                break;

            // When a client attempts to re-connect.
            case REOPEN:
                PacketInReOpen reopen = (PacketInReOpen) packet;
                client = disconnected.remove(reopen.previousID);
                protocolVersion = reopen.protocolVersion;

                // Treat this as a normal OPEN packet instead.
                if (client == null || client.isTimedOut()) {
                    client = new Client(reopen.name, UUID.randomUUID(), socket);
                } else {
                    isReconnect = true;
                    client.setName(reopen.name);
                }
                break;

            // Uh oh.
            default:
                new Client("unknown", UUID.randomUUID(), socket)
                        .error("Expected open or reopen packet");
                return;
        }

        if (protocolVersion != Client.PROTOCOL_VERSION) {
            client.error("RoyalUr protocol version mismatch, " + protocolVersion + " != " + Client.PROTOCOL_VERSION);
            return;
        }

        clients.put(socket, client);
        limboConnections.remove(socket);

        client.onConnect(socket);
        client.send(new PacketOutSetID(client.id));

        game.onConnect(client, isReconnect);
    }
}
