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
package org.kaaproject.kaa.demo.iotworld.citylights;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.citylights.mappers.SimpleMapperProvider;
import org.kaaproject.kaa.demo.iotworld.citylights.resources.ControllerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerApplication.class);

    private static final KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static KaaClient getKaaClient() {
        return kaaClient;
    }

    public static ScheduledExecutorService getExecutor() {
        return executor;
    }

    public static void main(String[] args) throws IOException {
        LOG.info("Starting kaa client");

        kaaClient.start();

        LOG.info("Starting service");
        
        String host = "localhost";
        int port = 10001;
        
        if(args.length == 2){
            host = args[0];
            port = Integer.valueOf(args[1]);
        }

        final ResourceConfig rc = new ResourceConfig().packages("org.kaaproject.kaa.demo.iotworld.citylights.resources");
        rc.register(JacksonFeature.class);
        rc.register(SimpleMapperProvider.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(buildURL(host, port), rc, false);

        server.start();

        ControllerResource.startSwitchTimer();

        System.in.read();

        kaaClient.stop();

        server.shutdownNow();

        executor.shutdownNow();
    }
    
    private static URI buildURL(String host, int port){
        return URI.create("http://" + host + ":" + port + "/api");
    } 
}
