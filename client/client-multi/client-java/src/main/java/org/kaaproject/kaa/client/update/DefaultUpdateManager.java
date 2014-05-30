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

package org.kaaproject.kaa.client.update;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.TransportExceptionHandler;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.profile.SerializedProfileContainer;
import org.kaaproject.kaa.client.transport.OperationsTransport;
import org.kaaproject.kaa.client.update.commands.Command;
import org.kaaproject.kaa.client.update.commands.CommandFactory;
import org.kaaproject.kaa.client.update.commands.DefaultCommandFactory;
import org.kaaproject.kaa.client.update.strategies.LongPollUpdateStrategy;
import org.kaaproject.kaa.client.update.strategies.PollingTaskContainer;
import org.kaaproject.kaa.client.update.strategies.UpdateStrategy;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link UpdateManager} implementation.
 *
 * @author Yaroslav Zeygerman
 *
 */
public final class DefaultUpdateManager implements UpdateManager, PollingTaskContainer {
    private static final long TIMEOUT = 60000L;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUpdateManager.class);

    private final KaaClientProperties properties;
    private OperationsTransport transport;
    private final KaaClientState state;
    private final List<UpdateListener> listeners;
    private SerializedProfileContainer profileContainer;
    private final Set<String> acceptedUnicastNotificationIds;
    private final List<SubscriptionCommand> notificationCommands = new LinkedList<SubscriptionCommand>();
    private final List<SubscriptionCommand> sentNotificationCommands = new LinkedList<SubscriptionCommand>();
    private final CommandFactory commandFactory;
    private final boolean schedulePolling;
    private final UpdateStrategy pollStrategy;
    private volatile boolean failover = false;

    public DefaultUpdateManager(KaaClientProperties properties, KaaClientState state) {
        this(properties, state, true);
    }

    public DefaultUpdateManager(KaaClientProperties properties, KaaClientState state, boolean schedulePolling) {
        super();
        this.properties = new KaaClientProperties(properties);
        this.listeners = new LinkedList<UpdateListener>();
        this.acceptedUnicastNotificationIds = new HashSet<>();
        this.state = state;
        this.commandFactory = new DefaultCommandFactory(this, this.state, this.properties);
        this.schedulePolling = schedulePolling;
        this.pollStrategy = new LongPollUpdateStrategy(this);
    }

    private void updateAppStateSeqNumber(SyncResponse response) {
        state.setAppStateSeqNumber(response.getAppStateSeqNumber());
    }

    private void updateNotificationStatus(SyncResponse response) {
        if (response.getNotificationSyncResponse() != null) {
            NotificationSyncResponse nfResponse = response.getNotificationSyncResponse();
            if (nfResponse.getAvailableTopics() != null) {
                for (Topic topic : nfResponse.getAvailableTopics()) {
                    state.addTopic(topic);
                }
            }
            if (nfResponse.getNotifications() != null) {
                for (Notification notification : nfResponse.getNotifications()) {
                    LOG.info("Received {}", notification);
                    if (notification.getUid() != null) {
                        LOG.info("Adding {} to unicast accepted notifications", notification.getUid());
                        acceptedUnicastNotificationIds.add(notification.getUid());
                    } else{
                        state.updateTopicSubscriptionInfo(notification.getTopicId(), notification.getSeqNumber());
                    }
                }
            }
        }
    }

    private boolean isProfileHashOutdated(byte [] newProfile) throws IOException {
        EndpointObjectHash profileHash = EndpointObjectHash.fromSHA1(newProfile);
        return !profileHash.equals(state.getProfileHash());
    }

    @Override
    public void start() throws IOException {
        byte [] serializedProfile = profileContainer.getSerializedProfile();
        if (state.isRegistered()) {
            if (isProfileHashOutdated(serializedProfile)) {
                startProfileUpdate(serializedProfile);
            } else {
                startPoll();
            }
        } else {
            startRegister(serializedProfile);
        }
    }

    @Override
    public void onProfileChange(byte[] newProfile) {
        try {
            if (state.isRegistered() && isProfileHashOutdated(newProfile)) {
                startProfileUpdate(newProfile);
            }
        } catch (Exception e) {
            LOG.error("Exception caught while processing profile update: {}", e.toString());
        }
    }

    @Override
    public boolean addUpdateListener(UpdateListener listener) {
        if (listener == null) {
            LOG.warn("Can't add null listener");
            return false;
        }
        if (!listeners.contains(listener)) {
            LOG.info("Adding update listener {}", listener);
            return listeners.add(listener);
        } else {
            LOG.warn("listener {} already added.", listener);
            return false;
        }
    }

    @Override
    public boolean removeUpdateListener(UpdateListener listener) {
        if (listener == null) {
            LOG.warn("Can't remove null listener");
            return false;
        }
        if (listeners.remove(listener)) {
            LOG.info("Removed update listener {}", listener);
            return true;
        } else {
            LOG.warn("Listener was not registered {}", listener);
            return false;
        }
    }

    @Override
    public void setTransport(OperationsTransport transport) {
        this.transport = transport;
    }

    @Override
    public void setTransportExceptionHandler(TransportExceptionHandler handler) {
        this.commandFactory.setTransportExceptionHandler(handler);
    }

    @Override
    public void stop() {
        pollStrategy.stopPoll();
    }

    @Override
    public void failover(Long milliseconds) throws IOException {
        failover = true;
        byte [] serializedProfile = profileContainer.getSerializedProfile();
        if (!state.isRegistered()) {
            pollStrategy.retryCommand(milliseconds, commandFactory.createRegisterCommand(transport, serializedProfile));
        } else if (isProfileHashOutdated(serializedProfile)) {
            pollStrategy.retryCommand(milliseconds, commandFactory.createProfileUpdateCommand(transport, serializedProfile, acceptedUnicastNotificationIds));
        } else {
            pollStrategy.retryCommand(milliseconds, getNextTask());
        }
    }

    private void notifyListeners(SyncResponse response) {
        for (UpdateListener listener : listeners) {
            try {
                listener.onDeltaUpdate(response);
            } catch (Exception e) {
                LOG.error("Listener failed to process event", e);
            }
        }
    }

    private void startProfileUpdate(byte[] profile) {
        LOG.info("Profile has been updated: {}", profile);

        state.setProfileHash(EndpointObjectHash.fromSHA1(profile));
        transport.abortRequest();
        pollStrategy.stopPoll();
        pollStrategy.executeCommand(commandFactory.createProfileUpdateCommand(transport, profile, acceptedUnicastNotificationIds));
        startPoll();
    }

    private void startRegister(byte[] profile) {
        state.setProfileHash(EndpointObjectHash.fromSHA1(profile));
        pollStrategy.stopPoll();
        pollStrategy.executeCommand(commandFactory.createRegisterCommand(transport, profile));
        startPoll();
    }

    private void startPoll() {
        if (schedulePolling) {
            pollStrategy.startPoll();
        } else {
            LOG.info("Poll scheduler disabled and will not start");
        }
    }

    @Override
    public void setSerializedProfileContainer(SerializedProfileContainer container) {
        this.profileContainer = container;
    }

    @Override
    public void onSyncResponse(SyncResponse response) {
        LOG.info("SyncResponse received: {}", response.toString());
        LOG.info("SyncResponse new seq number: {}", response.getAppStateSeqNumber());

        if (!state.isRegistered()) {
            state.setRegistered(true);
        }

        if (response.getResponseType() == SyncResponseStatus.PROFILE_RESYNC) {
            try {
                byte[] profile = profileContainer.getSerializedProfile();
                startProfileUpdate(profile);
            } catch (IOException e) {
                LOG.error("Failed to retrieve profile.");
            }
        }

        acceptedUnicastNotificationIds.clear();
        updateAppStateSeqNumber(response);
        updateNotificationStatus(response);
        notifyListeners(response);
        sentNotificationCommands.clear();
        state.persist();
        if (failover) {
            failover = false;
            pollStrategy.startPoll();
        }
    }

    @Override
    public void updateSubscriptionCommands(List<SubscriptionCommand> subscriptions) {
        if (subscriptions != null && !subscriptions.isEmpty()) {
            synchronized (notificationCommands) {
                notificationCommands.addAll(subscriptions);

                if (!failover) {
                    LOG.info("Voluntary subscription updated. Restarting poll...");

                    try {
                        transport.abortRequest();
                    } catch (Exception e) {
                        LOG.error("Failed to abort previous request..", e);
                    }

                    pollStrategy.stopPoll();
                    startPoll();
                }
            }
        }
    }

    @Override
    public Command getNextTask() {
        synchronized (notificationCommands) {
            sentNotificationCommands.addAll(notificationCommands);
            notificationCommands.clear();
        }
        return commandFactory.createLongPollCommand(transport, TIMEOUT,
                acceptedUnicastNotificationIds, sentNotificationCommands);
    }
}
