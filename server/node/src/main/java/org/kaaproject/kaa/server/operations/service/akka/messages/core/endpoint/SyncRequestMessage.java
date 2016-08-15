/*
 * Copyright 2014-2016 CyberVision, Inc.
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

import java.util.Arrays;
import java.util.UUID;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.UserClientSync;
import org.kaaproject.kaa.server.transport.channel.ChannelAware;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.Message;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

/**
 * The Class SyncRequestMessage.
 */
public class SyncRequestMessage extends EndpointAwareMessage implements ChannelAware {

    private static final Logger LOG = LoggerFactory.getLogger(SyncRequestMessage.class);

    /** The command. */
    private final Message command;

    /** The request. */
    private final ClientSync request;

    /** The session. */
    private final SessionInfo session;

    /**
     * Instantiates a new sync request message.
     * 
     * @param session           the session
     * @param request           the request
     * @param requestMessage    the request message
     * @param originator        the originator
     */
    public SyncRequestMessage(SessionInfo session, ClientSync request, Message requestMessage, ActorRef originator) {
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
    public ChannelContext getChannelContext() {
        return session.getCtx();
    }

    public SessionInfo getSession() {
        return session;
    }

    public Message getCommand() {
        return command;
    }

    public void updateRequest(ServerSync response) {
        UUID channelUuid = getChannelUuid();
        LOG.debug("[{}] Cleanup profile request", channelUuid);
        request.setProfileSync(null);
        if (request.getUserSync() != null) {
            LOG.debug("[{}] Cleanup user request", channelUuid);
            request.setUserSync(new UserClientSync());
        }
        if (request.getEventSync() != null) {
            LOG.debug("[{}] Cleanup event request", channelUuid);
            request.setEventSync(new EventClientSync());
        }
        if (request.getLogSync() != null) {
            LOG.debug("[{}] Cleanup log request", channelUuid);
            request.getLogSync().setLogEntries(null);
        }
        if (request.getNotificationSync() != null) {
            LOG.debug("[{}] Cleanup/update notification request", channelUuid);
            request.getNotificationSync().setSubscriptionCommands(null);
            request.getNotificationSync().setAcceptedUnicastNotifications(null);
        }
    }

    public ClientSync merge(SyncRequestMessage syncRequest) {
        UUID channelUuid = getChannelUuid();
        ClientSync other = syncRequest.getRequest();
        LOG.trace("[{}] Merging original request {} with new request {}", channelUuid, request, other);
        request.setRequestId(other.getRequestId());
        request.getClientSyncMetaData().setProfileHash(other.getClientSyncMetaData().getProfileHash());
        LOG.debug("[{}] Updated request id and profile hash", channelUuid);
        ClientSync diff = new ClientSync();
        diff.setRequestId(other.getRequestId());
        diff.setClientSyncMetaData(other.getClientSyncMetaData());
        diff.setUseConfigurationRawSchema(other.isUseConfigurationRawSchema());
        if (request.getClientSyncMetaData().getApplicationToken() != null) {
            LOG.debug("Setting application token, as it was null: {}", request.getClientSyncMetaData().getApplicationToken());
            diff.getClientSyncMetaData().setApplicationToken(request.getClientSyncMetaData().getApplicationToken());
        } else {
            LOG.trace("[{}] Application token is null for request", request);
        }
        boolean hasProfileSync = other.getProfileSync() != null;
        if (hasProfileSync) {
            diff.setProfileSync(other.getProfileSync());
            request.setProfileSync(other.getProfileSync());
            LOG.debug("[{}] Updated profile request", channelUuid);
        }
        if (other.getConfigurationSync() != null) {
            ConfigurationClientSync mergedConfigurationClientSync = hasProfileSync || other.isForceConfigurationSync() ? other
                    .getConfigurationSync() : diff(request.getConfigurationSync(), other.getConfigurationSync());
            diff.setConfigurationSync(mergedConfigurationClientSync);
            request.setConfigurationSync(other.getConfigurationSync());
            LOG.debug("[{}] Updated configuration request", channelUuid);
        } else {
            if (hasProfileSync) {
                diff.setConfigurationSync(request.getConfigurationSync());
            }
        }
        if (other.getNotificationSync() != null) {
            NotificationClientSync mergedNotificationClientSync = hasProfileSync || other.isForceNotificationSync() ? other
                    .getNotificationSync() : diff(request.getNotificationSync(), other.getNotificationSync());
            diff.setNotificationSync(mergedNotificationClientSync);
            request.setNotificationSync(other.getNotificationSync());
            LOG.debug("[{}] Updated notification request", channelUuid);
        } else {
            if (hasProfileSync) {
                diff.setNotificationSync(request.getNotificationSync());
            }
        }
        if (other.getUserSync() != null) {
            diff.setUserSync(other.getUserSync());
            request.setUserSync(other.getUserSync());
            LOG.debug("[{}] Updated user request", channelUuid);
        }
        if (other.getEventSync() != null) {
            diff.setEventSync(other.getEventSync());
            request.setEventSync(other.getEventSync());
            LOG.debug("[{}] Updated event request", channelUuid);
        }
        if (other.getLogSync() != null) {
            diff.setLogSync(other.getLogSync());
            request.setLogSync(other.getLogSync());
            LOG.debug("[{}] Updated log request", channelUuid);
        }
        return diff;
    }

    private ConfigurationClientSync diff(ConfigurationClientSync oldRequest, ConfigurationClientSync newRequest) {
        if (oldRequest == null) {
            return newRequest;
        } else {
            if (!Arrays.equals(oldRequest.getConfigurationHash().array(), newRequest.getConfigurationHash().array())) {
                return newRequest;
            } else {
                return null;
            }
        }
    }

    private NotificationClientSync diff(NotificationClientSync oldRequest, NotificationClientSync newRequest) {
        if (oldRequest == null) {
            return newRequest;
        } else {
            if ((newRequest.getAcceptedUnicastNotifications() != null && newRequest.getAcceptedUnicastNotifications().size() > 0)
                    || (newRequest.getSubscriptionCommands() != null && newRequest.getSubscriptionCommands().size() > 0)
                    || (newRequest.getTopicListHash() != oldRequest.getTopicListHash())) {
                return newRequest;
            } else {
                return null;
            }
        }
    }

    public boolean isValid(TransportType type) {
        switch (type) {
        case EVENT:
            return request.getEventSync() != null;
        case NOTIFICATION:
            return request.getNotificationSync() != null;
        case CONFIGURATION:
            return request.getConfigurationSync() != null;
        case USER:
            return request.getUserSync() != null;
        case PROFILE:
            return request.getProfileSync() != null;
        case LOGGING:
            return request.getLogSync() != null;
        default:
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SyncRequestMessage [command=");
        builder.append(command);
        builder.append(", request=");
        builder.append(request);
        builder.append(", session=");
        builder.append(session);
        builder.append("]");
        return builder.toString();
    }
}
