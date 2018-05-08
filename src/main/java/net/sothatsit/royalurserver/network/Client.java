package net.sothatsit.royalurserver.network;

import net.sothatsit.royalurserver.network.outgoing.PacketOut;
import net.sothatsit.royalurserver.network.outgoing.PacketOutError;
import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;
import org.java_websocket.WebSocket;

import java.util.UUID;

public class Client {

    public static final long TIMEOUT = 1000 * 30;

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

    public boolean isConnected() {
        return connected;
    }

    public Time getConnectTime() {
        return connectTime;
    }

    public Time getDisconnectTime() {
        return disconnectTime;
    }

    public boolean isTimedOut() {
        return disconnectTime.getTimeSinceMillis() > TIMEOUT;
    }

    protected void onConnect(WebSocket socket) {
        Checks.ensureNonNull(socket, "socket");

        this.socket = socket;
        this.connected = true;
        this.connectTime = Time.now();
    }

    protected void onDisconnect() {
        this.socket = null;
        this.connected = false;
        this.disconnectTime = Time.now();
    }

    public void error(String error) {
        Checks.ensureNonNull(error, "error");
        Checks.ensureState(socket != null, "client is already disconnected");

        send(PacketOutError.create(error));
        socket.close();
    }

    public void send(PacketOut packet) {
        Checks.ensureNonNull(packet, "packet");
        Checks.ensureState(socket != null, "cannot send packet to disconnected client");

        socket.send(packet.encode());
    }

    @Override
    public String toString() {
        return "client " + id.toString().substring(0, 8);
    }

}
