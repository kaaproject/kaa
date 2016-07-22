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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.ApplicationLogActorMessageProcessor.VoidCallback;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.AbstractActorCallback;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.MultiLogDeliveryCallback;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.SingleLogDeliveryCallback;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

/**
 * @author Bohdan Khablenko
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractActorCallback.class, SingleLogDeliveryCallback.class, MultiLogDeliveryCallback.class })
public class ApplicationLogActorMessageProcessorTest {

    private static final String APPLICATION_ID = "application_id";
    private static final String APPLICATION_TOKEN = "application_token";

    private static final String ENDPOINT_ID = "endpoint_id";
    private static final String ENDPOINT_KEY = "endpoint_key";

    private static final int LOG_SCHEMA_VERSION = 1;

    private static final int CLIENT_PROFILE_SCHEMA_ID = 100;
    private static final String CLIENT_PROFILE_SCHEMA_CTL_SCHEMA_ID = "1000";

    private static final int SERVER_PROFILE_SCHEMA_ID = 200;
    private static final String SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID = "2000";

    public static final String DEFAULT_FQN = "org.kaaproject.kaa.ctl.TestSchema";

    private static final int REQUEST_ID = 100;

    private static final int REQUIRED_APPENDERS_COUNT = 2;

    private AkkaContext context;

    private ApplicationDto application;
    private ApplicationService applicationService;

    private LogSchema logSchema;
    private LogAppender[] required;
    private LogAppender optional;
    private List<LogAppender> logAppenders;
    private LogAppenderService logAppenderService;

    private CacheService cacheService;
    private CTLService ctlService;

    private LogEventPackMessage message;

    @Before
    public void before() throws Exception {

        // Application
        application = Mockito.mock(ApplicationDto.class);
        Mockito.when(application.getId()).thenReturn(APPLICATION_ID);

        // Application service
        applicationService = Mockito.mock(ApplicationService.class);
        Mockito.when(applicationService.findAppByApplicationToken(APPLICATION_TOKEN)).thenReturn(application);

        // Log schema
        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setVersion(LOG_SCHEMA_VERSION);
        logSchema = new LogSchema(logSchemaDto, "");

        // Log appenders
        required = new LogAppender[REQUIRED_APPENDERS_COUNT];
        for (int i = 0; i < required.length; i++) {
            required[i] = Mockito.mock(LogAppender.class);
            Mockito.when(required[i].getAppenderId()).thenReturn(Integer.toString(i * 100));
            Mockito.when(required[i].isDeliveryConfirmationRequired()).thenReturn(Boolean.TRUE);
            Mockito.when(required[i].isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);
        }
        optional = Mockito.mock(LogAppender.class);
        Mockito.when(optional.getAppenderId()).thenReturn(Integer.toString(required.length * 100));
        Mockito.when(optional.isDeliveryConfirmationRequired()).thenReturn(Boolean.FALSE);
        Mockito.when(optional.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);

        // Log appender service
        logAppenders = new ArrayList<>();
        logAppenderService = Mockito.mock(LogAppenderService.class);
        Mockito.when(logAppenderService.getApplicationAppenders(APPLICATION_ID)).thenReturn(logAppenders);
        Mockito.when(logAppenderService.getLogSchema(APPLICATION_ID, LOG_SCHEMA_VERSION)).thenReturn(logSchema);

        // Endpoint profile schema
        EndpointProfileSchemaDto endpointProfileSchema = new EndpointProfileSchemaDto();
        endpointProfileSchema.setId(Integer.toString(CLIENT_PROFILE_SCHEMA_ID));
        endpointProfileSchema.setCtlSchemaId(CLIENT_PROFILE_SCHEMA_CTL_SCHEMA_ID);

        // Server profile schema
        ServerProfileSchemaDto serverProfileSchema = new ServerProfileSchemaDto();
        serverProfileSchema.setId(Integer.toString(SERVER_PROFILE_SCHEMA_ID));
        serverProfileSchema.setCtlSchemaId(SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID);

        cacheService = Mockito.mock(CacheService.class);
        ctlService = Mockito.mock(CTLService.class);

        // Cache services
        AppVersionKey key;
        key = new AppVersionKey(APPLICATION_TOKEN, CLIENT_PROFILE_SCHEMA_ID);
        Mockito.when(cacheService.getProfileSchemaByAppAndVersion(key)).thenReturn(endpointProfileSchema);
        key = new AppVersionKey(APPLICATION_TOKEN, SERVER_PROFILE_SCHEMA_ID);
        Mockito.when(cacheService.getServerProfileSchemaByAppAndVersion(key)).thenReturn(serverProfileSchema);

        // Endpoint profile CTL schema
        CTLSchemaDto endpointProfileCTLSchema = new CTLSchemaDto();
        endpointProfileCTLSchema.setId(CLIENT_PROFILE_SCHEMA_CTL_SCHEMA_ID);
        Mockito.when(cacheService.getCtlSchemaById(CLIENT_PROFILE_SCHEMA_CTL_SCHEMA_ID)).thenReturn(endpointProfileCTLSchema);

        // Server profile CTL schema
        CTLSchemaDto serverProfileCTLSchema = new CTLSchemaDto();
        serverProfileCTLSchema.setId(SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID);
        Mockito.when(cacheService.getCtlSchemaById(SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID)).thenReturn(serverProfileCTLSchema);

        Mockito.when(ctlService.flatExportAsString(endpointProfileCTLSchema)).thenReturn("Client Profile CTL Schema");
        Mockito.when(ctlService.flatExportAsString(serverProfileCTLSchema)).thenReturn("Server Profile CTL Schema");

        // Akka Context
        context = Mockito.mock(AkkaContext.class);
        Mockito.when(context.getApplicationService()).thenReturn(applicationService);
        Mockito.when(context.getCacheService()).thenReturn(cacheService);
        Mockito.when(context.getCtlService()).thenReturn(ctlService);
        Mockito.when(context.getLogAppenderService()).thenReturn(logAppenderService);

        // Log event pack message
        EndpointProfileDataDto endpoint = new EndpointProfileDataDto(ENDPOINT_ID, ENDPOINT_KEY, CLIENT_PROFILE_SCHEMA_ID, "", SERVER_PROFILE_SCHEMA_ID, "");
        BaseLogEventPack pack = new BaseLogEventPack(endpoint, System.currentTimeMillis(), LOG_SCHEMA_VERSION, new ArrayList<LogEvent>());
        message = new LogEventPackMessage(REQUEST_ID, ActorRef.noSender(), pack);
    }

    protected CTLSchemaDto generateCTLSchemaDto(String tenantId) {
        return generateCTLSchemaDto(DEFAULT_FQN, tenantId, null, 100);
    }

    protected CTLSchemaDto generateCTLSchemaDto(String fqn, String tenantId, String applicationId, int version) {
        CTLSchemaDto ctlSchema = new CTLSchemaDto();
        ctlSchema.setMetaInfo(new CTLSchemaMetaInfoDto(fqn, tenantId, applicationId));
        ctlSchema.setVersion(version);
        String name = fqn.substring(fqn.lastIndexOf(".") + 1);
        String namespace = fqn.substring(0, fqn.lastIndexOf("."));
        StringBuilder body = new StringBuilder("{\"type\": \"record\",");
        body = body.append("\"name\": \"").append(name).append("\",");
        body = body.append("\"namespace\": \"").append(namespace).append("\",");
        body = body.append("\"version\": ").append(version).append(",");
        body = body.append("\"dependencies\": [], \"fields\": []}");
        ctlSchema.setBody(body.toString());
        return ctlSchema;
    }

    /**
     * A test to ensure that the endpoint receives a response when there is
     * exactly one log appender that requires delivery confirmation. In such
     * case, the actor uses {@link SingleLogDeliveryCallback}
     *
     * @throws Exception
     */
    @Test
    public void withSingleRequiredDeliveryConfirmationTest() throws Exception {

        logAppenders.add(required[0]);
        logAppenders.add(optional);

        SingleLogDeliveryCallback callback = Mockito.spy(new SingleLogDeliveryCallback(message.getOriginator(), message.getRequestId()));
        PowerMockito.whenNew(SingleLogDeliveryCallback.class).withArguments(Mockito.any(), Mockito.anyInt()).thenReturn(callback);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                callback.onSuccess();
                return null;
            }
        }).when(required[0]).doAppend(Mockito.any(LogEventPack.class), Mockito.any(SingleLogDeliveryCallback.class));

        ApplicationLogActorMessageProcessor messageProcessor = new ApplicationLogActorMessageProcessor(context, APPLICATION_TOKEN);
        messageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), message);

        PowerMockito.verifyPrivate(callback).invoke("sendSuccessToEndpoint");
    }

    /**
     * A test to ensure that the endpoint receives a response when there are
     * multiple log appenders that require delivery confirmation. In such case,
     * the actor uses an instance of {@link MultiLogDeliveryCallback}
     *
     * @throws Exception
     */
    @Test
    public void withMultipleRequiredDeliveryConfirmationsTest() throws Exception {

        Arrays.stream(required).forEach(logAppenders::add);

        MultiLogDeliveryCallback object = new MultiLogDeliveryCallback(message.getOriginator(), message.getRequestId(), logAppenders.size());
        MultiLogDeliveryCallback callback = Mockito.spy(object);
        PowerMockito.whenNew(MultiLogDeliveryCallback.class).withArguments(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()).thenReturn(callback);

        Arrays.stream(required).forEach(appender -> {
            Mockito.doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    callback.onSuccess();
                    return null;
                }
            }).when(appender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(MultiLogDeliveryCallback.class));
        });

        ApplicationLogActorMessageProcessor messageProcessor = new ApplicationLogActorMessageProcessor(context, APPLICATION_TOKEN);
        messageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), message);

        PowerMockito.verifyPrivate(callback).invoke("sendSuccessToEndpoint");
    }

    /**
     * A test to ensure that the endpoint receives a response even when there
     * are no log appenders that require delivery confirmation.
     *
     * @throws Exception
     */
    @Test
    public void withoutRequiredDeliveryConfirmationsTest() throws Exception {

        logAppenders.add(optional);

        ApplicationLogActorMessageProcessor messageProcessor = Mockito.spy(new ApplicationLogActorMessageProcessor(context, APPLICATION_TOKEN));
        messageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), message);

        Mockito.verify(optional).doAppend(Mockito.eq(message.getLogEventPack()), Mockito.any(VoidCallback.class));
        Mockito.verify(messageProcessor).sendSuccessMessageToEndpoint(message);
    }
}
