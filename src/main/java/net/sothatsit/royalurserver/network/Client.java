package net.sothatsit.royalurserver.network;

import io.socket.socketio.server.SocketIoSocket;
import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.outgoing.PacketOutError;
import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;

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
    public static final int PROTOCOL_VERSION = 4;

    public static final int MAX_NAME_LENGTH = 12;
    public static final long DISCONNECT_TIMEOUT_MS = 5 * 60 * 1000;

    private String name;
    public final UUID id;

    private SocketIoSocket socket;
    private boolean connected;

    private Time connectTime;
    private Time disconnectTime;

    public Client(String name, UUID id, SocketIoSocket socket) {
        Checks.ensureNonNull(id, "id");
        Checks.ensureNonNull(socket, "socket");

        setName(name);
        this.id = id;
        this.connectTime = Time.now();
        this.socket = socket;
        if (socket == null) {
            this.socket = null;
            this.disconnectTime = Time.now();
        }
    }

    /** @return the name of this player. **/
    public String getName() {
        return name;
    }

    /** Sets the name of this player to {@param name}. **/
    public void setName(String name) {
        this.name = sanitiseName(name);
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
    public void error(String error) {
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
        socket.send(packet.write());
    }

    @Override
    public String toString() {
        return "Client(id=" + id.toString().substring(0, 8) + ", name=\"" + name + "\")";
    }

    /** Sanitises the given name to limit it to the max name length. **/
    public static String sanitiseName(String name) {
        return name.replaceAll("[^\\x00-\\x7F]", "").substring(0, Math.min(name.length(), MAX_NAME_LENGTH));
    }
}
