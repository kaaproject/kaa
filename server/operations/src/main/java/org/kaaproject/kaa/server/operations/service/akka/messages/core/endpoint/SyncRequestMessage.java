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

import java.util.Arrays;
import java.util.UUID;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.protocol.ClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ConfigurationClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.EventClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.NotificationClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.UserClientSync;
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
public class SyncRequestMessage extends EndpointAwareMessage implements ChannelAware {

    private static final Logger LOG = LoggerFactory.getLogger(SyncRequestMessage.class);

    /** The command. */
    private final Request command;

    /** The request. */
    private final ClientSync request;

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
    public SyncRequestMessage(NettySessionInfo session, ClientSync request, Request requestMessage, ActorRef originator) {
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
    public ClientSync getRequest() {
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

    public void updateRequest(ServerSync response) {
        UUID channelUuid = getChannelUuid();
        LOG.debug("[{}] Cleanup profile request", channelUuid);
        request.setProfileSyncRequest(null);
        if (request.getUserSyncRequest() != null) {
            LOG.debug("[{}] Cleanup user request", channelUuid);
            request.setUserSyncRequest(new UserClientSync());
        }
        if (request.getEventSyncRequest() != null) {
            LOG.debug("[{}] Cleanup event request", channelUuid);
            request.setEventSyncRequest(new EventClientSync());
        }
        if (request.getLogSyncRequest() != null) {
            LOG.debug("[{}] Cleanup log request", channelUuid);
            request.getLogSyncRequest().setLogEntries(null);
        }
        if (request.getNotificationSyncRequest() != null) {
            LOG.debug("[{}] Cleanup/update notification request", channelUuid);
            if (response != null && response.getNotificationSyncResponse() != null) {
                request.getNotificationSyncRequest().setAppStateSeqNumber(
                        response.getNotificationSyncResponse().getAppStateSeqNumber());
            }
            request.getNotificationSyncRequest().setSubscriptionCommands(null);
            request.getNotificationSyncRequest().setAcceptedUnicastNotifications(null);
        }
        if (request.getConfigurationSyncRequest() != null) {
            LOG.debug("[{}] Cleanup/update configuration request", channelUuid);
            if (response != null && response.getConfigurationSyncResponse() != null) {
                request.getConfigurationSyncRequest().setAppStateSeqNumber(
                        response.getConfigurationSyncResponse().getAppStateSeqNumber());
            }
        }
    }

    public ClientSync merge(SyncRequestMessage syncRequest) {
        UUID channelUuid = getChannelUuid();
        ClientSync other = syncRequest.getRequest();
        LOG.trace("[{}] Merging original request {} with new request {}", channelUuid, request, other);
        request.setRequestId(other.getRequestId());
        request.getSyncRequestMetaData().setProfileHash(other.getSyncRequestMetaData().getProfileHash());
        LOG.debug("[{}] Updated request id and profile hash", channelUuid);
        ClientSync diff = new ClientSync();
        diff.setRequestId(other.getRequestId());
        diff.setSyncRequestMetaData(other.getSyncRequestMetaData());
        if (other.getConfigurationSyncRequest() != null) {
            diff.setConfigurationSyncRequest(diff(request.getConfigurationSyncRequest(),
                    other.getConfigurationSyncRequest()));
            request.setConfigurationSyncRequest(other.getConfigurationSyncRequest());
            LOG.debug("[{}] Updated configuration request", channelUuid);
        }
        if (other.getNotificationSyncRequest() != null) {
            diff.setNotificationSyncRequest(diff(request.getNotificationSyncRequest(),
                    other.getNotificationSyncRequest()));
            request.setNotificationSyncRequest(other.getNotificationSyncRequest());
            LOG.debug("[{}] Updated notification request", channelUuid);
        }
        if (other.getProfileSyncRequest() != null) {
            diff.setProfileSyncRequest(other.getProfileSyncRequest());
            request.setProfileSyncRequest(other.getProfileSyncRequest());
            LOG.debug("[{}] Updated profile request", channelUuid);
        }
        if (other.getUserSyncRequest() != null) {
            diff.setUserSyncRequest(other.getUserSyncRequest());
            request.setUserSyncRequest(other.getUserSyncRequest());
            LOG.debug("[{}] Updated user request", channelUuid);
        }
        if (other.getEventSyncRequest() != null) {
            diff.setEventSyncRequest(other.getEventSyncRequest());
            request.setEventSyncRequest(other.getEventSyncRequest());
            LOG.debug("[{}] Updated event request", channelUuid);
        }
        if (other.getLogSyncRequest() != null) {
            diff.setLogSyncRequest(other.getLogSyncRequest());
            request.setLogSyncRequest(other.getLogSyncRequest());
            LOG.debug("[{}] Updated log request", channelUuid);
        }
        return diff;
    }

    private NotificationClientSync diff(NotificationClientSync oldRequest, NotificationClientSync newRequest) {
        if (oldRequest == null) {
            return newRequest;
        } else {
            if (oldRequest.getAppStateSeqNumber() < newRequest.getAppStateSeqNumber()
                    || (newRequest.getAcceptedUnicastNotifications() != null && newRequest
                            .getAcceptedUnicastNotifications().size() > 0)
                    || (newRequest.getSubscriptionCommands() != null && newRequest.getSubscriptionCommands().size() > 0)
            // TODO: Add topicListHash comparison
            ) {
                return newRequest;
            } else {
                return null;
            }
        }
    }

    private ConfigurationClientSync diff(ConfigurationClientSync oldRequest, ConfigurationClientSync newRequest) {
        if (oldRequest == null) {
            return newRequest;
        } else {
            if (oldRequest.getAppStateSeqNumber() != newRequest.getAppStateSeqNumber()
                    || Arrays.equals(oldRequest.getConfigurationHash().array(), newRequest.getConfigurationHash()
                            .array())) {
                return newRequest;
            } else {
                return null;
            }
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
