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

package org.kaaproject.kaa.client.channel.impl.channels;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.FailoverManager;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.ChannelSyncTask;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOperationHttpChannel extends AbstractHttpChannel {
    public static final Logger LOG = LoggerFactory // NOSONAR
            .getLogger(DefaultOperationsChannel.class);

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.UP);
        SUPPORTED_TYPES.put(TransportType.LOGGING, ChannelDirection.UP);
    }

    private static final String CHANNEL_ID = "default_operations_http_channel";

    private class OperationRunnable implements Runnable {

        private final ChannelSyncTask task;

        OperationRunnable(ChannelSyncTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                processTask(task);
                connectionFailed(false);
            } catch (TransportException e) {
                LOG.error("Failed to receive response from the operation {}", e);
                connectionFailed(true, e.getStatus());
            } catch (Exception e) {
                LOG.error("Failed to receive response from the operation {}", e);
                connectionFailed(true);
            }
        }
    }

    public DefaultOperationHttpChannel(AbstractKaaClient client, KaaClientState state, FailoverManager failoverManager) {
        super(client, state, failoverManager);
    }

    private void processTask(ChannelSyncTask task) throws Exception {
        byte[] requestBodyRaw = getMultiplexer().compileRequest(task);
        byte[] decodedResponse = null;
        synchronized (this) {
            LinkedHashMap<String, byte[]> requestEntity = HttpRequestCreator.createOperationHttpRequest(requestBodyRaw, getHttpClient()
                    .getEncoderDecoder());
            byte[] responseDataRaw = getHttpClient().executeHttpRequest("", requestEntity, false);
            decodedResponse = getHttpClient().getEncoderDecoder().decodeData(responseDataRaw);
        }
        getDemultiplexer().processResponse(decodedResponse);
    }

    @Override
    public String getId() {
        return CHANNEL_ID;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.OPERATIONS;
    }

    @Override
    public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    protected Runnable createChannelRunnable(ChannelSyncTask task) {
        return new OperationRunnable(task);
    }

    @Override
    protected String getURLSufix() {
        return "/EP/Sync";
    }
}
