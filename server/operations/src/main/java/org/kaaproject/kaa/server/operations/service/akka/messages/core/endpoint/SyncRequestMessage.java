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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint;

import io.netty.channel.ChannelHandlerContext;

import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;

import akka.actor.ActorRef;

/**
 * The Class SyncRequestMessage.
 */
public class SyncRequestMessage extends EndpointAwareMessage {

    /** The command. */
    protected final AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase> command;

    /** The handler uuid. */
    private final String channelId;

    /** The channel context. */
    private final ChannelHandlerContext channelContext;

    /** The type of channel. */
    private final ChannelType channelType;

    /** The request. */
    private final SyncRequest request;

    /**
     * Instantiates a new sync request message.
     *
     * @param appToken
     *            the app token
     * @param key
     *            the key
     * @param request
     *            the request
     * @param originator
     *            the originator
     */
    public SyncRequestMessage(AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase> command, String channelId,
            ChannelHandlerContext channelContext, ChannelType channelType, String appToken, EndpointObjectHash key, SyncRequest request, ActorRef originator) {
        super(appToken, key, originator);
        this.command = command;
        this.channelId = channelId;
        this.channelContext = channelContext;
        this.channelType = channelType;
        this.request = request;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public SyncRequest getRequest() {
        return request;
    }

    public String getChannelId() {
        return channelId;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    public AbstractOperationsCommand<SpecificRecordBase, SpecificRecordBase> getCommand() {
        return command;
    }

    public void update(SyncRequestMessage syncRequest) {
        SyncRequest other = syncRequest.getRequest();
        request.getSyncRequestMetaData().setProfileHash(other.getSyncRequestMetaData().getProfileHash());
        if (other.getConfigurationSyncRequest() != null) {
            request.setConfigurationSyncRequest(other.getConfigurationSyncRequest());
        }
        if (other.getNotificationSyncRequest() != null) {
            request.setNotificationSyncRequest(other.getNotificationSyncRequest());
        }
        if (other.getProfileSyncRequest() != null) {
            request.setProfileSyncRequest(other.getProfileSyncRequest());
        }
        if (other.getUserSyncRequest() != null) {
            request.setUserSyncRequest(other.getUserSyncRequest());
        }
        if (other.getEventSyncRequest() != null) {
            request.setEventSyncRequest(other.getEventSyncRequest());
        }
        if (other.getLogSyncRequest() != null) {
            request.setLogSyncRequest(other.getLogSyncRequest());
        }
    }

    public boolean isValid(TransportType type) {
        switch (type) {
        case EVENT:
            return request.getEventSyncRequest() != null;
        case NOTIFICATION:
            return request.getNotificationSyncRequest() != null;
        case CONFIGURATION:
            return request.getConfigurationSyncRequest() != null;
        case USER:
            return request.getUserSyncRequest() != null;
        case PROFILE:
            return request.getProfileSyncRequest() != null;
        case LOGGING:
            return request.getLogSyncRequest() != null;
        default:
            return false;
        }
    }
}
