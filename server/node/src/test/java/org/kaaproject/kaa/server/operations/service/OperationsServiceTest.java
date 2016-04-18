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

package org.kaaproject.kaa.server.operations.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.operations.service.delta.DeltaServiceIT;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.kaaproject.kaa.server.sync.Notification;
import org.kaaproject.kaa.server.sync.NotificationServerSync;
import org.kaaproject.kaa.server.sync.NotificationType;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncResponseStatus;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class OperationsServiceTest {
    private static final String UNICAST_NF_ID = "unicastNfId";
    private static final String SYSTEM_TOPIC_NF_ID = "systemTopicNfId";
    private static final String SYSTEM_TOPIC_ID = "systemTopicId";
    private static final String SYSTEM_TOPIC_NAME = "systemTopicName";
    private static final String USER_TOPIC_NF_ID = "userTopicNfId";
    private static final String USER_TOPIC_ID = "userTopicId";
    private static final String USER_TOPIC_NAME = "userTopicName";

    public static final String COMPLEX_PROTOCOL_SCHEMA = "operations/service/delta/complexFieldsDeltaProtocolSchema.json";


    protected static final Logger LOG = LoggerFactory.getLogger(DeltaServiceIT.class);

    private OperationsService operationsService;
    private NotificationDeltaService notificationDeltaService;

    private TopicDto systemTopic;
    private TopicDto userTopic;
    private NotificationDto unicastNfDto;
    private NotificationDto systemTopicNfDto;
    private NotificationDto userTopicNfDto;

    @Before
    public void before() throws IOException{
        operationsService = new DefaultOperationsService();
        notificationDeltaService = mock(NotificationDeltaService.class);
        ReflectionTestUtils.setField(operationsService, "notificationDeltaService", notificationDeltaService);

        unicastNfDto = new NotificationDto();
        unicastNfDto.setId(UNICAST_NF_ID);
        unicastNfDto.setTopicId(USER_TOPIC_ID);
        unicastNfDto.setType(NotificationTypeDto.USER);
        unicastNfDto.setBody("test".getBytes(Charset.defaultCharset()));
        Mockito.when(notificationDeltaService.findUnicastNotificationById(UNICAST_NF_ID)).thenReturn(unicastNfDto);

        systemTopicNfDto = new NotificationDto();
        systemTopicNfDto.setId(SYSTEM_TOPIC_NF_ID);
        systemTopicNfDto.setTopicId(SYSTEM_TOPIC_ID);
        systemTopicNfDto.setBody("test".getBytes(Charset.defaultCharset()));
        systemTopicNfDto.setType(NotificationTypeDto.SYSTEM);
        systemTopicNfDto.setSecNum(1);

        userTopicNfDto = new NotificationDto();
        userTopicNfDto.setId(USER_TOPIC_NF_ID);
        userTopicNfDto.setTopicId(USER_TOPIC_ID);
        userTopicNfDto.setBody("test".getBytes(Charset.defaultCharset()));
        userTopicNfDto.setType(NotificationTypeDto.USER);
        userTopicNfDto.setSecNum(1);

        systemTopic = new TopicDto();
        systemTopic.setId(SYSTEM_TOPIC_ID);
        systemTopic.setType(TopicTypeDto.MANDATORY);
        systemTopic.setName(SYSTEM_TOPIC_NAME);

        userTopic = new TopicDto();
        userTopic.setId(USER_TOPIC_ID);
        userTopic.setType(TopicTypeDto.OPTIONAL);
        userTopic.setName(USER_TOPIC_NAME);
    }

    @Test
    public void updateSyncResponseEmptyTest(){
        ServerSync response = new ServerSync();
        response.setStatus(SyncStatus.SUCCESS);
        ServerSync result = operationsService.updateSyncResponse(response, new ArrayList<NotificationDto>(), null);
        assertNull(result);
    }

    @Test
    public void updateSyncResponseNotEmptyTest(){
        ServerSync response = new ServerSync();
        response.setStatus(SyncStatus.SUCCESS);
        response.setNotificationSync(new NotificationServerSync());
        NotificationDto nfDto = new NotificationDto();
        nfDto.setId("nfId");
        nfDto.setBody("body".getBytes());
        nfDto.setType(NotificationTypeDto.SYSTEM);
        ServerSync result = operationsService.updateSyncResponse(response, Collections.singletonList(nfDto), null);
        assertNotNull(result);
        assertNotNull(result.getNotificationSync());
        assertNotNull(result.getNotificationSync().getNotifications());
        assertEquals(SyncResponseStatus.DELTA, result.getNotificationSync().getResponseStatus());


        response = new ServerSync();
        response.setStatus(SyncStatus.SUCCESS);
        NotificationServerSync nfResponse = new NotificationServerSync();
        nfResponse.setNotifications(new ArrayList<Notification>());

        response.setNotificationSync(new NotificationServerSync());

        result = operationsService.updateSyncResponse(response, Collections.singletonList(nfDto), null);
        assertNotNull(result);
        assertNotNull(result.getNotificationSync());
        assertNotNull(result.getNotificationSync().getNotifications());
        assertEquals(SyncResponseStatus.DELTA, result.getNotificationSync().getResponseStatus());
    }

    @Test
    public void updateSyncResponseUnicastTest(){
        ServerSync response = new ServerSync();
        response.setStatus(SyncStatus.SUCCESS);
        response.setNotificationSync(new NotificationServerSync());
        ServerSync result = operationsService.updateSyncResponse(response, new ArrayList<NotificationDto>(), UNICAST_NF_ID);
        assertNotNull(result);
        assertNotNull(result.getNotificationSync());
        assertNotNull(result.getNotificationSync().getNotifications());
        assertEquals(SyncResponseStatus.DELTA, result.getNotificationSync().getResponseStatus());
        assertEquals(1, result.getNotificationSync().getNotifications().size());
        assertEquals(UNICAST_NF_ID, result.getNotificationSync().getNotifications().get(0).getUid());
        assertNotNull(result.getNotificationSync().getNotifications().get(0).getUid());
        assertNull(result.getNotificationSync().getNotifications().get(0).getSeqNumber());
    }

    @Test
    public void updateSyncResponseTopicTest(){
        ServerSync response = new ServerSync();
        response.setStatus(SyncStatus.SUCCESS);
        response.setNotificationSync(new NotificationServerSync());
        ServerSync result = operationsService.updateSyncResponse(response, Collections.singletonList(systemTopicNfDto), null);
        assertNotNull(result);
        assertNotNull(result.getNotificationSync());
        assertNotNull(result.getNotificationSync().getNotifications());
        assertEquals(SyncResponseStatus.DELTA, result.getNotificationSync().getResponseStatus());
        assertEquals(1, result.getNotificationSync().getNotifications().size());
        assertEquals(SYSTEM_TOPIC_ID, result.getNotificationSync().getNotifications().get(0).getTopicId());
        assertEquals(NotificationType.SYSTEM, result.getNotificationSync().getNotifications().get(0).getType());
        assertNull(result.getNotificationSync().getNotifications().get(0).getUid());
        assertNotNull(result.getNotificationSync().getNotifications().get(0).getSeqNumber());
    }

    @Test
    public void isFirstRequestTest(){
        EndpointProfileDto profile = new EndpointProfileDto();
        assertTrue(DefaultOperationsService.isFirstRequest(profile));
        profile.setConfigurationHash(new byte[0]);
        assertTrue(DefaultOperationsService.isFirstRequest(profile));
        
        profile.setGroupState(Collections.singletonList(new EndpointGroupStateDto()));
        assertFalse(DefaultOperationsService.isFirstRequest(profile));        
    }

    //TODO: adjust to current logic
//    @Test
//    public void buildResponseEmptyTest() throws GetDeltaException{
//        GetDeltaResponse deltaResponse = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA);
//        GetNotificationResponse notificationResponse = new GetNotificationResponse();
//        SyncResponseHolder responseHolder = DefaultOperationsService.buildResponse(new SyncResponse(), 123, deltaResponse, notificationResponse);
//
//        assertNotNull(responseHolder);
//        assertNotNull(responseHolder.getResponse());
//        assertEquals(Integer.valueOf(123), responseHolder.getResponse().getAppStateSeqNumber());
//    }

//    @Test
//    public void buildResponseDeltaTest() throws GetDeltaException{
//        GetDeltaResponse deltaResponse = new GetDeltaResponse(GetDeltaResponseType.DELTA, 123, binaryDelta);
//        deltaResponse.setConfSchema(deltaSchemaBody);
//        GetNotificationResponse notificationResponse = new GetNotificationResponse();
//        SyncResponseHolder responseHolder = DefaultOperationsService.buildResponse(new SyncResponse(),123, deltaResponse, notificationResponse);
//
//        assertNotNull(responseHolder);
//        assertNotNull(responseHolder.getResponse());
//        assertEquals(Integer.valueOf(123), responseHolder.getResponse().getAppStateSeqNumber());
//        assertNotNull(responseHolder.getResponse().getConfSyncResponse().getConfDeltaBody());
//        assertNotNull(responseHolder.getResponse().getConfSyncResponse().getConfSchemaBody());
//    }

//    @Test
//    public void buildResponseNotificationTest() throws GetDeltaException{
//        GetDeltaResponse deltaResponse = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA);
//        GetNotificationResponse notificationResponse = new GetNotificationResponse();
//        List<NotificationDto> notifications = new ArrayList<>();
//        notifications.add(systemTopicNfDto);
//        notifications.add(userTopicNfDto);
//        notifications.add(unicastNfDto);
//        notificationResponse.setNotifications(notifications);
//        List<TopicDto> topics = new ArrayList<>();
//        topics.add(systemTopic);
//        topics.add(userTopic);
//        notificationResponse.setTopicList(topics);
//        SyncResponseHolder responseHolder = DefaultOperationsService.buildResponse(new SyncResponse(), 123, deltaResponse, notificationResponse);
//
//        assertNotNull(responseHolder);
//        assertNotNull(responseHolder.getResponse());
//        assertEquals(Integer.valueOf(123), responseHolder.getResponse().getAppStateSeqNumber());
//        assertNotNull(responseHolder.getResponse().getNotificationSyncResponse().getNotifications());
//        assertNotNull(responseHolder.getResponse().getNotificationSyncResponse().getAvailableTopics());
//    }
}
