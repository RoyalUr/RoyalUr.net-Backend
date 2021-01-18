package net.sothatsit.royalurserver.network;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.outgoing.PacketOutError;
import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;
import org.java_websocket.WebSocket;

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
    public static final int PROTOCOL_VERSION = 2;

    public static final long DISCONNECT_TIMEOUT_MS = 5 * 60 * 1000;
    public static final int MAX_NAME_LENGTH = 12;

    public final UUID id;

    private WebSocket socket;
    private boolean connected;
    private String name = "unknown";

    private Time connectTime;
    private Time disconnectTime;

    public Client(UUID id, WebSocket socket) {
        Checks.ensureNonNull(id, "id");
        Checks.ensureNonNull(socket, "socket");

        this.id = id;
        this.connectTime = Time.now();
        this.socket = socket;
        if (socket == null || socket.isClosed() || socket.isClosing()) {
            this.socket = null;
            this.disconnectTime = Time.now();
        }
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
    protected void onConnect(WebSocket socket) {
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

    /** Set the name of this client to {@param name}. **/
    public void setName(String name) {
        this.name = name.substring(0, Math.min(name.length(), MAX_NAME_LENGTH));
    }

    /** @return the name of this client. **/
    public String getName() {
        return name;
    }

    /** Send the error {@param error} to the client, and close their connection. **/
    public void error(String error) {
        Checks.ensureNonNull(error, "error");
        if (socket == null || socket.isClosing() || socket.isClosed())
            return;

        send(new PacketOutError(error));
        socket.close();
        socket = null;
    }

    /** Send the packet {@param packet} to the client. **/
    public void send(PacketOut packet) {
        Checks.ensureNonNull(packet, "packet");
        Checks.ensureState(
                isConnected() && socket != null && !socket.isClosed() && !socket.isClosing(),
                "cannot send packet to disconnected client"
        );
        socket.send(packet.write());
    }

    /** Try to send the packet {@param packet} to the client, with no error if the packet could not be sent. **/
    public void trySend(PacketOut packet) {
        Checks.ensureNonNull(packet, "packet");
        if (!isConnected() || socket == null || socket.isClosed() || socket.isClosing())
            return;
        send(packet);
    }

    @Override
    public String toString() {
        return "Client(id=" + id.toString().substring(0, 8) + ", name=\"" + name + "\")";
    }

}
