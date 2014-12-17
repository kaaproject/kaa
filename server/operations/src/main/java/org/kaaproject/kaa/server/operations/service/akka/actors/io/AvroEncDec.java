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
package org.kaaproject.kaa.server.operations.service.akka.actors.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.protocol.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSequenceNumberResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.kaaproject.kaa.common.endpoint.protocol.ClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ClientSyncMetaData;
import org.kaaproject.kaa.common.endpoint.protocol.ConfigurationClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ConfigurationServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.EndpointAttachRequest;
import org.kaaproject.kaa.common.endpoint.protocol.EndpointDetachRequest;
import org.kaaproject.kaa.common.endpoint.protocol.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.protocol.Event;
import org.kaaproject.kaa.common.endpoint.protocol.EventClassFamilyVersionInfo;
import org.kaaproject.kaa.common.endpoint.protocol.EventClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.EventListenersRequest;
import org.kaaproject.kaa.common.endpoint.protocol.EventSequenceNumberRequest;
import org.kaaproject.kaa.common.endpoint.protocol.EventServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.LogClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.LogEntry;
import org.kaaproject.kaa.common.endpoint.protocol.LogServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.NotificationClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.NotificationServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.ProfileClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ProfileServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.RedirectServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.ServerSync;
import org.kaaproject.kaa.common.endpoint.protocol.SubscriptionCommandType;
import org.kaaproject.kaa.common.endpoint.protocol.TopicState;
import org.kaaproject.kaa.common.endpoint.protocol.UserAttachRequest;
import org.kaaproject.kaa.common.endpoint.protocol.UserClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.UserServerSync;

public class AvroEncDec implements PlatformEncDec {

    private final AvroByteArrayConverter<SyncRequest> clientSyncConverter;

    private final AvroByteArrayConverter<SyncResponse> serverSyncConverter;

    public AvroEncDec() {
        super();
        this.clientSyncConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        this.serverSyncConverter = new AvroByteArrayConverter<>(SyncResponse.class);
    }

    @Override
    public ClientSync decode(byte[] data) throws IOException {
        SyncRequest source = clientSyncConverter.fromByteArray(data);
        if (source == null) {
            return null;
        }
        return convert(source);
    }

    @Override
    public byte[] encode(ServerSync sync) throws IOException {
        if (sync == null) {
            return null;
        }
        SyncResponse response = convert(sync);
        return serverSyncConverter.toByteArray(response);
    }

    public static ClientSync convert(SyncRequest source) {
        ClientSync dest = new ClientSync();
        dest.setRequestId(source.getRequestId());
        dest.setSyncRequestMetaData(convert(source.getSyncRequestMetaData()));
        dest.setProfileSyncRequest(convert(source.getProfileSyncRequest()));
        dest.setConfigurationSyncRequest(convert(source.getConfigurationSyncRequest()));
        dest.setNotificationSyncRequest(convert(source.getNotificationSyncRequest()));
        dest.setEventSyncRequest(convert(source.getEventSyncRequest()));
        dest.setUserSyncRequest(convert(source.getUserSyncRequest()));
        dest.setLogSyncRequest(convert(source.getLogSyncRequest()));
        return dest;
    }

    public static SyncResponse convert(ServerSync source) {
        SyncResponse sync = new SyncResponse();
        sync.setRequestId(source.getRequestId());
        sync.setStatus(convert(source.getStatus()));
        sync.setRedirectSyncResponse(convert(source.getRedirectSyncResponse()));
        sync.setProfileSyncResponse(convert(source.getProfileSyncResponse()));
        sync.setConfigurationSyncResponse(convert(source.getConfigurationSyncResponse()));
        sync.setNotificationSyncResponse(convert(source.getNotificationSyncResponse()));
        sync.setEventSyncResponse(convert(source.getEventSyncResponse()));
        sync.setUserSyncResponse(convert(source.getUserSyncResponse()));
        sync.setLogSyncResponse(convert(source.getLogSyncResponse()));
        return sync;
    }

    private static SyncResponseResultType convert(org.kaaproject.kaa.common.endpoint.protocol.SyncResponseResultType status) {
        if (status == null) {
            return null;
        }
        switch (status) {
        case SUCCESS:
            return SyncResponseResultType.SUCCESS;
        case FAILURE:
            return SyncResponseResultType.FAILURE;
        case PROFILE_RESYNC:
            return SyncResponseResultType.PROFILE_RESYNC;
        case REDIRECT:
            return SyncResponseResultType.REDIRECT;
        default:
            return null;
        }
    }

