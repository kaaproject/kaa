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

package org.kaaproject.kaa.client.update.listeners;

import java.io.IOException;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.bootstrap.OperationsServerInfo;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.transport.HttpOperationsTransport;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.client.update.UpdateListener;
import org.kaaproject.kaa.client.update.UpdateManager;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redirection updates listener ({@link UpdateListener}.
 */
public class RedirectionUpdateListener implements UpdateListener {
    private static final Logger LOG = LoggerFactory.getLogger(RedirectionUpdateListener.class);

    private final BootstrapManager bootstrapManager;
    private final UpdateManager updateManager;
    private final KaaClientState state;

    public RedirectionUpdateListener(BootstrapManager bootstrapManager, UpdateManager updateManager, KaaClientState state) {
        this.bootstrapManager = bootstrapManager;
        this.updateManager = updateManager;
        this.state = state;
    }

    @Override
    public void onDeltaUpdate(SyncResponse response) throws IOException {
        if (response.getResponseType() == SyncResponseStatus.REDIRECT) {
            RedirectSyncResponse redirectionResponse = response.getRedirectSyncResponse();
            if (redirectionResponse != null) {
                String name = redirectionResponse.getDnsName();
                OperationsServerInfo server = bootstrapManager.getOperationsServerByDnsName(name);
                if (server == null) {
                    try {
                        bootstrapManager.receiveOperationsServerList();
                    } catch (TransportException e) {
                        // TODO: use next bootstrap server
                    }
                    server = bootstrapManager.getOperationsServerByDnsName(name);
                }

                LOG.info("Redirectring to endpoint server {}", server.getHostName());

                updateManager.setTransport(new HttpOperationsTransport(server.getHostName(), state.getPrivateKey(), state.getPublicKey(), server.getKey()));
            }
        }
    }

}
