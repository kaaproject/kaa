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
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBootstrapChannel extends AbstractHttpChannel {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultBootstrapChannel.class);

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.BOOTSTRAP, ChannelDirection.BIDIRECTIONAL);
    }

    private static final String CHANNEL_ID = "default_bootstrap_channel";

    private class BootstrapRunnable implements Runnable {

        @Override
        public void run() {
            try {
                processTypes(SUPPORTED_TYPES);
                connectionFailed(false);
            } catch (Exception e) {
                LOG.error("Failed to receive operation servers list {}", e);
                connectionFailed(true);
            }
        }

    }

    public DefaultBootstrapChannel(AbstractKaaClient client, KaaClientState state) {
        super(client, state);
    }

    private void processTypes(Map<TransportType, ChannelDirection> types) throws Exception {
        byte [] requestBodyRaw = getMultiplexer().compileRequest(types);
        byte[] responseDataRaw = null;
        synchronized (this) {
            LinkedHashMap<String, byte[]> requestEntity = new LinkedHashMap<String, byte[]>(); //NOSONAR
            requestEntity.put(CommonBSConstants.APPLICATION_TOKEN_ATTR_NAME, requestBodyRaw);

            LOG.debug("Going to execute {}", requestEntity);
            responseDataRaw = getHttpClient().executeHttpRequest("", requestEntity, true);
        }
        getDemultiplexer().processResponse(responseDataRaw);
    }

    @Override
    public String getId() {
        return CHANNEL_ID;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.BOOTSTRAP;
    }

    @Override
    public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    protected Runnable createChannelRunnable(
            Map<TransportType, ChannelDirection> typeMap) {
        return new BootstrapRunnable();
    }


}
