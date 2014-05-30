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

package org.kaaproject.kaa.server.operations.service.akka;

import static org.mockito.Mockito.mock;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.LongSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.akka.DefaultAkkaService;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyEncodedRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettyDecodedResponseMessage;
import org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultAkkaServiceTest {

    
    private static final String TOPIC_ID = "TopicId";
    private static final String UNICAST_NOTIFICATION_ID = "UnicastNotificationId";
    private static final int TIMEOUT = 10000;
    private static final String APP_TOKEN = "APP_TOKEN";
    private static final String APP_ID = "APP_ID";
    private static final String ENDPOINT_PUBLIC_KEY = "EndpointPublicKey";
    private static final String ENDPOINT_PUBLIC_KEY_HASH = "EndpointPublicKeyHash";
    private static final String PROFILE_BODY = "ProfileBody";
    private AkkaService akkaService;
    //mocks
    private OperationsService operationsService;
    private NotificationDeltaService notificationDeltaService;
    private ApplicationService applicationService;
    private ApplicationDto applicationDto;
    private SyncResponseHolder noDeltaResponse;
    private SyncResponseHolder deltaResponse;
    private SyncResponseHolder noDeltaResponseWithTopicState;
    private NotificationDto topicNotification;
    
    @Before
    public void before() {
        akkaService = new DefaultAkkaService();
        operationsService = mock(OperationsService.class);
        notificationDeltaService = mock(NotificationDeltaService.class);
        applicationService = mock(ApplicationService.class);
        
        ReflectionTestUtils.setField(akkaService, "operationsService", operationsService);
        ReflectionTestUtils.setField(akkaService, "notificationDeltaService", notificationDeltaService);
        ReflectionTestUtils.setField(akkaService, "applicationService", applicationService);
        
        if(akkaService.getActorSystem() == null){
            ((DefaultAkkaService)akkaService).initActorSystem();
        }
        
        applicationDto = new ApplicationDto();
        applicationDto.setId(APP_ID);
        applicationDto.setApplicationToken(APP_TOKEN);
        
        SyncResponse response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.NO_DELTA);
        noDeltaResponse = new SyncResponseHolder(response);
        Map<String, Integer> subscriptionStates = new HashMap<>();
        subscriptionStates.put(TOPIC_ID, new Integer(0));
        noDeltaResponseWithTopicState = new SyncResponseHolder(response, subscriptionStates, 42, 43);
        
        response = new SyncResponse();
        response.setResponseType(SyncResponseStatus.DELTA);
        deltaResponse = new SyncResponseHolder(response, new HashMap<String, Integer>());
        
        topicNotification = new NotificationDto();
        topicNotification.setApplicationId(APP_ID);
        topicNotification.setTopicId(TOPIC_ID);
        topicNotification.setId(UNICAST_NOTIFICATION_ID);
        topicNotification.setExpiredAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
        topicNotification.setSecNum(1);
        topicNotification.setVersion(42);
        topicNotification.setType(NotificationTypeDto.SYSTEM);
        topicNotification.setBody("I am a dummy notification".getBytes());
    }
    
    @After
    public void after() {
        akkaService.getActorSystem().shutdown();
        akkaService.getActorSystem().awaitTermination();
    }
    
    @Test
    public void testAkkaInitialization(){
        Assert.assertNotNull(akkaService.getActorSystem());
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEncodeException() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        Mockito.when(commandMock.decode()).thenThrow(new GeneralSecurityException("test"));
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(channelContextMock, Mockito.timeout(TIMEOUT).atLeastOnce()).fireExceptionCaught(Mockito.any(GeneralSecurityException.class));
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testDecodeException() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        Mockito.doThrow(new IOException("test")).when(commandMock).encode(Mockito.any(SpecificRecordBase.class));
        
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(new NettyDecodedResponseMessage(null, channelContextMock, commandMock, null));
        
        Mockito.verify(channelContextMock, Mockito.timeout(TIMEOUT).atLeastOnce()).fireExceptionCaught(Mockito.any(IOException.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEndpointRegistrationRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        EndpointRegistrationRequest request = new EndpointRegistrationRequest();
        request.setApplicationToken(APP_TOKEN);
        request.setEndpointPublicKey(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY.getBytes()));
        request.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        request.setVersionInfo(new EndpointVersionInfo(1, 2, 3, 4));
        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse(), new HashMap<String, Integer>());
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.registerEndpoint(Mockito.any(EndpointRegistrationRequest.class))).thenReturn(holder);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).registerEndpoint(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEndpointUpdateRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setApplicationToken(APP_TOKEN);
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        request.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        request.setVersionInfo(new EndpointVersionInfo(1, 2, 3, 4));
        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse(), new HashMap<String, Integer>());
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.updateProfile(Mockito.any(ProfileUpdateRequest.class))).thenReturn(holder);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).updateProfile(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSyncRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        SyncRequest request = new SyncRequest();
        request.setApplicationToken(APP_TOKEN);
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse(), new HashMap<String, Integer>());
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(holder);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testMultipleSyncRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        SyncRequest request = new SyncRequest();
        request.setApplicationToken(APP_TOKEN);
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse(), new HashMap<String, Integer>());
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(holder);
        
        Assert.assertNotNull(akkaService.getActorSystem());
        
        NettyEncodedRequestMessage message1 = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        NettyEncodedRequestMessage message2 = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        akkaService.process(message1);
        akkaService.process(message2);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeast(2)).sync(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeast(2)).encode(Mockito.any(SyncResponse.class));
    }    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLongSyncRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        SyncRequest innerRequest = new SyncRequest();
        innerRequest.setApplicationToken(APP_TOKEN);
        innerRequest.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        LongSyncRequest request = new LongSyncRequest(innerRequest, 1000l);
                
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponse);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request.getSyncRequest());
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLongSyncNotification() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        SyncRequest innerRequest = new SyncRequest();
        innerRequest.setApplicationToken(APP_TOKEN);
        innerRequest.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        LongSyncRequest request = new LongSyncRequest(innerRequest, 2l * TIMEOUT);
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponse);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request.getSyncRequest());
        
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(deltaResponse);
        
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        akkaService.onNotification(thriftNotification);
        
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLongSyncUnicastNotification() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        SyncRequest innerRequest = new SyncRequest();
        innerRequest.setApplicationToken(APP_TOKEN);
        innerRequest.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        LongSyncRequest request = new LongSyncRequest(innerRequest, 2l * TIMEOUT);
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponse);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request.getSyncRequest());
        
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setUnicastNotificationId(UNICAST_NOTIFICATION_ID);
        thriftNotification.setKeyHash(ENDPOINT_PUBLIC_KEY_HASH.getBytes());
        akkaService.onNotification(thriftNotification);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).updateSyncResponse(noDeltaResponse.getResponse(), new ArrayList<NotificationDto>(), UNICAST_NOTIFICATION_ID);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLongSyncTopicNotificationOnStart() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setTopicId(TOPIC_ID);
        thriftNotification.setNotificationId(UNICAST_NOTIFICATION_ID);
        thriftNotification.setKeyHash(ENDPOINT_PUBLIC_KEY_HASH.getBytes());
        akkaService.onNotification(thriftNotification);        
        
        Mockito.when(notificationDeltaService.findNotificationById(UNICAST_NOTIFICATION_ID)).thenReturn(topicNotification);
        
        SyncRequest innerRequest = new SyncRequest();
        innerRequest.setApplicationToken(APP_TOKEN);
        innerRequest.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        LongSyncRequest request = new LongSyncRequest(innerRequest, 2l * TIMEOUT);
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponseWithTopicState);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request.getSyncRequest());
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).updateSyncResponse(noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLongSyncTopicNotification() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        SyncRequest innerRequest = new SyncRequest();
        innerRequest.setApplicationToken(APP_TOKEN);
        innerRequest.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        LongSyncRequest request = new LongSyncRequest(innerRequest, 4l * TIMEOUT);        
        
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Mockito.when(notificationDeltaService.findNotificationById(UNICAST_NOTIFICATION_ID)).thenReturn(topicNotification);
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponseWithTopicState);        

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Thread.sleep(3000);
        
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setTopicId(TOPIC_ID);
        thriftNotification.setNotificationId(UNICAST_NOTIFICATION_ID);
        thriftNotification.setKeyHash(ENDPOINT_PUBLIC_KEY_HASH.getBytes());
        akkaService.onNotification(thriftNotification);        
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request.getSyncRequest());
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).updateSyncResponse(noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRedirect() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        
        akkaService.onRedirectionRule(new RedirectionRule("testDNS", 123, 1.0, 1000));
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        akkaService.process(message);
        
        RedirectSyncResponse redirectSyncResponse = new RedirectSyncResponse("testDNS");
        SyncResponse response = new SyncResponse(0, SyncResponseStatus.REDIRECT, null, null, redirectSyncResponse);
        Mockito.verify(commandMock, Mockito.never()).decode();
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(response);
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRedirectExpire() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        SyncRequest request = new SyncRequest();
        request.setApplicationToken(APP_TOKEN);
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse(), new HashMap<String, Integer>());
        
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(holder);
        
        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS", 123, 1.0, 1000));
        Thread.sleep(2000);
        akkaService.process(message);
        
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }
}
