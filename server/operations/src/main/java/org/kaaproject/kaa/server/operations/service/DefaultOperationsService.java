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

package org.kaaproject.kaa.server.operations.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.endpoint.gen.ConfSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationRequest;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationResponse;
import org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse.GetDeltaResponseType;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.delta.DeltaService;
import org.kaaproject.kaa.server.operations.service.delta.HistoryDelta;
import org.kaaproject.kaa.server.operations.service.history.HistoryDeltaService;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.kaaproject.kaa.server.operations.service.profile.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The Class DefaultOperationsService.
 */
@Service
public class DefaultOperationsService implements OperationsService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOperationsService.class);

    /** The delta service. */
    @Autowired
    DeltaService deltaService;

    /** The profile service. */
    @Autowired
    ProfileService profileService;

    /** The cache service. */
    @Autowired
    CacheService cacheService;

    /** The delta service. */
    @Autowired
    HistoryDeltaService historyDeltaService;

    /** The notification delta service. */
    @Autowired
    NotificationDeltaService notificationDeltaService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.OperationsService#registerEndpoint
     * (org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest)
     */
    @Override
    public SyncResponseHolder registerEndpoint(EndpointRegistrationRequest request) throws GetDeltaException {
        LOG.debug("register endpoint. request: {}", request);
        String applicationToken = request.getApplicationToken().toString();
        byte[] endpointKey = toByteArray(request.getEndpointPublicKey());
        byte[] profileBody = toByteArray(request.getProfileBody());

        RegisterProfileRequest registerProfileRequest = new RegisterProfileRequest(applicationToken, endpointKey, request.getVersionInfo(),
                profileBody);
        EndpointProfileDto endpointProfile = profileService.registerProfile(registerProfileRequest);
        LOG.debug("profile registered. id: {}, endpointKeyHash: {}", endpointProfile.getId(), endpointProfile.getEndpointKeyHash());
        SyncResponseHolder response = processSync(toSyncRequest(request.getApplicationToken(), endpointProfile), endpointProfile, true);
        LOG.debug("register endpoint. response: {}", response);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.OperationsService#updateEndpoint
     * (org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest)
     */
    @Override
    public SyncResponseHolder updateProfile(ProfileUpdateRequest request) throws GetDeltaException {
        LOG.debug("update endpoint. request: {}", request);
        String applicationToken = request.getApplicationToken().toString();
        EndpointObjectHash endpointKeyHash = EndpointObjectHash.fromBytes(toByteArray(request.getEndpointPublicKeyHash()));
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(applicationToken, endpointKeyHash, request.getProfileBody().array(),
                request.getVersionInfo());
        EndpointProfileDto endpointProfile = profileService.updateProfile(updateRequest);

        LOG.debug("profile updated. id: {}, endpointKeyHash: {}", endpointProfile.getId(), endpointProfile.getEndpointKeyHash());
        SyncRequest syncRequest = toSyncRequest(request.getApplicationToken(), endpointProfile);
        syncRequest.setAcceptedUnicastNotifications(request.getAcceptedUnicastNotifications());
        syncRequest.setTopicStates(request.getTopicStates());
        SyncResponseHolder response = processSync(syncRequest, endpointProfile);
        LOG.debug("update endpoint. response: {}", response);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.OperationsService#sync(org.
     * kaaproject.kaa.common.endpoint.gen.SyncRequest)
     */
    @Override
    public SyncResponseHolder sync(SyncRequest request) throws GetDeltaException {
        return processSync(request, null, false);
    }

    /**
     * Process sync.
     *
     * @param request the request
     * @param profile the profile
     * @return the sync response holder
     * @throws GetDeltaException the get delta exception
     */
    private SyncResponseHolder processSync(SyncRequest request, EndpointProfileDto profile) throws GetDeltaException {
        return processSync(request, profile, false);
    }

    /**
     * Process sync.
     *
     * @param request the request
     * @param profile the profile
     * @param fetchSchema the fetch schema
     * @return the sync response holder
     * @throws GetDeltaException the get delta exception
     */
    private SyncResponseHolder processSync(SyncRequest request, EndpointProfileDto profile, boolean fetchSchema) throws GetDeltaException {
        String endpointId = Base64Util.encode(request.getEndpointPublicKeyHash().array());
        
        LOG.debug("[{}] processing sync request: {}", endpointId, request);

        int curAppSeqNumber = cacheService.getAppSeqNumber(request.getApplicationToken());
        LOG.debug("[{}] fetched app seq number {} for {}", endpointId, curAppSeqNumber, request.hashCode());

        LOG.debug("[{}] fetching profile for {}", endpointId, request.hashCode());
        profile = fetchProfile(request, profile);

        if (!Arrays.equals(profile.getProfileHash(), toByteArray(request.getProfileHash()))) {
            LOG.debug("[{}] Profile hash mismatch. Profile resync needed");
            return buildProfileResyncResponse(request);
        }

        LOG.debug("[{}] fetching history for {}", endpointId, request.hashCode());
        HistoryDelta historyDelta = fetchHistory(endpointId, request, profile, curAppSeqNumber);

        LOG.debug("[{}] calculating configuration delta for {}", endpointId, request.hashCode());
        GetDeltaResponse confResponse = calculateConfigurationDelta(request, profile, historyDelta, curAppSeqNumber, fetchSchema);

        LOG.debug("[{}] calculating notification delta for {}", endpointId, request.hashCode());
        GetNotificationResponse notificationResponse = calculateNotificationDelta(request, profile, historyDelta);

        LOG.debug("[{}] building response for {}", endpointId, request.hashCode());
        SyncResponseHolder syncResponseHolder = buildResponse(curAppSeqNumber, confResponse, notificationResponse, profile.getSystemNfVersion(), profile.getUserNfVersion());
        LOG.debug("[{}] response for {} is {}", endpointId, request.hashCode(), syncResponseHolder);
        
        if (historyDelta.isSmthChanged() || notificationResponse.isSubscriptionListChanged()) {
            if (historyDelta.isSmthChanged()){
                List<EndpointGroupStateDto> endpointGroups = historyDelta.getEndpointGroupStates();
                LOG.debug("[{}] Updating profile {} with endpoint groups.size {}, groups: {}", endpointId, profile, endpointGroups.size(), endpointGroups);
                profile.setEndpointGroups(endpointGroups);
            }
            profileService.updateProfile(profile);
        }
        
        LOG.debug("[{}] processed sync request: {}", endpointId, request);
        return syncResponseHolder;
    }

    /**
     * Fetch profile.
     *
     * @param request the request
     * @param profile the profile
     * @return the endpoint profile dto
     */
    private EndpointProfileDto fetchProfile(SyncRequest request, EndpointProfileDto profile) {
        if (profile == null) {
            EndpointObjectHash endpointHash = EndpointObjectHash.fromBytes(request.getEndpointPublicKeyHash().array());
            LOG.debug("Fetching endpoint profile for {}", endpointHash);
            profile = profileService.getProfile(endpointHash);
        }
        return profile;
    }

    /**
     * To sync request.
     *
     * @param applicationToken the application token
     * @param endpointProfile the endpoint profile
     * @return the sync request
     */
    public static SyncRequest toSyncRequest(String applicationToken, EndpointProfileDto endpointProfile) {
        SyncRequest syncRequest = toSyncRequest(endpointProfile);
        syncRequest.setApplicationToken(applicationToken);
        return syncRequest;
    }

    /**
     * Builds the response.
     *
     * @param curAppSeqNumber the cur app seq number
     * @param confResponse the conf response
     * @param notificationResponse the notification response
     * @return the sync response holder
     * @throws GetDeltaException the get delta exception
     */
    public static SyncResponseHolder buildResponse(int curAppSeqNumber, GetDeltaResponse confResponse, GetNotificationResponse notificationResponse) throws GetDeltaException {
        return buildResponse(curAppSeqNumber, confResponse, notificationResponse, 0, 0);
    }
    
    /**
     * Builds the response.
     *
     * @param curAppSeqNumber the cur app seq number
     * @param confResponse the conf response
     * @param notificationResponse the notification response
     * @param systemNfVersion the system nf version
     * @param userNfVersion the user nf version
     * @return the sync response holder
     * @throws GetDeltaException the get delta exception
     */
    public static SyncResponseHolder buildResponse(int curAppSeqNumber, GetDeltaResponse confResponse, GetNotificationResponse notificationResponse,
            int systemNfVersion, int userNfVersion) throws GetDeltaException {
        SyncResponse response = new SyncResponse();
        response.setAppStateSeqNumber(curAppSeqNumber);
        response.setResponseType(getResponseStatus(confResponse, notificationResponse));
        response.setConfSyncResponse(buildConfSyncResponse(confResponse));
        response.setNotificationSyncResponse(buildNotificationSyncResponse(notificationResponse));
        return new SyncResponseHolder(response, notificationResponse.getSubscriptionStates(), systemNfVersion, userNfVersion);
    }

    /**
     * To sync request.
     *
     * @param endpointProfile the endpoint profile
     * @return the sync request
     */
    private static SyncRequest toSyncRequest(EndpointProfileDto endpointProfile) {
        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setAppStateSeqNumber(endpointProfile.getSequenceNumber());
        syncRequest.setEndpointPublicKeyHash(ByteBuffer.wrap(endpointProfile.getEndpointKeyHash()));
        syncRequest.setProfileHash(ByteBuffer.wrap(endpointProfile.getProfileHash()));
        if (endpointProfile.getConfigurationHash() != null) {
            syncRequest.setConfigurationHash(ByteBuffer.wrap(endpointProfile.getConfigurationHash()));
        } else {
            syncRequest.setConfigurationHash(ByteBuffer.wrap(new byte[0]));
        }
        return syncRequest;
    }

    /**
     * Builds the notification sync response.
     *
     * @param notificationResponse the notification response
     * @return the notification sync response
     */
    private static NotificationSyncResponse buildNotificationSyncResponse(GetNotificationResponse notificationResponse) {
        NotificationSyncResponse notificationSyncResponse = new NotificationSyncResponse();
        if (notificationResponse.getNotifications() != null) {
            List<Notification> notifications = new ArrayList<Notification>();
            for (NotificationDto notificationDto : notificationResponse.getNotifications()) {
                notifications.add(convertNotification(notificationDto));
            }
            notificationSyncResponse.setNotifications(notifications);
        }

        if (notificationResponse.getTopicList() != null) {
            List<Topic> topicList = new ArrayList<Topic>();
            for (TopicDto topicDto : notificationResponse.getTopicList()) {
                Topic topic = new Topic();
                topic.setId(topicDto.getId());
                topic.setName(topicDto.getName());
                switch (topicDto.getType()) {
                case MANDATORY:
                    topic.setSubscriptionType(SubscriptionType.MANDATORY);
                    break;
                case VOLUNTARY:
                    topic.setSubscriptionType(SubscriptionType.VOLUNTARY);
                    break;
                default:
                    break;
                }
                topicList.add(topic);
            }
            notificationSyncResponse.setAvailableTopics(topicList);
        }

        return notificationSyncResponse;
    }

    /**
     * Convert notification.
     *
     * @param notificationDto the notification dto
     * @return the notification
     */
    private static Notification convertNotification(NotificationDto notificationDto) {
        Notification notification = new Notification();
        notification.setBody(ByteBuffer.wrap(notificationDto.getBody()));
        notification.setTopicId(notificationDto.getTopicId());
        switch (notificationDto.getType()) {
        case SYSTEM:
            notification.setType(NotificationType.SYSTEM);
            break;
        case USER:
            notification.setType(NotificationType.CUSTOM);
            break;
        default:
            break;
        }
        if(notificationDto.getSecNum() >= 0){
            notification.setSeqNumber(notificationDto.getSecNum());
        }else{
            //unicast notification
            notification.setUid(notificationDto.getId());
        }
        return notification;
    }

    /**
     * Builds the conf sync response.
     *
     * @param confResponse the conf response
     * @return the conf sync response
     * @throws GetDeltaException the get delta exception
     */
    private static ConfSyncResponse buildConfSyncResponse(GetDeltaResponse confResponse) throws GetDeltaException {
        ConfSyncResponse confSyncResponse = new ConfSyncResponse();
        if (confResponse.getDelta() != null) {
            try {
                confSyncResponse.setConfDeltaBody(ByteBuffer.wrap(confResponse.getDelta().getData()));
            } catch (IOException e) {
                LOG.error("conf delta invalid: {}", e);
                throw new GetDeltaException(e);
            }
        }
        if (confResponse.getConfSchema() != null) {
            try {
                confSyncResponse.setConfSchemaBody(ByteBuffer.wrap(confResponse.getConfSchema().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                LOG.error("conf schema invalid: {}", e);
                throw new GetDeltaException(e);
            }
        }
        return confSyncResponse;
    }

    /**
     * Gets the response status.
     *
     * @param confResponse the conf response
     * @param notificationResponse the notification response
     * @return the response status
     */
    private static SyncResponseStatus getResponseStatus(GetDeltaResponse confResponse, GetNotificationResponse notificationResponse) {
        SyncResponseStatus responseStatus = SyncResponseStatus.NO_DELTA;
        if (notificationResponse.hasDelta()) {
            responseStatus = SyncResponseStatus.DELTA;
        }
        if (confResponse.getResponseType() != GetDeltaResponseType.NO_DELTA) {
            switch (confResponse.getResponseType()) {
            case CONF_RESYNC:
                responseStatus = SyncResponseStatus.CONF_RESYNC;
                break;
            case DELTA:
                responseStatus = SyncResponseStatus.DELTA;
                break;
            default:
                break;
            }
        }
        return responseStatus;
    }

    /**
     * Calculate notification delta.
     *
     * @param syncRequest the sync request
     * @param profile the profile
     * @param historyDelta the history delta
     * @return the gets the notification response
     */
    private GetNotificationResponse calculateNotificationDelta(SyncRequest syncRequest, EndpointProfileDto profile, HistoryDelta historyDelta) {
        GetNotificationRequest request = new GetNotificationRequest(profile, syncRequest.getSubscriptionCommands(),
                syncRequest.getAcceptedUnicastNotifications(), syncRequest.getTopicStates());
        return notificationDeltaService.getNotificationDelta(request, historyDelta);
    }

    /**
     * Calculate configuration delta.
     *
     * @param request the request
     * @param profile the profile
     * @param historyDelta the history delta
     * @param curAppSeqNumber the cur app seq number
     * @param fetchSchema the fetch schema
     * @return the gets the delta response
     * @throws GetDeltaException the get delta exception
     */
    private GetDeltaResponse calculateConfigurationDelta(SyncRequest request, EndpointProfileDto profile, HistoryDelta historyDelta,
            int curAppSeqNumber, boolean fetchSchema) throws GetDeltaException {
        EndpointObjectHash confHash = EndpointObjectHash.fromBytes(request.getConfigurationHash().array());
        // TODO: remove nulls
        GetDeltaRequest deltaRequest = new GetDeltaRequest(request.getApplicationToken(), null, null, confHash, request.getAppStateSeqNumber());
        deltaRequest.setEndpointProfile(profile);
        deltaRequest.setFetchSchema(fetchSchema);
        GetDeltaResponse confResponse = deltaService.getDelta(deltaRequest, historyDelta, curAppSeqNumber);
        return confResponse;
    }

    /**
     * Fetch history.
     *
     * @param request the request
     * @param profile the profile
     * @param curAppSeqNumber the cur app seq number
     * @return the history delta
     */
    private HistoryDelta fetchHistory(String endpointId, SyncRequest request, EndpointProfileDto profile, int curAppSeqNumber) {
        String applicationToken = request.getApplicationToken();
        int oldAppSeqNumber = request.getAppStateSeqNumber();

        if (isFirstRequest(request)) {
            LOG.debug("[{}] Profile has no endpoint groups yet. calculating full list", endpointId);
            return historyDeltaService.getDelta(profile, applicationToken, curAppSeqNumber);
        } else {
            LOG.debug("[{}] Profile has endpoint groups. Calculating changes", endpointId);
            return historyDeltaService.getDelta(profile, applicationToken, oldAppSeqNumber, curAppSeqNumber);
        }
    }

    /**
     * Checks if is first request.
     *
     * @param request the request
     * @return true, if is first request
     */
    public static boolean isFirstRequest(SyncRequest request) {
        return request.getConfigurationHash() == null || toByteArray(request.getConfigurationHash()).length == 0;
    }

    /**
     * Builds the profile resync response.
     *
     * @param request the request
     * @return the sync response holder
     */
    public static SyncResponseHolder buildProfileResyncResponse(SyncRequest request) {
        SyncResponse response = new SyncResponse();
        response.setAppStateSeqNumber(request.getAppStateSeqNumber());
        response.setResponseType(SyncResponseStatus.PROFILE_RESYNC);
        return new SyncResponseHolder(response);
    }

    /**
     * To byte array.
     * 
     * @param buffer
     *            the buffer
     * @return the byte[]
     */
    private static byte[] toByteArray(ByteBuffer buffer) {
        return Arrays.copyOf(buffer.array(), buffer.array().length);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.OperationsService#updateSyncResponse(org.kaaproject.kaa.common.endpoint.gen.SyncResponse, java.util.List, java.lang.String)
     */
    @Override
    public SyncResponse updateSyncResponse(SyncResponse response, List<NotificationDto> notificationDtos, String unicastNotificationId) {
        NotificationSyncResponse notificationResponse = response.getNotificationSyncResponse();
        if (notificationResponse == null) {
            notificationResponse = new NotificationSyncResponse();
            response.setNotificationSyncResponse(notificationResponse);
        }

        List<Notification> notifications = notificationResponse.getNotifications();
        if (notifications == null) {
            notifications = new ArrayList<Notification>();
        }
        for (NotificationDto notificationDto : notificationDtos) {
            notifications.add(convertNotification(notificationDto));
        }
        if (unicastNotificationId != null) {
            NotificationDto unicast = notificationDeltaService.findUnicastNotificationById(unicastNotificationId);
            notifications.add(convertNotification(unicast));
        }

        notificationResponse.setNotifications(notifications);

        response.setResponseType(SyncResponseStatus.DELTA);
        return response;
    }
}
