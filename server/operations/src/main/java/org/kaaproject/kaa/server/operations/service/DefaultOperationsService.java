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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventListenersResponse;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationRequest;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationResponse;
import org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.delta.DeltaService;
import org.kaaproject.kaa.server.operations.service.delta.HistoryDelta;
import org.kaaproject.kaa.server.operations.service.history.HistoryDeltaService;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.kaaproject.kaa.server.operations.service.profile.ProfileService;
import org.kaaproject.kaa.server.operations.service.user.EndpointUserService;
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

    @Autowired
    EndpointUserService endpointUserService;

    private String operationServerHash;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.OperationsService#sync(org.
     * kaaproject.kaa.common.endpoint.gen.SyncRequest)
     */
    @Override
    public SyncResponseHolder sync(SyncRequest request) throws GetDeltaException {
        return processSync(request);
    }

    /**
     * Process sync.
     *
     * @param request
     *            the request
     * @param profile
     *            the profile
     * @param fetchSchema
     *            the fetch schema
     * @return the sync response holder
     * @throws GetDeltaException
     *             the get delta exception
     */
    private SyncResponseHolder processSync(SyncRequest request) throws GetDeltaException {
        SyncRequestMetaData metaData = request.getSyncRequestMetaData();
        String endpointId = Base64Util.encode(metaData.getEndpointPublicKeyHash().array());
        int requestHash = request.hashCode();
        LOG.trace("[{}][{}] processing sync. request: ", endpointId, requestHash, request);

        if (!validate(endpointId, request)) {
            return SyncResponseHolder.failure();
        }

        SyncResponseHolder response = new SyncResponseHolder(new SyncResponse());
        response.setStatus(SyncResponseResultType.SUCCESS);

        EndpointProfileDto profile = null;

        ProfileSyncRequest profileSyncRequest = request.getProfileSyncRequest();
        if (profileSyncRequest != null) {
            ProfileSyncResponse profileSyncResponse;
            if (profileSyncRequest.getEndpointPublicKey() != null) {
                LOG.debug("[{}][{}] registration of endpoint started.", endpointId, requestHash);
                profile = registerEndpoint(endpointId, requestHash, metaData, profileSyncRequest);
            } else {
                LOG.debug("[{}][{}] update of endpoint profile started.", endpointId, requestHash);
                profile = updateEndpoint(endpointId, requestHash, metaData, profileSyncRequest);
            }
            profileSyncResponse = new ProfileSyncResponse(SyncResponseStatus.DELTA);
            metaData.setProfileHash(ByteBuffer.wrap(profile.getProfileHash()));
            response.setProfileSyncResponse(profileSyncResponse);
        }

        AppSeqNumber appSeqNumber = cacheService.getAppSeqNumber(metaData.getApplicationToken());
        int curAppSeqNumber = appSeqNumber.getSeqNumber();
        LOG.debug("[{}][{}] fetched app seq number {}", endpointId, requestHash, curAppSeqNumber);

        if (profile == null) {
            LOG.debug("[{}][{}] fetching profile.", endpointId, requestHash);
            EndpointObjectHash endpointHash = EndpointObjectHash.fromBytes(metaData.getEndpointPublicKeyHash().array());
            profile = profileService.getProfile(endpointHash);
            LOG.trace("[{}][{}] fetched profile {}.", endpointId, requestHash, profile);
        }

        response.setEndpointProfile(profile);

        if (!Arrays.equals(profile.getProfileHash(), toByteArray(metaData.getProfileHash()))) {
            LOG.debug("[{}] Profile hash mismatch. Profile resync needed");
            return buildProfileResyncResponse(request);
        }

        if (request.getUserSyncRequest() != null) {
            LOG.trace("[{}][{}] procesing user sync request {}.", endpointId, requestHash, request.getUserSyncRequest());
            UserSyncResponse userSyncResponse = processUserSyncRequest(endpointId, requestHash, metaData, request.getUserSyncRequest(), profile);
            response.setUserSyncResponse(userSyncResponse);
        }

        if (request.getEventSyncRequest() != null) {
            LOG.trace("[{}][{}] procesing event sync request {}.", endpointId, requestHash, request.getUserSyncRequest());
            EventSyncResponse eventSyncResponse = processEventSyncResponse(endpointId, requestHash, appSeqNumber, request.getEventSyncRequest(), profile);
            response.setEventSyncResponse(eventSyncResponse);
        }

        boolean updateProfileRequired = false;

        if (request.getConfigurationSyncRequest() != null) {
            ConfigurationSyncRequest confSyncRequest = request.getConfigurationSyncRequest();
            LOG.trace("[{}][{}] procesing configuration sync request {}.", endpointId, requestHash, confSyncRequest);
            LOG.debug("[{}][{}] fetching history for seq numbers {}-{}", endpointId, requestHash, confSyncRequest.getAppStateSeqNumber(), curAppSeqNumber);
            LOG.debug("[{}][{}] calculating configuration delta", endpointId, requestHash);
            HistoryDelta historyDelta = fetchHistory(endpointId, requestHash, metaData.getApplicationToken(), profile, confSyncRequest.getAppStateSeqNumber(),
                    curAppSeqNumber);
            GetDeltaResponse confResponse = calculateConfigurationDelta(metaData, confSyncRequest, profile, historyDelta, curAppSeqNumber);
            ConfigurationSyncResponse confSyncResponse = buildConfSyncResponse(confResponse, curAppSeqNumber);
            response.setConfigurationSyncResponse(confSyncResponse);

            if (historyDelta.isSmthChanged()) {
                List<EndpointGroupStateDto> endpointGroups = historyDelta.getEndpointGroupStates();
                LOG.debug("[{}][{}] Updating profile with endpoint groups.size {}, groups: {}", endpointId, requestHash, endpointGroups.size(), endpointGroups);
                profile.setEndpointGroups(endpointGroups);
                updateProfileRequired = true;
            }
        }

        if (request.getNotificationSyncRequest() != null) {
            NotificationSyncRequest nfSyncRequest = request.getNotificationSyncRequest();
            LOG.trace("[{}][{}] procesing notification sync request {}.", endpointId, requestHash, nfSyncRequest);
            LOG.debug("[{}][{}] fetching history for seq numbers {}-{}", endpointId, requestHash, nfSyncRequest.getAppStateSeqNumber(), curAppSeqNumber);
            HistoryDelta historyDelta = fetchHistory(endpointId, requestHash, metaData.getApplicationToken(), profile, nfSyncRequest.getAppStateSeqNumber(),
                    curAppSeqNumber);
            LOG.debug("[{}][{}] calculating notification delta", endpointId, requestHash);
            GetNotificationResponse notificationResponse = calculateNotificationDelta(nfSyncRequest, profile, historyDelta);
            response.setSubscriptionStates(notificationResponse.getSubscriptionStates());
            NotificationSyncResponse nfSyncResponse = buildNotificationSyncResponse(notificationResponse, curAppSeqNumber);
            response.setNotificationSyncResponse(nfSyncResponse);

            updateProfileRequired = updateProfileRequired || notificationResponse.isSubscriptionListChanged();
            if (historyDelta.isSmthChanged()) {
                List<EndpointGroupStateDto> endpointGroups = historyDelta.getEndpointGroupStates();
                LOG.debug("[{}][{}] Updating profile with endpoint groups.size {}, groups: {}", endpointId, requestHash, endpointGroups.size(), endpointGroups);
                profile.setEndpointGroups(endpointGroups);
                updateProfileRequired = true;
            }
        }

        LOG.debug("[{}][{}] response is {}", endpointId, request.hashCode(), response);

        if (!operationServerHash.equals(profile.getServerHash())) {
            LOG.debug("[{}] Operations server hash changed from {} to {}", endpointId, profile.getServerHash(), operationServerHash);
            profile.setServerHash(operationServerHash);
            updateProfileRequired = true;
        }

        if (updateProfileRequired) {
            profileService.updateProfile(profile);
        }

        LOG.debug("[{}][{}] processed sync request", endpointId, requestHash);
        return response;
    }

    private EndpointProfileDto registerEndpoint(String endpointId, int requestHash, SyncRequestMetaData metaData, ProfileSyncRequest request) {
        LOG.debug("[{}][{}] register endpoint. request: {}", endpointId, requestHash, request);
        byte[] endpointKey = toByteArray(request.getEndpointPublicKey());
        byte[] profileBody = toByteArray(request.getProfileBody());

        RegisterProfileRequest registerProfileRequest = new RegisterProfileRequest(metaData.getApplicationToken(), endpointKey, request.getVersionInfo(),
                profileBody, request.getEndpointAccessToken());
        EndpointProfileDto endpointProfile = profileService.registerProfile(registerProfileRequest);
        LOG.debug("profile registered. id: {}, endpointKeyHash: {}", endpointProfile.getId(), endpointProfile.getEndpointKeyHash());
        return endpointProfile;
    }

    private EndpointProfileDto updateEndpoint(String endpointId, int requestHash, SyncRequestMetaData metaData, ProfileSyncRequest request) {
        LOG.debug("[{}][{}] update endpoint. request: {}", endpointId, requestHash, request);
        EndpointObjectHash endpointKeyHash = EndpointObjectHash.fromBytes(toByteArray(metaData.getEndpointPublicKeyHash()));
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(metaData.getApplicationToken(), endpointKeyHash, request.getEndpointAccessToken(),
                request.getProfileBody().array(), request.getVersionInfo());
        EndpointProfileDto endpointProfile = profileService.updateProfile(updateRequest);
        LOG.debug("profile updated. id: {}, endpointKeyHash: {}", endpointProfile.getId(), endpointProfile.getEndpointKeyHash());
        return endpointProfile;
    }

    private EventSyncResponse processEventSyncResponse(String endpointId, int requestHash, AppSeqNumber appSeqNumber, EventSyncRequest request,
            EndpointProfileDto profile) {
        EventSyncResponse response = new EventSyncResponse();
        List<EventListenersRequest> requests = request.getEventListenersRequests();
        if (requests != null && !requests.isEmpty()) {
            LOG.debug("[{}] processing {} endpoint detach requests", endpointId, requests.size());
            List<EventListenersResponse> responses = new ArrayList<>(requests.size());
            for (EventListenersRequest elRequest : requests) {
                LOG.debug("[{}] processing event listener request {}", endpointId, request);
                EventListenersResponse elResponse = endpointUserService.findListeners(profile, appSeqNumber, elRequest);
                LOG.debug("[{}] event listener response {}", endpointId, response);
                responses.add(elResponse);
            }
            response.setEventListenersResponses(responses);
        } else {
            List<EventListenersResponse> emptyList = Collections.emptyList();
            response.setEventListenersResponses(emptyList);
        }
        return response;
    }

    private UserSyncResponse processUserSyncRequest(String endpointId, int requestHash, SyncRequestMetaData metaData, UserSyncRequest request,
            EndpointProfileDto profile) {
        UserSyncResponse response = new UserSyncResponse();
        if (request.getUserAttachRequest() != null) {
            LOG.debug("[{}] processing user attach request {}", endpointId, request.getUserAttachRequest());
            UserAttachResponse userAttachResponse = endpointUserService.attachUser(profile, request.getUserAttachRequest());
            LOG.debug("[{}] user attach response {}", endpointId, userAttachResponse);
            response.setUserAttachResponse(userAttachResponse);
        }
        if (request.getEndpointAttachRequests() != null) {
            response.setEndpointAttachResponses(processEndpointAttachRequests(endpointId, requestHash, request, profile));
        }
        if (request.getEndpointDetachRequests() != null) {
            response.setEndpointDetachResponses(processEndpointDetachRequests(endpointId, requestHash, request, profile));
        }
        return response;
    }

    private boolean validate(String endpointId, SyncRequest request) {
        SyncRequestMetaData md = request.getSyncRequestMetaData();
        // TODO: validate if public key hash matches hash of public key during
        // profile registration command.
        if (md.getProfileHash() == null) {
            ProfileSyncRequest profileRequest = request.getProfileSyncRequest();
            if (profileRequest != null && profileRequest.getEndpointPublicKey() != null) {
                return true;
            } else {
                LOG.warn("[{}] Request is not valid. It does not contain profile information!", endpointId);
                return false;
            }
        } else {
            return true;
        }
    }

    private List<EndpointAttachResponse> processEndpointAttachRequests(String endpointId, int requestHash, UserSyncRequest syncRequest,
            EndpointProfileDto profile) {
        List<EndpointAttachRequest> requests = syncRequest.getEndpointAttachRequests();
        if (requests != null && !requests.isEmpty()) {
            LOG.debug("[{}][{}] processing {} endpoint attach requests", endpointId, requestHash, requests.size());
            List<EndpointAttachResponse> responses = new ArrayList<>(requests.size());
            for (EndpointAttachRequest request : syncRequest.getEndpointAttachRequests()) {
                LOG.debug("[{}][{}] processing endpoint attach request {}", endpointId, requestHash, request);
                EndpointAttachResponse response = endpointUserService.attachEndpoint(profile, request);
                LOG.debug("[{}][{}] endpoint attach response {}", endpointId, requestHash, response);
                responses.add(response);
            }
            return responses;
        } else {
            return Collections.emptyList();
        }
    }

    private List<EndpointDetachResponse> processEndpointDetachRequests(String endpointId, int requestHash, UserSyncRequest syncRequest,
            EndpointProfileDto profile) {
        List<EndpointDetachRequest> requests = syncRequest.getEndpointDetachRequests();
        if (requests != null && !requests.isEmpty()) {
            LOG.debug("[{}] processing {} endpoint detach requests", endpointId, requests.size());
            List<EndpointDetachResponse> responses = new ArrayList<>(requests.size());
            for (EndpointDetachRequest request : requests) {
                LOG.debug("[{}] processing endpoint detach request {}", endpointId, request);
                EndpointDetachResponse response = endpointUserService.detachEndpoint(profile, request);
                LOG.debug("[{}] endpoint detach response {}", endpointId, response);
                responses.add(response);
            }
            return responses;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Builds the notification sync response.
     *
     * @param notificationResponse
     *            the notification response
     * @return the notification sync response
     */
    private static NotificationSyncResponse buildNotificationSyncResponse(GetNotificationResponse notificationResponse, int curAppSeqNumber) {
        NotificationSyncResponse response = new NotificationSyncResponse();
        response.setResponseStatus(SyncResponseStatus.NO_DELTA);

        if (notificationResponse.getNotifications() != null) {
            List<Notification> notifications = new ArrayList<Notification>();
            for (NotificationDto notificationDto : notificationResponse.getNotifications()) {
                notifications.add(convertNotification(notificationDto));
            }
            response.setNotifications(notifications);
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
            if (notificationResponse.hasDelta()) {
                response.setResponseStatus(SyncResponseStatus.DELTA);
            }
            response.setAvailableTopics(topicList);
        }
        response.setAppStateSeqNumber(curAppSeqNumber);
        return response;
    }

    /**
     * Convert notification.
     *
     * @param notificationDto
     *            the notification dto
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
        if (notificationDto.getSecNum() >= 0) {
            notification.setSeqNumber(notificationDto.getSecNum());
        } else {
            // unicast notification
            notification.setUid(notificationDto.getId());
        }
        return notification;
    }

    /**
     * Builds the conf sync response.
     *
     * @param deltaResponse
     *            the conf response
     * @return the conf sync response
     * @throws GetDeltaException
     *             the get delta exception
     */
    private static ConfigurationSyncResponse buildConfSyncResponse(GetDeltaResponse deltaResponse, int curAppSeqNumber) throws GetDeltaException {
        ConfigurationSyncResponse response = new ConfigurationSyncResponse();
        if (deltaResponse.getDelta() != null) {
            try {
                response.setConfDeltaBody(ByteBuffer.wrap(deltaResponse.getDelta().getData()));
            } catch (IOException e) {
                LOG.error("conf delta invalid: {}", e);
                throw new GetDeltaException(e);
            }
        }
        if (deltaResponse.getConfSchema() != null) {
            try {
                response.setConfSchemaBody(ByteBuffer.wrap(deltaResponse.getConfSchema().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                LOG.error("conf schema invalid: {}", e);
                throw new GetDeltaException(e);
            }
        }
        switch (deltaResponse.getResponseType()) {
        case CONF_RESYNC:
            response.setResponseStatus(SyncResponseStatus.RESYNC);
            break;
        case DELTA:
            response.setResponseStatus(SyncResponseStatus.DELTA);
            break;
        default:
            response.setResponseStatus(SyncResponseStatus.NO_DELTA);
            break;
        }
        response.setAppStateSeqNumber(curAppSeqNumber);
        return response;
    }

    /**
     * Calculate notification delta.
     *
     * @param syncRequest
     *            the sync request
     * @param profile
     *            the profile
     * @param historyDelta
     *            the history delta
     * @return the gets the notification response
     */
    private GetNotificationResponse calculateNotificationDelta(NotificationSyncRequest syncRequest, EndpointProfileDto profile, HistoryDelta historyDelta) {
        GetNotificationRequest request = new GetNotificationRequest(profile, syncRequest.getSubscriptionCommands(),
                syncRequest.getAcceptedUnicastNotifications(), syncRequest.getTopicStates());
        return notificationDeltaService.getNotificationDelta(request, historyDelta);
    }

    /**
     * Calculate configuration delta.
     *
     * @param request
     *            the request
     * @param profile
     *            the profile
     * @param historyDelta
     *            the history delta
     * @param curAppSeqNumber
     *            the cur app seq number
     * @param fetchSchema
     *            the fetch schema
     * @return the gets the delta response
     * @throws GetDeltaException
     *             the get delta exception
     */
    private GetDeltaResponse calculateConfigurationDelta(SyncRequestMetaData metaData, ConfigurationSyncRequest request, EndpointProfileDto profile, HistoryDelta historyDelta, int curAppSeqNumber) throws GetDeltaException {
        GetDeltaRequest deltaRequest;
        int curEndpointSeqNumber = Math.min(request.getAppStateSeqNumber(), profile.getSequenceNumber());
        if(request.getConfigurationHash() != null){
            deltaRequest = new GetDeltaRequest(metaData.getApplicationToken(), EndpointObjectHash.fromBytes(request.getConfigurationHash().array()), curEndpointSeqNumber);
        }else{
            deltaRequest = new GetDeltaRequest(metaData.getApplicationToken(), curEndpointSeqNumber);
        }
        deltaRequest.setEndpointProfile(profile);
        GetDeltaResponse confResponse = deltaService.getDelta(deltaRequest, historyDelta, curAppSeqNumber);
        return confResponse;
    }

    /**
     * Fetch history.
     *
     * @param request
     *            the request
     * @param profile
     *            the profile
     * @param curAppSeqNumber
     *            the cur app seq number
     * @return the history delta
     */
    private HistoryDelta fetchHistory(String endpointId, int requesHash, String applicationToken, EndpointProfileDto profile, int startSeqNumber,
            int endSeqNumber) {
        int curEndpointSeqNumber = Math.min(startSeqNumber, profile.getSequenceNumber());
        if (isFirstRequest(profile)) {
            LOG.debug("[{}] Profile has no endpoint groups yet. calculating full list", endpointId);
            return historyDeltaService.getDelta(profile, applicationToken, endSeqNumber);
        } else {
            LOG.debug("[{}] Profile has endpoint groups. Calculating changes", endpointId);
            return historyDeltaService.getDelta(profile, applicationToken, curEndpointSeqNumber, endSeqNumber);
        }
    }

    /**
     * Checks if is first request.
     *
     * @param request
     *            the request
     * @return true, if is first request
     */
    public static boolean isFirstRequest(EndpointProfileDto profile) {
        return profile.getConfigurationHash() == null || profile.getConfigurationHash().length == 0;
    }

    /**
     * Builds the profile resync response.
     *
     * @param request
     *            the request
     * @return the sync response holder
     */
    public static SyncResponseHolder buildProfileResyncResponse(SyncRequest request) {
        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.PROFILE_RESYNC);
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

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.OperationsService#
     * updateSyncResponse(org.kaaproject.kaa.common.endpoint.gen.SyncResponse,
     * java.util.List, java.lang.String)
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

        notificationResponse.setResponseStatus(SyncResponseStatus.DELTA);
        return response;
    }

    @Override
    public void setPublicKey(PublicKey publicKey) {
        operationServerHash = Base64Util.encode(SHA1HashUtils.hashToBytes(publicKey.getEncoded()));
    }
}
