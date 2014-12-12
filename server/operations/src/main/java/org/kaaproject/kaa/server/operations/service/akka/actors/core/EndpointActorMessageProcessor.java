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
package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventSequenceNumberResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryStatus;
import org.kaaproject.kaa.common.endpoint.gen.LogEntry;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.ChannelMap.ChannelMetaData;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointStopMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ActorTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ChannelTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.RequestTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.TimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicRegistrationRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage.EventDeliveryStatus;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserAttachMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDetachMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.ChannelAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SyncStatistics;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettySessionResponseMessage;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class EndpointActorMessageProcessor {

    private static final int ENDPOINT_ACTOR_INACTIVITY_TIMEOUT = 600 * 1000;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EndpointActorMessageProcessor.class);

    /** The operations service. */
    private final OperationsService operationsService;

    /** The map of channel-request-response entities. */
    private final ChannelMap channelMap;

    /** The app token. */
    private final String appToken;

    /** The key. */
    private final EndpointObjectHash key;

    /** The actor key. */
    private final String actorKey;

    /** The actor key. */
    private final String endpointKey;

    private final Map<String, LogDeliveryMessage> logUploadResponseMap;

    /** The sync time. */
    private long syncTime;

    private long lastActivityTime;

    private boolean userRegistrationRequestSent;

    private String userId;

    private int processedEventSeqNum = Integer.MIN_VALUE;

    private EndpointProfileDto endpointProfile;

    protected EndpointActorMessageProcessor(OperationsService operationsService, String appToken, EndpointObjectHash key, String actorKey) {
        super();
        this.operationsService = operationsService;
        this.appToken = appToken;
        this.key = key;
        this.actorKey = actorKey;
        this.endpointKey = Base64Util.encode(key.getData());
        this.channelMap = new ChannelMap(this.endpointKey, this.actorKey);
        this.logUploadResponseMap = new HashMap<>();
    }

    public void processEndpointSync(ActorContext context, SyncRequestMessage message) {
        sync(context, message);
    }

    public void processEndpointEventReceiveMessage(ActorContext context, EndpointEventReceiveMessage message) {
        EndpointEventDeliveryMessage response;
        List<ChannelMetaData> eventChannels = channelMap.getByTransportType(TransportType.EVENT);
        if (!eventChannels.isEmpty()) {
            for (ChannelMetaData eventChannel : eventChannels) {
                addEventsAndReply(context, eventChannel, message);
            }
            response = new EndpointEventDeliveryMessage(message, EventDeliveryStatus.SUCCESS);
        } else {
            LOG.debug("[{}] Message ignored due to no channel contexts registered for events", actorKey, message);
            response = new EndpointEventDeliveryMessage(message, EventDeliveryStatus.FAILURE);
            userRegistrationRequestSent = false;
        }
        tellParent(context, response);
    }

    protected void tellParent(ActorContext context, Object response) {
        context.parent().tell(response, context.self());
    }

    public void processThriftNotification(ActorContext context, ThriftNotificationMessage message) {
        Set<ChannelMetaData> channels = new HashSet<>();

        channels.addAll(channelMap.getByTransportType(TransportType.CONFIGURATION));
        channels.addAll(channelMap.getByTransportType(TransportType.NOTIFICATION));

        LOG.debug("[{}][{}] Processing thrift norification for {} channels", endpointKey, actorKey, channels.size());

        for (ChannelMetaData channel : channels) {
            SyncRequest originalRequest = channel.getRequestMessage().getRequest();
            SyncResponse syncResponse = channel.getResponseHolder().getResponse();

            SyncRequest newRequest = new SyncRequest();
            newRequest.setRequestId(originalRequest.getRequestId());
            newRequest.setSyncRequestMetaData(originalRequest.getSyncRequestMetaData());
            if (originalRequest.getConfigurationSyncRequest() != null) {
                ConfigurationSyncRequest configurationSyncRequest = originalRequest.getConfigurationSyncRequest();
                if (syncResponse.getConfigurationSyncResponse() != null) {
                    int newSeqNumber = syncResponse.getConfigurationSyncResponse().getAppStateSeqNumber();
                    LOG.debug("[{}][{}] Change original configuration request {} appSeqNumber from {} to {}", endpointKey, actorKey,
                            originalRequest, configurationSyncRequest.getAppStateSeqNumber(), newSeqNumber);
                    configurationSyncRequest.setAppStateSeqNumber(newSeqNumber);
                }
                newRequest.setConfigurationSyncRequest(configurationSyncRequest);
                originalRequest.setConfigurationSyncRequest(null);
            }
            if (originalRequest.getNotificationSyncRequest() != null) {
                NotificationSyncRequest notificationSyncRequest = originalRequest.getNotificationSyncRequest();
                if (syncResponse.getNotificationSyncResponse() != null) {
                    int newSeqNumber = syncResponse.getNotificationSyncResponse().getAppStateSeqNumber();
                    LOG.debug("[{}][{}] Change original notification request {} appSeqNumber from {} to {}", endpointKey, actorKey,
                            originalRequest, notificationSyncRequest.getAppStateSeqNumber(), newSeqNumber);
                    notificationSyncRequest.setAppStateSeqNumber(newSeqNumber);
                }
                newRequest.setNotificationSyncRequest(notificationSyncRequest);
                originalRequest.setNotificationSyncRequest(null);
            }
            LOG.debug("[{}][{}] Processing request {}", endpointKey, actorKey, originalRequest);
            sync(context, new SyncRequestMessage(channel.getRequestMessage().getSession(), newRequest, channel.getRequestMessage()
                    .getCommand(), channel.getRequestMessage().getOriginator()));
        }
    }

    public void processNotification(ActorContext context, NotificationMessage message) {
        LOG.debug("[{}][{}] Processing notification {}", endpointKey, actorKey, message);
        List<ChannelMetaData> channels = channelMap.getByTransportType(TransportType.NOTIFICATION);
        for (ChannelMetaData channel : channels) {
            LOG.debug("[{}][{}] processing channel {} and response {}", endpointKey, actorKey, channel, channel.getResponseHolder()
                    .getResponse());
            SyncResponse syncResponse = operationsService.updateSyncResponse(channel.getResponseHolder().getResponse(),
                    message.getNotifications(), message.getUnicastNotificationId());
            if (syncResponse != null) {
                LOG.debug("[{}][{}] processed channel {} and response {}", endpointKey, actorKey, channel, syncResponse);
                sendReply(context, channel.getRequestMessage(), syncResponse);
                if (!channel.getType().isAsync()) {
                    channelMap.removeChannel(channel);
                }
            }
        }
    }

    public void processRequestTimeoutMessage(ActorContext context, RequestTimeoutMessage message) {
        ChannelMetaData channel = channelMap.getByRequestId(message.getRequestId());
        if (channel != null) {
            SyncResponseHolder response = channel.getResponseHolder();
            sendReply(context, channel.getRequestMessage(), response.getResponse());
            if (!channel.getType().isAsync()) {
                channelMap.removeChannel(channel);
            }
        } else {
            LOG.debug("[{}][{}] Failed to find request by id [{}].", endpointKey, actorKey, message.getRequestId());
        }
    }

    public void processActorTimeoutMessage(ActorContext context, ActorTimeoutMessage message) {
        if (lastActivityTime <= message.getLastActivityTime()) {
            LOG.debug("[{}][{}] Request stop of endpoint actor due to inactivity timeout", endpointKey, actorKey);
            tellParent(context, new EndpointStopMessage(key, actorKey, context.self()));
        }
    }

    private void sync(ActorContext context, SyncRequestMessage requestMessage) {
        try {
            long start = lastActivityTime = System.currentTimeMillis();

            ChannelMetaData channel = initChannel(context, requestMessage);

            SyncRequest request;
            if (channel.getType().isAsync()) {
                if (channel.isFirstRequest()) {
                    request = channel.getRequestMessage().getRequest();
                } else {
                    LOG.debug("[{}][{}] Updating request for async channel {}", endpointKey, actorKey, channel);
                    request = channel.mergeRequest(requestMessage);
                    LOG.trace("[{}][{}] Updated request for async channel {} : {}", endpointKey, actorKey, channel, request);
                }
            } else {
                request = channel.getRequestMessage().getRequest();
            }

            ChannelType channelType = channel.getType();
            LOG.debug("[{}][{}] Processing sync request {} from {} channel [{}]", endpointKey, actorKey, request, channelType,
                    requestMessage.getChannelUuid());

            SyncResponseHolder responseHolder = operationsService.sync(request, endpointProfile);

            endpointProfile = responseHolder.getEndpointProfile();

            if (endpointProfile != null) {
                processLogUpload(context, request, responseHolder);
                processEvents(context, request, responseHolder);
                processUserAttachDetachResults(context, request, responseHolder);
            } else {
                LOG.warn("[{}][{}] Endpoint profile is not set after request processing!", endpointKey, actorKey);
            }

            this.syncTime += System.currentTimeMillis() - start;
            LOG.debug("[{}][{}] SyncResponseHolder {}", endpointKey, actorKey, responseHolder);

            if (channelType.isAsync()) {
                LOG.debug("[{}][{}] Adding async request from channel [{}] to map ", endpointKey, actorKey, requestMessage.getChannelUuid());
                channel.update(responseHolder);
                subscribeToTopics(context, responseHolder);
                sendReply(context, requestMessage, responseHolder.getResponse());
            } else {
                if (channelType.isLongPoll() && !responseHolder.requireImmediateReply()) {
                    LOG.debug("[{}][{}] Adding long poll request from channel [{}] to map ", endpointKey, actorKey,
                            requestMessage.getChannelUuid());
                    channel.update(responseHolder);
                    subscribeToTopics(context, responseHolder);
                    scheduleTimeoutMessage(context, requestMessage.getChannelUuid(), getDelay(requestMessage, start));
                } else {
                    sendReply(context, requestMessage, responseHolder.getResponse());
                    channelMap.removeChannel(channel);
                }
            }
        } catch (GetDeltaException e) {
            LOG.error("[{}][{}] processEndpointRequest", endpointKey, actorKey, e);
            sendReply(context, requestMessage, e);
        }
    }

    private void processEvents(ActorContext context, SyncRequest request, SyncResponseHolder responseHolder) {
        if (isValidForEvents(endpointProfile)) {
            updateUserConnection(context);
            if (request.getEventSyncRequest() != null) {
                EventSyncRequest eventRequest = request.getEventSyncRequest();
                processSeqNumber(eventRequest, responseHolder);
                sendEventsIfPresent(context, eventRequest);
            }
        } else {
            LOG.debug(
                    "[{}][{}] Endpoint profile is not valid for send/receive events. Either no assigned user or no event families in sdk",
                    endpointKey, actorKey);
        }
    }

    private void processSeqNumber(EventSyncRequest request, SyncResponseHolder responseHolder) {
        if (request.getEventSequenceNumberRequest() != null) {
            EventSyncResponse response = responseHolder.getResponse().getEventSyncResponse();
            if (response == null) {
                response = new EventSyncResponse();
                responseHolder.getResponse().setEventSyncResponse(response);
            }
            response.setEventSequenceNumberResponse(new EventSequenceNumberResponse(Math.max(processedEventSeqNum, 0)));
        }
    }

    private void updateUserConnection(ActorContext context) {
        if (userId != null && !userId.equals(endpointProfile.getEndpointUserId())) {
            sendDisconnectFromOldUser(context, endpointProfile);
            userRegistrationRequestSent = false;
        }
        if (!userRegistrationRequestSent) {
            userId = endpointProfile.getEndpointUserId();
            sendConnectToNewUser(context, endpointProfile);
            userRegistrationRequestSent = true;
        } else {
            LOG.trace("[{}][{}] User registration request is already sent.", endpointKey, actorKey);
        }
    }

    private void processLogUpload(ActorContext context, SyncRequest syncRequest, SyncResponseHolder responseHolder) {
        LogSyncRequest request = syncRequest.getLogSyncRequest();
        if (request != null) {
            if (request.getLogEntries() != null && request.getLogEntries().size() > 0) {
                LOG.debug("[{}][{}] Processing log upload request {}", endpointKey, actorKey, request.getLogEntries().size());
                LogEventPack logPack = new LogEventPack();
                logPack.setDateCreated(System.currentTimeMillis());
                logPack.setEndpointKey(Base64Util.encode(key.getData()));
                List<LogEvent> logEvents = new ArrayList<>(request.getLogEntries().size());
                for (LogEntry logEntry : request.getLogEntries()) {
                    LogEvent logEvent = new LogEvent();
                    logEvent.setLogData(logEntry.getData().array());
                    logEvents.add(logEvent);
                }
                logPack.setEvents(logEvents);
                logPack.setLogSchemaVersion(responseHolder.getEndpointProfile().getLogSchemaVersion());
                context.parent().tell(new LogEventPackMessage(request.getRequestId(), context.self(), logPack), context.self());
            }
            if (logUploadResponseMap.size() > 0) {
                responseHolder.getResponse().setLogSyncResponse(toLogDeliveryStatus());
                logUploadResponseMap.clear();
            }
        }
    }

    private void sendConnectToNewUser(ActorContext context, EndpointProfileDto endpointProfile) {
        List<EventClassFamilyVersion> ecfVersions = convertToECFVersions(endpointProfile.getEcfVersionStates());
        EndpointUserConnectMessage userRegistrationMessage = new EndpointUserConnectMessage(userId, key, ecfVersions, appToken,
                context.self());
        LOG.debug("[{}][{}] Sending user registration request {}", endpointKey, actorKey, userRegistrationMessage);
        context.parent().tell(userRegistrationMessage, context.self());
    }

    private void sendDisconnectFromOldUser(ActorContext context, EndpointProfileDto endpointProfile) {
        LOG.debug("[{}][{}] Detected user change from [{}] to [{}]", endpointKey, actorKey, userId, endpointProfile.getEndpointUserId());
        EndpointUserDisconnectMessage userDisconnectMessage = new EndpointUserDisconnectMessage(userId, key, appToken, context.self());
        context.parent().tell(userDisconnectMessage, context.self());
    }

    private long getDelay(SyncRequestMessage requestMessage, long start) {
        long delay = requestMessage.getRequest().getSyncRequestMetaData().getTimeout() - (System.currentTimeMillis() - start);
        return delay;
    }

    private ChannelMetaData initChannel(ActorContext context, SyncRequestMessage requestMessage) {
        ChannelMetaData channel = channelMap.getById(requestMessage.getChannelUuid());
        if (channel == null) {
            channel = new ChannelMetaData(requestMessage);

            if (!channel.getType().isAsync() && channel.getType().isLongPoll()) {
                LOG.debug("[{}][{}] Received request using long poll channel.", endpointKey, actorKey);
                // Probably old long poll channels lost connection. Sending
                // reply to them just in case
                List<ChannelMetaData> channels = channelMap.getByTransportType(TransportType.EVENT);
                for (ChannelMetaData oldChannel : channels) {
                    if (!oldChannel.getType().isAsync() && channel.getType().isLongPoll()) {
                        LOG.debug("[{}][{}] Closing old long poll channel [{}]", endpointKey, actorKey, oldChannel.getId());
                        sendReply(context, oldChannel.getRequestMessage(), oldChannel.getResponseHolder().getResponse());
                        channelMap.removeChannel(oldChannel);
                    }
                }
            }

            long time = System.currentTimeMillis();

            channel.setLastActivityTime(time);

            if (channel.getType().isAsync() && channel.getKeepAlive() > 0) {
                scheduleKeepAliveCheck(context, channel);
            }

            channelMap.addChannel(channel);
        }
        return channel;
    }

    private void scheduleKeepAliveCheck(ActorContext context, ChannelMetaData channel) {
        TimeoutMessage message = new ChannelTimeoutMessage(channel.getId(), channel.getLastActivityTime());
        LOG.debug("Scheduling channel timeout message: {} to timout in {}", message, channel.getKeepAlive() * 1000);
        scheduleTimeoutMessage(context, message, channel.getKeepAlive() * 1000);
    }

    private void processUserAttachDetachResults(ActorContext context, SyncRequest request, SyncResponseHolder responseHolder) {
        if (responseHolder.getResponse().getUserSyncResponse() != null) {
            List<EndpointAttachResponse> attachResponses = responseHolder.getResponse().getUserSyncResponse().getEndpointAttachResponses();
            if (attachResponses != null && !attachResponses.isEmpty()) {
                resetEventSeqNumber();
                for (EndpointAttachResponse response : attachResponses) {
                    if (response.getResult() != SyncResponseResultType.SUCCESS) {
                        LOG.debug("[{}][{}] Skipped unsuccessful attach response [{}]", endpointKey, actorKey, response.getRequestId());
                        continue;
                    }
                    EndpointUserAttachMessage attachMessage = new EndpointUserAttachMessage(EndpointObjectHash.fromBytes(Base64Util
                            .decode(response.getEndpointKeyHash())), userId, endpointKey);
                    context.parent().tell(attachMessage, context.self());
                    LOG.debug("[{}][{}] Notification to attached endpoint [{}] sent", endpointKey, actorKey, response.getEndpointKeyHash());
                }
            }

            List<EndpointDetachRequest> detachRequests = request.getUserSyncRequest() == null ? null : request.getUserSyncRequest()
                    .getEndpointDetachRequests();
            if (detachRequests != null && !detachRequests.isEmpty()) {
                resetEventSeqNumber();
                for (EndpointDetachRequest detachRequest : detachRequests) {
                    for (EndpointDetachResponse detachResponse : responseHolder.getResponse().getUserSyncResponse()
                            .getEndpointDetachResponses()) {
                        if (detachRequest.getRequestId().equals(detachResponse.getRequestId())) {
                            if (detachResponse.getResult() != SyncResponseResultType.SUCCESS) {
                                LOG.debug("[{}][{}] Skipped unsuccessful detach response [{}]", endpointKey, actorKey,
                                        detachResponse.getRequestId());
                                continue;
                            }
                            EndpointUserDetachMessage attachMessage = new EndpointUserDetachMessage(EndpointObjectHash.fromBytes(Base64Util
                                    .decode(detachRequest.getEndpointKeyHash())), userId, endpointKey);
                            context.parent().tell(attachMessage, context.self());
                            LOG.debug("[{}][{}] Notification to detached endpoint [{}] sent", endpointKey, actorKey,
                                    detachRequest.getEndpointKeyHash());
                        }
                    }
                }
            }
        }
    }

    private void resetEventSeqNumber() {
        processedEventSeqNum = Integer.MIN_VALUE;
    }

    protected void scheduleActorTimeout(ActorContext context) {
        if (channelMap.isEmpty()) {
            scheduleTimeoutMessage(context, new ActorTimeoutMessage(lastActivityTime), ENDPOINT_ACTOR_INACTIVITY_TIMEOUT);
        }
    }

    /**
     * Subscribe to topics.
     * 
     * @param response
     *            the response
     */
    private void subscribeToTopics(ActorContext context, SyncResponseHolder response) {
        for (Entry<String, Integer> entry : response.getSubscriptionStates().entrySet()) {
            TopicRegistrationRequestMessage topicSubscriptionMessage = new TopicRegistrationRequestMessage(entry.getKey(),
                    entry.getValue(), response.getSystemNfVersion(), response.getUserNfVersion(), appToken, key, context.self());
            context.parent().tell(topicSubscriptionMessage, context.self());
        }
    }

    private void scheduleTimeoutMessage(ActorContext context, UUID requestId, long delay) {
        scheduleTimeoutMessage(context, new RequestTimeoutMessage(requestId), delay);
    }

    private void scheduleTimeoutMessage(ActorContext context, TimeoutMessage message, long delay) {
        context.system().scheduler()
                .scheduleOnce(Duration.create(delay, TimeUnit.MILLISECONDS), context.self(), message, context.dispatcher(), context.self());
    }

    private void addEventsAndReply(ActorContext context, ChannelMetaData channel, EndpointEventReceiveMessage message) {
        SyncRequestMessage pendingRequest = channel.getRequestMessage();
        SyncResponseHolder pendingResponse = channel.getResponseHolder();

        EventSyncResponse eventResponse = pendingResponse.getResponse().getEventSyncResponse();
        if (eventResponse == null) {
            eventResponse = new EventSyncResponse();
            pendingResponse.getResponse().setEventSyncResponse(eventResponse);
        }

        eventResponse.setEvents(message.getEvents());
        sendReply(context, pendingRequest, pendingResponse.getResponse());
        if (!channel.getType().isAsync()) {
            channelMap.removeChannel(channel);
        }
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, SyncResponse syncResponse) {
        sendReply(context, request, null, syncResponse);
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, GetDeltaException e) {
        sendReply(context, request, e, null);
    }

    /**
     * Send reply.
     * 
     * @param pendingRequest
     *            the pending request
     * @param syncResponse
     *            the sync response
     */
    private void sendReply(ActorContext context, SyncRequestMessage request, GetDeltaException e, SyncResponse syncResponse) {
        LOG.debug("[{}] response: {}", actorKey, syncResponse);

        SyncStatistics stats = request.getCommand().getSyncStatistics();
        if (stats != null) {
            stats.reportSyncTime(syncTime);
        }

        SyncResponse copy = deepCopy(syncResponse);

        NettySessionResponseMessage response = new NettySessionResponseMessage(request.getSession(), copy, request.getCommand()
                .getResponseBuilder(), request.getCommand().getErrorBuilder());

        tellActor(context, request.getOriginator(), response);
        scheduleActorTimeout(context);
    }

    private SyncResponse deepCopy(SyncResponse source) {
        if (source == null) {
            return null;
        }
        SyncResponse copy = new SyncResponse();
        copy.setRequestId(source.getRequestId());
        copy.setStatus(source.getStatus());
        copy.setUserSyncResponse(deepCopy(source.getUserSyncResponse()));
        copy.setRedirectSyncResponse(deepCopy(source.getRedirectSyncResponse()));
        copy.setProfileSyncResponse(deepCopy(source.getProfileSyncResponse()));
        copy.setNotificationSyncResponse(deepCopy(source.getNotificationSyncResponse()));
        copy.setLogSyncResponse(deepCopy(source.getLogSyncResponse()));
        copy.setEventSyncResponse(deepCopy(source.getEventSyncResponse()));
        copy.setConfigurationSyncResponse(deepCopy(source.getConfigurationSyncResponse()));
        return copy;
    }

    private ConfigurationSyncResponse deepCopy(ConfigurationSyncResponse source) {
        if (source == null) {
            return null;
        }
        ConfigurationSyncResponse copy = new ConfigurationSyncResponse();
        copy.setAppStateSeqNumber(source.getAppStateSeqNumber());
        copy.setResponseStatus(source.getResponseStatus());
        copy.setConfDeltaBody(source.getConfDeltaBody());
        copy.setConfSchemaBody(source.getConfSchemaBody());
        return copy;
    }

    private EventSyncResponse deepCopy(EventSyncResponse source) {
        if (source == null) {
            return null;
        }
        EventSyncResponse copy = new EventSyncResponse();
        if (source.getEventSequenceNumberResponse() != null) {
            copy.setEventSequenceNumberResponse(source.getEventSequenceNumberResponse());
        }
        if (source.getEvents() != null) {
            copy.setEvents(new ArrayList<>(source.getEvents()));
        }
        if (source.getEventListenersResponses() != null) {
            copy.setEventListenersResponses(new ArrayList<>(source.getEventListenersResponses()));
        }
        return copy;
    }

    private LogSyncResponse deepCopy(LogSyncResponse source) {
        if (source == null) {
            return null;
        }
        if (source.getDeliveryStatuses() != null) {
            List<LogDeliveryStatus> statusList = new ArrayList<>(source.getDeliveryStatuses().size());
            for (LogDeliveryStatus status : source.getDeliveryStatuses()) {
                statusList.add(new LogDeliveryStatus(status.getRequestId(), status.getResult(), status.getErrorCode()));
            }
            return new LogSyncResponse(statusList);
        } else {
            return new LogSyncResponse();
        }
    }

    private NotificationSyncResponse deepCopy(NotificationSyncResponse source) {
        if (source == null) {
            return null;
        }
        NotificationSyncResponse copy = new NotificationSyncResponse();
        copy.setAppStateSeqNumber(source.getAppStateSeqNumber());
        copy.setResponseStatus(source.getResponseStatus());
        if (source.getNotifications() != null) {
            copy.setNotifications(new ArrayList<>(source.getNotifications()));
        }
        if (source.getAvailableTopics() != null) {
            copy.setAvailableTopics(new ArrayList<>(source.getAvailableTopics()));
        }
        return copy;
    }

    private ProfileSyncResponse deepCopy(ProfileSyncResponse source) {
        if (source == null) {
            return null;
        }
        return new ProfileSyncResponse(source.getResponseStatus());
    }

    private RedirectSyncResponse deepCopy(RedirectSyncResponse source) {
        if (source == null) {
            return null;
        }
        return new RedirectSyncResponse(source.getDnsName());
    }

    private UserSyncResponse deepCopy(UserSyncResponse source) {
        if (source == null) {
            return null;
        }
        UserSyncResponse copy = new UserSyncResponse();
        if (source.getEndpointAttachResponses() != null) {
            copy.setEndpointAttachResponses(new ArrayList<>(source.getEndpointAttachResponses()));
        }
        if (source.getEndpointDetachResponses() != null) {
            copy.setEndpointDetachResponses(new ArrayList<>(source.getEndpointDetachResponses()));
        }
        if (source.getUserAttachNotification() != null) {
            copy.setUserAttachNotification(new UserAttachNotification(source.getUserAttachNotification().getUserExternalId(), source
                    .getUserAttachNotification().getEndpointAccessToken()));
        }
        if (source.getUserAttachResponse() != null) {
            copy.setUserAttachResponse(new UserAttachResponse(source.getUserAttachResponse().getResult()));
        }
        if (source.getUserDetachNotification() != null) {
            copy.setUserDetachNotification(new UserDetachNotification(source.getUserDetachNotification().getEndpointAccessToken()));
        }
        return copy;
    }

    protected void tellActor(ActorContext context, ActorRef target, Object message) {
        target.tell(message, context.self());
    }

    protected void sendEventsIfPresent(ActorContext context, EventSyncRequest request) {
        List<Event> events = request.getEvents();
        if (userId != null && events != null && !events.isEmpty()) {
            LOG.debug("[{}][{}] Processing events {} with seq number > {}", endpointKey, actorKey, events, processedEventSeqNum);
            List<Event> eventsToSend = new ArrayList<>(events.size());
            int maxSentEventSeqNum = processedEventSeqNum;
            for (Event event : events) {
                if (event.getSeqNum() > processedEventSeqNum) {
                    event.setSource(endpointKey);
                    eventsToSend.add(event);
                    maxSentEventSeqNum = Math.max(event.getSeqNum(), maxSentEventSeqNum);
                } else {
                    LOG.debug("[{}][{}] Ignoring duplicate/old event {} due to seq number < {}", endpointKey, actorKey, events,
                            processedEventSeqNum);
                }
            }
            processedEventSeqNum = maxSentEventSeqNum;
            if (!eventsToSend.isEmpty()) {
                EndpointEventSendMessage message = new EndpointEventSendMessage(userId, eventsToSend, key, appToken, context.self());
                context.parent().tell(message, context.self());
            }
        }
    }

    private boolean isValidForEvents(EndpointProfileDto profile) {
        return profile.getEndpointUserId() != null && !profile.getEndpointUserId().isEmpty() && profile.getEcfVersionStates() != null
                && !profile.getEcfVersionStates().isEmpty();
    }

    private List<EventClassFamilyVersion> convertToECFVersions(List<EventClassFamilyVersionStateDto> ecfVersionStates) {
        List<EventClassFamilyVersion> result = new ArrayList<>(ecfVersionStates.size());
        for (EventClassFamilyVersionStateDto dto : ecfVersionStates) {
            result.add(new EventClassFamilyVersion(dto.getEcfId(), dto.getVersion()));
        }
        return result;
    }

    public void processEndpointUserActionMessage(ActorContext context, EndpointUserActionMessage message) {
        Set<ChannelMetaData> eventChannels = new HashSet<ChannelMetaData>();
        eventChannels.addAll(channelMap.getByTransportType(TransportType.EVENT));
        eventChannels.addAll(channelMap.getByTransportType(TransportType.USER));
        LOG.debug("[{}][{}] Current Endpoint was attached/detached from user. Need to close all current event channels {}", endpointKey,
                actorKey, eventChannels.size());
        userRegistrationRequestSent = false;
        if (!eventChannels.isEmpty()) {
            for (ChannelMetaData channel : eventChannels) {
                SyncRequestMessage pendingRequest = channel.getRequestMessage();
                SyncResponse pendingResponse = channel.getResponseHolder().getResponse();

                UserSyncResponse userSyncResponse = pendingResponse.getUserSyncResponse();
                if (userSyncResponse != null) {
                    if (message instanceof EndpointUserAttachMessage) {
                        if (endpointProfile != null) {
                            endpointProfile.setEndpointUserId(message.getUserId());
                        }
                        userSyncResponse
                                .setUserAttachNotification(new UserAttachNotification(message.getUserId(), message.getOriginator()));
                        LOG.debug("[{}][{}] Adding user attach notification", endpointKey, actorKey);
                    } else if (message instanceof EndpointUserDetachMessage) {
                        if (endpointProfile != null && message.getUserId().equals(endpointProfile.getEndpointUserId())) {
                            endpointProfile.setEndpointUserId(null);
                        }
                        userSyncResponse.setUserDetachNotification(new UserDetachNotification(message.getOriginator()));
                        LOG.debug("[{}][{}] Adding user detach notification", endpointKey, actorKey);
                    }
                }

                LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
                sendReply(context, pendingRequest, pendingResponse);
                if (channel.getType().isAsync()) {
                    updateUserConnection(context);
                } else {
                    channelMap.removeChannel(channel);
                }
            }
        } else {
            LOG.debug("[{}][{}] Message ignored due to no channel contexts registered for events", endpointKey, actorKey, message);
        }
    }

    public boolean processDisconnectMessage(ActorContext context, ChannelAware message) {
        LOG.debug("[{}][{}] Received disconnect message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = channelMap.getById(message.getChannelUuid());
        if (channel != null) {
            channelMap.removeChannel(channel);
            return true;
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public boolean processPingMessage(ActorContext context, ChannelAware message) {
        LOG.debug("[{}][{}] Received ping message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = channelMap.getById(message.getChannelUuid());
        if (channel != null) {
            long lastActivityTime = System.currentTimeMillis();
            LOG.debug("[{}][{}] Updating last activity time for channel [{}] to ", endpointKey, actorKey, message.getChannelUuid(),
                    lastActivityTime);
            channel.setLastActivityTime(lastActivityTime);
            scheduleKeepAliveCheck(context, channel);
            channel.getContext().writeAndFlush(new PingResponse());
            return true;
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public boolean processChannelTimeoutMessage(ActorContext context, ChannelTimeoutMessage message) {
        LOG.debug("[{}][{}] Received channel timeout message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = channelMap.getById(message.getChannelUuid());
        if (channel != null) {
            if (channel.getLastActivityTime() <= message.getLastActivityTime()) {
                LOG.debug("[{}][{}] Timeout message accepted for channel [{}]. Last activity time {} and timeout is {} ", endpointKey,
                        actorKey, message.getChannelUuid(), channel.getLastActivityTime(), message.getLastActivityTime());
                channelMap.removeChannel(channel);
                return true;
            } else {
                LOG.debug("[{}][{}] Timeout message ignored for channel [{}]. Last activity time {} and timeout is {} ", endpointKey,
                        actorKey, message.getChannelUuid(), channel.getLastActivityTime(), message.getLastActivityTime());
                return false;
            }
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public void processLogDeliveryMessage(ActorContext context, LogDeliveryMessage message) {
        LOG.debug("[{}][{}] Received log delivery message for request [{}] with status {}", endpointKey, actorKey, message.getRequestId(),
                message.isSuccess());
        logUploadResponseMap.put(message.getRequestId(), message);
        List<ChannelMetaData> channels = channelMap.getByTransportType(TransportType.LOGGING);
        if (channels.size() > 0) {
            ChannelMetaData channel = channels.get(0);
            SyncRequestMessage pendingRequest = channel.getRequestMessage();
            SyncResponse pendingResponse = channel.getResponseHolder().getResponse();

            pendingResponse.setLogSyncResponse(toLogDeliveryStatus());
            logUploadResponseMap.clear();

            LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
            sendReply(context, pendingRequest, pendingResponse);
            if (!channel.getType().isAsync()) {
                channelMap.removeChannel(channel);
            }
        }
    }

    private LogSyncResponse toLogDeliveryStatus() {
        List<LogDeliveryStatus> statusList = new ArrayList<>();
        for (Entry<String, LogDeliveryMessage> response : logUploadResponseMap.entrySet()) {
            LogDeliveryMessage message = response.getValue();
            statusList.add(new LogDeliveryStatus(response.getKey(), message.isSuccess() ? SyncResponseResultType.SUCCESS
                    : SyncResponseResultType.FAILURE, toErrorCode(message.getErrorCode())));
        }
        return new LogSyncResponse(statusList);
    }

    private static LogDeliveryErrorCode toErrorCode(org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode errorCode) {
        switch (errorCode) {
        case APPENDER_INTERNAL_ERROR:
            return LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR;
        case NO_APPENDERS_CONFIGURED:
            return LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED;
        case REMOTE_CONNECTION_ERROR:
            return LogDeliveryErrorCode.REMOTE_CONNECTION_ERROR;
        case REMOTE_INTERNAL_ERROR:
            return LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR;
        default:
            return null;
        }
    }
}
