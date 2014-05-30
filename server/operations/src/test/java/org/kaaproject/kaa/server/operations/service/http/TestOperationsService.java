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

package org.kaaproject.kaa.server.operations.service.http;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.endpoint.gen.ConfSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;

/**
 * Test Endpoint Service
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class TestOperationsService implements OperationsService {

    /** MAX topic list size */
    private static final int MAX_TOPIC_LIST_SIZE = 100;
    
    /** MAX notification list size */
    private static final int MAX_NOTIFICATION_LIST_SIZE = 100;
    
    /** Random generator */
    private Random rnd;
    
    /**
     * Constructor
     */
    public TestOperationsService() {
        rnd = new Random();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.EndpointService#registerEndpoint(org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest)
     */
    @Override
    public SyncResponseHolder registerEndpoint(
            EndpointRegistrationRequest request) throws GetDeltaException {
        OperationsHttpServerIT.EndpointRegisterTestSetRequestReceived(
                request.getVersionInfo().getConfigVersion(),
                request);
        SyncResponse response = generateSyncResponse();
        OperationsHttpServerIT.EndpointRegisterTestSetResponseSent(
                request.getVersionInfo().getConfigVersion(),
                response);
        SyncResponseHolder holder = new SyncResponseHolder(response);
        return holder;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.EndpointService#updateEndpoint(org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest)
     */
    @Override
    public SyncResponseHolder updateProfile(ProfileUpdateRequest request)
            throws GetDeltaException {
        OperationsHttpServerIT.UpdateProfileSetRequestReceived(
                request.getVersionInfo().getConfigVersion(), 
                request);
        SyncResponse response = generateSyncResponse();
        OperationsHttpServerIT.UpdateProfileSetResponseSent(
                request.getVersionInfo().getConfigVersion(),
                response);
        SyncResponseHolder holder = new SyncResponseHolder(response);
        return holder;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.EndpointService#sync(org.kaaproject.kaa.common.endpoint.gen.SyncRequest)
     */
    @Override
    public SyncResponseHolder sync(SyncRequest request)
            throws GetDeltaException {
        
        SyncResponse response = generateSyncResponse();
        SyncResponseHolder holder = new SyncResponseHolder(response);
        
        return holder;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.EndpointService#updateSyncResponse(org.kaaproject.kaa.common.endpoint.gen.SyncResponse, java.util.List, java.lang.String)
     */
    @Override
    public SyncResponse updateSyncResponse(SyncResponse response,
            List<NotificationDto> notifications, String unicastNotificationId) {
        return null;
    }

    /**
     * Generate SyncResponse with random type
     * @return SyncResponse
     */
    private SyncResponse generateSyncResponse() {
        SyncResponse response = null;
        
        int t = rnd.nextInt(5);
        switch (t) {
        case 0:
            response = generateRedirectionResponse();
            break;
        case 1:
            response = generateConfResyncResponse();
            break;
        case 2:
            response = generateProfResyncResponse();
            break;
        case 3:
            response = generateDeltaResponse();
            break;
        case 4:
            response = generateNoDeltaResponse();
            break;            
        default:
            response = generateRedirectionResponse();
            break;
        }
        return response;
    }
    
    /**
     * Generate redirection response with random DNS name with size 30 chars.
     * @return SyncResponse
     */
    private SyncResponse generateRedirectionResponse() {
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.REDIRECT);
        RedirectSyncResponse redirectResponse = new RedirectSyncResponse();
        redirectResponse.setDnsName(MultipartObjects.getRandomString(30));
        response.setRedirectSyncResponse(redirectResponse);
        return response;
    }
    
    /**
     * generate ConfResync type response with random Conf delta body size 4096 and Schema body with size 4096
     * @return SyncResponse
     */
    private SyncResponse generateConfResyncResponse() {
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.CONF_RESYNC);
        ConfSyncResponse confSyncResponse = new ConfSyncResponse();
        confSyncResponse.setConfDeltaBody(ByteBuffer.wrap(HttpTestSyncClient.getRandomBytes(4096)));
        confSyncResponse.setConfSchemaBody(ByteBuffer.wrap(HttpTestSyncClient.getRandomBytes(4096)));
        response.setConfSyncResponse(confSyncResponse);
        return response;
    }
    
    /**
     * Generate Profile resync
     * @return SyncResponse
     */
    private SyncResponse generateProfResyncResponse() {
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.PROFILE_RESYNC);
        return response;
    }
    
    /**
     * generate Delta response with possible Notification
     * @return SyncResponse
     */
    private SyncResponse generateDeltaResponse() {
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.DELTA);
        if (rnd.nextBoolean()) {
            response = generateNotificationSyncResponse(response);
        }
        return response;
    }
    
    /**
     * Generate No Delta response with possible Notification
     * @return SyncResponse
     */
    private SyncResponse generateNoDeltaResponse() {
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.NO_DELTA);
        if (rnd.nextBoolean()) {
            response = generateNotificationSyncResponse(response);
        }
        return response;
    }
    
    /**
     * generate Notification
     * @param response
     * @return SyncResponse
     */
    private SyncResponse generateNotificationSyncResponse(SyncResponse response) {
        NotificationSyncResponse notificationSyncResponse = new NotificationSyncResponse();
        int topicListSize = rnd.nextInt(MAX_TOPIC_LIST_SIZE);
        List<Topic> topics = new Vector<>(topicListSize);
        for(int i=0; i<topicListSize;i++) {
            Topic topic = new Topic();
            topic.setId(MultipartObjects.getRandomString(10));
            topic.setName(MultipartObjects.getRandomString(20));
            if (rnd.nextBoolean()) {
                topic.setSubscriptionType(SubscriptionType.MANDATORY);
            } else {
                topic.setSubscriptionType(SubscriptionType.VOLUNTARY);
            }
            topics.add(topic);
        }
        notificationSyncResponse.setAvailableTopics(topics);
        
        int notifListSize = rnd.nextInt(MAX_NOTIFICATION_LIST_SIZE);
        List<Notification> notifications = new Vector<>(notifListSize);
        for(int i=0; i<notifListSize;i++) {
            Notification notif = new Notification();
            notif.setUid(MultipartObjects.getRandomString(10));
            notif.setTopicId(MultipartObjects.getRandomString(20));
            notif.setSeqNumber(rnd.nextInt());
            notif.setBody(ByteBuffer.wrap(HttpTestSyncClient.getRandomBytes(256)));
            if (rnd.nextBoolean()) {
                notif.setType(NotificationType.CUSTOM);
            } else {
                notif.setType(NotificationType.SYSTEM);
            }
            notifications.add(notif);
        }
        notificationSyncResponse.setNotifications(notifications);
        response.setNotificationSyncResponse(notificationSyncResponse);
        return response;
    }
}
