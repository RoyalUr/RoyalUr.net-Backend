package net.sothatsit.royalurserver.network;

import net.sothatsit.royalurserver.RoyalUr;
import net.sothatsit.royalurserver.ssl.KeyInfo;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.annotation.Nullable;

/**
 * Manages the Jetty server that is used by the Royal Game of Ur backend.
 */
public class RoyalUrServer {

    private static final int SECURE_PORT = 9113;
    private static final int WEB_PORT = 9112;

    private final boolean usingSSL;
    private final Server server;
    private final GameServer gameServer;

    private final @Nullable SslContextFactory.Server sslContextFactory;
    private final @Nullable SslConnectionFactory tls;

    public RoyalUrServer(RoyalUr game, @Nullable KeyInfo key) {
        this.usingSSL = key != null;
        this.server = new Server();
        this.gameServer = new GameServer(game);

        // Configure the HttpConfiguration for the clear-text connector.
        HttpConfiguration httpConfig = new HttpConfiguration();
        if (usingSSL) {
            httpConfig.addCustomizer(new SecureRequestCustomizer());
        }
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(SECURE_PORT);

        // The clear-text connector.
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(WEB_PORT);
        server.addConnector(connector);

        ConstraintSecurityHandler securityHandler = null;
        if (usingSSL) {
            // Configure the HttpConfiguration for the encrypted connector.
            HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
            // Add the SecureRequestCustomizer because we are using TLS.
            httpConfig.addCustomizer(new SecureRequestCustomizer());
            HttpConnectionFactory https11 = new HttpConnectionFactory(httpsConfig);

            // Configure the SslContextFactory with the keyStore information.
            sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStore(key.keyStore);
            sslContextFactory.setKeyStorePassword(key.password);
            tls = new SslConnectionFactory(sslContextFactory, https11.getProtocol());

            // The encrypted connector.
            ServerConnector secureConnector = new ServerConnector(server, tls, https11);
            secureConnector.setPort(SECURE_PORT);
            server.addConnector(secureConnector);

            // Add a constraint so that all HTTP requests return a 403 error
            securityHandler = new ConstraintSecurityHandler();
            Constraint constraint = new Constraint();
            constraint.setDataConstraint(Constraint.DC_CONFIDENTIAL);

            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setPathSpec( "/*" );
            mapping.setConstraint(constraint);
            securityHandler.addConstraintMapping(mapping);
        } else {
            sslContextFactory = null;
            tls = null;
        }

        // Add the request handlers.
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        if (usingSSL) {
            // Redirects insecure HTTP requests to HTTPS.
            handlers.addHandler(new SecuredRedirectHandler());
        }
        gameServer.addJettyHandlers(handlers);
        handlers.addHandler(new DefaultHandler());

        if (usingSSL) {
            securityHandler.setHandler(handlers);
            server.setHandler(securityHandler);
        } else {
            server.setHandler(handlers);
        }
    }

    public void reloadSSL(KeyInfo keyStore) {
        if (!usingSSL || sslContextFactory == null)
            throw new IllegalStateException("This server has not been set up to use SSL");

        sslContextFactory.setKeyStore(keyStore.keyStore);
        sslContextFactory.setKeyStorePassword(keyStore.password);
        try {
            sslContextFactory.reload(ignored -> {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            server.start();
            gameServer.start();
            System.out.println("Started server" + (usingSSL ? " using SSL Encryption" : ""));
        } catch (Exception e) {
            throw new RuntimeException("Error starting server", e);
        }
    }

    public void stop() {
        try {
            server.stop();
            gameServer.stop();
        } catch (Exception e) {
            throw new RuntimeException("Error stopping server", e);
        }
    }
}