    private static RedirectSyncResponse convert(RedirectServerSync redirectSyncResponse) {
        if (redirectSyncResponse == null) {
            return null;
        }
        return new RedirectSyncResponse(redirectSyncResponse.getDnsName());
    }

    private static ProfileSyncResponse convert(ProfileServerSync profileSyncResponse) {
        if (profileSyncResponse == null) {
            return null;
        }
        return new ProfileSyncResponse(convert(profileSyncResponse.getResponseStatus()));
    }

    private static SyncResponseStatus convert(org.kaaproject.kaa.common.endpoint.protocol.SyncResponseStatus responseStatus) {
        if (responseStatus == null) {
            return null;
        }
        switch (responseStatus) {
        case DELTA:
            return SyncResponseStatus.DELTA;
        case NO_DELTA:
            return SyncResponseStatus.NO_DELTA;
        case RESYNC:
            return SyncResponseStatus.RESYNC;
        default:
            return null;
        }
    }

    private static ConfigurationSyncResponse convert(ConfigurationServerSync source) {
        if (source == null) {
            return null;
        }
        ConfigurationSyncResponse sync = new ConfigurationSyncResponse();
        sync.setAppStateSeqNumber(source.getAppStateSeqNumber());
        sync.setConfDeltaBody(source.getConfDeltaBody());
        sync.setConfSchemaBody(source.getConfSchemaBody());
        sync.setResponseStatus(convert(source.getResponseStatus()));
        return sync;
    }

    private static NotificationSyncResponse convert(NotificationServerSync source) {
        if (source == null) {
            return null;
        }
        NotificationSyncResponse sync = new NotificationSyncResponse();
        sync.setAppStateSeqNumber(source.getAppStateSeqNumber());
        sync.setResponseStatus(convert(source.getResponseStatus()));
        if (source.getAvailableTopics() != null) {
            List<Topic> topics = new ArrayList<>(source.getAvailableTopics().size());
            for (org.kaaproject.kaa.common.endpoint.protocol.Topic topic : source.getAvailableTopics()) {
                topics.add(new Topic(topic.getId(), topic.getName(), convert(topic.getSubscriptionType())));
            }
            sync.setAvailableTopics(topics);
        }
        if (source.getNotifications() != null) {
            List<Notification> notifications = new ArrayList<>(source.getNotifications().size());
            for (org.kaaproject.kaa.common.endpoint.protocol.Notification notification : source.getNotifications()) {
                notifications.add(new Notification(notification.getTopicId(), convert(notification.getType()), notification.getUid(),
                        notification.getSeqNumber(), notification.getBody()));
            }
            sync.setNotifications(notifications);
        }
        return sync;
    }

    private static EventSyncResponse convert(EventServerSync source) {
        if (source == null) {
            return null;
        }
        EventSyncResponse sync = new EventSyncResponse();
        if (source.getEventSequenceNumberResponse() != null) {
            sync.setEventSequenceNumberResponse(new EventSequenceNumberResponse(source.getEventSequenceNumberResponse().getSeqNum()));
        }
        if (source.getEvents() != null) {
            List<org.kaaproject.kaa.common.endpoint.gen.Event> events = new ArrayList<>(source.getEvents().size());
            for (Event event : source.getEvents()) {
                events.add(convert(event));
            }
            sync.setEvents(events);
        }
        if (source.getEventListenersResponses() != null) {
            List<EventListenersResponse> responses = new ArrayList<>(source.getEventListenersResponses().size());
            for (org.kaaproject.kaa.common.endpoint.protocol.EventListenersResponse response : source.getEventListenersResponses()) {
                responses.add(new EventListenersResponse(response.getRequestId(), response.getListeners(), convert(response.getResult())));
            }
            sync.setEventListenersResponses(responses);
        }
        return sync;
    }

    public static org.kaaproject.kaa.common.endpoint.gen.Event convert(Event event) {
        if(event == null){
            return null;
        }
        return new org.kaaproject.kaa.common.endpoint.gen.Event(event.getSeqNum(), event.getEventClassFQN(), event
                .getEventData(), event.getSource(), event.getTarget());
    }

