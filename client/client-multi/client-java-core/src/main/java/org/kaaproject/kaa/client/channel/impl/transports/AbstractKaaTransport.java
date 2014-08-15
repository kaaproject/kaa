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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaTransport;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKaaTransport implements KaaTransport {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractKaaTransport.class);

    private KaaChannelManager channelManager;

    protected KaaClientState clientState;

    @Override
    public void setChannelManager(KaaChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void setClientState(KaaClientState state) {
        this.clientState = state;
    }

    private KaaDataChannel getChannel(TransportType type) {
        KaaDataChannel result = null;
        if (channelManager != null) {
            result = channelManager.getChannelByTransportType(type);
        }
        if (result == null) {
            LOG.error("Failed to find channel for transport {}", type);
            throw new ChannelRuntimeException("Failed to find channel for transport " + type.toString());
        }
        return result;
    }

    protected void syncByType(TransportType type) {
        syncByType(type, false);
    }

    protected void syncAckByType(TransportType type) {
        syncByType(type, true);
    }

    protected void syncByType(TransportType type, boolean ack) {
        LOG.debug("Lookup channel by type {}", type);
        KaaDataChannel channel = getChannel(type);
        LOG.debug("Going to invoke sync method on channel {}", channel);
        if (channel != null) {
            if(ack){
                channel.syncAck(type);
            }else{
                channel.sync(type);
            }
        }
    }

    protected void syncAll(TransportType type) {
        KaaDataChannel channel = getChannel(type);
        if (channel != null) {
            channel.syncAll();
        }
    }

    @Override
    public void sync() {
        syncByType(getTransportType());
    }

    protected void syncAck() {
        syncAckByType(getTransportType());
    }

    protected void syncAck(SyncResponseStatus status){
        if(status != SyncResponseStatus.NO_DELTA){
            LOG.info("Sending ack due to response status: {}", status);
            syncAck();
        }
    }


    abstract protected TransportType getTransportType();

}
