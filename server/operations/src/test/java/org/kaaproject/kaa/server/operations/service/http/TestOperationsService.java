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
import java.security.PublicKey;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.pojo.sync.ClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ConfigurationServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.Notification;
import org.kaaproject.kaa.server.operations.pojo.sync.NotificationServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.NotificationType;
import org.kaaproject.kaa.server.operations.pojo.sync.RedirectServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.SubscriptionType;
import org.kaaproject.kaa.server.operations.pojo.sync.SyncResponseResultType;
import org.kaaproject.kaa.server.operations.pojo.sync.SyncResponseStatus;
import org.kaaproject.kaa.server.operations.pojo.sync.Topic;
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
    private final Random rnd;

    /**
     * Constructor
     */
    public TestOperationsService() {
        rnd = new Random();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.EndpointService#sync(org.kaaproject.kaa.common.endpoint.gen.SyncRequest)
     */
    @Override
    public SyncResponseHolder sync(ClientSync request)
            throws GetDeltaException {

        return sync(request, null);
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.EndpointService#sync(org.kaaproject.kaa.common.endpoint.gen.SyncRequest)
     */
    @Override
    public SyncResponseHolder sync(ClientSync request, EndpointProfileDto profile)
            throws GetDeltaException {

        ServerSync response = generateSyncResponse();
        SyncResponseHolder holder = new SyncResponseHolder(response);

        return holder;
    }    

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.EndpointService#updateSyncResponse(org.kaaproject.kaa.common.endpoint.gen.SyncResponse, java.util.List, java.lang.String)
     */
    @Override
    public ServerSync updateSyncResponse(ServerSync response,
            List<NotificationDto> notifications, String unicastNotificationId) {
        return null;
    }

    /**
     * Generate SyncResponse with random type
     * @return SyncResponse
     */
    private ServerSync generateSyncResponse() {
        ServerSync response = null;

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
    private ServerSync generateRedirectionResponse() {
        ServerSync response = new ServerSync();
        response.setStatus(SyncResponseResultType.REDIRECT);
        RedirectServerSync redirectResponse = new RedirectServerSync();
        redirectResponse.setDnsName(MultipartObjects.getRandomString(30));
        response.setRedirectSync(redirectResponse);
        return response;
    }

    /**
     * generate ConfResync type response with random Conf delta body size 4096 and Schema body with size 4096
     * @return SyncResponse
     */
    private ServerSync generateConfResyncResponse() {
        ServerSync response = new ServerSync();
        response.setStatus(SyncResponseResultType.SUCCESS);
        ConfigurationServerSync confSyncResponse = new ConfigurationServerSync();
        confSyncResponse.setResponseStatus(SyncResponseStatus.RESYNC);
        confSyncResponse.setConfDeltaBody(ByteBuffer.wrap(HttpTestSyncClient.getRandomBytes(4096)));
        confSyncResponse.setConfSchemaBody(ByteBuffer.wrap(HttpTestSyncClient.getRandomBytes(4096)));
        response.setConfigurationSync(confSyncResponse);
        return response;
    }

    /**
     * Generate Profile resync
     * @return SyncResponse
     */
    private ServerSync generateProfResyncResponse() {
        ServerSync response = new ServerSync();
        response.setStatus(SyncResponseResultType.PROFILE_RESYNC);
        return response;
    }

    /**
     * generate Delta response with possible Notification
     * @return SyncResponse
     */
    private ServerSync generateDeltaResponse() {
        ServerSync response = new ServerSync();
        response.setStatus(SyncResponseResultType.SUCCESS);
        if (rnd.nextBoolean()) {
            response = generateNotificationSyncResponse(response, SyncResponseStatus.DELTA);
        }
        return response;
    }

    /**
     * Generate No Delta response with possible Notification
     * @return SyncResponse
     */
    private ServerSync generateNoDeltaResponse() {
        ServerSync response = new ServerSync();
        response.setStatus(SyncResponseResultType.SUCCESS);
        if (rnd.nextBoolean()) {
            response = generateNotificationSyncResponse(response, SyncResponseStatus.NO_DELTA);
        }
        return response;
    }

    /**
     * generate Notification
     * @param response
     * @return SyncResponse
     */
    private ServerSync generateNotificationSyncResponse(ServerSync response, SyncResponseStatus status) {
        NotificationServerSync notificationSyncResponse = new NotificationServerSync();
        notificationSyncResponse.setResponseStatus(status);
        int topicListSize = rnd.nextInt(MAX_TOPIC_LIST_SIZE);
        List<Topic> topics = new Vector<>(topicListSize);
        for(int i=0; i<topicListSize;i++) {
            Topic topic = new Topic();
            topic.setId(MultipartObjects.getRandomString(10));
            topic.setName(MultipartObjects.getRandomString(20));
            if (rnd.nextBoolean()) {
                topic.setSubscriptionType(SubscriptionType.MANDATORY);
            } else {
                topic.setSubscriptionType(SubscriptionType.OPTIONAL);
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
        response.setNotificationSync(notificationSyncResponse);
        return response;
    }

    @Override
    public void setPublicKey(PublicKey publicKey) {
        // TODO Auto-generated method stub

    }
}
