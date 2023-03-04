package net.royalur.backend.network;

import io.socket.socketio.server.SocketIoSocket;
import net.royalur.backend.RoyalUrNetIdentity;
import net.royalur.backend.util.Checks;
import net.royalur.backend.util.Time;
import net.royalur.backend.network.outgoing.PacketOut;
import net.royalur.backend.network.outgoing.PacketOutError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

/**
 * A client connected to this application.
 *
 * @author Paddy Lamont
 */
public class Client {

    /**
     * Each breaking change to the protocol between
     * the server and the client should result in an
     * increase to the current protocol version.
     */
    public static final int PROTOCOL_VERSION = 5;

    public static final int MAX_NAME_LENGTH = 12;
    public static final long DISCONNECT_TIMEOUT_MS = 5 * 60 * 1000;

    private static final String[] DEFAULT_NAMES = {
            "Panda", "Lion", "Tiger", "Bear", "Shark", "Mittens"
    };

    private String name;
    private final UUID sessionID;

    private SocketIoSocket socket;
    private boolean connected;

    private Time connectTime;
    private Time disconnectTime;

    public Client(String name, UUID sessionID, SocketIoSocket socket) {
        Checks.ensureNonNull(sessionID, "sessionID");
        Checks.ensureNonNull(socket, "socket");

        setName(name);
        this.sessionID = sessionID;
        this.connectTime = Time.now();
        this.socket = socket;
        if (socket == null) {
            this.socket = null;
            this.disconnectTime = Time.now();
        }
    }

    /**
     * Retrieves the session ID of this client.
     * @return The session ID of this client.
     */
    public @Nonnull UUID getSessionID() {
        return sessionID;
    }

    /** @return the name of this player. **/
    public @Nullable String getName() {
        return name;
    }

    /** Sets the name of this player to {@param name}. **/
    public void setName(String name) {
        this.name = sanitiseName(name);
    }

    public RoyalUrNetIdentity getIdentity() {
        return new RoyalUrNetIdentity("session(" + sessionID.toString() + ")", name);
    }

    /** @return Whether this client is currently connected. **/
    public boolean isConnected() {
        return connected;
    }

    /** @return The time this client connected, or null if the client is disconnected. **/
    public Time getConnectTime() {
        return connectTime;
    }

    /** @return The time this client disconnected, or null if the client is connected. **/
    public Time getDisconnectTime() {
        return disconnectTime;
    }

    /** @return Whether this client has been disconnected long enough to be removed. **/
    public boolean isTimedOut() {
        return disconnectTime != null && disconnectTime.getMillisSince() > DISCONNECT_TIMEOUT_MS;
    }

    /** Update this client to indicate that they've just connected through the socket {@param socket}. **/
    protected void onConnect(SocketIoSocket socket) {
        Checks.ensureNonNull(socket, "socket");

        this.socket = socket;
        this.connected = true;
        this.connectTime = Time.now();
        this.disconnectTime = null;
    }

    /** Update this client to indicate that they've just disconnected. **/
    protected void onDisconnect() {
        this.socket = null;
        this.connected = false;
        this.connectTime = null;
        this.disconnectTime = Time.now();
    }

    private boolean isSocketOpen() {
        return connected && socket != null;
    }

    /** Send the error {@param error} to the client, and close their connection. **/
    public void error(@Nonnull String error) {
        Checks.ensureNonNull(error, "error");
        if (!isSocketOpen())
            return;

        trySend(new PacketOutError(error));

        try {
            if (isSocketOpen()) {
                socket.disconnect(true);
            }
        } catch (Exception e) {
            new RuntimeException("Error closing socket", e).printStackTrace();
        } finally {
            socket = null;
        }
    }

    /** Try to send the packet {@param packet} to the client, with no error if the packet could not be sent. **/
    public void trySend(PacketOut packet) {
        Checks.ensureNonNull(packet, "packet");
        if (!isSocketOpen())
            return;

        try {
            send(packet);
        } catch (Exception e) {
            new RuntimeException("Error sending packet", e).printStackTrace();
        }
    }

    /** Send the packet {@param packet} to the client. **/
    public void send(PacketOut packet) {
        Checks.ensureNonNull(packet, "packet");
        Checks.ensureState(isSocketOpen(), "cannot send packet to disconnected client");
        socket.send("message", packet.write());
    }

    @Override
    public String toString() {
        return "Client(id=" + sessionID.toString().substring(0, 8) + ", name=\"" + name + "\")";
    }

    /** Sanitises the given name to limit it to the max name length. **/
    public static String sanitiseName(String name) {
        name = name.replaceAll("[^\\x00-\\x7F]", "").trim();
        if (name.isEmpty()) {
            // Pick a random name.
            name = DEFAULT_NAMES[new Random().nextInt(DEFAULT_NAMES.length)];
        }
        return name.substring(0, Math.min(name.length(), MAX_NAME_LENGTH));
    }
}
