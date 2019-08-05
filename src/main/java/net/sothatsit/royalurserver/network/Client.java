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

    public static final long DISCONNECT_TIMEOUT = 1000 * 30;

    public final UUID id;

    private WebSocket socket;
    private boolean connected;

    private Time connectTime;
    private Time disconnectTime;

    public Client(UUID id, WebSocket socket) {
        Checks.ensureNonNull(id, "id");
        Checks.ensureNonNull(socket, "socket");

        this.id = id;
        this.connectTime = Time.now();
        this.socket = socket;
    }

    /**
     * @return Whether this client is currently connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return The time this client connected, or null if the client is disconnected.
     */
    public Time getConnectTime() {
        return connectTime;
    }

    /**
     * @return The time this client disconnected, or null if the client is connected.
     */
    public Time getDisconnectTime() {
        return disconnectTime;
    }

    /**
     * @return Whether this client has been disconnected long enough to be removed.
     */
    public boolean isTimedOut() {
        return disconnectTime != null && disconnectTime.getMillisSince() > DISCONNECT_TIMEOUT;
    }

    /**
     * Update this client to indicate that they've just connected through the socket {@param socket}.
     */
    protected void onConnect(WebSocket socket) {
        Checks.ensureNonNull(socket, "socket");

        this.socket = socket;
        this.connected = true;
        this.connectTime = Time.now();
        this.disconnectTime = null;
    }

    /**
     * Update this client to indicate that they've just disconnected.
     */
    protected void onDisconnect() {
        this.socket = null;
        this.connected = false;
        this.connectTime = null;
        this.disconnectTime = Time.now();
    }

    /**
     * Send the error {@param error} to the client, and close their connection.
     */
    public void error(String error) {
        Checks.ensureNonNull(error, "error");
        Checks.ensureState(socket != null, "client is already disconnected");

        send(PacketOutError.create(error));
        socket.close();
    }

    /**
     * Send the packet {@param packet} to the client.
     */
    public void send(PacketOut packet) {
        Checks.ensureNonNull(packet, "packet");
        Checks.ensureState(socket != null, "cannot send packet to disconnected client");

        socket.send(packet.encode());
    }

    @Override
    public String toString() {
        return "Client(" + id.toString().substring(0, 8) + ")";
    }

}