    private static UserSyncResponse convert(UserServerSync source) {
        if (source == null) {
            return null;
        }
        UserSyncResponse sync = new UserSyncResponse();
        if (source.getUserAttachNotification() != null) {
            sync.setUserAttachNotification(new UserAttachNotification(source.getUserAttachNotification().getUserExternalId(), source
                    .getUserAttachNotification().getEndpointAccessToken()));
        }
        if(source.getUserDetachNotification() != null){
            sync.setUserDetachNotification(new UserDetachNotification(source.getUserDetachNotification().getEndpointAccessToken()));
        }
        if(source.getUserAttachResponse() != null){
            sync.setUserAttachResponse(new UserAttachResponse(convert(source.getUserAttachResponse().getResult())));
        }
        if(source.getEndpointAttachResponses() != null){
            List<EndpointAttachResponse> responses = new ArrayList<>(source.getEndpointAttachResponses().size());
            for(org.kaaproject.kaa.common.endpoint.protocol.EndpointAttachResponse response : source.getEndpointAttachResponses()){
                responses.add(new EndpointAttachResponse(response.getRequestId(), response.getEndpointKeyHash(), convert(response.getResult())));
            }
            sync.setEndpointAttachResponses(responses);
        }
        if(source.getEndpointDetachResponses() != null){
            List<EndpointDetachResponse> responses = new ArrayList<>(source.getEndpointDetachResponses().size());
            for(org.kaaproject.kaa.common.endpoint.protocol.EndpointDetachResponse response : source.getEndpointDetachResponses()){
                responses.add(new EndpointDetachResponse(response.getRequestId(), convert(response.getResult())));
            }
            sync.setEndpointDetachResponses(responses);
        }
        return sync;
    }

