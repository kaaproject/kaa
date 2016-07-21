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

package org.kaaproject.kaa.server.operations.service.akka;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointAttachRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointDetachRequest;
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
import org.kaaproject.kaa.common.endpoint.gen.UserAttachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserDetachNotification;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncResponse;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder.CipherPair;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;
import org.kaaproject.kaa.server.common.dao.exception.EndpointRegistrationServiceException;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftEndpointDeregistrationMessage;
import org.kaaproject.kaa.server.common.thrift.gen.operations.ThriftUnicastNotificationMessage;
import org.kaaproject.kaa.server.node.service.credentials.CredentialsService;
import org.kaaproject.kaa.server.node.service.credentials.CredentialsServiceLocator;
import org.kaaproject.kaa.server.node.service.registration.RegistrationService;
import org.kaaproject.kaa.server.operations.pojo.SyncContext;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ActorClassifier;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointAddress;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.RouteOperation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ThriftEndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.cluster.ClusterService;
import org.kaaproject.kaa.server.operations.service.event.EndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent;
import org.kaaproject.kaa.server.operations.service.event.RouteInfo;
import org.kaaproject.kaa.server.operations.service.event.RouteTableAddress;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.kaaproject.kaa.server.operations.service.event.UserRouteInfo;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.metrics.MeterClient;
import org.kaaproject.kaa.server.operations.service.metrics.MetricsService;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;
import org.kaaproject.kaa.server.operations.service.user.EndpointUserService;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.ConfigurationServerSync;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.EventServerSync;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.UserClientSync;
import org.kaaproject.kaa.server.sync.UserServerSync;
import org.kaaproject.kaa.server.sync.platform.AvroEncDec;
import org.kaaproject.kaa.server.transport.EndpointRevocationException;
import org.kaaproject.kaa.server.transport.EndpointVerificationException;
import org.kaaproject.kaa.server.transport.InvalidSDKTokenException;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.SessionAwareMessage;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultAkkaServiceTest {

    private static final String LOCAL_NODE_ID = "LOCAL_NODE_ID";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAkkaServiceTest.class);

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
    private static final String SDK_TOKEN = "SDK_TOKEN";
    private static final String INVALID_SDK_TOKEN = "INVALID_SDK_TOKEN";
    private static final String APP_ID = "APP_ID";
    private static final String PROFILE_BODY = "ProfileBody";

    private static final int REQUEST_ID = 42;

    private DefaultAkkaService akkaService;
    private AkkaClusterServiceListener clusterServiceListener;

    // mocks
    private ClusterService clusterService;
    private CacheService cacheService;
    private MetricsService metricsService;
    private KeyStoreService operationsKeyStoreService;
    private OperationsService operationsService;
    private NotificationDeltaService notificationDeltaService;
    private ApplicationService applicationService;
    private EventService eventService;
    private ApplicationDto applicationDto;

    private SyncContext simpleResponse;
    private SyncContext noDeltaResponse;
    private SyncContext deltaResponse;
    private SyncContext deltaResponseWithProfile;
    private SyncContext noDeltaResponseWithTopicState;
    private NotificationDto topicNotification;
    private LogAppenderService logAppenderService;
    private EndpointUserService endpointUserService;
    private CTLService ctlService;
    private CredentialsServiceLocator credentialsServiceLocator;
    private CredentialsService credentialsService;
    private RegistrationService registrationService;

    private KeyPair clientPair;
    private KeyPair targetPair;
    private KeyPair serverPair;

    private ByteBuffer clientPublicKey;
    private ByteBuffer clientPublicKeyHash;

    private ByteBuffer targetPublicKeyHash;

    private EndpointProfileDto mockProfile;

    @Before
    public void before() throws GeneralSecurityException, CredentialsServiceException, EndpointRegistrationServiceException {
        akkaService = new DefaultAkkaService();

        AkkaContext context = new AkkaContext();

        clusterService = mock(ClusterService.class);
        cacheService = mock(CacheService.class);
        metricsService = mock(MetricsService.class);
        operationsKeyStoreService = mock(KeyStoreService.class);
        operationsService = mock(OperationsService.class);
        notificationDeltaService = mock(NotificationDeltaService.class);
        applicationService = mock(ApplicationService.class);
        eventService = mock(EventService.class);
        logAppenderService = mock(LogAppenderService.class);
        endpointUserService = mock(EndpointUserService.class);
        ctlService = mock(CTLService.class);
        credentialsServiceLocator = mock(CredentialsServiceLocator.class);
        credentialsService = mock(CredentialsService.class);
        registrationService = mock(RegistrationService.class);

        ReflectionTestUtils.setField(context, "clusterService", clusterService);
        ReflectionTestUtils.setField(context, "cacheService", cacheService);
        ReflectionTestUtils.setField(context, "metricsService", metricsService);
        ReflectionTestUtils.setField(context, "operationsKeyStoreService", operationsKeyStoreService);
        ReflectionTestUtils.setField(context, "operationsService", operationsService);
        ReflectionTestUtils.setField(context, "notificationDeltaService", notificationDeltaService);
        ReflectionTestUtils.setField(context, "applicationService", applicationService);
        ReflectionTestUtils.setField(context, "eventService", eventService);
        ReflectionTestUtils.setField(context, "logAppenderService", logAppenderService);
        ReflectionTestUtils.setField(context, "endpointUserService", endpointUserService);
        ReflectionTestUtils.setField(context, "ctlService", ctlService);
        ReflectionTestUtils.setField(context, "credentialsServiceLocator", credentialsServiceLocator);
        ReflectionTestUtils.setField(context, "registrationService", registrationService);

        clientPair = KeyUtil.generateKeyPair();
        targetPair = KeyUtil.generateKeyPair();
        serverPair = KeyUtil.generateKeyPair();

        Mockito.when(operationsKeyStoreService.getPublicKey()).thenReturn(serverPair.getPublic());
        Mockito.when(operationsKeyStoreService.getPrivateKey()).thenReturn(serverPair.getPrivate());
        Mockito.when(metricsService.createMeter(Mockito.anyString(), Mockito.anyString())).thenReturn(Mockito.mock(MeterClient.class));
        
        Mockito.when(credentialsServiceLocator.getCredentialsService(Mockito.anyString())).thenReturn(credentialsService);
        Mockito.when(credentialsService.lookupCredentials(Mockito.anyString())).thenReturn(Optional.ofNullable((CredentialsDto)null));
        
        registerPublicKey(clientPair.getPublic());
        registerPublicKey(targetPair.getPublic());

        ReflectionTestUtils.setField(akkaService, "context", context);

        if (akkaService.getActorSystem() == null) {
            akkaService.initActorSystem();
        }

        clusterServiceListener = (AkkaClusterServiceListener) ReflectionTestUtils.getField(akkaService, "clusterListener");

        clientPublicKey = ByteBuffer.wrap(clientPair.getPublic().getEncoded());
        clientPublicKeyHash = ByteBuffer.wrap(SHA1HashUtils.hashToBytes(clientPair.getPublic().getEncoded()));

        targetPublicKeyHash = ByteBuffer.wrap(SHA1HashUtils.hashToBytes(targetPair.getPublic().getEncoded()));

        Mockito.when(cacheService.getTenantIdByAppToken(APP_TOKEN)).thenReturn(TENANT_ID);
        Mockito.when(cacheService.getAppTokenBySdkToken(SDK_TOKEN)).thenReturn(APP_TOKEN);
        Mockito.when(cacheService.getAppTokenBySdkToken(INVALID_SDK_TOKEN)).thenReturn(null);
        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(targetPublicKeyHash.array())))
                .thenReturn(targetPair.getPublic());

        applicationDto = new ApplicationDto();
        applicationDto.setId(APP_ID);
        applicationDto.setApplicationToken(APP_TOKEN);
        applicationDto.setTenantId(TENANT_ID);

        ServerSync response = new ServerSync();
        response.setRequestId(REQUEST_ID);
        response.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        ConfigurationServerSync confSyncResponse = new ConfigurationServerSync();
        confSyncResponse.setResponseStatus(org.kaaproject.kaa.server.sync.SyncResponseStatus.NO_DELTA);
        response.setConfigurationSync(confSyncResponse);
        noDeltaResponse = new SyncContext(response);

        Map<String, Integer> subscriptionStates = new HashMap<>();
        subscriptionStates.put(TOPIC_ID, new Integer(0));
        noDeltaResponseWithTopicState = new SyncContext(response);
        noDeltaResponseWithTopicState.setSubscriptionStates(subscriptionStates);
        EndpointProfileDto epDto = new EndpointProfileDto();
        epDto.setSystemNfVersion(42);
        epDto.setUserNfVersion(43);
        epDto.setLogSchemaVersion(44);
        noDeltaResponseWithTopicState.setEndpointProfile(epDto);

        response = new ServerSync();
        response.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        confSyncResponse = new ConfigurationServerSync();
        confSyncResponse.setResponseStatus(org.kaaproject.kaa.server.sync.SyncResponseStatus.DELTA);
        response.setConfigurationSync(confSyncResponse);
        deltaResponse = new SyncContext(response);

        response = new ServerSync();
        response.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        confSyncResponse = new ConfigurationServerSync();
        confSyncResponse.setResponseStatus(org.kaaproject.kaa.server.sync.SyncResponseStatus.DELTA);
        response.setConfigurationSync(confSyncResponse);
        deltaResponseWithProfile = new SyncContext(response);
        mockProfile = mock(EndpointProfileDto.class);
        deltaResponseWithProfile.setEndpointProfile(mockProfile);

        response = new ServerSync();
        response.setRequestId(REQUEST_ID);
        response.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        simpleResponse = new SyncContext(response);

        topicNotification = new NotificationDto();
        topicNotification.setApplicationId(APP_ID);
        topicNotification.setTopicId(TOPIC_ID);
        topicNotification.setId(UNICAST_NOTIFICATION_ID);
        topicNotification.setExpiredAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
        topicNotification.setSecNum(1);
        topicNotification.setNfVersion(42);
        topicNotification.setType(NotificationTypeDto.SYSTEM);
        topicNotification.setBody("I am a dummy notification".getBytes());

        when(applicationService.findAppByApplicationToken(APP_TOKEN)).thenReturn(applicationDto);
        when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);

        when(endpointUserService.findUserVerifiers(APP_ID)).thenReturn(new ArrayList<UserVerifierDto>());

        when(eventService.isMainUserNode(Mockito.anyString())).thenReturn(true);

        when(clusterService.getNodeId()).thenReturn(LOCAL_NODE_ID);
        when(clusterService.getEntityNode(Mockito.any(byte[].class))).thenReturn(LOCAL_NODE_ID);
        when(clusterService.getEntityNode(Mockito.any(EndpointObjectHash.class))).thenReturn(LOCAL_NODE_ID);
    }

    private void registerPublicKey(PublicKey publicKey) throws EndpointRegistrationServiceException {
        byte[] clientPublicKeyBytes = publicKey.getEncoded();
        String endpointId = Base64Util.encode(EndpointObjectHash.fromSHA1(clientPublicKeyBytes).getData());
        EndpointRegistrationDto endpointRegistration = new EndpointRegistrationDto(APP_ID, endpointId, endpointId, null, null);
        Mockito.when(registrationService.findEndpointRegistrationByCredentialsId(endpointId)).thenReturn(Optional.of(endpointRegistration));
    }

    @After
    public void after() {
        akkaService.getActorSystem().shutdown();
        akkaService.getActorSystem().awaitTermination();
    }

    private SessionInitMessage toSignedRequest(final UUID uuid, final ChannelType channelType, final ChannelContext ctx,
            SyncRequest request, final MessageBuilder responseBuilder, final ErrorBuilder errorBuilder) throws Exception {
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        return toSignedRequest(uuid, channelType, ctx, request, responseBuilder, errorBuilder, crypt);
    }

    private SessionInitMessage toSignedRequest(final UUID uuid, final ChannelType channelType, final ChannelContext ctx,
            SyncRequest request, final MessageBuilder responseBuilder, final ErrorBuilder errorBuilder, MessageEncoderDecoder crypt)
                    throws Exception {
        AvroByteArrayConverter<SyncRequest> requestConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        byte[] data = requestConverter.toByteArray(request);

        final byte[] encodedData = crypt.encodeData(data);
        final byte[] encodedSessionKey = crypt.getEncodedSessionKey();
        final byte[] sessionKeySignature = crypt.sign(encodedSessionKey);

        return new SessionInitMessage() {
            @Override
            public UUID getChannelUuid() {
                return uuid;
            }

            @Override
            public ChannelType getChannelType() {
                return channelType;
            }

            @Override
            public ChannelContext getChannelContext() {
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
            public byte[] getEncodedMessageData() {
                return encodedData;
            }

            @Override
            public MessageBuilder getMessageBuilder() {
                return responseBuilder;
            }

            @Override
            public ErrorBuilder getErrorBuilder() {
                return errorBuilder;
            }

            @Override
            public void onSessionCreated(SessionInfo session) {
            }

            @Override
            public int getKeepAlive() {
                return 100;
            }

            @Override
            public boolean isEncrypted() {
                return true;
            }

            @Override
            public int getPlatformId() {
                return Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID;
            }
        };
    }

    @Test
    public void testAkkaInitialization() {
        Assert.assertNotNull(akkaService.getActorSystem());
    }

    @Test
    public void testDecodeSighnedException() throws Exception {
        SessionInitMessage message = Mockito.mock(SessionInitMessage.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        Mockito.when(message.getChannelContext()).thenReturn(Mockito.mock(ChannelContext.class));
        Mockito.when(message.getErrorBuilder()).thenReturn(errorBuilder);
        Mockito.when(message.getEncodedMessageData()).thenReturn("dummy".getBytes());
        Mockito.when(message.getEncodedSessionKey()).thenReturn("dummy".getBytes());
        Mockito.when(message.getSessionKeySignature()).thenReturn("dummy".getBytes());
        Mockito.when(message.isEncrypted()).thenReturn(true);
        akkaService.process(message);
        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).build(Mockito.any(Exception.class));
    }

    @Test
    public void testDecodeSessionException() throws Exception {
        SessionAwareMessage message = Mockito.mock(SessionAwareMessage.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInfo sessionInfo = new SessionInfo(UUID.randomUUID(), Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID,
                Mockito.mock(ChannelContext.class), ChannelType.ASYNC, Mockito.mock(CipherPair.class), EndpointObjectHash.fromSHA1("test"),
                "applicationToken", "sdkToken", 100, true);
        Mockito.when(message.getChannelContext()).thenReturn(Mockito.mock(ChannelContext.class));
        Mockito.when(message.getErrorBuilder()).thenReturn(errorBuilder);
        Mockito.when(message.getSessionInfo()).thenReturn(sessionInfo);
        Mockito.when(message.getEncodedMessageData()).thenReturn("dummy".getBytes());
        Mockito.when(message.isEncrypted()).thenReturn(true);
        akkaService.process(message);
        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).build(Mockito.any(Exception.class));
    }

    @Test
    public void testInvalidSDKTokenException() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(INVALID_SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        ProfileSyncRequest profileSync = new ProfileSyncRequest();
        profileSync.setEndpointPublicKey(clientPublicKey);
        profileSync.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        request.setProfileSyncRequest(profileSync);

        whenSync(simpleResponse);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);

        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);
        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.isA(InvalidSDKTokenException.class));
    }

    @Test
    public void testEndpointRegistrationRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        ProfileSyncRequest profileSync = new ProfileSyncRequest();
        profileSync.setEndpointPublicKey(clientPublicKey);
        profileSync.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        request.setProfileSyncRequest(profileSync);

        whenSync(simpleResponse);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);

        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    @Test
    public void testEndpointUpdateRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        ProfileSyncRequest profileSync = new ProfileSyncRequest();
        profileSync.setProfileBody(ByteBuffer.wrap(PROFILE_BODY.getBytes()));
        request.setProfileSyncRequest(profileSync);

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(simpleResponse);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    @Test
    public void testSyncRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        SyncContext holder = simpleResponse;

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(holder);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    @Test
    public void testMultipleSyncRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        SyncContext holder = simpleResponse;

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(holder);

        Assert.assertNotNull(akkaService.getActorSystem());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message1 = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);
        SessionInitMessage message2 = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);
        akkaService.process(message1);
        akkaService.process(message2);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT * 10).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeast(2)).build(Mockito.any(byte[].class), Mockito.any(boolean.class));
    }

    @Test
    public void testLongSyncRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        md.setTimeout(1000l);
        request.setSyncRequestMetaData(md);

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(noDeltaResponse);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    private void whenSync(SyncContext response) throws GetDeltaException {
        Mockito.when(operationsService.syncClientProfile(Mockito.any(SyncContext.class), Mockito.any(ProfileClientSync.class)))
                .thenReturn(response);
        Mockito.when(
                operationsService.processEndpointAttachDetachRequests(Mockito.any(SyncContext.class), Mockito.any(UserClientSync.class)))
                .thenReturn(response);
        Mockito.when(operationsService.processEventListenerRequests(Mockito.any(SyncContext.class), Mockito.any(EventClientSync.class)))
                .thenReturn(response);
        Mockito.when(operationsService.syncConfiguration(Mockito.any(SyncContext.class), Mockito.any(ConfigurationClientSync.class)))
                .thenReturn(response);
        Mockito.when(operationsService.syncNotification(Mockito.any(SyncContext.class), Mockito.any(NotificationClientSync.class)))
                .thenReturn(response);
        Mockito.when(operationsService.syncUseConfigurationRawSchema(Mockito.any(SyncContext.class), Matchers.anyBoolean()))
                .thenReturn(response);
    }

    private void whenSync(ClientSync request, SyncContext response) throws GetDeltaException {
        SyncContext context = new SyncContext(new ServerSync());
        if (request.getClientSyncMetaData() != null) {
            request.getClientSyncMetaData().setApplicationToken(APP_TOKEN);
        }
        context.setRequestHash(request.hashCode());

        Mockito.when(operationsService.syncClientProfile(context, request.getProfileSync())).thenReturn(response);
        Mockito.when(operationsService.processEndpointAttachDetachRequests(response, request.getUserSync())).thenReturn(response);
        Mockito.when(operationsService.processEventListenerRequests(response, request.getEventSync())).thenReturn(response);
        Mockito.when(operationsService.syncConfiguration(response, request.getConfigurationSync())).thenReturn(response);
        Mockito.when(operationsService.syncNotification(response, request.getNotificationSync())).thenReturn(response);

        Mockito.when(operationsService.syncUseConfigurationRawSchema(response, request.isUseConfigurationRawSchema())).thenReturn(response);
    }

    @Test
    public void testLongSyncNotification() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = buildSyncRequestMetaData();
        request.setSyncRequestMetaData(md);

        ConfigurationSyncRequest csRequest = new ConfigurationSyncRequest();
        csRequest.setConfigurationHash(ByteBuffer.wrap(new byte[] {}));
        csRequest.setResyncOnly(true);
        request.setConfigurationSyncRequest(csRequest);

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(noDeltaResponse);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        whenSync(deltaResponse);

        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        akkaService.onNotification(thriftNotification);

        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    @Test
    public void testLongSyncUnicastNotification() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        request.setSyncRequestMetaData(buildSyncRequestMetaData());

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        whenSync(noDeltaResponse);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        Mockito.when(operationsService.updateSyncResponse(noDeltaResponse.getResponse(), new ArrayList<NotificationDto>(),
                UNICAST_NOTIFICATION_ID)).thenReturn(noDeltaResponse.getResponse());
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);

        EndpointAddress address = new EndpointAddress(applicationDto.getTenantId(), applicationDto.getApplicationToken(),
                EndpointObjectHash.fromBytes(clientPublicKeyHash.array()));
        ActorClassifier classifier =  ActorClassifier.GLOBAL;
        // TODO: replace nulls with values
        ThriftUnicastNotificationMessage msg = new ThriftUnicastNotificationMessage(null, null, UNICAST_NOTIFICATION_ID);
        clusterServiceListener.onEndpointActorMsg(new ThriftEndpointActorMsg<ThriftUnicastNotificationMessage>(address, classifier, msg));

        Mockito.verify(operationsService, Mockito.timeout(10 * TIMEOUT / 2).atLeastOnce()).updateSyncResponse(noDeltaResponse.getResponse(),
                new ArrayList<NotificationDto>(), UNICAST_NOTIFICATION_ID);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));

    }

    @Test
    public void testLongSyncTopicNotificationOnStart() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setTopicId(TOPIC_ID);
        thriftNotification.setNotificationId(UNICAST_NOTIFICATION_ID);
        akkaService.onNotification(thriftNotification);

        Mockito.when(notificationDeltaService.findNotificationById(UNICAST_NOTIFICATION_ID)).thenReturn(topicNotification);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        request.setSyncRequestMetaData(buildSyncRequestMetaData());

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        whenSync(noDeltaResponseWithTopicState);
        Mockito.when(operationsService.updateSyncResponse(noDeltaResponseWithTopicState.getResponse(),
                Collections.singletonList(topicNotification), null)).thenReturn(noDeltaResponseWithTopicState.getResponse());

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce())
                .updateSyncResponse(noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    @Test
    public void testLongSyncTopicNotification() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        request.setSyncRequestMetaData(buildSyncRequestMetaData());

        NotificationSyncRequest nfRequest = new NotificationSyncRequest();
        request.setNotificationSyncRequest(nfRequest);

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        Mockito.when(notificationDeltaService.findNotificationById(UNICAST_NOTIFICATION_ID)).thenReturn(topicNotification);
        whenSync(noDeltaResponseWithTopicState);
        Mockito.when(operationsService.updateSyncResponse(noDeltaResponseWithTopicState.getResponse(),
                Collections.singletonList(topicNotification), null)).thenReturn(noDeltaResponseWithTopicState.getResponse());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Thread.sleep(3000);

        Notification thriftNotification = new Notification();
        thriftNotification.setAppId(APP_ID);
        thriftNotification.setTopicId(TOPIC_ID);
        thriftNotification.setNotificationId(UNICAST_NOTIFICATION_ID);
        akkaService.onNotification(thriftNotification);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce())
                .updateSyncResponse(noDeltaResponseWithTopicState.getResponse(), Collections.singletonList(topicNotification), null);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    @Test
    public void testRedirect() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS".hashCode(), 123, 1.0, 0.0, 60000));

        Thread.sleep(1000);

        SyncRequest request = new SyncRequest();
        request.setRequestId(32);
        request.setSyncRequestMetaData(buildSyncRequestMetaData());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder, crypt);
        akkaService.process(message);

        SyncResponse response = new SyncResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(new RedirectSyncResponse("testDNS".hashCode()));

        Thread.sleep(TIMEOUT / 2);
        Mockito.verify(operationsService, Mockito.never()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] encodedData = crypt.encodeData(responseConverter.toByteArray(response));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRedirectSessionRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS".hashCode(), 123, 0.0, 1.0, 60000));

        Thread.sleep(1000);

        SyncRequest request = new SyncRequest();
        request.setRequestId(32);
        request.setSyncRequestMetaData(buildSyncRequestMetaData());

        final MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        final ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        AvroByteArrayConverter<SyncRequest> requestConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        final org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest kaaSync = new org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest(
                crypt.encodeData(requestConverter.toByteArray(request)), false, true);
        final SessionInfo session = new SessionInfo(UUID.randomUUID(), Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, channelContextMock,
                ChannelType.ASYNC, crypt.getSessionCipherPair(), EndpointObjectHash.fromBytes(clientPublicKey.array()), APP_TOKEN,
                SDK_TOKEN, 100, true);

        SessionAwareMessage message = new SessionAwareMessage() {

            @Override
            public SessionInfo getSessionInfo() {
                return session;
            }

            @Override
            public int getPlatformId() {
                return session.getPlatformId();
            }

            @Override
            public UUID getChannelUuid() {
                return session.getUuid();
            }

            @Override
            public ChannelType getChannelType() {
                return session.getChannelType();
            }

            @Override
            public ChannelContext getChannelContext() {
                return session.getCtx();
            }

            @Override
            public boolean isEncrypted() {
                return session.isEncrypted();
            }

            @Override
            public MessageBuilder getMessageBuilder() {
                return responseBuilder;
            }

            @Override
            public ErrorBuilder getErrorBuilder() {
                return errorBuilder;
            }

            @Override
            public byte[] getEncodedMessageData() {
                return kaaSync.getAvroObject();
            }
        };
        akkaService.process(message);

        SyncResponse response = new SyncResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(new RedirectSyncResponse("testDNS".hashCode()));

        Thread.sleep(TIMEOUT / 2);
        Mockito.verify(operationsService, Mockito.never()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] encodedData = crypt.encodeData(responseConverter.toByteArray(response));
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRedirectExpire() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        akkaService.onRedirectionRule(new RedirectionRule("testDNS".hashCode(), 123, 1.0, 1.0, 1000));

        Thread.sleep(2000);

        SyncRequest request = new SyncRequest();
        request.setRequestId(32);
        request.setSyncRequestMetaData(buildSyncRequestMetaData());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder, crypt);

        whenSync(noDeltaResponseWithTopicState);

        akkaService.process(message);

        SyncResponse response = new SyncResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(new RedirectSyncResponse("testDNS".hashCode()));

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT / 2).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
    }

    @Test
    public void testEndpointEventBasic() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);
        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), Base64Util.encode(clientPublicKeyHash.array()), null);

        SyncRequest sourceRequest = new SyncRequest();
        sourceRequest.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        sourceRequest.setSyncRequestMetaData(md);

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEvents(Arrays.asList(event));
        sourceRequest.setEventSyncRequest(eventRequest);

        ServerSync sourceResponse = new ServerSync();
        sourceResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        SyncContext sourceResponseHolder = new SyncContext(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        SyncRequest targetRequest = new SyncRequest();
        targetRequest.setRequestId(REQUEST_ID);
        md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(targetPublicKeyHash);
        md.setProfileHash(targetPublicKeyHash);
        md.setTimeout(TIMEOUT * 1L);
        targetRequest.setSyncRequestMetaData(md);

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        ServerSync targetResponse = new ServerSync();
        targetResponse.setRequestId(REQUEST_ID);
        targetResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        SyncContext targetResponseHolder = new SyncContext(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        whenSync(AvroEncDec.convert(sourceRequest), sourceResponseHolder);
        whenSync(AvroEncDec.convert(targetRequest), targetResponseHolder);

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

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder sourceCrypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(),
                serverPair.getPublic());
        SessionInitMessage sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                sourceRequest, responseBuilder, errorBuilder, sourceCrypt);
        akkaService.process(sourceMessage);

        // sourceRequest
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());
        SessionInitMessage targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                targetRequest, responseBuilder, errorBuilder, targetCrypt);
        akkaService.process(targetMessage);

        // targetRequest
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        SyncResponse eventResponse = new SyncResponse();
        eventResponse.setRequestId(REQUEST_ID);
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
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest sourceRequest = new SyncRequest();
        sourceRequest.setRequestId(REQUEST_ID);
        sourceRequest.setSyncRequestMetaData(buildSyncRequestMetaData());

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEventSequenceNumberRequest(new EventSequenceNumberRequest());
        sourceRequest.setEventSyncRequest(eventRequest);

        ServerSync sourceResponse = new ServerSync();
        sourceResponse.setRequestId(REQUEST_ID);
        sourceResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        SyncContext sourceResponseHolder = new SyncContext(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        whenSync(AvroEncDec.convert(sourceRequest), sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder sourceCrypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(),
                serverPair.getPublic());
        SessionInitMessage sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                sourceRequest, responseBuilder, errorBuilder, sourceCrypt);
        akkaService.process(sourceMessage);

        // sourceRequest
        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        SyncResponse eventResponse = new SyncResponse();
        eventResponse.setRequestId(REQUEST_ID);
        eventResponse.setStatus(SyncResponseResultType.SUCCESS);
        eventResponse.setEventSyncResponse(new EventSyncResponse());
        eventResponse.getEventSyncResponse().setEventSequenceNumberResponse(new EventSequenceNumberResponse(0));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(eventResponse);
        LOG.trace("Response to compare {}", Arrays.toString(response));
        byte[] encodedData = sourceCrypt.encodeData(response);

        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRemoteIncomingEndpointEventBasic() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        targetRequest.setRequestId(REQUEST_ID);
        targetRequest.setSyncRequestMetaData(buildSyncRequestMetaData(targetPublicKeyHash));

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        ServerSync targetResponse = new ServerSync();

        targetResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        SyncContext targetResponseHolder = new SyncContext(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        whenSync(AvroEncDec.convert(targetRequest), targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());
        SessionInitMessage targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                targetRequest, responseBuilder, errorBuilder, targetCrypt);
        akkaService.process(targetMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        org.kaaproject.kaa.server.sync.Event event = new org.kaaproject.kaa.server.sync.Event(0, FQN1, ByteBuffer.wrap(new byte[0]), null,
                null);
        EndpointEvent endpointEvent = new EndpointEvent(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()), event, UUID.randomUUID(),
                System.currentTimeMillis(), ECF1_VERSION);
        RemoteEndpointEvent remoteEvent = new RemoteEndpointEvent(TENANT_ID, USER_ID, endpointEvent,
                new RouteTableAddress(EndpointObjectHash.fromBytes(targetPublicKeyHash.array()), APP_TOKEN, "SERVER1"));
        akkaService.getListener().onEvent(remoteEvent);

        event = new org.kaaproject.kaa.server.sync.Event(0, FQN1, ByteBuffer.wrap(new byte[0]), null,
                Base64Util.encode(targetPublicKeyHash.array()));

        ServerSync eventResponse = new ServerSync();
        eventResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        eventResponse.setEventSync(new EventServerSync());
        eventResponse.getEventSync().setEvents(Arrays.asList(event));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(AvroEncDec.convert(eventResponse));
        byte[] encodedData = targetCrypt.encodeData(response);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testRemoteOutcomingEndpointEventBasic() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        Event event = new Event(0, FQN1, ByteBuffer.wrap(new byte[0]), Base64Util.encode(clientPublicKeyHash.array()), null);

        SyncRequest sourceRequest = new SyncRequest();
        sourceRequest.setRequestId(REQUEST_ID);
        sourceRequest.setSyncRequestMetaData(buildSyncRequestMetaData(clientPublicKeyHash));

        EventSyncRequest eventRequest = new EventSyncRequest();
        eventRequest.setEvents(Arrays.asList(event));
        sourceRequest.setEventSyncRequest(eventRequest);

        ServerSync sourceResponse = new ServerSync();
        sourceResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        SyncContext sourceResponseHolder = new SyncContext(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        whenSync(AvroEncDec.convert(sourceRequest), sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        MessageEncoderDecoder sourceCrypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(),
                serverPair.getPublic());
        SessionInitMessage sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                sourceRequest, responseBuilder, errorBuilder, sourceCrypt);

        akkaService.process(sourceMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        UserRouteInfo userRouteInfo = new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.ADD);
        akkaService.getListener().onUserRouteInfo(userRouteInfo);

        RouteTableAddress remoteAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(targetPublicKeyHash.array()), APP_TOKEN,
                SERVER2);
        RouteInfo routeInfo = new RouteInfo(TENANT_ID, USER_ID, remoteAddress,
                Arrays.asList(new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION)));
        TimeUnit.SECONDS.sleep(2);
        akkaService.getListener().onRouteInfo(routeInfo);

        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendEvent(Mockito.any(RemoteEndpointEvent.class));
    }

    @Test
    public void testLogSyncRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        md.setTimeout(1000l);
        request.setSyncRequestMetaData(md);

        LogSyncRequest logRequest = new LogSyncRequest(REQUEST_ID,
                Collections.singletonList(new LogEntry(ByteBuffer.wrap("String".getBytes()))));
        request.setLogSyncRequest(logRequest);

        whenSync(noDeltaResponseWithTopicState);

        LogAppender mockAppender = Mockito.mock(LogAppender.class);
        Mockito.when(logAppenderService.getApplicationAppenders(APP_ID)).thenReturn(Collections.singletonList(mockAppender));
        Mockito.when(logAppenderService.getLogSchema(Mockito.anyString(), Mockito.anyInt())).thenReturn(new LogSchema(new LogSchemaDto(), ""));
        EndpointProfileSchemaDto profileSchemaDto = new EndpointProfileSchemaDto();
        profileSchemaDto.setId("1");
        profileSchemaDto.setCtlSchemaId("22");

        CTLSchemaDto ctlSchema = new CTLSchemaDto();
        ctlSchema.setId("22");

        when(cacheService.getProfileSchemaByAppAndVersion(new AppVersionKey(APP_TOKEN, 0))).thenReturn(profileSchemaDto);
        when(cacheService.getCtlSchemaById("22")).thenReturn(ctlSchema);
        when(ctlService.flatExportAsString(ctlSchema)).thenReturn("ClientProfileSchema");

        ServerProfileSchemaDto serverProfileSchemaDto = new ServerProfileSchemaDto();
        serverProfileSchemaDto.setId("1");
        serverProfileSchemaDto.setCtlSchemaId("23");

        CTLSchemaDto serverCtlSchema = new CTLSchemaDto();
        serverCtlSchema.setId("23");

        when(cacheService.getServerProfileSchemaByAppAndVersion(new AppVersionKey(APP_TOKEN, 0))).thenReturn(serverProfileSchemaDto);
        when(cacheService.getCtlSchemaById("23")).thenReturn(serverCtlSchema);
        when(ctlService.flatExportAsString(serverCtlSchema)).thenReturn("ServerProfileSchema");

        Mockito.when(mockAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(true);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder);

        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(logAppenderService, Mockito.timeout(TIMEOUT).atLeastOnce()).getLogSchema(APP_ID, 44);

        Mockito.verify(mockAppender, Mockito.timeout(TIMEOUT).atLeastOnce()).doAppend(Mockito.any(BaseLogEventPack.class),
                Mockito.any(LogDeliveryCallback.class));

        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(byte[].class),
                Mockito.any(boolean.class));
    }

    @Test
    public void testUserChange() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);
        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        targetRequest.setRequestId(REQUEST_ID);
        targetRequest.setSyncRequestMetaData(buildSyncRequestMetaData(targetPublicKeyHash));

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        ServerSync targetResponse = new ServerSync();
        targetResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        SyncContext targetResponseHolder = new SyncContext(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        whenSync(AvroEncDec.convert(targetRequest), targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, targetRequest,
                responseBuilder, errorBuilder, targetCrypt);

        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        UserRouteInfo userRouteInfo = new UserRouteInfo(TENANT_ID, USER_ID, SERVER2, RouteOperation.ADD);
        akkaService.getListener().onUserRouteInfo(userRouteInfo);
        TimeUnit.SECONDS.sleep(2);

        RouteTableAddress remoteAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()), APP_TOKEN,
                SERVER2);
        RouteInfo remoteRouteInfo = new RouteInfo(TENANT_ID, USER_ID, remoteAddress,
                Arrays.asList(new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION)));
        TimeUnit.SECONDS.sleep(2);
        akkaService.getListener().onRouteInfo(remoteRouteInfo);

        RouteTableAddress localAddress = new RouteTableAddress(EndpointObjectHash.fromBytes(targetPublicKeyHash.array()), APP_TOKEN, null);
        RouteInfo localRouteInfo = new RouteInfo(TENANT_ID, USER_ID, localAddress,
                Arrays.asList(new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION)));
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendRouteInfo(Collections.singletonList(localRouteInfo),
                SERVER2);

        targetRequest = new SyncRequest();
        targetRequest.setRequestId(REQUEST_ID);
        targetRequest.setSyncRequestMetaData(buildSyncRequestMetaData(targetPublicKeyHash));

        targetResponse = new ServerSync();
        targetResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        targetResponseHolder = new SyncContext(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID + "2");

        whenSync(AvroEncDec.convert(targetRequest), targetResponseHolder);

        SessionInitMessage targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                targetRequest, responseBuilder, errorBuilder, targetCrypt);
        akkaService.process(targetMessage);
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID + "2"));
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce())
                .sendRouteInfo(RouteInfo.deleteRouteFromAddress(TENANT_ID, USER_ID, localAddress), SERVER2);
    }

    @Test
    public void testEndpointAttach() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);
        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        targetRequest.setRequestId(REQUEST_ID);
        targetRequest.setSyncRequestMetaData(buildSyncRequestMetaData(targetPublicKeyHash));

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        ServerSync targetResponse = new ServerSync();
        targetResponse.setRequestId(REQUEST_ID);
        targetResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        targetResponse.setUserSync(new UserServerSync());
        SyncContext targetResponseHolder = new SyncContext(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        whenSync(AvroEncDec.convert(targetRequest), targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                targetRequest, responseBuilder, errorBuilder, targetCrypt);

        akkaService.process(targetMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        SyncRequest sourceRequest = new SyncRequest();
        sourceRequest.setRequestId(REQUEST_ID);
        sourceRequest.setSyncRequestMetaData(buildSyncRequestMetaData(clientPublicKeyHash));

        sourceRequest.setEventSyncRequest(new EventSyncRequest());

        UserSyncRequest userSyncRequest = new UserSyncRequest();
        EndpointAttachRequest eaRequest = new EndpointAttachRequest(REQUEST_ID, "token");
        userSyncRequest.setEndpointAttachRequests(Collections.singletonList(eaRequest));
        sourceRequest.setUserSyncRequest(userSyncRequest);

        ServerSync sourceResponse = new ServerSync();
        sourceResponse.setRequestId(REQUEST_ID);
        sourceResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        UserServerSync userSyncResponse = new UserServerSync();
        userSyncResponse
                .setEndpointAttachResponses(Collections.singletonList(new org.kaaproject.kaa.server.sync.EndpointAttachResponse(REQUEST_ID,
                        Base64Util.encode(targetPublicKeyHash.array()), org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS)));
        sourceResponse.setUserSync(userSyncResponse);

        SyncContext sourceResponseHolder = new SyncContext(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        whenSync(AvroEncDec.convert(sourceRequest), sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        MessageBuilder sourceResponseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder sourceErrorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                sourceRequest, sourceResponseBuilder, sourceErrorBuilder);
        akkaService.process(sourceMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        SyncResponse targetSyncResponse = new SyncResponse();
        targetSyncResponse.setRequestId(REQUEST_ID);
        targetSyncResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetSyncResponse.setUserSyncResponse(new UserSyncResponse());
        targetSyncResponse.getUserSyncResponse()
                .setUserAttachNotification(new UserAttachNotification(USER_ID, Base64Util.encode(clientPublicKeyHash.array())));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(targetSyncResponse);
        LOG.trace("Expected response {}", Arrays.toString(response));
        byte[] encodedData = targetCrypt.encodeData(response);
        Mockito.verify(responseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    @Test
    public void testEndpointDetach() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);
        MessageEncoderDecoder targetCrypt = new MessageEncoderDecoder(targetPair.getPrivate(), targetPair.getPublic(),
                serverPair.getPublic());

        EndpointProfileDto targetProfileMock = Mockito.mock(EndpointProfileDto.class);

        EventClassFamilyVersionStateDto ecfVdto = new EventClassFamilyVersionStateDto();
        ecfVdto.setEcfId(ECF1_ID);
        ecfVdto.setVersion(ECF1_VERSION);

        SyncRequest targetRequest = new SyncRequest();
        targetRequest.setRequestId(REQUEST_ID);
        targetRequest.setSyncRequestMetaData(buildSyncRequestMetaData(targetPublicKeyHash));

        targetRequest.setEventSyncRequest(new EventSyncRequest());

        ServerSync targetResponse = new ServerSync();
        targetResponse.setRequestId(REQUEST_ID);
        targetResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        targetResponse.setUserSync(new UserServerSync());
        SyncContext targetResponseHolder = new SyncContext(targetResponse);
        targetResponseHolder.setEndpointProfile(targetProfileMock);

        whenSync(AvroEncDec.convert(targetRequest), targetResponseHolder);

        when(targetProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(targetProfileMock.getEndpointKeyHash()).thenReturn(targetPublicKeyHash.array());
        when(targetProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        when(cacheService.getEventClassFamilyIdByEventClassFqn(new EventClassFqnKey(TENANT_ID, FQN1))).thenReturn(ECF1_ID);
        RouteTableKey routeKey = new RouteTableKey(APP_TOKEN, new EventClassFamilyVersion(ECF1_ID, ECF1_VERSION));
        when(cacheService.getRouteKeys(new EventClassFqnVersion(TENANT_ID, FQN1, ECF1_VERSION)))
                .thenReturn(Collections.singleton(routeKey));

        Assert.assertNotNull(akkaService.getActorSystem());

        MessageBuilder targetResponseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder targetErrorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage targetMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                targetRequest, targetResponseBuilder, targetErrorBuilder, targetCrypt);
        akkaService.process(targetMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        Mockito.verify(eventService, Mockito.timeout(TIMEOUT).atLeastOnce()).sendUserRouteInfo(new UserRouteInfo(TENANT_ID, USER_ID));

        EndpointProfileDto sourceProfileMock = Mockito.mock(EndpointProfileDto.class);

        SyncRequest sourceRequest = new SyncRequest();
        sourceRequest.setRequestId(REQUEST_ID);
        sourceRequest.setSyncRequestMetaData(buildSyncRequestMetaData(clientPublicKeyHash));

        sourceRequest.setEventSyncRequest(new EventSyncRequest());

        UserSyncRequest userSyncRequest = new UserSyncRequest();
        EndpointDetachRequest eaRequest = new EndpointDetachRequest(REQUEST_ID, Base64Util.encode(targetPublicKeyHash.array()));
        userSyncRequest.setEndpointDetachRequests(Collections.singletonList(eaRequest));
        sourceRequest.setUserSyncRequest(userSyncRequest);

        ServerSync sourceResponse = new ServerSync();
        sourceResponse.setRequestId(REQUEST_ID);
        sourceResponse.setStatus(org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS);
        UserServerSync userSyncResponse = new UserServerSync();
        userSyncResponse.setEndpointDetachResponses(Collections.singletonList(
                new org.kaaproject.kaa.server.sync.EndpointDetachResponse(REQUEST_ID, org.kaaproject.kaa.server.sync.SyncStatus.SUCCESS)));
        sourceResponse.setUserSync(userSyncResponse);
        SyncContext sourceResponseHolder = new SyncContext(sourceResponse);
        sourceResponseHolder.setEndpointProfile(sourceProfileMock);

        whenSync(AvroEncDec.convert(sourceRequest), sourceResponseHolder);

        when(sourceProfileMock.getEndpointUserId()).thenReturn(USER_ID);
        when(sourceProfileMock.getEndpointKeyHash()).thenReturn(clientPublicKeyHash.array());
        when(sourceProfileMock.getEcfVersionStates()).thenReturn(Arrays.asList(ecfVdto));

        MessageBuilder sourceResponseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder sourceErrorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage sourceMessage = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock,
                sourceRequest, sourceResponseBuilder, sourceErrorBuilder);
        akkaService.process(sourceMessage);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));
        SyncResponse targetSyncResponse = new SyncResponse();
        targetSyncResponse.setRequestId(REQUEST_ID);
        targetSyncResponse.setStatus(SyncResponseResultType.SUCCESS);
        targetSyncResponse.setUserSyncResponse(new UserSyncResponse());
        targetSyncResponse.getUserSyncResponse()
                .setUserDetachNotification(new UserDetachNotification(Base64Util.encode(clientPublicKeyHash.array())));

        AvroByteArrayConverter<SyncResponse> responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
        byte[] response = responseConverter.toByteArray(targetSyncResponse);
        byte[] encodedData = targetCrypt.encodeData(response);
        Mockito.verify(targetResponseBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(encodedData, true);
    }

    // TODO: fix when server profile feature will be ready
    // @Test
    // public void testServerProfileUpdate() throws Exception {
    // ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);
    //
    // SyncRequest request = new SyncRequest();
    // request.setRequestId(REQUEST_ID);
    // SyncRequestMetaData md = buildSyncRequestMetaData();
    // request.setSyncRequestMetaData(md);
    //
    // ConfigurationSyncRequest csRequest = new ConfigurationSyncRequest();
    // request.setConfigurationSyncRequest(csRequest);
    //
    // Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()))).thenReturn(
    // clientPair.getPublic());
    // whenSync(deltaResponseWithProfile);
    //
    // MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
    // ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
    //
    // SessionInitMessage message = toSignedRequest(UUID.randomUUID(),
    // ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
    // responseBuilder, errorBuilder);
    // Assert.assertNotNull(akkaService.getActorSystem());
    // akkaService.process(message);
    //
    // Mockito.verify(operationsService,
    // Mockito.timeout(TIMEOUT).atLeastOnce()).syncProfile(Mockito.any(SyncContext.class),
    // Mockito.any(ProfileClientSync.class));
    //
    // Notification thriftNotification = new Notification();
    // thriftNotification.setAppId(APP_ID);
    // thriftNotification.setOp(Operation.UPDATE_SERVER_PROFILE);
    // thriftNotification.setKeyHash(clientPublicKeyHash);
    // akkaService.onNotification(thriftNotification);
    //
    // Mockito.verify(operationsService,
    // Mockito.timeout(TIMEOUT*100).atLeastOnce())
    // .refreshServerEndpointProfile(EndpointObjectHash.fromBytes(clientPublicKeyHash.array()));
    // }
    
    //TODO: Implement tests that cover endpoint verification logic.

    @Test
    public void testNoEndpointCredentialsSyncRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        SyncContext holder = simpleResponse;

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(holder);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        
        Mockito.when(registrationService.findEndpointRegistrationByCredentialsId(Mockito.anyString())).thenReturn(Optional.ofNullable((EndpointRegistrationDto)null));

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(EndpointVerificationException.class));
    }
    
    @Test
    public void testRevokedEndpointCredentialsSyncRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        SyncContext holder = simpleResponse;

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(holder);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        
        Mockito.when(registrationService.findEndpointRegistrationByCredentialsId(Mockito.anyString())).thenReturn(Optional.ofNullable((EndpointRegistrationDto)null));
        Mockito.when(credentialsService.lookupCredentials(Mockito.anyString())).thenReturn(Optional.of(new CredentialsDto(new byte[]{}, CredentialsStatus.REVOKED)));

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(EndpointVerificationException.class));
    }
    
    @Test
    public void testInUseEndpointCredentialsSyncRequest() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(clientPublicKeyHash);
        md.setProfileHash(clientPublicKeyHash);
        request.setSyncRequestMetaData(md);

        SyncContext holder = simpleResponse;

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(holder);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        
        Mockito.when(registrationService.findEndpointRegistrationByCredentialsId(Mockito.anyString())).thenReturn(Optional.ofNullable((EndpointRegistrationDto)null));
        Mockito.when(credentialsService.lookupCredentials(Mockito.anyString())).thenReturn(Optional.of(new CredentialsDto(new byte[]{}, CredentialsStatus.IN_USE)));

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC, channelContextMock, request, responseBuilder,
                errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(EndpointVerificationException.class));
    }
    
    @Test
    public void testLongSyncRevocation() throws Exception {
        ChannelContext channelContextMock = Mockito.mock(ChannelContext.class);

        SyncRequest request = new SyncRequest();
        request.setRequestId(REQUEST_ID);
        SyncRequestMetaData md = buildSyncRequestMetaData();
        request.setSyncRequestMetaData(md);

        ConfigurationSyncRequest csRequest = new ConfigurationSyncRequest();
        csRequest.setConfigurationHash(ByteBuffer.wrap(new byte[] {}));
        csRequest.setResyncOnly(true);
        request.setConfigurationSyncRequest(csRequest);

        Mockito.when(cacheService.getEndpointKey(EndpointObjectHash.fromBytes(clientPublicKeyHash.array())))
                .thenReturn(clientPair.getPublic());
        whenSync(noDeltaResponse);

        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        SessionInitMessage message = toSignedRequest(UUID.randomUUID(), ChannelType.SYNC_WITH_TIMEOUT, channelContextMock, request,
                responseBuilder, errorBuilder);
        Assert.assertNotNull(akkaService.getActorSystem());
        akkaService.process(message);

        Mockito.verify(operationsService, Mockito.timeout(TIMEOUT).atLeastOnce()).syncClientProfile(Mockito.any(SyncContext.class),
                Mockito.any(ProfileClientSync.class));

        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(applicationDto);
        whenSync(deltaResponse);

        EndpointAddress address = new EndpointAddress(applicationDto.getTenantId(), applicationDto.getApplicationToken(),
                EndpointObjectHash.fromBytes(clientPublicKeyHash.array()));
        ActorClassifier classifier =  ActorClassifier.APPLICATION;
        
        clusterServiceListener.onEndpointActorMsg(new ThriftEndpointActorMsg<ThriftEndpointDeregistrationMessage>(
                address, classifier, new ThriftEndpointDeregistrationMessage()));

        Mockito.verify(errorBuilder, Mockito.timeout(TIMEOUT).atLeastOnce()).build(Mockito.any(EndpointRevocationException.class));
    }
    
    private SyncRequestMetaData buildSyncRequestMetaData() {
        return buildSyncRequestMetaData(clientPublicKeyHash);
    }

    private SyncRequestMetaData buildSyncRequestMetaData(ByteBuffer keyHash) {
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setSdkToken(SDK_TOKEN);
        md.setEndpointPublicKeyHash(keyHash);
        md.setProfileHash(keyHash);
        md.setTimeout(2l * TIMEOUT);
        return md;
    }
}
