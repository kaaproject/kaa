/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.plugin.rest;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.kaaproject.kaa.server.plugin.rest.gen.HttpSchemaType;
import org.kaaproject.kaa.server.plugin.rest.gen.KaaRestPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginHttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(PluginHttpServer.class);

    private HttpServer httpServer;

    public void initServer(KaaRestPluginConfig config) {
        try {

            if (HttpSchemaType.HTTPS.equals(config.getHttpSchema())) {

            } else {

            }

            httpServer = new HttpServer();
            httpServer.addListener(new NetworkListener("rest", config.getHost(), config.getPort()));
            httpServer.getServerConfiguration().addHttpHandler(new HttpHandler() {
                @Override
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
            LOG.info("Starting Grizzly HTTP Server...");
            httpServer.start();
        } catch (Exception cause) {
            cause.printStackTrace();
        }
    }

    public void stop() {
        LOG.info("Stopping Grizzly HTTP Server...");
        httpServer.shutdownNow();
    }
}
