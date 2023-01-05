package net.sothatsit.royalurserver.network;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
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
import java.util.EnumSet;

/**
 * Handles the HTTP and WebSocket servers that are used for Socket.io connections.
 */
public class SocketIoServlet {

    public final String[] allowedCORSOrigins;

    protected final EngineIoServer engineIoServer;
    protected final SocketIoServer socketIoServer;

    public SocketIoServlet(String[] allowedCORSOrigins) {
        this.allowedCORSOrigins = allowedCORSOrigins;

        // Construct the Socket.io servers.
        EngineIoServerOptions eioOptions = EngineIoServerOptions.newFromDefault();
        eioOptions.setAllowSyncPolling(false);
        eioOptions.setAllowedCorsOrigins(allowedCORSOrigins);

        engineIoServer = new EngineIoServer(eioOptions);
        socketIoServer = new SocketIoServer(engineIoServer);
    }

    protected ServletContextHandler createContextHandler() {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addFilter(RemoteAddrFilter.class, "/socket.io/*", EnumSet.of(DispatcherType.REQUEST));

        // Handle HTTP requests.
        servletContextHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                if (request.getQueryString() == null) {
                    response.sendError(404, "No resource found");
                    return;
                }
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
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return servletContextHandler;
    }

    public SocketIoNamespace namespace(String namespace) {
        return socketIoServer.namespace(namespace);
    }
}
