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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftServerProfileUpdateMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.operations.pojo.SyncContext;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.AbstractEndpointActorMessageProcessor;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local.ChannelMap.ChannelMetaData;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ThriftEndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ActorTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ChannelTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.RequestTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.TimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicSubscriptionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicUnsubscriptionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage.EventDeliveryStatus;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserAttachMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDetachMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettySessionResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.utils.EntityConvertUtils;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.sync.Event;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.EventSequenceNumberResponse;
import org.kaaproject.kaa.server.sync.EventServerSync;
import org.kaaproject.kaa.server.sync.LogClientSync;
import org.kaaproject.kaa.server.sync.LogEntry;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.UserAttachNotification;
import org.kaaproject.kaa.server.sync.UserAttachRequest;
import org.kaaproject.kaa.server.sync.UserClientSync;
import org.kaaproject.kaa.server.sync.UserDetachNotification;
import org.kaaproject.kaa.server.sync.UserServerSync;
import org.kaaproject.kaa.server.transport.EndpointRevocationException;
import org.kaaproject.kaa.server.transport.channel.ChannelAware;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;
import scala.concurrent.duration.Duration;

public class LocalEndpointActorMessageProcessor extends AbstractEndpointActorMessageProcessor<LocalEndpointActorState> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LocalEndpointActorMessageProcessor.class);

    private final Map<Integer, LogDeliveryMessage> logUploadResponseMap;

    private final Map<UUID, UserVerificationResponseMessage> userAttachResponseMap;

    public LocalEndpointActorMessageProcessor(AkkaContext context, String appToken, EndpointObjectHash key, String actorKey) {
        super(new LocalEndpointActorState(Base64Util.encode(key.getData()), actorKey), context.getOperationsService(), appToken, key,
                actorKey, Base64Util.encode(key.getData()), context.getLocalEndpointTimeout());
        this.logUploadResponseMap = new HashMap<>();
        this.userAttachResponseMap = new LinkedHashMap<>();
    }

    public void processEndpointSync(ActorContext context, SyncRequestMessage message) {
        sync(context, message);
    }

    public void processEndpointEventReceiveMessage(ActorContext context, EndpointEventReceiveMessage message) {
        EndpointEventDeliveryMessage response;
        Set<ChannelMetaData> eventChannels = state.getChannelsByType(TransportType.EVENT);
        if (!eventChannels.isEmpty()) {
            for (ChannelMetaData eventChannel : eventChannels) {
                addEventsAndReply(context, eventChannel, message);
            }
            response = new EndpointEventDeliveryMessage(message, EventDeliveryStatus.SUCCESS);
        } else {
            LOG.debug("[{}] Message ignored due to no channel contexts registered for events", actorKey, message);
            response = new EndpointEventDeliveryMessage(message, EventDeliveryStatus.FAILURE);
            state.setUserRegistrationPending(false);
        }
        tellParent(context, response);
    }

    public void processThriftNotification(ActorContext context) {
        Set<ChannelMetaData> channels = state.getChannelsByTypes(TransportType.CONFIGURATION, TransportType.NOTIFICATION);
        LOG.debug("[{}][{}] Processing thrift norification for {} channels", endpointKey, actorKey, channels.size());
        syncChannels(context, channels, true, true);
    }

    public void processUserConfigurationUpdate(ActorContext context, EndpointUserConfigurationUpdateMessage message) {
        if (message.getUserConfigurationUpdate() != null) {
            state.setUcfHash(message.getUserConfigurationUpdate().getHash());
            syncChannels(context, state.getChannelsByTypes(TransportType.CONFIGURATION), true, false);
        }
    }

    @Override
    protected void processThriftMsg(ActorContext context, ThriftEndpointActorMsg<?> msg) {
        Object thriftMsg = msg.getMsg();
        if (thriftMsg instanceof ThriftServerProfileUpdateMessage) {
            processServerProfileUpdateMsg(context, (ThriftServerProfileUpdateMessage) thriftMsg);
        } else if (thriftMsg instanceof ThriftUnicastNotificationMessage) {
            processUnicastNotificationMsg(context, (ThriftUnicastNotificationMessage) thriftMsg);
        } else if (thriftMsg instanceof ThriftEndpointDeregistrationMessage) {
            processEndpointDeregistrationMessage(context, (ThriftEndpointDeregistrationMessage) thriftMsg);
        }
    }

    private void processServerProfileUpdateMsg(ActorContext context, ThriftServerProfileUpdateMessage thriftMsg) {
        EndpointProfileDto endpointProfile = state.getProfile();
        if (endpointProfile != null) {
            state.setProfile(operationsService.refreshServerEndpointProfile(key));
            Set<ChannelMetaData> channels = state.getChannelsByTypes(TransportType.CONFIGURATION, TransportType.NOTIFICATION);
            LOG.debug("[{}][{}] Processing profile update for {} channels", endpointKey, actorKey, channels.size());
            syncChannels(context, channels, true, true);
        } else {
            LOG.warn("[{}][{}] Can't update server profile for an empty state", endpointKey, actorKey);
        }
    }

    private void processUnicastNotificationMsg(ActorContext context, ThriftUnicastNotificationMessage thriftMsg) {
        processNotification(context, NotificationMessage.fromUnicastId(thriftMsg.getNotificationId()));
    }

    private void processEndpointDeregistrationMessage(ActorContext context, ThriftEndpointDeregistrationMessage thriftMsg) {
        for (ChannelMetaData channel : state.getAllChannels()) {
            sendReply(context, channel.request, new EndpointRevocationException());
        }
    }

    public void processNotification(ActorContext context, NotificationMessage message) {
        LOG.debug("[{}][{}] Processing notification message {}", endpointKey, actorKey, message);

        Set<ChannelMetaData> channels = state.getChannelsByType(TransportType.NOTIFICATION);
        if (channels.isEmpty()) {
            LOG.debug("[{}][{}] No channels to process notification message", endpointKey, actorKey);
            return;
        }
        String unicastNotificationId = message.getUnicastNotificationId();
        List<NotificationDto> validNfs = state.filter(message.getNotifications());
        if (unicastNotificationId == null && validNfs.isEmpty()) {
            LOG.debug("[{}][{}] message is no longer valid for current endpoint", endpointKey, actorKey);
            return;
        }
        for (ChannelMetaData channel : channels) {
            LOG.debug("[{}][{}] processing channel {} and response {}", endpointKey, actorKey, channel,
                    channel.getResponseHolder().getResponse());
            ServerSync syncResponse = operationsService.updateSyncResponse(channel.getResponseHolder().getResponse(), validNfs,
                    unicastNotificationId);
            if (syncResponse != null) {
                LOG.debug("[{}][{}] processed channel {} and response {}", endpointKey, actorKey, channel, syncResponse);
                sendReply(context, channel.getRequestMessage(), syncResponse);
                if (!channel.getType().isAsync()) {
                    state.removeChannel(channel);
                }
            }
        }
    }

    public void processRequestTimeoutMessage(ActorContext context, RequestTimeoutMessage message) {
        ChannelMetaData channel = state.getChannelByRequestId(message.getRequestId());
        if (channel != null) {
            SyncContext response = channel.getResponseHolder();
            sendReply(context, channel.getRequestMessage(), response.getResponse());
            if (!channel.getType().isAsync()) {
                state.removeChannel(channel);
            }
        } else {
            LOG.debug("[{}][{}] Failed to find request by id [{}].", endpointKey, actorKey, message.getRequestId());
        }
    }

    private void sync(ActorContext context, SyncRequestMessage requestMessage) {
        try {
            state.setLastActivityTime(System.currentTimeMillis());
            long start = state.getLastActivityTime();

            ChannelMetaData channel = initChannel(context, requestMessage);

            ClientSync request = mergeRequestForChannel(channel, requestMessage);

            ChannelType channelType = channel.getType();
            LOG.debug("[{}][{}] Processing sync request {} from {} channel [{}]", endpointKey, actorKey, request, channelType,
                    requestMessage.getChannelUuid());

            SyncContext responseHolder = sync(request);

            state.setProfile(responseHolder.getEndpointProfile());

            if (state.getProfile() != null) {
                processLogUpload(context, request, responseHolder);
                processUserAttachRequest(context, request, responseHolder);
                updateUserConnection(context);
                processEvents(context, request, responseHolder);
                notifyAffectedEndpoints(context, request, responseHolder);
            } else {
                LOG.warn("[{}][{}] Endpoint profile is not set after request processing!", endpointKey, actorKey);
            }

            LOG.debug("[{}][{}] SyncResponseHolder {}", endpointKey, actorKey, responseHolder);

            if (channelType.isAsync()) {
                LOG.debug("[{}][{}] Adding async request from channel [{}] to map ", endpointKey, actorKey,
                        requestMessage.getChannelUuid());
                channel.update(responseHolder);
                updateSubscriptionsToTopics(context, responseHolder);
                sendReply(context, requestMessage, responseHolder.getResponse());
            } else {
                if (channelType.isLongPoll() && !responseHolder.requireImmediateReply()) {
                    LOG.debug("[{}][{}] Adding long poll request from channel [{}] to map ", endpointKey, actorKey,
                            requestMessage.getChannelUuid());
                    channel.update(responseHolder);
                    updateSubscriptionsToTopics(context, responseHolder);
                    scheduleTimeoutMessage(context, requestMessage.getChannelUuid(), getDelay(requestMessage, start));
                } else {
                    sendReply(context, requestMessage, responseHolder.getResponse());
                    state.removeChannel(channel);
                }
            }
        } catch (Exception e) {
            LOG.error("[{}][{}] processEndpointRequest", endpointKey, actorKey, e);
            sendReply(context, requestMessage, e);
        }
    }

    private SyncContext sync(ClientSync request) throws GetDeltaException {
        if (!request.isValid()) {
            LOG.warn("[{}] Request is not valid. It does not contain profile information!", endpointKey);
            return SyncContext.failure(request.getRequestId());
        }
        SyncContext context = new SyncContext(new ServerSync());
        context.setEndpointProfile(state.getProfile());
        context.setRequestId(request.getRequestId());
        context.setStatus(SyncStatus.SUCCESS);
        context.setEndpointKey(endpointKey);
        context.setRequestHash(request.hashCode());
        context.setMetaData(request.getClientSyncMetaData());


        LOG.trace("[{}][{}] processing sync. Request: {}", endpointKey, context.getRequestHash(), request);

        context = operationsService.syncClientProfile(context, request.getProfileSync());
        context = operationsService.syncUseConfigurationRawSchema(context, request.isUseConfigurationRawSchema());

        if (context.getStatus() != SyncStatus.SUCCESS) {
            return context;
        }
        if (state.isUcfHashRequiresIntialization()) {
            byte[] hash = operationsService.fetchUcfHash(appToken, state.getProfile());
            LOG.debug("[{}][{}] Initialized endpoint user configuration hash {}", endpointKey, context.getRequestHash(),
                    Arrays.toString(hash));
            state.setUcfHash(hash);
        }

        context = operationsService.processEndpointAttachDetachRequests(context, request.getUserSync());
        context = operationsService.processEventListenerRequests(context, request.getEventSync());

        if (state.isUserConfigurationUpdatePending()) {
            context = operationsService.syncUserConfigurationHash(context, state.getUcfHash());
        }

        context = operationsService.syncConfiguration(context, request.getConfigurationSync());

        context = operationsService.syncNotification(context, request.getNotificationSync());

        LOG.trace("[{}][{}] processed sync. Response is {}", endpointKey, request.hashCode(), context.getResponse());

        return context;
    }

    private void syncChannels(ActorContext context, Set<ChannelMetaData> channels, boolean cfUpdate, boolean nfUpdate) {
        for (ChannelMetaData channel : channels) {
            ClientSync originalRequest = channel.getRequestMessage().getRequest();
            ClientSync newRequest = new ClientSync();
            newRequest.setRequestId(originalRequest.getRequestId());
            newRequest.setClientSyncMetaData(originalRequest.getClientSyncMetaData());
            newRequest.setUseConfigurationRawSchema(originalRequest.isUseConfigurationRawSchema());
            if (cfUpdate && originalRequest.getConfigurationSync() != null) {
                newRequest.setForceConfigurationSync(true);
                newRequest.setConfigurationSync(originalRequest.getConfigurationSync());
            }
            if (nfUpdate && originalRequest.getNotificationSync() != null) {
                newRequest.setForceNotificationSync(true);
                newRequest.setNotificationSync(originalRequest.getNotificationSync());
            }
            LOG.debug("[{}][{}] Processing request {}", endpointKey, actorKey, newRequest);
            sync(context, new SyncRequestMessage(channel.getRequestMessage().getSession(), newRequest,
                    channel.getRequestMessage().getCommand(), channel.getRequestMessage().getOriginator()));
        }
    }

    private ClientSync mergeRequestForChannel(ChannelMetaData channel, SyncRequestMessage requestMessage) {
        ClientSync request;
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
        return request;
    }

    private void processUserAttachRequest(ActorContext context, ClientSync syncRequest, SyncContext responseHolder) {
        UserClientSync request = syncRequest.getUserSync();
        if (request != null && request.getUserAttachRequest() != null) {
            UserAttachRequest aRequest = request.getUserAttachRequest();
            context.parent().tell(new UserVerificationRequestMessage(context.self(), aRequest.getUserVerifierId(),
                    aRequest.getUserExternalId(), aRequest.getUserAccessToken()), context.self());
            LOG.debug("[{}][{}] received and forwarded user attach request {}", endpointKey, actorKey, request.getUserAttachRequest());

            if (userAttachResponseMap.size() > 0) {
                Entry<UUID, UserVerificationResponseMessage> entryToSend = userAttachResponseMap.entrySet().iterator().next();
                updateResponseWithUserAttachResults(responseHolder.getResponse(), entryToSend.getValue());
                userAttachResponseMap.remove(entryToSend.getKey());
            }
        }
    }

    private void updateResponseWithUserAttachResults(ServerSync response, UserVerificationResponseMessage message) {
        if (response.getUserSync() == null) {
            response.setUserSync(new UserServerSync());
        }
        response.getUserSync().setUserAttachResponse(EntityConvertUtils.convert(message));
    }

    private void processEvents(ActorContext context, ClientSync request, SyncContext responseHolder) {
        if (request.getEventSync() != null) {
            EventClientSync eventRequest = request.getEventSync();
            processSeqNumber(eventRequest, responseHolder);
            if (state.isValidForEvents()) {
                sendEventsIfPresent(context, eventRequest);
            } else {
                LOG.debug(
                        "[{}][{}] Endpoint profile is not valid for send/receive events. Either no assigned user or no event families in sdk",
                        endpointKey, actorKey);
            }
        }
    }

    private void processSeqNumber(EventClientSync request, SyncContext responseHolder) {
        if (request.isSeqNumberRequest()) {
            EventServerSync response = responseHolder.getResponse().getEventSync();
            if (response == null) {
                response = new EventServerSync();
                responseHolder.getResponse().setEventSync(response);
            }
            response.setEventSequenceNumberResponse(new EventSequenceNumberResponse(Math.max(state.getEventSeqNumber(), 0)));
        }
    }

    private void updateUserConnection(ActorContext context) {
        if (!state.isValidForUser()) {
            return;
        }
        if (state.userIdMismatch()) {
            sendDisconnectFromOldUser(context, state.getProfile());
            state.setUserRegistrationPending(false);
        }
        if (!state.isUserRegistrationPending()) {
            state.setUserId(state.getProfileUserId());
            if (state.getUserId() != null) {
                sendConnectToNewUser(context, state.getProfile());
                state.setUserRegistrationPending(true);
            }
        } else {
            LOG.trace("[{}][{}] User registration request is already sent.", endpointKey, actorKey);
        }
    }

    private void processLogUpload(ActorContext context, ClientSync syncRequest, SyncContext responseHolder) {
        LogClientSync request = syncRequest.getLogSync();
        if (request != null) {
            if (request.getLogEntries() != null && request.getLogEntries().size() > 0) {
                LOG.debug("[{}][{}] Processing log upload request {}", endpointKey, actorKey, request.getLogEntries().size());
                EndpointProfileDataDto profileDto = convert(responseHolder.getEndpointProfile());
                List<LogEvent> logEvents = new ArrayList<>(request.getLogEntries().size());
                for (LogEntry logEntry : request.getLogEntries()) {
                    LogEvent logEvent = new LogEvent();
                    logEvent.setLogData(logEntry.getData().array());
                    logEvents.add(logEvent);
                }
                BaseLogEventPack logPack = new BaseLogEventPack(profileDto, System.currentTimeMillis(),
                        responseHolder.getEndpointProfile().getLogSchemaVersion(), logEvents);
                logPack.setUserId(state.getUserId());
                context.parent().tell(new LogEventPackMessage(request.getRequestId(), context.self(), logPack), context.self());
            }
            if (logUploadResponseMap.size() > 0) {
                responseHolder.getResponse().setLogSync(EntityConvertUtils.convert(logUploadResponseMap));
                logUploadResponseMap.clear();
            }
        }
    }

    private EndpointProfileDataDto convert(EndpointProfileDto profileDto) {
        return new EndpointProfileDataDto(profileDto.getId(), endpointKey, profileDto.getClientProfileVersion(),
                profileDto.getClientProfileBody(), profileDto.getServerProfileVersion(), profileDto.getServerProfileBody());
    }

    private void sendConnectToNewUser(ActorContext context, EndpointProfileDto endpointProfile) {
        List<EventClassFamilyVersion> ecfVersions = EntityConvertUtils.convertToECFVersions(endpointProfile.getEcfVersionStates());
        EndpointUserConnectMessage userRegistrationMessage = new EndpointUserConnectMessage(state.getUserId(), key, ecfVersions,
                endpointProfile.getConfigurationVersion(), endpointProfile.getUserConfigurationHash(), appToken, context.self());
        LOG.debug("[{}][{}] Sending user registration request {}", endpointKey, actorKey, userRegistrationMessage);
        context.parent().tell(userRegistrationMessage, context.self());
    }

    private void sendDisconnectFromOldUser(ActorContext context, EndpointProfileDto endpointProfile) {
        LOG.debug("[{}][{}] Detected user change from [{}] to [{}]", endpointKey, actorKey, state.getUserId(),
                endpointProfile.getEndpointUserId());
        EndpointUserDisconnectMessage userDisconnectMessage = new EndpointUserDisconnectMessage(state.getUserId(), key, appToken,
                context.self());
        context.parent().tell(userDisconnectMessage, context.self());
    }

    private long getDelay(SyncRequestMessage requestMessage, long start) {
        return requestMessage.getRequest().getClientSyncMetaData().getTimeout() - (System.currentTimeMillis() - start);
    }

    private ChannelMetaData initChannel(ActorContext context, SyncRequestMessage requestMessage) {
        ChannelMetaData channel = state.getChannelById(requestMessage.getChannelUuid());
        if (channel == null) {
            channel = new ChannelMetaData(requestMessage);

            if (!channel.getType().isAsync() && channel.getType().isLongPoll()) {
                LOG.debug("[{}][{}] Received request using long poll channel.", endpointKey, actorKey);
                // Probably old long poll channels lost connection. Sending
                // reply to them just in case
                Set<ChannelMetaData> channels = state.getChannelsByType(TransportType.EVENT);
                for (ChannelMetaData oldChannel : channels) {
                    if (!oldChannel.getType().isAsync() && channel.getType().isLongPoll()) {
                        LOG.debug("[{}][{}] Closing old long poll channel [{}]", endpointKey, actorKey, oldChannel.getId());
                        sendReply(context, oldChannel.getRequestMessage(), oldChannel.getResponseHolder().getResponse());
                        state.removeChannel(oldChannel);
                    }
                }
            }

            long time = System.currentTimeMillis();

            channel.setLastActivityTime(time);

            if (channel.getType().isAsync() && channel.getKeepAlive() > 0) {
                scheduleKeepAliveCheck(context, channel);
            }

            state.addChannel(channel);
        }
        return channel;
    }

    private void scheduleKeepAliveCheck(ActorContext context, ChannelMetaData channel) {
        TimeoutMessage message = new ChannelTimeoutMessage(channel.getId(), channel.getLastActivityTime());
        LOG.debug("Scheduling channel timeout message: {} to timeout in {}", message, channel.getKeepAlive() * 1000);
        scheduleTimeoutMessage(context, message, channel.getKeepAlive() * 1000);
    }

    private void notifyAffectedEndpoints(ActorContext context, ClientSync request, SyncContext responseHolder) {
        if (responseHolder.getResponse().getUserSync() != null) {
            List<EndpointAttachResponse> attachResponses = responseHolder.getResponse().getUserSync().getEndpointAttachResponses();
            if (attachResponses != null && !attachResponses.isEmpty()) {
                state.resetEventSeqNumber();
                for (EndpointAttachResponse response : attachResponses) {
                    if (response.getResult() != SyncStatus.SUCCESS) {
                        LOG.debug("[{}][{}] Skipped unsuccessful attach response [{}]", endpointKey, actorKey, response.getRequestId());
                        continue;
                    }
                    EndpointUserAttachMessage attachMessage = new EndpointUserAttachMessage(
                            EndpointObjectHash.fromBytes(Base64Util.decode(response.getEndpointKeyHash())), state.getUserId(), endpointKey);
                    context.parent().tell(attachMessage, context.self());
                    LOG.debug("[{}][{}] Notification to attached endpoint [{}] sent", endpointKey, actorKey, response.getEndpointKeyHash());
                }
            }

            List<EndpointDetachRequest> detachRequests = request.getUserSync() == null ? null
                    : request.getUserSync().getEndpointDetachRequests();
            if (detachRequests != null && !detachRequests.isEmpty()) {
                state.resetEventSeqNumber();
                for (EndpointDetachRequest detachRequest : detachRequests) {
                    for (EndpointDetachResponse detachResponse : responseHolder.getResponse().getUserSync().getEndpointDetachResponses()) {
                        if (detachRequest.getRequestId() == detachResponse.getRequestId()) {
                            if (detachResponse.getResult() != SyncStatus.SUCCESS) {
                                LOG.debug("[{}][{}] Skipped unsuccessful detach response [{}]", endpointKey, actorKey,
                                        detachResponse.getRequestId());
                                continue;
                            }
                            EndpointUserDetachMessage attachMessage = new EndpointUserDetachMessage(
                                    EndpointObjectHash.fromBytes(Base64Util.decode(detachRequest.getEndpointKeyHash())), state.getUserId(),
                                    endpointKey);
                            context.parent().tell(attachMessage, context.self());
                            LOG.debug("[{}][{}] Notification to detached endpoint [{}] sent", endpointKey, actorKey,
                                    detachRequest.getEndpointKeyHash());
                        }
                    }
                }
            }
        }
    }

    protected void scheduleActorTimeout(ActorContext context) {
        if (state.isNoChannels()) {
            scheduleTimeoutMessage(context, new ActorTimeoutMessage(state.getLastActivityTime()), getInactivityTimeout());
        }
    }

    /**
     * Subscribe to topics.
     *
     * @param response
     *            the response
     */
    private void updateSubscriptionsToTopics(ActorContext context, SyncContext response) {
        Map<String, Integer> newStates = response.getSubscriptionStates();
        if (newStates == null) {
            return;
        }
        Map<String, Integer> currentStates = state.getSubscriptionStates();
        // detect and remove unsubscribed topics;
        Iterator<String> currentSubscriptionsIterator = currentStates.keySet().iterator();
        while (currentSubscriptionsIterator.hasNext()) {
            String subscribedTopic = currentSubscriptionsIterator.next();
            if (!newStates.containsKey(subscribedTopic)) {
                currentSubscriptionsIterator.remove();
                TopicUnsubscriptionMessage topicSubscriptionMessage = new TopicUnsubscriptionMessage(subscribedTopic, appToken, key,
                        context.self());
                context.parent().tell(topicSubscriptionMessage, context.self());
            }
        }
        // subscribe to new topics;
        for (Entry<String, Integer> entry : newStates.entrySet()) {
            if (!currentStates.containsKey(entry.getKey())) {
                TopicSubscriptionMessage topicSubscriptionMessage = new TopicSubscriptionMessage(entry.getKey(), entry.getValue(),
                        response.getSystemNfVersion(), response.getUserNfVersion(), appToken, key, context.self());
                context.parent().tell(topicSubscriptionMessage, context.self());
            }
        }
        state.setSubscriptionStates(newStates);
    }

    private void scheduleTimeoutMessage(ActorContext context, UUID requestId, long delay) {
        scheduleTimeoutMessage(context, new RequestTimeoutMessage(requestId), delay);
    }

    private void scheduleTimeoutMessage(ActorContext context, TimeoutMessage message, long delay) {
        context.system().scheduler().scheduleOnce(Duration.create(delay, TimeUnit.MILLISECONDS), context.self(), message,
                context.dispatcher(), context.self());
    }

    private void addEventsAndReply(ActorContext context, ChannelMetaData channel, EndpointEventReceiveMessage message) {
        SyncRequestMessage pendingRequest = channel.getRequestMessage();
        SyncContext pendingResponse = channel.getResponseHolder();

        EventServerSync eventResponse = pendingResponse.getResponse().getEventSync();
        if (eventResponse == null) {
            eventResponse = new EventServerSync();
            pendingResponse.getResponse().setEventSync(eventResponse);
        }

        eventResponse.setEvents(message.getEvents());
        sendReply(context, pendingRequest, pendingResponse.getResponse());
        if (!channel.getType().isAsync()) {
            state.removeChannel(channel);
        }
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, ServerSync syncResponse) {
        sendReply(context, request, null, syncResponse);
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, Exception e) {
        sendReply(context, request, e, null);
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, Exception e, ServerSync syncResponse) {
        LOG.debug("[{}] response: {}", actorKey, syncResponse);

        ServerSync copy = ServerSync.deepCopy(syncResponse);
        ServerSync.cleanup(syncResponse);

        NettySessionResponseMessage response = new NettySessionResponseMessage(request.getSession(), copy, e,
                request.getCommand().getMessageBuilder(), request.getCommand().getErrorBuilder());

        tellActor(context, request.getOriginator(), response);
        scheduleActorTimeout(context);
    }

    protected void sendEventsIfPresent(ActorContext context, EventClientSync request) {
        List<Event> events = request.getEvents();
        if (state.getUserId() != null && events != null && !events.isEmpty()) {
            LOG.debug("[{}][{}] Processing events {} with seq number > {}", endpointKey, actorKey, events, state.getEventSeqNumber());
            List<Event> eventsToSend = new ArrayList<>(events.size());
            int maxSentEventSeqNum = state.getEventSeqNumber();
            for (Event event : events) {
                if (event.getSeqNum() > state.getEventSeqNumber()) {
                    event.setSource(endpointKey);
                    eventsToSend.add(event);
                    maxSentEventSeqNum = Math.max(event.getSeqNum(), maxSentEventSeqNum);
                } else {
                    LOG.debug("[{}][{}] Ignoring duplicate/old event {} due to seq number < {}", endpointKey, actorKey, events,
                            state.getEventSeqNumber());
                }
            }
            state.setEventSeqNumber(maxSentEventSeqNum);
            if (!eventsToSend.isEmpty()) {
                EndpointEventSendMessage message = new EndpointEventSendMessage(state.getUserId(), eventsToSend, key, appToken,
                        context.self());
                context.parent().tell(message, context.self());
            }
        }
    }

    public void processEndpointUserActionMessage(ActorContext context, EndpointUserActionMessage message) {
        Set<ChannelMetaData> eventChannels = state.getChannelsByTypes(TransportType.EVENT, TransportType.USER);
        LOG.debug("[{}][{}] Current Endpoint was attached/detached from user. Need to close all current event channels {}", endpointKey,
                actorKey, eventChannels.size());
        state.setUserRegistrationPending(false);
        state.setProfile(operationsService.refreshServerEndpointProfile(key));
        if (message instanceof EndpointUserAttachMessage) {
            LOG.debug("[{}][{}] Updating endpoint user id to {} in profile", endpointKey, actorKey, message.getUserId());
        } else if (message instanceof EndpointUserDetachMessage) {
            LOG.debug("[{}][{}] Clanup endpoint user id in profile", endpointKey, actorKey, message.getUserId());
        }

        if (!eventChannels.isEmpty()) {
            updateUserConnection(context);
            for (ChannelMetaData channel : eventChannels) {
                SyncRequestMessage pendingRequest = channel.getRequestMessage();
                ServerSync pendingResponse = channel.getResponseHolder().getResponse();

                UserServerSync userSyncResponse = pendingResponse.getUserSync();

                if (userSyncResponse == null && pendingRequest.isValid(TransportType.USER)) {
                    userSyncResponse = new UserServerSync();
                    pendingResponse.setUserSync(userSyncResponse);
                }
                if (userSyncResponse != null) {
                    if (message instanceof EndpointUserAttachMessage) {
                        userSyncResponse
                                .setUserAttachNotification(new UserAttachNotification(message.getUserId(), message.getOriginator()));
                        LOG.debug("[{}][{}] Adding user attach notification", endpointKey, actorKey);
                    } else if (message instanceof EndpointUserDetachMessage) {
                        userSyncResponse.setUserDetachNotification(new UserDetachNotification(message.getOriginator()));
                        LOG.debug("[{}][{}] Adding user detach notification", endpointKey, actorKey);
                    }
                }

                LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
                sendReply(context, pendingRequest, pendingResponse);
                if (!channel.getType().isAsync()) {
                    state.removeChannel(channel);
                }
            }
        } else {
            LOG.debug("[{}][{}] Message ignored due to no channel contexts registered for events", endpointKey, actorKey, message);
        }
    }

    public boolean processDisconnectMessage(ActorContext context, ChannelAware message) {
        LOG.debug("[{}][{}] Received disconnect message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = state.getChannelById(message.getChannelUuid());
        if (channel != null) {
            state.removeChannel(channel);
            scheduleActorTimeout(context);
            return true;
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public boolean processPingMessage(ActorContext context, ChannelAware message) {
        LOG.debug("[{}][{}] Received ping message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = state.getChannelById(message.getChannelUuid());
        if (channel != null) {
            long lastActivityTime = System.currentTimeMillis();
            LOG.debug("[{}][{}] Updating last activity time for channel [{}] to ", endpointKey, actorKey, message.getChannelUuid(),
                    lastActivityTime);
            channel.setLastActivityTime(lastActivityTime);
            channel.getContext().writeAndFlush(new PingResponse());
            return true;
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public boolean processChannelTimeoutMessage(ActorContext context, ChannelTimeoutMessage message) {
        LOG.debug("[{}][{}] Received channel timeout message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = state.getChannelById(message.getChannelUuid());
        if (channel != null) {
            if (channel.getLastActivityTime() <= message.getLastActivityTime()) {
                LOG.debug("[{}][{}] Timeout message accepted for channel [{}]. Last activity time {} and timeout is {} ", endpointKey,
                        actorKey, message.getChannelUuid(), channel.getLastActivityTime(), message.getLastActivityTime());
                state.removeChannel(channel);
                scheduleActorTimeout(context);
                return true;
            } else {
                LOG.debug("[{}][{}] Timeout message ignored for channel [{}]. Last activity time {} and timeout is {} ", endpointKey,
                        actorKey, message.getChannelUuid(), channel.getLastActivityTime(), message.getLastActivityTime());
                scheduleKeepAliveCheck(context, channel);
                return false;
            }
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            scheduleActorTimeout(context);
            return false;
        }
    }

    public void processLogDeliveryMessage(ActorContext context, LogDeliveryMessage message) {
        LOG.debug("[{}][{}] Received log delivery message for request [{}] with status {}", endpointKey, actorKey, message.getRequestId(),
                message.isSuccess());
        logUploadResponseMap.put(message.getRequestId(), message);
        Set<ChannelMetaData> channels = state.getChannelsByType(TransportType.LOGGING);
        for (ChannelMetaData channel : channels) {
            SyncRequestMessage pendingRequest = channel.getRequestMessage();
            ServerSync pendingResponse = channel.getResponseHolder().getResponse();

            pendingResponse.setLogSync(EntityConvertUtils.convert(logUploadResponseMap));

            LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
            sendReply(context, pendingRequest, pendingResponse);
            if (!channel.getType().isAsync()) {
                state.removeChannel(channel);
            }
        }
        logUploadResponseMap.clear();
    }

    public void processUserVerificationMessage(ActorContext context, UserVerificationResponseMessage message) {
        LOG.debug("[{}][{}] Received user verification message for request [{}] with status {}", endpointKey, actorKey,
                message.getRequestId(), message.isSuccess());
        userAttachResponseMap.put(message.getRequestId(), message);
        Set<ChannelMetaData> channels = state.getChannelsByType(TransportType.USER);
        Entry<UUID, UserVerificationResponseMessage> entryToSend = userAttachResponseMap.entrySet().iterator().next();
        for (ChannelMetaData channel : channels) {
            SyncRequestMessage pendingRequest = channel.getRequestMessage();
            ServerSync pendingResponse = channel.getResponseHolder().getResponse();

            updateResponseWithUserAttachResults(pendingResponse, entryToSend.getValue());

            LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
            sendReply(context, pendingRequest, pendingResponse);
            if (!channel.getType().isAsync()) {
                state.removeChannel(channel);
            }
        }
        userAttachResponseMap.remove(entryToSend.getKey());
        if (message.isSuccess()) {
            state.setProfile(operationsService.attachEndpointToUser(state.getProfile(), appToken, message.getUserId()));
            updateUserConnection(context);
        }
    }
}
