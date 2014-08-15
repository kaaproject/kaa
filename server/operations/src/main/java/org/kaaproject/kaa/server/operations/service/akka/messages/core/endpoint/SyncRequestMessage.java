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

import java.util.UUID;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.ChannelAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.Request;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

/**
 * The Class SyncRequestMessage.
 */
public class SyncRequestMessage extends EndpointAwareMessage implements ChannelAware{

    private static final Logger LOG = LoggerFactory.getLogger(SyncRequestMessage.class);

    /** The command. */
    private final Request command;

    /** The request. */
    private final SyncRequest request;

    /** The session. */
    private final NettySessionInfo session;

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
    public SyncRequestMessage(NettySessionInfo session, SyncRequest request, Request requestMessage, ActorRef originator) {
        super(session.getApplicationToken(), session.getKey(), originator);
        this.command = requestMessage;
        this.request = request;
        this.session = session;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public SyncRequest getRequest() {
        return request;
    }

    @Override
    public UUID getChannelUuid() {
        return session.getUuid();
    }

    @Override
    public ChannelType getChannelType() {
        return session.getChannelType();
    }

    @Override
    public ChannelHandlerContext getChannelContext() {
        return session.getCtx();
    }

    public NettySessionInfo getSession() {
        return session;
    }

    public Request getCommand() {
        return command;
    }

    public void cleanRequest() {
        UUID channelUuid = getChannelUuid();
        LOG.debug("[{}] Cleanup profile request", channelUuid);
        request.setProfileSyncRequest(null);
        if(request.getUserSyncRequest() != null){
            LOG.debug("[{}] Cleanup user request", channelUuid);
            request.setUserSyncRequest(new UserSyncRequest());
        }
        if(request.getEventSyncRequest() != null){
            LOG.debug("[{}] Cleanup event request", channelUuid);
            request.setEventSyncRequest(new EventSyncRequest());
        }
        if(request.getLogSyncRequest() != null){
            LOG.debug("[{}] Cleanup log request", channelUuid);
            request.getLogSyncRequest().setLogEntries(null);
        }
        if(request.getNotificationSyncRequest() != null){
            LOG.debug("[{}] Cleanup notification request", channelUuid);
            request.getNotificationSyncRequest().setSubscriptionCommands(null);
            request.getNotificationSyncRequest().setAcceptedUnicastNotifications(null);
        }
    }

    public void update(SyncRequestMessage syncRequest) {
        UUID channelUuid = getChannelUuid();
        SyncRequest other = syncRequest.getRequest();
        request.setRequestId(other.getRequestId());
        request.getSyncRequestMetaData().setProfileHash(other.getSyncRequestMetaData().getProfileHash());
        LOG.debug("[{}] Updated request id and profile hash", channelUuid);
        if (other.getConfigurationSyncRequest() != null) {
            request.setConfigurationSyncRequest(other.getConfigurationSyncRequest());
            LOG.debug("[{}] Updated configuration request", channelUuid);
        }
        if (other.getNotificationSyncRequest() != null) {
            request.setNotificationSyncRequest(other.getNotificationSyncRequest());
            LOG.debug("[{}] Updated notification request", channelUuid);
        }
        if (other.getProfileSyncRequest() != null) {
            request.setProfileSyncRequest(other.getProfileSyncRequest());
            LOG.debug("[{}] Updated profile request", channelUuid);
        }
        if (other.getUserSyncRequest() != null) {
            request.setUserSyncRequest(other.getUserSyncRequest());
            LOG.debug("[{}] Updated user request", channelUuid);
        }
        if (other.getEventSyncRequest() != null) {
            request.setEventSyncRequest(other.getEventSyncRequest());
            LOG.debug("[{}] Updated event request", channelUuid);
        }
        if (other.getLogSyncRequest() != null) {
            request.setLogSyncRequest(other.getLogSyncRequest());
            LOG.debug("[{}] Updated log request", channelUuid);
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
