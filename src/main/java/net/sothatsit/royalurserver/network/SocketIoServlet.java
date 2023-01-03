package net.sothatsit.royalurserver.network;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.NativeWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;

/**
 * Handles the HTTP and WebSocket servers that are used
 * to handle Socket.io connections.
 */
public class SocketIoServlet {

    public final InetSocketAddress address;
    public final String[] allowedCORSOrigins;

    protected final Server server;
    protected final EngineIoServer engineIoServer;
    protected final SocketIoServer socketIoServer;

    public SocketIoServlet(InetSocketAddress address, String[] allowedCORSOrigins) {
        this.address = address;
        this.allowedCORSOrigins = allowedCORSOrigins;

        // Construct all the server objects.
        server = new Server(address);

        EngineIoServerOptions eioOptions = EngineIoServerOptions.newFromDefault();
        eioOptions.setAllowSyncPolling(false);
        eioOptions.setAllowedCorsOrigins(allowedCORSOrigins);

        engineIoServer = new EngineIoServer(eioOptions);
        socketIoServer = new SocketIoServer(engineIoServer);

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addFilter(RemoteAddrFilter.class, "/socket.io/*", EnumSet.of(DispatcherType.REQUEST));

        // Handle HTTP requests.
        servletContextHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                engineIoServer.handleRequest(request, response);
            }
        }), "/socket.io/*");

        // Handle WebSocket requests.
        try {
            WebSocketUpgradeFilter.configure(servletContextHandler);
            NativeWebSocketServletContainerInitializer.configure(servletContextHandler, (ctx, config) -> {
                config.addMapping(
                        new ServletPathSpec("/socket.io/*"),
                        (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(engineIoServer)
                );
            });
        } catch (ServletException ex) {
            ex.printStackTrace();
        }

        // Register the Socket.io handlers with the Jetty server.
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] { servletContextHandler });
        server.setHandler(handlerList);
    }

    public SocketIoNamespace namespace(String namespace) {
        return socketIoServer.namespace(namespace);
    }

    public void startServer() throws Exception {
        server.start();
    }

    public void stopServer() throws Exception {
        server.stop();
    }
}
