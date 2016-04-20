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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicUnsubscriptionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicSubscriptionMessage;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * The Class TopicActor.
 */
public class TopicActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TopicActor.class);

    /** The notification service. */
    private final NotificationDeltaService notificationService;

    /** The endpoint sessions. */
    private final Map<String, ActorInfo> endpointSessions;

    /** The notification cache. */
    private final TreeMap<Integer, NotificationDto> notificationCache; // NOSONAR

    /**
     * Instantiates a new topic actor.
     *
     * @param notificationService
     *            the notification service
     */
    public TopicActor(NotificationDeltaService notificationService) {
        this.notificationService = notificationService;
        this.endpointSessions = new HashMap<>();
        this.notificationCache = new TreeMap<>();
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<TopicActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The notification service. */
        private final NotificationDeltaService notificationService;

        /**
         * Instantiates a new actor creator.
         *
         * @param notificationService
         *            the notification service
         */
        public ActorCreator(NotificationDeltaService notificationService) {
            super();
            this.notificationService = notificationService;
        }

        /*
         * (non-Javadoc)
         * 
         * @see akka.japi.Creator#create()
         */
        @Override
        public TopicActor create() throws Exception {
            return new TopicActor(notificationService);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("Received: {}", message);
        if (message instanceof EndpointAwareMessage) {
            if (message instanceof TopicSubscriptionMessage) {
                processEndpointRegistration((TopicSubscriptionMessage) message);
            } else if (message instanceof TopicUnsubscriptionMessage) {
                processEndpointDeregistration((TopicUnsubscriptionMessage) message);
            }
        } else if (message instanceof Terminated) {
            processTermination((Terminated) message);
        } else if (message instanceof ThriftNotificationMessage) {
            broadcastToAllEndpoints((ThriftNotificationMessage) message);
        }
    }

    /**
     * Process endpoint registration.
     *
     * @param message
     *            the message
     */
    private void processEndpointRegistration(TopicSubscriptionMessage message) {
        ActorRef endpointActor = message.getOriginator();
        Integer seqNum = message.getSeqNumber();
        SortedMap<Integer, NotificationDto> pendingNotificationMap = notificationCache.tailMap(seqNum, false);
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        List<NotificationDto> pendingNotifications = filterMap(pendingNotificationMap, message.getSystemNfSchemaVersion(),
                message.getUserNfSchemaVersion(), calendar);
        if (!pendingNotifications.isEmpty()) {
            LOG.debug("Detected new messages during endpoint subscription!");
            NotificationMessage notificationMessage = NotificationMessage.fromNotifications(pendingNotifications);
            endpointActor.tell(notificationMessage, self());
        } else {
            LOG.debug("No new messages detected. Subscribing endpoint actor to topic actor");
            String endpointKey = message.getOriginator().path().name();
            ActorInfo actorInfo = new ActorInfo(endpointActor, message.getSystemNfSchemaVersion(), message.getUserNfSchemaVersion());
            if (endpointSessions.put(endpointKey, actorInfo) != null) {
                LOG.warn("Detected duplication of registration message: {}", message);
            }
            context().watch(endpointActor);
        }
    }

    private void processEndpointDeregistration(TopicUnsubscriptionMessage message) {
        String endpointKey = message.getOriginator().path().name();
        if (endpointSessions.remove(endpointKey) != null) {
            LOG.debug("Removed subsctioption for endpoint {}", endpointKey);
        } else {
            LOG.warn("Failed to remove subscription for endpoint {} from topic", endpointKey);
        }
    }

    /**
     * Broadcast to all endpoints.
     *
     * @param message
     *            the message
     */
    private void broadcastToAllEndpoints(ThriftNotificationMessage message) {
        String notificationId = message.getNotification().getNotificationId();
        NotificationDto notificationDto = notificationService.findNotificationById(notificationId);
        if (notificationDto == null) {
            LOG.warn("Can't find notification by id {}. Probably it has already expired!");
        } else {
            notificationCache.put(notificationDto.getSecNum(), notificationDto);
            LOG.debug("[{}] Put notification to topic actor cache {}", notificationDto.getTopicId(), notificationDto);
            NotificationMessage notificationMessage = NotificationMessage.fromNotifications(Collections.singletonList(notificationDto));
            for (ActorInfo endpoint : endpointSessions.values()) {
                if (isSchemaVersionMatch(notificationDto, endpoint.getSystemNfVersion(), endpoint.getUserNfVersion())) {
                    endpoint.getActorRef().tell(notificationMessage, self());
                }
            }
        }
    }

    /**
     * Process termination.
     *
     * @param message
     *            the message
     */
    private void processTermination(Terminated message) {
        ActorRef terminated = message.actor();
        if (terminated instanceof LocalActorRef) {
            LocalActorRef localActor = (LocalActorRef) terminated;
            String name = localActor.path().name();
            if (endpointSessions.remove(name) != null) {
                LOG.debug("removed: {}", localActor);
            }
        } else {
            LOG.warn("remove commands for remote actors are not supported yet!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.info("Starting " + this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        LOG.info("Stoped " + this);
    }

    /**
     * Filter map.
     *
     * @param   pendingNotificationMap  the pending notification map
     * @param   systemNfSchemaVersion   the system nf schema version
     * @param   userNfSchemaVersion     the user nf schema version
     * @param   calendar                the calendar
     * @return  the list
     */
    public static List<NotificationDto> filterMap(SortedMap<Integer, NotificationDto> pendingNotificationMap, int systemNfSchemaVersion,
            int userNfSchemaVersion, Calendar calendar) {
        List<NotificationDto> pendingNotifications = new ArrayList<>(pendingNotificationMap.size());

        long now = calendar.getTimeInMillis();

        List<NotificationDto> expiredNotifications = null;
        for (NotificationDto dto : pendingNotificationMap.values()) {
            LOG.trace("Filtering notification {} using system schema version {} and user schema version {}", dto, systemNfSchemaVersion,
                    userNfSchemaVersion);
            Date date = dto.getExpiredAt();
            if (date != null && date.getTime() > now) {
                if (isSchemaVersionMatch(dto, systemNfSchemaVersion, userNfSchemaVersion)) {
                    pendingNotifications.add(dto);
                }
            } else {
                if (expiredNotifications == null) {
                    expiredNotifications = new ArrayList<>();
                }
                expiredNotifications.add(dto);
                LOG.trace("Detected expired notification: {}, nfTime: {}, curTime: {}", dto, date == null ? date : date.getTime(), now);
            }
        }

        if (expiredNotifications != null) {
            LOG.trace("Removing {} notifications from pendingNotificationMap", expiredNotifications.size());
            pendingNotificationMap.values().removeAll(expiredNotifications);
        }
        return pendingNotifications;
    }

    /**
     * Checks if is schema version match.
     *
     * @param notificationDto
     *            the notification dto
     * @param systemNfVersion
     *            the system nf version
     * @param userNfVersion
     *            the user nf version
     * @return true, if is schema version match
     */
    public static boolean isSchemaVersionMatch(NotificationDto notificationDto, int systemNfVersion, int userNfVersion) {
        if (notificationDto.getType() == NotificationTypeDto.SYSTEM) {
            return notificationDto.getNfVersion() == systemNfVersion;
        } else if (notificationDto.getType() == NotificationTypeDto.USER) {
            return notificationDto.getNfVersion() == userNfVersion;
        } else {
            return false;
        }
    }

    /**
     * The Class ActorInfo.
     */
    public static final class ActorInfo {

        /** The actor ref. */
        private final ActorRef actorRef;

        /** The system nf version. */
        private final int systemNfVersion;

        /** The user nf version. */
        private final int userNfVersion;

        /**
         * Instantiates a new actor info.
         *
         * @param actorRef
         *            the actor ref
         * @param systemNfVersion
         *            the system nf version
         * @param userNfVersion
         *            the user nf version
         */
        public ActorInfo(ActorRef actorRef, int systemNfVersion, int userNfVersion) {
            super();
            this.actorRef = actorRef;
            this.systemNfVersion = systemNfVersion;
            this.userNfVersion = userNfVersion;
        }

        /**
         * Gets the actor ref.
         *
         * @return the actor ref
         */
        public ActorRef getActorRef() {
            return actorRef;
        }

        /**
         * Gets the system nf version.
         *
         * @return the system nf version
         */
        public int getSystemNfVersion() {
            return systemNfVersion;
        }

        /**
         * Gets the user nf version.
         *
         * @return the user nf version
         */
        public int getUserNfVersion() {
            return userNfVersion;
        }
    }
}
