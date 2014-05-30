/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.client;

import java.io.IOException;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.bootstrap.OperationsServerInfo;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.transport.HttpOperationsTransport;
import org.kaaproject.kaa.client.update.DefaultUpdateManager;
import org.kaaproject.kaa.client.update.UpdateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link TransportExceptionHandler} implementation.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultTransportExceptionHandler implements TransportExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUpdateManager.class);
    private static final long TIMEOUT = 5000L;

    private final BootstrapManager bootstrapManager;
    private final UpdateManager updateManager;
    private final KaaClientState state;

    public DefaultTransportExceptionHandler(BootstrapManager bootstrapManager, UpdateManager updateManager, KaaClientState state) {
        this.bootstrapManager = bootstrapManager;
        this.updateManager = updateManager;
        this.state = state;
    }

    @Override
    public void onTransportException() {
        OperationsServerInfo server = bootstrapManager.getNextOperationsServer();
        if (server != null) {
            LOG.info("Switching to new endpoint server {}", server.getHostName());
            updateManager.setTransport(new HttpOperationsTransport(server
                    .getHostName(), this.state.getPrivateKey(), this.state
                    .getPublicKey(), server.getKey()));
        }
        try {
            updateManager.failover(TIMEOUT);
        } catch (IOException e) {
            LOG.error("Failed to enter the failover mode", e);
        }
    }

}