    private static NotificationType convert(org.kaaproject.kaa.common.endpoint.protocol.NotificationType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
        case SYSTEM:
            return NotificationType.SYSTEM;
        case CUSTOM:
            return NotificationType.CUSTOM;
        default:
            return null;
        }
    }

    private static SubscriptionType convert(org.kaaproject.kaa.common.endpoint.protocol.SubscriptionType subscriptionType) {
        if (subscriptionType == null) {
            return null;
        }
        switch (subscriptionType) {
        case MANDATORY:
            return SubscriptionType.MANDATORY;
        case OPTIONAL:
            return SubscriptionType.OPTIONAL;
        default:
            return null;
        }
    }

    private static LogSyncResponse convert(LogServerSync source) {
        if (source == null) {
            return null;
        }
        LogSyncResponse sync = new LogSyncResponse();
        sync.setRequestId(source.getRequestId());
        sync.setResult(convert(source.getResult()));
        return sync;
    }

    private static ClientSyncMetaData convert(SyncRequestMetaData source) {
        if (source == null) {
            return null;
        }
        return new ClientSyncMetaData(source.getApplicationToken(), source.getEndpointPublicKeyHash(), source.getProfileHash(),
                source.getTimeout());
    }

    private static ProfileClientSync convert(ProfileSyncRequest source) {
        if (source == null) {
            return null;
        }
        ProfileClientSync sync = new ProfileClientSync();
        sync.setEndpointAccessToken(source.getEndpointAccessToken());
        sync.setEndpointPublicKey(source.getEndpointPublicKey());
        sync.setProfileBody(source.getProfileBody());
        sync.setVersionInfo(convert(source.getVersionInfo()));
        return sync;
    }

    private static EndpointVersionInfo convert(org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo source) {
        if (source == null) {
            return null;
        }
        EndpointVersionInfo dest = new EndpointVersionInfo();
        dest.setConfigVersion(source.getConfigVersion());
        dest.setLogSchemaVersion(source.getLogSchemaVersion());
        dest.setProfileVersion(source.getProfileVersion());
        dest.setSystemNfVersion(source.getSystemNfVersion());
        dest.setUserNfVersion(source.getUserNfVersion());
        if (source.getEventFamilyVersions() != null) {
            List<EventClassFamilyVersionInfo> ecfVersions = new ArrayList<>(source.getEventFamilyVersions().size());
            for (org.kaaproject.kaa.common.endpoint.gen.EventClassFamilyVersionInfo ecfVersion : source.getEventFamilyVersions()) {
                ecfVersions.add(new EventClassFamilyVersionInfo(ecfVersion.getName(), ecfVersion.getVersion()));
            }
            dest.setEventFamilyVersions(ecfVersions);
        }
        return dest;
    }

    private static ConfigurationClientSync convert(ConfigurationSyncRequest source) {
        if (source == null) {
            return null;
        }
        return new ConfigurationClientSync(source.getAppStateSeqNumber(), source.getConfigurationHash());
    }

    private static NotificationClientSync convert(NotificationSyncRequest source) {
        if (source == null) {
            return null;
        }
        NotificationClientSync sync = new NotificationClientSync();
        sync.setAppStateSeqNumber(source.getAppStateSeqNumber());
        sync.setTopicListHash(source.getTopicListHash());
        if (source.getAcceptedUnicastNotifications() != null) {
            sync.setAcceptedUnicastNotifications(new ArrayList<String>(source.getAcceptedUnicastNotifications()));
        }
        if (source.getSubscriptionCommands() != null) {
            List<SubscriptionCommand> commands = new ArrayList<SubscriptionCommand>(source.getSubscriptionCommands().size());
            for (org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand command : source.getSubscriptionCommands()) {
                SubscriptionCommand copy = new SubscriptionCommand();
                copy.setTopicId(command.getTopicId());
                switch (command.getCommand()) {
                case ADD:
                    copy.setCommand(SubscriptionCommandType.ADD);
                    break;
                case REMOVE:
                    copy.setCommand(SubscriptionCommandType.REMOVE);
                    break;
                }
                commands.add(copy);
            }
            sync.setSubscriptionCommands(commands);
        }
        if (source.getTopicStates() != null) {
            List<TopicState> states = new ArrayList<TopicState>(source.getTopicStates().size());
            for (org.kaaproject.kaa.common.endpoint.gen.TopicState state : source.getTopicStates()) {
                states.add(new TopicState(state.getTopicId(), state.getSeqNumber()));
            }
        }
        return sync;
    }

    private static EventClientSync convert(EventSyncRequest source) {
        if (source == null) {
            return null;
        }
        EventClientSync sync = new EventClientSync();
        if (source.getEventSequenceNumberRequest() != null) {
            sync.setEventSequenceNumberRequest(new EventSequenceNumberRequest());
        }
        if (source.getEvents() != null) {
            List<Event> events = new ArrayList<Event>(source.getEvents().size());
            for (org.kaaproject.kaa.common.endpoint.gen.Event event : source.getEvents()) {
                events.add(convert(event));
            }
            sync.setEvents(events);
        }
        if (source.getEventListenersRequests() != null) {
            List<EventListenersRequest> requests = new ArrayList<EventListenersRequest>(source.getEventListenersRequests().size());
            for (org.kaaproject.kaa.common.endpoint.gen.EventListenersRequest request : source.getEventListenersRequests()) {
                requests.add(new EventListenersRequest(request.getRequestId(), request.getEventClassFQNs()));
            }
            sync.setEventListenersRequests(requests);
        }

        return sync;
    }

    public static Event convert(org.kaaproject.kaa.common.endpoint.gen.Event event) {
        if(event == null){
            return null;
        }
        return new Event(event.getSeqNum(), event.getEventClassFQN(), event.getEventData(), event.getSource(), event
                .getTarget());
    }

    private static LogClientSync convert(LogSyncRequest source) {
        if (source == null) {
            return null;
        }
        LogClientSync sync = new LogClientSync();
        sync.setRequestId(source.getRequestId());
        if (source.getLogEntries() != null) {
            List<LogEntry> logs = new ArrayList<LogEntry>(source.getLogEntries().size());
            for (org.kaaproject.kaa.common.endpoint.gen.LogEntry log : source.getLogEntries()) {
                logs.add(new LogEntry(log.getData()));
            }
            sync.setLogEntries(logs);
        }
        return sync;
    }

    private static UserClientSync convert(UserSyncRequest source) {
        if(source == null){
            return null;
        }
        UserClientSync sync = new UserClientSync();
        if (source.getUserAttachRequest() != null) {
            sync.setUserAttachRequest(new UserAttachRequest(source.getUserAttachRequest().getUserExternalId(), source
                    .getUserAttachRequest().getUserAccessToken()));
        }
        if (source.getEndpointAttachRequests() != null) {
            List<EndpointAttachRequest> requests = new ArrayList<>(source.getEndpointAttachRequests().size());
            for (org.kaaproject.kaa.common.endpoint.gen.EndpointAttachRequest request : source.getEndpointAttachRequests()) {
                requests.add(new EndpointAttachRequest(request.getRequestId(), request.getEndpointAccessToken()));
            }
            sync.setEndpointAttachRequests(requests);
        }
        if (source.getEndpointDetachRequests() != null) {
            List<EndpointDetachRequest> requests = new ArrayList<>(source.getEndpointDetachRequests().size());
            for (org.kaaproject.kaa.common.endpoint.gen.EndpointDetachRequest request : source.getEndpointDetachRequests()) {
                requests.add(new EndpointDetachRequest(request.getRequestId(), request.getEndpointKeyHash()));
            }
            sync.setEndpointDetachRequests(requests);
        }
        return sync;
    }
}
