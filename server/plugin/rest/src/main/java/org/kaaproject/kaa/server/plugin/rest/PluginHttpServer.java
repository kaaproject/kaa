package org.kaaproject.kaa.server.plugin.rest;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfigSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kaaproject.kaa.server.plugin.rest.gen.HttpSchemaType.HTTPS;

public class PluginHttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(PluginHttpServer.class);

    private HttpServer server;

    public void initServer(KaaRestPluginConfigSchema conf) {
        try {
            if (HTTPS.equals(conf.getHttpSchema())) {

            } else {

            }
            server = new HttpServer();
            NetworkListener listener = new NetworkListener("rest", conf.getHost(), conf.getPort());
            server.addListener(listener);
            server.getServerConfiguration().addHttpHandler(new HttpHandler() {
                public void service(Request request, Response response) throws Exception {
                    LOG.debug("Got request {}", request.getContextPath());
                    LOG.debug("Got request {}", request.getParameter("keyHash"));
                }
            }, "/getEndpointStatus");

        } catch (Exception e) {
            LOG.warn("Can't initialize grizzly http server.", e);
        }
    }

    public void start() {
        try {
            LOG.info("Starting grizzly http server...");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        LOG.info("Stop grizzly http server...");
        server.shutdownNow();
    }
}
