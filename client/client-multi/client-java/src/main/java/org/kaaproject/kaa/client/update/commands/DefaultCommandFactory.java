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

package org.kaaproject.kaa.client.update.commands;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.TransportExceptionHandler;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.transport.OperationsTransport;
import org.kaaproject.kaa.client.update.UpdateManager;
import org.kaaproject.kaa.client.update.commands.Command;
import org.kaaproject.kaa.client.update.commands.LongPollCommand;
import org.kaaproject.kaa.client.update.commands.PollCommand;
import org.kaaproject.kaa.client.update.commands.ProfileUpdateCommand;
import org.kaaproject.kaa.client.update.commands.RegisterCommand;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.LongSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.TopicState;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link CommandFactory} implementation.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultCommandFactory implements CommandFactory {
    
    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultCommandFactory.class);

    private final UpdateManager manager;
    private final KaaClientState state;
    private final KaaClientProperties properties;

    private TransportExceptionHandler handler;

    public DefaultCommandFactory(UpdateManager manager, KaaClientState state, KaaClientProperties properties) {
        this.manager = manager;
        this.state = state;
        this.properties = properties;
    }

    private SyncRequest createSyncRequest(
            Set<String> acceptedUnicastNotificationIds,
            List<SubscriptionCommand> notificationCommands) {
        SyncRequest request = new SyncRequest();

        request.setApplicationToken(properties.getApplicationToken());
        request.setAppStateSeqNumber(state.getAppStateSeqNumber());

        ByteBuffer publicKeyBuffer = ByteBuffer.wrap(EndpointObjectHash.fromSHA1(state.getPublicKey().getEncoded()).getData());
        request.setEndpointPublicKeyHash(publicKeyBuffer);

        ByteBuffer configurationHashBuffer = ByteBuffer.wrap(state.getConfigurationHash().getData());
        LOG.info("Configuration hash: {}", Arrays.toString(configurationHashBuffer.array()));
        request.setConfigurationHash(configurationHashBuffer);

        ByteBuffer profileHashBuffer = ByteBuffer.wrap(state.getProfileHash().getData());
        LOG.info("Profile hash: {}", Arrays.toString(profileHashBuffer.array()));
        request.setProfileHash(profileHashBuffer);

        // TODO: implement topic's id list hash calculation
        request.setTopicListHash(profileHashBuffer);

        request.setTopicStates(getTopicStates());

        if(!acceptedUnicastNotificationIds.isEmpty()){
            LOG.info("Accepted unicast Notifications: {}", acceptedUnicastNotificationIds.size());
            request.setAcceptedUnicastNotifications(new ArrayList<>(acceptedUnicastNotificationIds));
        }
        List<SubscriptionCommand> commandsToSend = null;
        if (!notificationCommands.isEmpty()) {
            commandsToSend = new LinkedList<SubscriptionCommand>(notificationCommands);
        }
        if (commandsToSend != null) {
            request.setSubscriptionCommands(commandsToSend);
        }
        return request;
    }

    @Override
    public void setTransportExceptionHandler(TransportExceptionHandler handler) {
        this.handler = handler;
    }

    @Override
    public Command createRegisterCommand(OperationsTransport transport, byte [] profile) {
        EndpointRegistrationRequest request = new EndpointRegistrationRequest();
        request.setApplicationToken(properties.getApplicationToken());
        request.setVersionInfo(new EndpointVersionInfo(properties.getSupportedConfigVersion(), properties.getSupportedProfileVersion(),
                properties.getSupportedSystemNTVersion(), properties.getSupportedUserNTVersion()));
        ByteBuffer publicKeyBuffer = ByteBuffer.wrap(state.getPublicKey().getEncoded());
        request.setEndpointPublicKey(publicKeyBuffer);
        request.setProfileBody(ByteBuffer.wrap(profile));
        return new RegisterCommand(request, manager, transport, handler);
    }

    @Override
    public Command createProfileUpdateCommand(OperationsTransport transport, byte [] profile, Set<String> acceptedUnicastNotificationIds) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setApplicationToken(properties.getApplicationToken());
        request.setVersionInfo(new EndpointVersionInfo(properties.getSupportedConfigVersion(), properties.getSupportedProfileVersion(),
                properties.getSupportedSystemNTVersion(), properties.getSupportedUserNTVersion()));
        ByteBuffer publicKeyBuffer = ByteBuffer.wrap(EndpointObjectHash.fromSHA1(state.getPublicKey().getEncoded()).getData());
        request.setEndpointPublicKeyHash(publicKeyBuffer);
        request.setProfileBody(ByteBuffer.wrap(profile));
        request.setTopicStates(getTopicStates());
        if(!acceptedUnicastNotificationIds.isEmpty()){
            LOG.info("Accepted unicast Notifications: {}", acceptedUnicastNotificationIds.size());
            request.setAcceptedUnicastNotifications(new ArrayList<>(acceptedUnicastNotificationIds));
        }
        return new ProfileUpdateCommand(request, manager, transport, handler);
    }

    @Override
    public Command createPollCommand(OperationsTransport transport,
            Set<String> acceptedUnicastNotificationIds,
            List<SubscriptionCommand> notificationCommands) {
        SyncRequest request = createSyncRequest(acceptedUnicastNotificationIds, notificationCommands);
        return new PollCommand(request, manager, transport, handler);
    }

    @Override
    public Command createLongPollCommand(OperationsTransport transport,
            Long timeout, Set<String> acceptedUnicastNotificationIds,
            List<SubscriptionCommand> notificationCommands) {
        SyncRequest request = createSyncRequest(acceptedUnicastNotificationIds, notificationCommands);
        return new LongPollCommand(new LongSyncRequest(request, timeout), manager, transport, handler);
    }

    protected List<TopicState> getTopicStates() {
        List<TopicState> states = null;
        Map<String, Integer> nfSubscriptions = state.getNfSubscriptions();
        if(!nfSubscriptions.isEmpty()){
            states = new ArrayList<>();
            LOG.info("Topic States:");
            for(Entry<String, Integer> nfSubscription : nfSubscriptions.entrySet()){
                TopicState state = new TopicState(nfSubscription.getKey(), nfSubscription.getValue());
                states.add(state);
                LOG.info("{} : {}", state.getTopicId(), state.getSeqNumber());
            }

        }
        return states;
    }
}
