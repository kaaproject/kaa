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
import static org.mockito.Mockito.when;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
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
import org.kaaproject.kaa.common.endpoint.gen.EventSequenceNumberRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSequenceNumberResponse;
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
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder.CipherPair;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ErrorBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpSyncMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ResponseBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionAwareRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionInitRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SyncStatistics;
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
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.metrics.MeterClient;
import org.kaaproject.kaa.server.operations.service.metrics.MetricsService;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;
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
    private static final String PROFILE_BODY = "ProfileBody";

    private static final int REQUEST_ID = 42;

    private DefaultAkkaService akkaService;

    // mocks
    private CacheService cacheService;
    private MetricsService metricsService;
    private KeyStoreService keyStoreService;
    private OperationsService operationsService;
    private NotificationDeltaService notificationDeltaService;
    private ApplicationService applicationService;
    private EventService eventService;
    private ApplicationDto applicationDto;

    private SyncResponseHolder simpleResponse;
    private SyncResponseHolder noDeltaResponse;
    private SyncResponseHolder deltaResponse;
    private SyncResponseHolder noDeltaResponseWithTopicState;
    private NotificationDto topicNotification;
    private LogAppenderService logAppenderService;

    private KeyPair clientPair;
    private KeyPair targetPair;
    private KeyPair serverPair;

    private ByteBuffer clientPublicKey;
    private ByteBuffer clientPublicKeyHash;

    private ByteBuffer targetPublicKeyHash;

    @Before
    public void before() throws GeneralSecurityException {
        akkaService = new DefaultAkkaService();
        cacheService = mock(CacheService.class);
        metricsService = mock(MetricsService.class);
        keyStoreService = mock(KeyStoreService.class);
        operationsService = mock(OperationsService.class);
        notificationDeltaService = mock(NotificationDeltaService.class);
        applicationService = mock(ApplicationService.class);
        eventService = mock(EventService.class);
        logAppenderService = mock(LogAppenderService.class);

        ReflectionTestUtils.setField(akkaService, "cacheService", cacheService);
        ReflectionTestUtils.setField(akkaService, "metricsService", metricsService);
        ReflectionTestUtils.setField(akkaService, "keyStoreService", keyStoreService);
        ReflectionTestUtils.setField(akkaService, "operationsService", operationsService);
        ReflectionTestUtils.setField(akkaService, "notificationDeltaService", notificationDeltaService);
        ReflectionTestUtils.setField(akkaService, "applicationService", applicationService);
        ReflectionTestUtils.setField(akkaService, "eventService", eventService);
        ReflectionTestUtils.setField(akkaService, "logAppenderService", logAppenderService);

        clientPair = KeyUtil.generateKeyPair();
        targetPair = KeyUtil.generateKeyPair();
        serverPair = KeyUtil.generateKeyPair();

        Mockito.when(keyStoreService.getPublicKey()).thenReturn(serverPair.getPublic());
        Mockito.when(keyStoreService.getPrivateKey()).thenReturn(serverPair.getPrivate());
        Mockito.when(metricsService.createMeter(Mockito.anyString(), Mockito.anyString())).thenReturn(Mockito.mock(MeterClient.class));

        if (akkaService.getActorSystem() == null) {
            akkaService.initActorSystem();
        }

        clientPublicKey = ByteBuffer.wrap(clientPair.getPublic().getEncoded());
        clientPublicKeyHash = ByteBuffer.wrap(SHA1HashUtils.hashToBytes(clientPair.getPublic().getEncoded()));

        targetPublicKeyHash = ByteBuffer.wrap(SHA1HashUtils.hashToBytes(targetPair.getPublic().getEncoded()));

        Mockito.when(cacheService.getTenantIdByAppToken(APP_TOKEN)).thenReturn(TENANT_ID);
        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()))).thenReturn(
                clientPair.getPublic());
        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(targetPublicKeyHash.array()))).thenReturn(
                targetPair.getPublic());

        applicationDto = new ApplicationDto();
        applicationDto.setId(APP_ID);
        applicationDto.setApplicationToken(APP_TOKEN);

        SyncResponse response = new SyncResponse();
        response.setRequestId(REQUEST_ID);
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

        response = new SyncResponse();
        response.setRequestId(REQUEST_ID);
        response.setStatus(SyncResponseResultType.SUCCESS);
        simpleResponse = new SyncResponseHolder(response);

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

    private SessionInitRequest toSignedRequest(final UUID uuid, final ChannelType channelType, final ChannelHandlerContext ctx,
            SyncRequest request, final ResponseBuilder responseBuilder, final ErrorBuilder errorBuilder) throws Exception {
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        return toSignedRequest(uuid, channelType, ctx, request, responseBuilder, errorBuilder, crypt);
    }

    private SessionInitRequest toSignedRequest(final UUID uuid, final ChannelType channelType, final ChannelHandlerContext ctx,
            SyncRequest request, final ResponseBuilder responseBuilder, final ErrorBuilder errorBuilder, MessageEncoderDecoder crypt)
            throws Exception {
        AvroByteArrayConverter<SyncRequest> requestConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        byte[] data = requestConverter.toByteArray(request);

        final byte[] encodedData = crypt.encodeData(data);
        final byte[] encodedSessionKey = crypt.getEncodedSessionKey();
        final byte[] sessionKeySignature = crypt.sign(encodedSessionKey);

        return new SessionInitRequest() {
            @Override
            public UUID getChannelUuid() {
                return uuid;
            }

            @Override
            public ChannelType getChannelType() {
                return channelType;
            }

            @Override
            public ChannelHandlerContext getChannelContext() {
                return ctx;
            }

            @Override
            public byte[] getSessionKeySignature() {
                return sessionKeySignature;
            }

            @Override
            public byte[] getEncodedSessionKey() {
                return encodedSessionKey;
            }

            @Override
            public byte[] getEncodedRequestData() {
                return encodedData;
            }

            @Override
            public SyncStatistics getSyncStatistics() {
                return null;
            }

            @Override
            public ResponseBuilder getResponseBuilder() {
                return responseBuilder;
            }

            @Override
            public ErrorBuilder getErrorBuilder() {
                return errorBuilder;
            }

            @Override
            public void onSessionCreated(NettySessionInfo session) {
            }

            @Override
            public int getKeepAlive() {
                return 100;
            }

            @Override
            public boolean isEncrypted() {
                return true;
            }
        };
    }

    @Test
    public void testAkkaInitialization() {
        Assert.assertNotNull(akkaService.getActorSystem());
    }

    @Test
    public void testDecodeSighnedException() throws Exception {
        SessionInitRequest message = Mockito.mock(SessionInitRequest.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        Mockito.when(message.getChannelContext()).thenReturn(Mockito.mock(ChannelHandlerContext.class));
        Mockito.when(message.getErrorBuilder()).thenReturn(errorBuilder);
        Mockito.when(message.getEncodedRequestData()).thenReturn("dummy".getBytes());
        Mockito.when(message.getEncodedSessionKey()).thenReturn("dummy".getBytes());
        Mockito.when(message.getSessionKeySignature()).thenReturn("dummy".getBytes());
        Mockito.when(message.isEncrypted()).thenReturn(true);
        akkaService.process(message);
        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).build(Mockito.any(Exception.class));
    }

    @Test
    public void testDecodeSessionException() throws Exception {
        SessionAwareRequest message = Mockito.mock(SessionAwareRequest.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        NettySessionInfo sessionInfo = new NettySessionInfo(UUID.randomUUID(), Mockito.mock(ChannelHandlerContext.class), ChannelType.TCP,
                Mockito.mock(CipherPair.class), EndpointObjectHash.fromSHA1("test"), "applicationToken", 100, true);
        Mockito.when(message.getChannelContext()).thenReturn(Mockito.mock(ChannelHandlerContext.class));
        Mockito.when(message.getErrorBuilder()).thenReturn(errorBuilder);
        Mockito.when(message.getSessionInfo()).thenReturn(sessionInfo);
        Mockito.when(message.getEncodedRequestData()).thenReturn("dummy".getBytes());
        Mockito.when(message.isEncrypted()).thenReturn(true);
        akkaService.process(message);
        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).build(Mockito.any(Exception.class));
    }

    @Test
    public void testEndpointRegistrationRequest() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        ProfileSyncRequest profileSync = new ProfileSyncRequest();
        profileSync.setEndpointPublicKey(clientPublicKey);
        profileSync.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        profileSync.setVersionInfo(new EndpointVersionInfo(1, 2, 3, 4, null, 5));
        request.setProfileSyncRequest(profileSync);

        Mockito.when(operationsService.sync(request, null)).thenReturn(simpleResponse);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP, channelContextMock, request, responseBuilder,
                errorBuilder);

        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).sync(request, null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testEndpointUpdateRequest() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        ProfileSyncRequest profileSync = new ProfileSyncRequest();
        profileSync.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        profileSync.setVersionInfo(new EndpointVersionInfo(1, 2, 3, 4, null, 5));
        request.setProfileSyncRequest(profileSync);

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()))).thenReturn(
                clientPair.getPublic());
        Mockito.when(operationsService.sync(request, null)).thenReturn(simpleResponse);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request, null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testSyncRequest() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        SyncResponseHolder holder = simpleResponse;

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()))).thenReturn(
                clientPair.getPublic());
        Mockito.when(operationsService.sync(request, null)).thenReturn(holder);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request, null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testMultipleSyncRequest() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        SyncResponseHolder holder = simpleResponse;

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()))).thenReturn(
                clientPair.getPublic());
        Mockito.when(operationsService.sync(request, null)).thenReturn(holder);

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message1 = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP, channelContextMock, request, responseBuilder,
                errorBuilder);
        SessionInitRequest message2 = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP, channelContextMock, request, responseBuilder,
                errorBuilder);
        akkaService.process(message1);
        akkaService.process(message2);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeast(2)).sync(request, null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeast(2)).build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testLongSyncRequest() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(1000l);
        request.setSyncRequestMetaData(md);

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()))).thenReturn(
                clientPair.getPublic());
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class), Mockito.any(EndpointProfileDto.class))).thenReturn(
                noDeltaResponse);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(request, null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testLongSyncNotification() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        ConfigurationSyncRequest csRequest = new ConfigurationSyncRequest();
        request.setConfigurationSyncRequest(csRequest);

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()))).thenReturn(
                clientPair.getPublic());
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class), Mockito.any(EndpointProfileDto.class))).thenReturn(
                noDeltaResponse);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request, null);

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class), Mockito.any(EndpointProfileDto.class))).thenReturn(
                deltaResponse);

        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        akkaService.onNotification(thriftNotification);

        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testLongSyncUnicastNotification() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class), Mockito.any(EndpointProfileDto.class))).thenReturn(
                noDeltaResponse);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request, null);

        Mockito.when(
                operationsService.updateSyncResponse(noDeltaResponse.getResponse(), new ArrayList<NotificationDto>(),
                        UNICAST_NOTIFICATION_ID)).thenReturn(noDeltaResponse.getResponse());
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);

        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setUnicastNotificationId(UNICAST_NOTIFICATION_ID);
        thriftNotification.setKeyHash(clientPublicKeyHash.array());
        akkaService.onNotification(thriftNotification);

        Mockito.verify(operationsService, Mockito.timeout(10 * TIMEOUT / 2).atLeastOnce()).updateSyncResponse(
                noDeltaResponse.getResponse(), new ArrayList<NotificationDto>(), UNICAST_NOTIFICATION_ID);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));

    }

    @Test
    public void testLongSyncTopicNotificationOnStart() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setTopicId(TOPIC_ID);
        thriftNotification.setNotificationId(UNICAST_NOTIFICATION_ID);
        thriftNotification.setKeyHash(clientPublicKeyHash);
        akkaService.onNotification(thriftNotification);

        Mockito.when(notificationDeltaService.findNotificationById(UNICAST_NOTIFICATION_ID)).thenReturn(topicNotification);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        Mockito.when(operationsService.sync(request, null)).thenReturn(noDeltaResponseWithTopicState);
        Mockito.when(
                operationsService.updateSyncResponse(noDeltaResponseWithTopicState.getResponse(),
                        Collections.singletonList(topicNotification), null)).thenReturn(noDeltaResponseWithTopicState.getResponse());

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request, null);
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).updateSyncResponse(
                noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testLongSyncTopicNotification() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Mockito.when(notificationDeltaService.findNotificationById(UNICAST_NOTIFICATION_ID)).thenReturn(topicNotification);
        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class), Mockito.any(EndpointProfileDto.class))).thenReturn(
                noDeltaResponseWithTopicState);
        Mockito.when(
                operationsService.updateSyncResponse(noDeltaResponseWithTopicState.getResponse(),
                        Collections.singletonList(topicNotification), null)).thenReturn(noDeltaResponseWithTopicState.getResponse());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Thread.sleep(3000);

        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setTopicId(TOPIC_ID);
        thriftNotification.setNotificationId(UNICAST_NOTIFICATION_ID);
        thriftNotification.setKeyHash(clientPublicKeyHash);
        akkaService.onNotification(thriftNotification);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request, null);
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).updateSyncResponse(
                noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testRedirect() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS", 123, 1.0, 60000));

        Thread.sleep(1000);

        SyncRequest request = new SyncRequest();
        request.setRequestId(32);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder, crypt);
        akkaService.process(message);

        SyncResponse response = new SyncResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(new RedirectSyncResponse("testDNS"));

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).never()).sync(request);

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] encodedData = crypt.encodeData(responseConverter.toByteArray(response));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRedirectSessionRequest() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS", 123, 1.0, 60000));

        Thread.sleep(1000);

        SyncRequest request = new SyncRequest();
        request.setRequestId(32);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        AvroByteArrayConverter<SyncRequest> requestConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest kaaSync = new org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest(
                crypt.encodeData(requestConverter.toByteArray(request)), false, true);
        NettySessionInfo session = new NettySessionInfo(UUID.randomUUID(), channelContextMock, ChannelType.TCP,
                crypt.getSessionCipherPair(), EndpointObjectHash.fromBytes(clientPublicKey.array()), APP_TOKEN, 100, true);

        SessionAwareRequest message = new NettyTcpSyncMessage(kaaSync, session, responseBuilder, errorBuilder, null);
        akkaService.process(message);

        SyncResponse response = new SyncResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(new RedirectSyncResponse("testDNS"));

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).never()).sync(request);

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] encodedData = crypt.encodeData(responseConverter.toByteArray(response));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRedirectExpire() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS", 123, 1.0, 1000));

        Thread.sleep(2000);

        SyncRequest request = new SyncRequest();
        request.setRequestId(32);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(2l * TIMEOUT);
        request.setSyncRequestMetaData(md);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder, crypt);

        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class), Mockito.any(EndpointProfileDto.class))).thenReturn(
                noDeltaResponseWithTopicState);

        akkaService.process(message);

        SyncResponse response = new SyncResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(new RedirectSyncResponse("testDNS"));

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).sync(request, null);
    }

    @Test
    public void testEndpointEventBasic() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);
        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), Base64Util.encode(clientPublicKeyHash.array()), null);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        sourceRequest.setSyncRequestMetaData(md);

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEvents(Arrays.asList(event));
        sourceRequest.setEventSyncRequest(eventRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        SyncRequest targetRequest = new SyncRequest();
        md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(targetPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(operationsService.sync(sourceRequest, null)).thenReturn(sourceResponseHolder);
        when(operationsService.sync(targetRequest, null)).thenReturn(targetResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder sourceCrypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(),
                serverPair.getPublic());
        SessionInitRequest sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, sourceRequest,
                responseBuilder, errorBuilder, sourceCrypt);
        akkaService.process(sourceMessage);

        // sourceRequest
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(Mockito.any(SyncRequest.class),
                Mockito.any(EndpointProfileDto.class));

        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());
        SessionInitRequest targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, targetRequest,
                responseBuilder, errorBuilder, targetCrypt);
        akkaService.process(targetMessage);

        // targetRequest
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(Mockito.any(SyncRequest.class),
                Mockito.any(EndpointProfileDto.class));

        SyncResponse eventResponse = new SyncResponse();
        eventResponse.setStatus(SyncResponseResultType.SUCCESS);
        eventResponse.setEventSyncResponse(new EventSyncResponse());
        eventResponse.getEventSyncResponse().setEvents(Arrays.asList(event));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(eventResponse);
        byte[] encodedData = targetCrypt.encodeData(response);

        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testEndpointEventSeqNumberBasic() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        sourceRequest.setSyncRequestMetaData(md);

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEventSequenceNumberRequest(new EventSequenceNumberRequest());
        sourceRequest.setEventSyncRequest(eventRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        when(operationsService.sync(sourceRequest, null)).thenReturn(sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder sourceCrypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(),
                serverPair.getPublic());
        SessionInitRequest sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, sourceRequest,
                responseBuilder, errorBuilder, sourceCrypt);
        akkaService.process(sourceMessage);

        // sourceRequest
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(Mockito.any(SyncRequest.class),
                Mockito.any(EndpointProfileDto.class));

        SyncResponse eventResponse = new SyncResponse();
        eventResponse.setStatus(SyncResponseResultType.SUCCESS);
        eventResponse.setEventSyncResponse(new EventSyncResponse());
        eventResponse.getEventSyncResponse().setEventSequenceNumberResponse(new EventSequenceNumberResponse(0));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(eventResponse);
        byte[] encodedData = sourceCrypt.encodeData(response);

        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRemoteIncomingEndpointEventBasic() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(targetPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();

        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(operationsService.sync(targetRequest, null)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());
        SessionInitRequest targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, targetRequest,
                responseBuilder, errorBuilder, targetCrypt);
        akkaService.process(targetMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest, null);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), null, null);
        EndpointEvent endpointEvent = new EndpointEvent(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()), event,
                UUID.randomUUID(), System.currentTimeMillis(), ECF1_VERSION);
        RemoteEndpointEvent remoteEvent = new RemoteEndpointEvent(TENANT_ID, USER_ID, endpointEvent, new RouteTableAddress(
                EndpointObjectHash.fromBytes(targetPublicKeyHash.array()), APP_TOKEN, "SERVER1"));
        akkaService.getListener().onEvent(remoteEvent);

        event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), null, Base64Util.encode(targetPublicKeyHash.array()));

        SyncResponse eventResponse = new SyncResponse();
        eventResponse.setStatus(SyncResponseResultType.SUCCESS);
        eventResponse.setEventSyncResponse(new EventSyncResponse());
        eventResponse.getEventSyncResponse().setEvents(Arrays.asList(event));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(eventResponse);
        byte[] encodedData = targetCrypt.encodeData(response);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRemoteOutcomingEndpointEventBasic() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), Base64Util.encode(clientPublicKeyHash.array()), null);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        sourceRequest.setSyncRequestMetaData(md);

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEvents(Arrays.asList(event));
        sourceRequest.setEventSyncRequest(eventRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        when(operationsService.sync(sourceRequest, null)).thenReturn(sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder sourceCrypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(),
                serverPair.getPublic());
        SessionInitRequest sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, sourceRequest,
                responseBuilder, errorBuilder, sourceCrypt);

        akkaService.process(sourceMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(Mockito.any(SyncRequest.class),
                Mockito.any(EndpointProfileDto.class));

        UserRouteInfo userRouteInfo = new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.ADD);
        akkaService.getListener().onUserRouteInfo(userRouteInfo);

        RouteTableAddress remoteAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(targetPublicKeyHash.array()), APP_TOKEN,
                SERVER2);
        RouteInfo routeInfo = new RouteInfo(TENANT_ID, USER_ID, remoteAddress, Arrays.asList(new EventClassFamilyVersion(ECF1_ID,
                ECF1_VERSION)));
        TimeUnit.SECONDS.sleep(2);
        akkaService.getListener().onRouteInfo(routeInfo);

        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendEvent(Mockito.any(RemoteEndpointEvent.class));
    }

    @Test
    public void testLogSyncRequest() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);

        SyncRequest request = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setTimeout(1000l);
        request.setSyncRequestMetaData(md);

        LogSyncRequest logRequest = new LogSyncRequest("logUploadRequest1", Collections.singletonList(new LogEntry(ByteBuffer.wrap("String"
                .getBytes()))));
        request.setLogSyncRequest(logRequest);

        Mockito.when(operationsService.sync(Mockito.any(SyncRequest.class), Mockito.any(EndpointProfileDto.class))).thenReturn(
                noDeltaResponseWithTopicState);
        LogAppender mockAppender = Mockito.mock(LogAppender.class);
        Mockito.when(logAppenderService.getApplicationAppenders(APP_ID)).thenReturn(Collections.singletonList(mockAppender));
        Mockito.when(logAppenderService.getLogSchema(Mockito.anyString(), Mockito.anyInt())).thenReturn(Mockito.mock(LogSchema.class));
        Mockito.when(mockAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(true);

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, request, responseBuilder,
                errorBuilder);

        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(Mockito.any(SyncRequest.class),
                Mockito.any(EndpointProfileDto.class));
        Mockito.verify(logAppenderService, Mockito.timeout(TIMEOUT).atLeastOnce()).getLogSchema(APP_ID, 44);
        Mockito.verify(mockAppender, Mockito.timeout(TIMEOUT).atLeastOnce()).doAppend(Mockito.any(LogEventPack.class),
                Mockito.any(LogDeliveryCallback.class));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce())
                .build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testUserChange() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(targetPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(operationsService.sync(targetRequest, null)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest message = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, targetRequest,
                responseBuilder, errorBuilder, targetCrypt);

        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest, null);
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        UserRouteInfo userRouteInfo = new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.ADD);
        akkaService.getListener().onUserRouteInfo(userRouteInfo);
        TimeUnit.SECONDS.sleep(2);

        RouteTableAddress remoteAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()), APP_TOKEN,
                SERVER2);
        RouteInfo remoteRouteInfo = new RouteInfo(TENANT_ID, USER_ID, remoteAddress, Arrays.asList(new EventClassFamilyVersion(ECF1_ID,
                ECF1_VERSION)));
        TimeUnit.SECONDS.sleep(2);
        akkaService.getListener().onRouteInfo(remoteRouteInfo);

        RouteTableAddress localAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(targetPublicKeyHash.array()), APP_TOKEN, null);
        RouteInfo localRouteInfo = new RouteInfo(TENANT_ID, USER_ID, localAddress, Arrays.asList(new EventClassFamilyVersion(ECF1_ID,
                ECF1_VERSION)));
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendRouteInfo(Collections.singletonList(localRouteInfo),
                SERVER2);

        targetRequest = new SyncRequest();
        md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(targetPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        targetRequest.setSyncRequestMetaData(md);

        targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID + "2");

        when(operationsService.sync(targetRequest, targetProfileMock)).thenReturn(targetResponseHolder);

        SessionInitRequest targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, targetRequest,
                responseBuilder, errorBuilder, targetCrypt);
        akkaService.process(targetMessage);
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID + "2"));
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendRouteInfo(
                RouteInfo.deleteRouteFromAddress(TENANT_ID, USER_ID, localAddress), SERVER2);
    }

    @Test
    public void testEndpointAttach() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(targetPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(operationsService.sync(targetRequest, null)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, targetRequest,
                responseBuilder, errorBuilder, targetCrypt);

        akkaService.process(targetMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest, null);
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData sourceMd = new SyncRequestMetaData();
        sourceMd.setApplicationToken(APP_TOKEN);
        sourceMd.setEndpointPublicKeyHash(clientPublicKeyHash);
        sourceMd.setTimeout(TIMEOUT * 1L);
        sourceRequest.setSyncRequestMetaData(sourceMd);

        sourceRequest.setEventSyncRequest(new EventSyncRequest());

        UserSyncRequest userSyncRequest = new UserSyncRequest();
        EndpointAttachRequest eaRequest = new EndpointAttachRequest("request1", "token");
        userSyncRequest.setEndpointAttachRequests(Collections.singletonList(eaRequest));
        sourceRequest.setUserSyncRequest(userSyncRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        UserSyncResponse userSyncResponse = new UserSyncResponse();
        userSyncResponse.setEndpointAttachResponses(Collections.singletonList(new EndpointAttachResponse("request1", Base64Util
                .encode(targetPublicKeyHash.array()), SyncResponseResultType.SUCCESS)));
        sourceResponse.setUserSyncResponse(userSyncResponse);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        when(operationsService.sync(sourceRequest, null)).thenReturn(sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        ResponseBuilder sourceResponseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder sourceErrorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, sourceRequest,
                sourceResponseBuilder, sourceErrorBuilder);
        akkaService.process(sourceMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(sourceRequest, null);
        SyncResponse targetSyncResponse = new SyncResponse();
        targetSyncResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetSyncResponse.setUserSyncResponse(new UserSyncResponse());
        targetSyncResponse.getUserSyncResponse().setUserAttachNotification(
                new UserAttachNotification(USER_ID, Base64Util.encode(clientPublicKeyHash.array())));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(targetSyncResponse);
        byte[] encodedData = targetCrypt.encodeData(response);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testEndpointDetach() throws Exception {
        ChannelHandlerContext channelContextMock = Mockito.mock(ChannelHandlerContext.class);
        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken(APP_TOKEN);
        md.setEndpointPublicKeyHash(targetPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        SyncResponse targetResponse = new SyncResponse();
        targetResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        targetResponse.setUserSyncResponse(new UserSyncResponse());
        SyncResponseHolder targetResponseHolder = new SyncResponseHolder(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(operationsService.sync(targetRequest, null)).thenReturn(targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        ResponseBuilder targetResponseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder targetErrorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, targetRequest,
                targetResponseBuilder, targetErrorBuilder, targetCrypt);
        akkaService.process(targetMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(targetRequest, null);
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        SyncRequest sourceRequest = new SyncRequest();
        SyncRequestMetaData sourceMd = new SyncRequestMetaData();
        sourceMd.setApplicationToken(APP_TOKEN);
        sourceMd.setEndpointPublicKeyHash(clientPublicKeyHash);
        sourceMd.setTimeout(TIMEOUT * 1L);
        sourceRequest.setSyncRequestMetaData(sourceMd);

        sourceRequest.setEventSyncRequest(new EventSyncRequest());

        UserSyncRequest userSyncRequest = new UserSyncRequest();
        EndpointDetachRequest eaRequest = new EndpointDetachRequest("request1", Base64Util.encode(targetPublicKeyHash.array()));
        userSyncRequest.setEndpointDetachRequests(Collections.singletonList(eaRequest));
        sourceRequest.setUserSyncRequest(userSyncRequest);

        SyncResponse sourceResponse = new SyncResponse();
        sourceResponse.setStatus(SyncResponseResultType.SUCCESS);
        UserSyncResponse userSyncResponse = new UserSyncResponse();
        userSyncResponse.setEndpointDetachResponses(Collections.singletonList(new EndpointDetachResponse("request1",
                SyncResponseResultType.SUCCESS)));
        sourceResponse.setUserSyncResponse(userSyncResponse);
        SyncResponseHolder sourceResponseHolder = new SyncResponseHolder(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        when(operationsService.sync(sourceRequest, null)).thenReturn(sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        ResponseBuilder sourceResponseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder sourceErrorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitRequest sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.HTTP_LP, channelContextMock, sourceRequest,
                sourceResponseBuilder, sourceErrorBuilder);
        akkaService.process(sourceMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).sync(sourceRequest, null);
        SyncResponse targetSyncResponse = new SyncResponse();
        targetSyncResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetSyncResponse.setUserSyncResponse(new UserSyncResponse());
        targetSyncResponse.getUserSyncResponse().setUserDetachNotification(
                new UserDetachNotification(Base64Util.encode(clientPublicKeyHash.array())));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(targetSyncResponse);
        byte[] encodedData = targetCrypt.encodeData(response);
        Mockito.verify(targetResponseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

}
