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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.Event;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.LogEntry;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyEncodedRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettyDecodedResponseMessage;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.event.EndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.RouteInfo;
import org.kaaproject.kaa.server.operations.service.event.RouteOperation;
import org.kaaproject.kaa.server.operations.service.event.RouteTableAddress;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.kaaproject.kaa.server.operations.service.event.UserRouteInfo;
import org.kaaproject.kaa.server.operations.service.http.commands.AbstractOperationsCommand;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultAkkaServiceTest {

    private static final String SERVER2 = "SERVER2";
    private static final String FQN1 = "fqn1";
    private static final int ECF1_VERSION = 43;
    private static final String ECF1_ID = "EF1_ID";
    private static final String TOPIC_ID = "TopicId";
    private static final String UNICAST_NOTIFICATION_ID = "UnicastNotificationId";
    private static final int TIMEOUT = 10000;
    private static final String TENANT_ID = "TENANT_ID";
    private static final String USER_ID = "USER_ID";
    private static final String APP_TOKEN = "APP_TOKEN";
    private static final String APP_ID = "APP_ID";
    private static final String ENDPOINT_PUBLIC_KEY = "EndpointPublicKey";
    private static final String ENDPOINT_PUBLIC_KEY_HASH = "EndpointPublicKeyHash";
    private static final String PROFILE_BODY = "ProfileBody";
    private DefaultAkkaService akkaService;

    //mocks
    private CacheService cacheService;
    private OperationsService operationsService;
    private NotificationDeltaService notificationDeltaService;
    private ApplicationService applicationService;
    private EventService eventService;
    private ApplicationDto applicationDto;
    private SyncResponseHolder noDeltaResponse;
    private SyncResponseHolder deltaResponse;
    private SyncResponseHolder noDeltaResponseWithTopicState;
    private NotificationDto topicNotification;
    private LogAppenderService logAppenderService;

    @Before
    public void before() {
        akkaService = new DefaultAkkaService();
        cacheService = mock(CacheService.class);
        operationsService = mock(OperationsService.class);
        notificationDeltaService = mock(NotificationDeltaService.class);
        applicationService = mock(ApplicationService.class);
        eventService = mock(EventService.class);
        logAppenderService = mock(LogAppenderService.class);


        ReflectionTestUtils.setField(akkaService, "cacheService", cacheService);
        ReflectionTestUtils.setField(akkaService, "operationsService", operationsService);
        ReflectionTestUtils.setField(akkaService, "notificationDeltaService", notificationDeltaService);
        ReflectionTestUtils.setField(akkaService, "applicationService", applicationService);
        ReflectionTestUtils.setField(akkaService, "eventService", eventService);
        ReflectionTestUtils.setField(akkaService, "logAppenderService", logAppenderService);


        if(akkaService.getActorSystem() == null){
            akkaService.initActorSystem();
        }

        Mockito.when(cacheService.getTenantIdByAppToken(APP_TOKEN)).thenReturn(TENANT_ID);

        applicationDto = new ApplicationDto();
        applicationDto.setId(APP_ID);
        applicationDto.setApplicationToken(APP_TOKEN);

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);
        ConfigurationSyncResponse confSyncResponse = new ConfigurationSyncResponse();
        confSyncResponse.setResponseStatus(SyncResponseStatus.NO_DELTA);
        response.setConfigurationSyncResponse(confSyncResponse);
        noDeltaResponse = new SyncResponseHolder(response);

        Map<String, Integer> subscriptionStates = new HashMap<>();
        subscriptionStates.put(TOPIC_ID, new Integer(0));
        noDeltaResponseWithTopicState = new SyncResponseHolder(response);
        noDeltaResponseWithTopicState.setSubscriptionStates(subscriptionStates);
        EndpointProfileDto epDto = new EndpointProfileDto();
        epDto.setSystemNfVersion(42);
        epDto.setUserNfVersion(43);
        epDto.setLogSchemaVersion(44);
        noDeltaResponseWithTopicState.setEndpointProfile(epDto);


        response = new SyncResponse();
        response.setStatus(SyncResponseResultType.SUCCESS);
        confSyncResponse = new ConfigurationSyncResponse();
        confSyncResponse.setResponseStatus(SyncResponseStatus.DELTA);
        response.setConfigurationSyncResponse(confSyncResponse);
        deltaResponse = new SyncResponseHolder(response);

        topicNotification = new NotificationDto();
        topicNotification.setApplicationId(APP_ID);
        topicNotification.setTopicId(TOPIC_ID);
        topicNotification.setId(UNICAST_NOTIFICATION_ID);
        topicNotification.setExpiredAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
        topicNotification.setSecNum(1);
        topicNotification.setVersion(42);
        topicNotification.setType(NotificationTypeDto.SYSTEM);
        topicNotification.setBody("I am a dummy notification".getBytes());

        when(applicationService.findAppByApplicationToken(APP_TOKEN)).thenReturn(applicationDto);
        when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
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

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP);
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
        akkaService.process(new NettyDecodedResponseMessage(null, channelContextMock, commandMock, ChannelType.HTTP, null));

        Mockito.verify(channelContextMock, Mockito.timeout(TIMEOUT).atLeastOnce()).fireExceptionCaught(Mockito.any(IOException.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEndpointRegistrationRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(SHA1HashUtils.hashToBytes(ENDPOINT_PUBLIC_KEY.getBytes())));
        request.setSyncRequestMetaData(md);

        ProfileSyncRequest profileSync = new ProfileSyncRequest();
        profileSync.setEndpointPublicKey(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY.getBytes()));
        profileSync.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        profileSync.setVersionInfo(new EndpointVersionInfo(1, 2, 3, 4, null, 5));
        request.setProfileSyncRequest(profileSync);

        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse());

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(request)).thenReturn(holder);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEndpointUpdateRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        request.setSyncRequestMetaData(md);

        ProfileSyncRequest profileSync = new ProfileSyncRequest();
        profileSync.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        profileSync.setVersionInfo(new EndpointVersionInfo(1, 2, 3, 4, null, 5));
        request.setProfileSyncRequest(profileSync);

        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse());


        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(request)).thenReturn(holder);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSyncRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);
        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        request.setSyncRequestMetaData(md);

        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse());

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(request)).thenReturn(holder);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP);
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
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        request.setSyncRequestMetaData(md);


        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse());

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(request)).thenReturn(holder);

        Assert.assertNotNull(akkaService.getActorSystem());

        NettyEncodedRequestMessage message1 = new NettyEncodedRequestMessage("testUUID1", channelContextMock, commandMock, ChannelType.HTTP);
        NettyEncodedRequestMessage message2 = new NettyEncodedRequestMessage("testUUID2", channelContextMock, commandMock, ChannelType.HTTP);
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

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(1000l);
        request.setSyncRequestMetaData(md);

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponse);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP_LP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLongSyncNotification() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        ConfigurationSyncRequest csRequest = new ConfigurationSyncRequest();
        request.setConfigurationSyncRequest(csRequest);

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponse);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP_LP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request);

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

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponse);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP_LP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request);

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

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(request)).thenReturn(noDeltaResponseWithTopicState);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP_LP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request);
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).updateSyncResponse(noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLongSyncTopicNotification() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Mockito.when(notificationDeltaService.findNotificationById(UNICAST_NOTIFICATION_ID)).thenReturn(topicNotification);
        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponseWithTopicState);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP_LP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Thread.sleep(3000);

        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setTopicId(TOPIC_ID);
        thriftNotification.setNotificationId(UNICAST_NOTIFICATION_ID);
        thriftNotification.setKeyHash(ENDPOINT_PUBLIC_KEY_HASH.getBytes());
        akkaService.onNotification(thriftNotification);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request);
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).updateSyncResponse(noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRedirect() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);

        akkaService.onRedirectionRule(new RedirectionRule("testDNS", 123, 1.0, 1000));

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.TCP);
        akkaService.process(message);

        SyncResponse response = new SyncResponse();
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(new RedirectSyncResponse("testDNS"));

        Mockito.verify(commandMock, Mockito.never()).decode();
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).encode(response);
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRedirectExpire() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        request.setSyncRequestMetaData(md);

        SyncResponseHolder holder = new SyncResponseHolder(new SyncResponse());

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(holder);

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS", 123, 1.0, 1000));
        Thread.sleep(2000);
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request);
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));
    }

    private static final byte[] ENDPOINT_KEY_HASH_SOURCE = new byte[]{1,2,3};
    private static final byte[] ENDPOINT_KEY_HASH_TARGET = new byte[]{4,5,6};

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEndpointEventBasic() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand sourceCommandMock = Mockito.mock(AbstractOperationsCommand.class);
        AbstractOperationsCommand targetCommandMock = Mockito.mock(AbstractOperationsCommand.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);
        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), null, null);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(TIMEOUT*1L);
        sourceRequest.setSyncRequestMetaData(md);

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEvents(Arrays.asList(event));
        sourceRequest.setEventSyncRequest(eventRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        SyncRequest targetRequest = new SyncRequest();
        md = new SyncRequestMetaData( );
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_TARGET));
        md.setTimeout(TIMEOUT*1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(sourceCommandMock.decode()).thenReturn(sourceRequest);
        when(targetCommandMock.decode()).thenReturn(targetRequest);
        when(operationsService.sync(sourceRequest)).thenReturn(sourceResponseHolder);
        when(operationsService.sync(targetRequest)).thenReturn(targetResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_SOURCE);
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_TARGET);
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        NettyEncodedRequestMessage sourceMessage = new NettyEncodedRequestMessage("testUUID1", channelContextMock, sourceCommandMock, ChannelType.HTTP_LP);
        akkaService.process(sourceMessage);

        NettyEncodedRequestMessage targetMessage = new NettyEncodedRequestMessage("testUUID2", channelContextMock, targetCommandMock, ChannelType.HTTP_LP);
        akkaService.process(targetMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(sourceRequest);
        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest);

        SyncResponse eventResponse = new SyncResponse();
        eventResponse.setStatus(SyncResponseResultType.SUCCESS);
        eventResponse.setEventSyncResponse(new EventSyncResponse());
        eventResponse.getEventSyncResponse().setEvents(Arrays.asList(event));
        verify(targetCommandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(eventResponse);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRemoteIncomingEndpointEventBasic() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand targetCommandMock = Mockito.mock(AbstractOperationsCommand.class);

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData( );
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_TARGET));
        md.setTimeout(TIMEOUT*1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();

        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(targetCommandMock.decode()).thenReturn(targetRequest);
        when(operationsService.sync(targetRequest)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_TARGET);
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        NettyEncodedRequestMessage targetMessage = new NettyEncodedRequestMessage("testUUID2", channelContextMock, targetCommandMock, ChannelType.HTTP_LP);
        akkaService.process(targetMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), null, null);
        EndpointEvent endpointEvent = new EndpointEvent(EndpointObjectHash.fromBytes(ENDPOINT_KEY_HASH_SOURCE), event, UUID.randomUUID(), System.currentTimeMillis(), ECF1_VERSION);
        RemoteEndpointEvent remoteEvent = new RemoteEndpointEvent(TENANT_ID, USER_ID, endpointEvent, new RouteTableAddress(EndpointObjectHash.fromBytes(ENDPOINT_KEY_HASH_TARGET), APP_TOKEN, "SERVER1"));
        akkaService.getListener().onEvent(remoteEvent);

        SyncResponse eventResponse = new SyncResponse();
        eventResponse.setStatus(SyncResponseResultType.SUCCESS);
        eventResponse.setEventSyncResponse(new EventSyncResponse());
        eventResponse.getEventSyncResponse().setEvents(Arrays.asList(event));
        verify(targetCommandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(eventResponse);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRemoteOutcomingEndpointEventBasic() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand sourceCommandMock = Mockito.mock(AbstractOperationsCommand.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), null, null);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(TIMEOUT*1L);
        sourceRequest.setSyncRequestMetaData(md);

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEvents(Arrays.asList(event));
        sourceRequest.setEventSyncRequest(eventRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        when(sourceCommandMock.decode()).thenReturn(sourceRequest);
        when(operationsService.sync(sourceRequest)).thenReturn(sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_SOURCE);
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        NettyEncodedRequestMessage sourceMessage = new NettyEncodedRequestMessage("testUUID1", channelContextMock, sourceCommandMock, ChannelType.HTTP_LP);
        akkaService.process(sourceMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(sourceRequest);

        UserRouteInfo userRouteInfo = new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.ADD);
        akkaService.getListener().onUserRouteInfo(userRouteInfo);

        RouteTableAddress remoteAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(ENDPOINT_KEY_HASH_TARGET), APP_TOKEN, SERVER2);
        RouteInfo routeInfo = new RouteInfo(TENANT_ID, USER_ID, remoteAddress, Arrays.asList(new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION)));
        TimeUnit.SECONDS.sleep(2);
        akkaService.getListener().onRouteInfo(routeInfo);

        verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendEvent(Mockito.any(RemoteEndpointEvent.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLogSyncRequest() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand commandMock = Mockito.mock(AbstractOperationsCommand.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_PUBLIC_KEY_HASH.getBytes()));
        md.setTimeout(1000l);
        request.setSyncRequestMetaData(md);

        LogSyncRequest logRequest = new LogSyncRequest("logUploadRequest1", new ArrayList<LogEntry>());
        request.setLogSyncRequest(logRequest);

        Mockito.when(commandMock.decode()).thenReturn(request);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class))).thenReturn(noDeltaResponseWithTopicState);
        LogAppender mockAppender = Mockito.mock(LogAppender.class);
        Mockito.when(logAppenderService.getApplicationAppenders(APP_ID)).thenReturn(Collections.singletonList(mockAppender));

        NettyEncodedRequestMessage message = new NettyEncodedRequestMessage("testUUID", channelContextMock, commandMock, ChannelType.HTTP_LP);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request);
        Mockito.verify(logAppenderService, Mockito.timeout(TIMEOUT).atLeastOnce()).getLogSchema(APP_ID, 44);
        Mockito.verify(mockAppender, Mockito.timeout(TIMEOUT).atLeastOnce()).doAppend(Mockito.any(LogEventPack.class));
        Mockito.verify(commandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(Mockito.any(SyncResponse.class));

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testUserChange() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand targetCommandMock = Mockito.mock(AbstractOperationsCommand.class);

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData( );
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_TARGET));
        md.setTimeout(TIMEOUT*1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(targetCommandMock.decode()).thenReturn(targetRequest);
        when(operationsService.sync(targetRequest)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_TARGET);
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        NettyEncodedRequestMessage targetMessage = new NettyEncodedRequestMessage("testUUID2", channelContextMock, targetCommandMock, ChannelType.HTTP_LP);
        akkaService.process(targetMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest);
        verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        UserRouteInfo userRouteInfo = new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.ADD);
        akkaService.getListener().onUserRouteInfo(userRouteInfo);
        TimeUnit.SECONDS.sleep(2);

        RouteTableAddress remoteAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(ENDPOINT_KEY_HASH_SOURCE), APP_TOKEN, SERVER2);
        RouteInfo routeInfo = new RouteInfo(TENANT_ID, USER_ID, remoteAddress, Arrays.asList(new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION)));
        TimeUnit.SECONDS.sleep(2);
        akkaService.getListener().onRouteInfo(routeInfo);


        targetRequest = new SyncRequest();
        md = new SyncRequestMetaData( );
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_TARGET));
        md.setTimeout(TIMEOUT*1L);
        targetRequest.setSyncRequestMetaData(md);

        targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID+"2");
        when(targetCommandMock.decode()).thenReturn(targetRequest);
        when(operationsService.sync(targetRequest)).thenReturn(targetResponseHolder);

        targetMessage = new NettyEncodedRequestMessage("testUUID3", channelContextMock, targetCommandMock, ChannelType.HTTP_LP);
        akkaService.process(targetMessage);
        verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID+"2"));
        RouteTableAddress localAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(ENDPOINT_KEY_HASH_TARGET), APP_TOKEN, null);
        verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendRouteInfo(RouteInfo.deleteRouteFromAddress(TENANT_ID, USER_ID, localAddress), "SERVER2");

    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEndpointAttach() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand targetCommandMock = Mockito.mock(AbstractOperationsCommand.class);

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData( );
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_TARGET));
        md.setTimeout(TIMEOUT*1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(targetCommandMock.decode()).thenReturn(targetRequest);
        when(operationsService.sync(targetRequest)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_TARGET);
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        NettyEncodedRequestMessage targetMessage = new NettyEncodedRequestMessage("testUUID2", channelContextMock, targetCommandMock, ChannelType.HTTP_LP);
        akkaService.process(targetMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest);
        verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData sourceMd = new SyncRequestMetaData( );
        sourceMd.setApplicationToken(APP_TOKEN);
        sourceMd.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_SOURCE));
        sourceMd.setTimeout(TIMEOUT*1L);
        sourceRequest.setSyncRequestMetaData(sourceMd);

        sourceRequest.setEventSyncRequest(new EventSyncRequest());

        UserSyncRequest userSyncRequest = new UserSyncRequest();
        EndpointAttachRequest eaRequest = new EndpointAttachRequest("request1", "token");
        userSyncRequest.setEndpointAttachRequests(Collections.singletonList(eaRequest));
        sourceRequest.setUserSyncRequest(userSyncRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        UserSyncResponse userSyncResponse = new UserSyncResponse();
        userSyncResponse.setEndpointAttachResponses(Collections.singletonList(new EndpointAttachResponse("request1", Base64Util.encode(ENDPOINT_KEY_HASH_TARGET), SyncResponseResultType.SUCCESS)));
        sourceResponse.setUserSyncResponse(userSyncResponse);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        AbstractOperationsCommand sourceCommandMock = Mockito.mock(AbstractOperationsCommand.class);
        when(sourceCommandMock.decode()).thenReturn(sourceRequest);
        when(operationsService.sync(sourceRequest)).thenReturn(sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_SOURCE);
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        NettyEncodedRequestMessage sourceMessage = new NettyEncodedRequestMessage("testUUID2", channelContextMock, sourceCommandMock, ChannelType.HTTP_LP);
        akkaService.process(sourceMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(sourceRequest);
        SyncResponse targetSyncResponse = new SyncResponse();
        targetSyncResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetSyncResponse.setUserSyncResponse(new UserSyncResponse());
        targetSyncResponse.getUserSyncResponse().setUserAttachNotification(new UserAttachNotification(USER_ID, Base64Util.encode(ENDPOINT_KEY_HASH_SOURCE)));
        verify(targetCommandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(targetSyncResponse);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEndpointDetach() throws Exception{
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        AbstractOperationsCommand targetCommandMock = Mockito.mock(AbstractOperationsCommand.class);

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData( );
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_TARGET));
        md.setTimeout(TIMEOUT*1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(targetCommandMock.decode()).thenReturn(targetRequest);
        when(operationsService.sync(targetRequest)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_TARGET);
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION))).thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        NettyEncodedRequestMessage targetMessage = new NettyEncodedRequestMessage("testUUID2", channelContextMock, targetCommandMock, ChannelType.HTTP_LP);
        akkaService.process(targetMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest);
        verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData sourceMd = new SyncRequestMetaData( );
        sourceMd.setApplicationToken(APP_TOKEN);
        sourceMd.setEndpointPublicKeyHash(ByteBuffer.wrap(ENDPOINT_KEY_HASH_SOURCE));
        sourceMd.setTimeout(TIMEOUT*1L);
        sourceRequest.setSyncRequestMetaData(sourceMd);

        sourceRequest.setEventSyncRequest(new EventSyncRequest());

        UserSyncRequest userSyncRequest = new UserSyncRequest();
        EndpointDetachRequest eaRequest = new EndpointDetachRequest("request1", Base64Util.encode(ENDPOINT_KEY_HASH_TARGET));
        userSyncRequest.setEndpointDetachRequests(Collections.singletonList(eaRequest));
        sourceRequest.setUserSyncRequest(userSyncRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        UserSyncResponse userSyncResponse = new UserSyncResponse();
        userSyncResponse.setEndpointDetachResponses(Collections.singletonList(new EndpointDetachResponse("request1", SyncResponseResultType.SUCCESS)));
        sourceResponse.setUserSyncResponse(userSyncResponse);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        AbstractOperationsCommand sourceCommandMock = Mockito.mock(AbstractOperationsCommand.class);
        when(sourceCommandMock.decode()).thenReturn(sourceRequest);
        when(operationsService.sync(sourceRequest)).thenReturn(sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(ENDPOINT_KEY_HASH_SOURCE);
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        NettyEncodedRequestMessage sourceMessage = new NettyEncodedRequestMessage("testUUID2", channelContextMock, sourceCommandMock, ChannelType.HTTP_LP);
        akkaService.process(sourceMessage);

        verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(sourceRequest);
        SyncResponse targetSyncResponse = new SyncResponse();
        targetSyncResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetSyncResponse.setUserSyncResponse(new UserSyncResponse());
        targetSyncResponse.getUserSyncResponse().setUserDetachNotification(new UserDetachNotification(Base64Util.encode(ENDPOINT_KEY_HASH_SOURCE)));
        verify(targetCommandMock, Mockito.timeout(TIMEOUT).atLeastOnce()).encode(targetSyncResponse);
    }

}
