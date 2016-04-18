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

package org.kaaproject.kaa.server.operations.service.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TimeZone;

import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.NotificationService;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationRequest;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationResponse;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.TopicListCacheEntry;
import org.kaaproject.kaa.server.sync.SubscriptionCommand;
import org.kaaproject.kaa.server.sync.SubscriptionCommandType;
import org.kaaproject.kaa.server.sync.TopicState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class DefaultNotificationDeltaService.
 */
@Service
public class DefaultNotificationDeltaService implements NotificationDeltaService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationDeltaService.class);

    /** The notification service. */
    @Autowired
    private NotificationService notificationService;

    /** The topic service. */
    @Autowired
    private TopicService topicService;

    /** The endpoint service. */
    @Autowired
    private EndpointService endpointService;

    /** The cache service. */
    @Autowired
    CacheService cacheService;

    @Override
    public TopicListCacheEntry getTopicListHash(String appToken, String endpointId, EndpointProfileDto profile) {
        LOG.debug("[{}][{}] Calculating new topic list", appToken, endpointId);
        List<TopicDto> topics = recalculateTopicList(profile.getGroupState());
        Collections.sort(topics);
        long[] ids = new long[topics.size()];
        StringJoiner joiner = new StringJoiner("|");
        for (int i = 0; i < topics.size(); i++) {
            Long id = Long.valueOf(topics.get(i).getId());
            ids[i] = id.longValue();
            joiner.add(id.toString());
        }
        int simpleHash = Arrays.hashCode(ids);
        EndpointObjectHash complexHash = EndpointObjectHash.fromBytes(SHA1HashUtils.hashToBytes(joiner.toString()));
        TopicListCacheEntry entry = new TopicListCacheEntry(simpleHash, complexHash, topics);
        cacheService.putTopicList(complexHash, entry);
        LOG.debug("[{}][{}] Calculated new topic list {}", appToken, endpointId, entry);
        return entry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.notification.
     * NotificationDeltaService
     * #getNotificationDelta(org.kaaproject.kaa.server.operations
     * .pojo.GetNotificationRequest,
     * org.kaaproject.kaa.server.operations.service.delta.HistoryDelta)
     */
    @Override
    public GetNotificationResponse getNotificationDelta(GetNotificationRequest request) {
        String endpointId = Base64Util.encode(request.getProfile());

        GetNotificationResponse response = new GetNotificationResponse();
        EndpointProfileDto profile = request.getProfile();
        Set<String> subscriptionSet = buildSubscriptionSet(profile);
        boolean subscriptionSetChanged = false;

        if (request.getTopicHash() != profile.getSimpleTopicHash()) {
            LOG.debug("[{}] Topic list changed. recalculating topic list", endpointId);
            TopicListCacheEntry topicListCache = cacheService.getTopicListByHash(EndpointObjectHash.fromBytes(profile.getTopicHash()));
            List<TopicDto> topicList = topicListCache.getTopics();
            LOG.debug("[{}] New topic list contains {} topics", endpointId, topicList.size());
            List<String> allPossibleTopics = new ArrayList<>(topicList.size());

            for (TopicDto topic : topicList) {
                allPossibleTopics.add(topic.getId());
                if (topic.getType() == TopicTypeDto.MANDATORY) {
                    if (subscriptionSet.add(topic.getId())) { // NOSONAR
                        subscriptionSetChanged = true;
                        LOG.debug("[{}] added subscription for mandatory topic id: {}, name: {}", endpointId, topic.getId(),
                                topic.getName());
                    }
                }
            }
            // Remove all topics that are outdated after latest history changes;
            if (subscriptionSet.retainAll(allPossibleTopics)) {
                subscriptionSetChanged = true;
            }

            response.setTopicList(topicList);
        }

        if (request.getSubscriptionCommands() != null) {
            for (SubscriptionCommand sCommand : request.getSubscriptionCommands()) {
                if (sCommand.getCommand() == SubscriptionCommandType.ADD) {
                    if (subscriptionSet.add(sCommand.getTopicId())) {
                        LOG.debug("[{}] added subscription for topic id: {} based on client request", endpointId, sCommand.getTopicId());
                        subscriptionSetChanged = true;
                    }
                } else {
                    if (subscriptionSet.remove(sCommand.getTopicId())) {
                        LOG.debug("[{}] removed subscription for topic id: {} based on client request", endpointId, sCommand.getTopicId());
                        subscriptionSetChanged = true;
                    }
                }
            }
        }

        Map<String, Integer> subscriptionStates = buildTopicStateMap(request, subscriptionSet);

        long now = new GregorianCalendar(TimeZone.getTimeZone("UTC")).getTimeInMillis();

        List<NotificationDto> notifications = new ArrayList<>();
        for (String topicId : subscriptionSet) {
            int seqNumber = subscriptionStates.get(topicId);
            LOG.debug(
                    "[{}] fetch new subscriptions for topic id: {}, system schema version {}, user schema version {}, starting seq number {}",
                    endpointId, topicId, profile.getSystemNfVersion(), profile.getUserNfVersion(), seqNumber);
            List<NotificationDto> topicNotifications = notificationService.findNotificationsByTopicIdAndVersionAndStartSecNum(topicId,
                    seqNumber, profile.getSystemNfVersion(), profile.getUserNfVersion());
            if (topicNotifications != null) {
                int count = 0;
                for (NotificationDto notification : topicNotifications) {
                    seqNumber = Math.max(seqNumber, notification.getSecNum());
                    Date date = notification.getExpiredAt();
                    if (date != null && date.getTime() > now) {
                        notifications.add(notification);
                        count++;
                    }
                }
                LOG.debug("[{}] detected {} new subscriptions for topic id: {} ", endpointId, count, topicId);
                subscriptionStates.put(topicId, seqNumber);
            }
        }

        if (request.getAcceptedUnicastNotifications() != null) {
            for (String acceptedUnicastId : request.getAcceptedUnicastNotifications()) {
                notificationService.removeUnicastNotificationById(acceptedUnicastId);
                LOG.debug("[{}] deleted accepted unicast notification {} ", endpointId, acceptedUnicastId);
            }
        }

        List<EndpointNotificationDto> unicastNotifications = notificationService.findUnicastNotificationsByKeyHash(request.getProfile()
                .getEndpointKeyHash());
        for (EndpointNotificationDto unicastNotification : unicastNotifications) {
            LOG.debug("[{}] detected new unicast notification: {} ", endpointId, unicastNotification.getId());
            LOG.trace("[{}] detected new unicast notification: {} ", endpointId, unicastNotification);
            NotificationDto notificationDto = unicastNotification.getNotificationDto();
            if (notificationDto != null) {
                Date date = notificationDto.getExpiredAt();
                if (date != null && date.getTime() > now) {
                    LOG.trace("[{}] notification expiration time is {}({}) which is later then {}", endpointId, date.getTime(), date, now);
                    notificationDto.setId(unicastNotification.getId());
                    notifications.add(notificationDto);
                }
            }
        }

        response.setNotifications(notifications);
        response.setSubscriptionStates(subscriptionStates);

        if (subscriptionSetChanged) {
            LOG.debug("[{}] Updating profile with subscription set. Size {}", endpointId, subscriptionSet.size());
            response.setSubscriptionSetChanged(true);
            response.setSubscriptionSet(subscriptionSet);
        }

        return response;
    }

    /**
     * Convert unicast notification.
     *
     * @param unicastNotification
     *            the unicast notification
     * @return the notification dto
     */
    private NotificationDto convertUnicastNotification(EndpointNotificationDto unicastNotification) {
        NotificationDto dto = unicastNotification.getNotificationDto();
        dto.setId(unicastNotification.getId());
        return dto;
    }

    /**
     * Builds the topic state map.
     *
     * @param request
     *            the request
     * @param subscriptionSet
     *            the subscription set
     * @return the map
     */
    private Map<String, Integer> buildTopicStateMap(GetNotificationRequest request, Set<String> subscriptionSet) {
        Map<String, Integer> topicStates = new HashMap<String, Integer>();
        if (request.getTopicStates() != null) {
            for (TopicState topicState : request.getTopicStates()) {
                topicStates.put(topicState.getTopicId(), topicState.getSeqNumber());
            }
        }
        Map<String, Integer> subscriptionStates = new HashMap<String, Integer>();
        for (String subscription : subscriptionSet) {
            Integer seqNumber = topicStates.get(subscription);
            subscriptionStates.put(subscription, seqNumber != null ? seqNumber : 0);
        }

        return subscriptionStates;
    }

    /**
     * Recalculate topic list.
     *
     * @param historyDelta
     *            the history delta
     * @return the list
     */
    private List<TopicDto> recalculateTopicList(List<EndpointGroupStateDto> groups) {
        Set<TopicDto> topicSet = new HashSet<TopicDto>();
        for (EndpointGroupStateDto egs : groups) {
            EndpointGroupDto endpointGroup = cacheService.getEndpointGroupById(egs.getEndpointGroupId());
            if (endpointGroup.getTopics() != null) {
                for (String topicId : endpointGroup.getTopics()) {
                    TopicDto topic = cacheService.getTopicById(topicId);
                    topicSet.add(topic);
                }
            }
        }
        List<TopicDto> topicList = new ArrayList<TopicDto>(topicSet);
        Collections.sort(topicList, new Comparator<TopicDto>() {
            @Override
            public int compare(TopicDto o1, TopicDto o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return topicList;
    }

    /**
     * Builds the subscription set.
     *
     * @param profile
     *            the profile
     * @return the sets the
     */
    private Set<String> buildSubscriptionSet(EndpointProfileDto profile) {
        return profile.getSubscriptions() != null ? new HashSet<String>(profile.getSubscriptions()) : new HashSet<String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.notification.
     * NotificationDeltaService#findNotificationById(java.lang.String)
     */
    @Override
    public NotificationDto findNotificationById(String notificationId) {
        return notificationService.findNotificationById(notificationId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.notification.
     * NotificationDeltaService#findUnicastNotificationById(java.lang.String)
     */
    @Override
    public NotificationDto findUnicastNotificationById(String unicastNotificationId) {
        EndpointNotificationDto notification = notificationService.findUnicastNotificationById(unicastNotificationId);
        if (notification == null) {
            return null;
        } else {
            return convertUnicastNotification(notification);
        }
    }
}
