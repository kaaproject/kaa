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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.NotificationType;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationResponse;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse.GetDeltaResponseType;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.DefaultOperationsService;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.delta.DefaultDeltaCalculatorTest;
import org.kaaproject.kaa.server.operations.service.delta.DeltaServiceIT;
import org.kaaproject.kaa.server.operations.service.delta.RawBinaryDelta;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
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
    
    

    protected static final Logger LOG = LoggerFactory.getLogger(DeltaServiceIT.class);

    private OperationsService operationsService;
    private NotificationDeltaService notificationDeltaService;
    
    private TopicDto systemTopic;
    private TopicDto userTopic;
    private NotificationDto unicastNfDto;
    private NotificationDto systemTopicNfDto;
    private NotificationDto userTopicNfDto;
    private RawBinaryDelta binaryDelta;
    private String deltaSchemaBody;

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
        userTopic.setType(TopicTypeDto.VOLUNTARY);
        userTopic.setName(USER_TOPIC_NAME);        
        
        deltaSchemaBody = OperationsServiceIT.getResourceAsString(DefaultDeltaCalculatorTest.COMPLEX_PROTOCOL_SCHEMA);
        binaryDelta = DefaultDeltaCalculatorTest.getComplexFieldDelta(new Schema.Parser().parse(deltaSchemaBody));
    }
    
    @Test
    public void updateSyncResponseEmptyTest(){
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.NO_DELTA);
        SyncResponse result = operationsService.updateSyncResponse(response, new ArrayList<NotificationDto>(), null);
        assertNotNull(result);
        assertEquals(SyncResponseStatus.DELTA, response.getResponseType());
        assertNotNull(result.getNotificationSyncResponse());
        assertNotNull(result.getNotificationSyncResponse().getNotifications());
    }
    
    @Test
    public void updateSyncResponseNotEmptyTest(){
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.NO_DELTA);
        response.setNotificationSyncResponse(new NotificationSyncResponse());
        SyncResponse result = operationsService.updateSyncResponse(response, new ArrayList<NotificationDto>(), null);
        assertNotNull(result);
        assertEquals(SyncResponseStatus.DELTA, response.getResponseType());
        assertNotNull(result.getNotificationSyncResponse());
        assertNotNull(result.getNotificationSyncResponse().getNotifications());
        
        response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.NO_DELTA);
        NotificationSyncResponse nfResponse = new NotificationSyncResponse();
        nfResponse.setNotifications(new ArrayList<Notification>());
        
        response.setNotificationSyncResponse(new NotificationSyncResponse());

        result = operationsService.updateSyncResponse(response, new ArrayList<NotificationDto>(), null);
        assertNotNull(result);
        assertEquals(SyncResponseStatus.DELTA, response.getResponseType());
        assertNotNull(result.getNotificationSyncResponse());
        assertNotNull(result.getNotificationSyncResponse().getNotifications());        
    }
    
    @Test
    public void updateSyncResponseUnicastTest(){
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.NO_DELTA);
        response.setNotificationSyncResponse(new NotificationSyncResponse());
        SyncResponse result = operationsService.updateSyncResponse(response, new ArrayList<NotificationDto>(), UNICAST_NF_ID);
        assertNotNull(result);
        assertEquals(SyncResponseStatus.DELTA, response.getResponseType());
        assertNotNull(result.getNotificationSyncResponse());
        assertNotNull(result.getNotificationSyncResponse().getNotifications());        
        assertEquals(1, result.getNotificationSyncResponse().getNotifications().size());
        assertEquals(UNICAST_NF_ID, result.getNotificationSyncResponse().getNotifications().get(0).getUid());
        assertNotNull(result.getNotificationSyncResponse().getNotifications().get(0).getUid());
        assertNull(result.getNotificationSyncResponse().getNotifications().get(0).getSeqNumber());
    }
    
    @Test
    public void updateSyncResponseTopicTest(){
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.NO_DELTA);
        response.setNotificationSyncResponse(new NotificationSyncResponse());
        SyncResponse result = operationsService.updateSyncResponse(response, Collections.singletonList(systemTopicNfDto), null);
        assertNotNull(result);
        assertEquals(SyncResponseStatus.DELTA, response.getResponseType());
        assertNotNull(result.getNotificationSyncResponse());
        assertNotNull(result.getNotificationSyncResponse().getNotifications());        
        assertEquals(1, result.getNotificationSyncResponse().getNotifications().size());
        assertEquals(SYSTEM_TOPIC_ID, result.getNotificationSyncResponse().getNotifications().get(0).getTopicId());
        assertEquals(NotificationType.SYSTEM, result.getNotificationSyncResponse().getNotifications().get(0).getType());
        assertNull(result.getNotificationSyncResponse().getNotifications().get(0).getUid());
        assertNotNull(result.getNotificationSyncResponse().getNotifications().get(0).getSeqNumber());
    }
    
    @Test 
    public void buildProfileResyncResponseTest(){
        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setAppStateSeqNumber(123);
        SyncResponseHolder syncResponseHolder = DefaultOperationsService.buildProfileResyncResponse(syncRequest);
        assertNotNull(syncResponseHolder);
        assertNotNull(syncResponseHolder.getResponse());
        assertEquals(SyncResponseStatus.PROFILE_RESYNC, syncResponseHolder.getResponse().getResponseType());
    }
    
    @Test 
    public void isFirstRequestTest(){
        SyncRequest syncRequest = new SyncRequest();
        assertTrue(DefaultOperationsService.isFirstRequest(syncRequest));
        syncRequest.setConfigurationHash(ByteBuffer.wrap(new byte[0]));
        assertTrue(DefaultOperationsService.isFirstRequest(syncRequest));
    }
    
    @Test
    public void buildResponseEmptyTest() throws GetDeltaException{
        GetDeltaResponse deltaResponse = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA);
        GetNotificationResponse notificationResponse = new GetNotificationResponse();
        SyncResponseHolder responseHolder = DefaultOperationsService.buildResponse(123, deltaResponse, notificationResponse);
        
        assertNotNull(responseHolder);
        assertNotNull(responseHolder.getResponse());
        assertEquals(Integer.valueOf(123), responseHolder.getResponse().getAppStateSeqNumber());
    }

    @Test
    public void buildResponseDeltaTest() throws GetDeltaException{
        GetDeltaResponse deltaResponse = new GetDeltaResponse(GetDeltaResponseType.DELTA, 123, binaryDelta);
        deltaResponse.setConfSchema(deltaSchemaBody);
        GetNotificationResponse notificationResponse = new GetNotificationResponse();
        SyncResponseHolder responseHolder = DefaultOperationsService.buildResponse(123, deltaResponse, notificationResponse);
        
        assertNotNull(responseHolder);
        assertNotNull(responseHolder.getResponse());
        assertEquals(Integer.valueOf(123), responseHolder.getResponse().getAppStateSeqNumber());
        assertNotNull(responseHolder.getResponse().getConfSyncResponse().getConfDeltaBody());
        assertNotNull(responseHolder.getResponse().getConfSyncResponse().getConfSchemaBody());
    }
    
    @Test
    public void buildResponseNotificationTest() throws GetDeltaException{
        GetDeltaResponse deltaResponse = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA);
        GetNotificationResponse notificationResponse = new GetNotificationResponse();
        List<NotificationDto> notifications = new ArrayList<>();
        notifications.add(systemTopicNfDto);
        notifications.add(userTopicNfDto);
        notifications.add(unicastNfDto);
        notificationResponse.setNotifications(notifications);
        List<TopicDto> topics = new ArrayList<>();
        topics.add(systemTopic);
        topics.add(userTopic);
        notificationResponse.setTopicList(topics);
        SyncResponseHolder responseHolder = DefaultOperationsService.buildResponse(123, deltaResponse, notificationResponse);
        
        assertNotNull(responseHolder);
        assertNotNull(responseHolder.getResponse());
        assertEquals(Integer.valueOf(123), responseHolder.getResponse().getAppStateSeqNumber());
        assertNotNull(responseHolder.getResponse().getNotificationSyncResponse().getNotifications());
        assertNotNull(responseHolder.getResponse().getNotificationSyncResponse().getAvailableTopics());
    }
}
