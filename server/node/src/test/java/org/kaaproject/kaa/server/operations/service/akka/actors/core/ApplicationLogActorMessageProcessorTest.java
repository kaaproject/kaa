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
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.AbstractActorCallback;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.mockito.Mockito;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

/**
 * @author Bohdan Khablenko
 */
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

    private static final int REQUEST_ID = 100;

    private static final String REQUIRED_LOG_APPENDER_ID = "100";
    private static final String OPTIONAL_LOG_APPENDER_ID = "200";

    private AkkaContext context;

    private ApplicationDto application;
    private ApplicationService applicationService;

    private LogSchema logSchema;
    private LogAppender required;
    private LogAppender optional;
    private List<LogAppender> logAppenders;
    private LogAppenderService logAppenderService;

    private CacheService cacheService;
    private CTLService ctlService;

    private LogEventPackMessage message;

    @Before
    public void before() throws Exception {

        // Application
        this.application = Mockito.mock(ApplicationDto.class);
        Mockito.when(this.application.getId()).thenReturn(APPLICATION_ID);

        // Application service
        this.applicationService = Mockito.mock(ApplicationService.class);
        Mockito.when(this.applicationService.findAppByApplicationToken(APPLICATION_TOKEN)).thenReturn(this.application);

        // Log schema
        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setVersion(LOG_SCHEMA_VERSION);
        this.logSchema = new LogSchema(logSchemaDto);

        // Log appenders
        this.logAppenders = new ArrayList<>();
        this.required = Mockito.mock(LogAppender.class, "Some Required Log Appender");
        this.optional = Mockito.mock(LogAppender.class, "Some Optional Log Appender");
        Mockito.when(this.required.getAppenderId()).thenReturn(REQUIRED_LOG_APPENDER_ID);
        Mockito.when(this.optional.getAppenderId()).thenReturn(OPTIONAL_LOG_APPENDER_ID);
        Mockito.when(this.required.isDeliveryConfirmationRequired()).thenReturn(Boolean.TRUE);
        Mockito.when(this.optional.isDeliveryConfirmationRequired()).thenReturn(Boolean.FALSE);
        Mockito.when(this.required.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);
        Mockito.when(this.optional.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);

        // Log appender service
        this.logAppenderService = Mockito.mock(LogAppenderService.class);
        Mockito.when(this.logAppenderService.getApplicationAppenders(APPLICATION_ID)).thenReturn(this.logAppenders);
        Mockito.when(this.logAppenderService.getLogSchema(APPLICATION_ID, LOG_SCHEMA_VERSION)).thenReturn(this.logSchema);

        // Endpoint profile schema
        EndpointProfileSchemaDto endpointProfileSchema = new EndpointProfileSchemaDto();
        endpointProfileSchema.setId(Integer.toString(CLIENT_PROFILE_SCHEMA_ID));
        endpointProfileSchema.setCtlSchemaId(CLIENT_PROFILE_SCHEMA_CTL_SCHEMA_ID);

        // Server profile schema
        ServerProfileSchemaDto serverProfileSchema = new ServerProfileSchemaDto();
        serverProfileSchema.setId(Integer.toString(SERVER_PROFILE_SCHEMA_ID));
        serverProfileSchema.setCtlSchemaId(SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID);

        this.cacheService = Mockito.mock(CacheService.class);
        this.ctlService = Mockito.mock(CTLService.class);

        // Cache services
        AppVersionKey key;
        key = new AppVersionKey(APPLICATION_TOKEN, CLIENT_PROFILE_SCHEMA_ID);
        Mockito.when(this.cacheService.getProfileSchemaByAppAndVersion(key)).thenReturn(endpointProfileSchema);
        key = new AppVersionKey(APPLICATION_TOKEN, SERVER_PROFILE_SCHEMA_ID);
        Mockito.when(this.cacheService.getServerProfileSchemaByAppAndVersion(key)).thenReturn(serverProfileSchema);

        // Endpoint profile CTL schema
        CTLSchemaDto endpointProfileCTLSchema = new CTLSchemaDto();
        endpointProfileCTLSchema.setId(CLIENT_PROFILE_SCHEMA_CTL_SCHEMA_ID);
        Mockito.when(this.cacheService.getCtlSchemaById(CLIENT_PROFILE_SCHEMA_CTL_SCHEMA_ID)).thenReturn(endpointProfileCTLSchema);

        // Server profile CTL schema
        CTLSchemaDto serverProfileCTLSchema = new CTLSchemaDto();
        serverProfileCTLSchema.setId(SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID);
        Mockito.when(this.cacheService.getCtlSchemaById(SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID)).thenReturn(serverProfileCTLSchema);

        Mockito.when(this.ctlService.flatExportAsString(endpointProfileCTLSchema)).thenReturn("Client Profile CTL Schema");
        Mockito.when(this.ctlService.flatExportAsString(serverProfileCTLSchema)).thenReturn("Server Profile CTL Schema");

        // Akka Context
        this.context = Mockito.mock(AkkaContext.class);
        Mockito.when(this.context.getApplicationService()).thenReturn(this.applicationService);
        Mockito.when(this.context.getCacheService()).thenReturn(this.cacheService);
        Mockito.when(this.context.getCtlService()).thenReturn(this.ctlService);
        Mockito.when(this.context.getLogAppenderService()).thenReturn(this.logAppenderService);

        // Log event pack message
        EndpointProfileDataDto endpoint = new EndpointProfileDataDto(ENDPOINT_ID, ENDPOINT_KEY, CLIENT_PROFILE_SCHEMA_ID, "", SERVER_PROFILE_SCHEMA_ID, "");
        BaseLogEventPack pack = new BaseLogEventPack(endpoint, System.currentTimeMillis(), LOG_SCHEMA_VERSION, new ArrayList<LogEvent>());
        this.message = new LogEventPackMessage(REQUEST_ID, ActorRef.noSender(), pack);
    }

    /**
     * A test to ensure that the endpoint receives a response.
     *
     * @throws Exception
     */
    @Test
    public void worksAsIntendedTest() throws Exception {

        this.logAppenders.add(this.required);
        this.logAppenders.add(this.optional);

        ApplicationLogActorMessageProcessor messageProcessor = new ApplicationLogActorMessageProcessor(this.context, APPLICATION_TOKEN);
        messageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), this.message);

        Mockito.verify(this.required).doAppend(Mockito.eq(this.message.getLogEventPack()), Mockito.any(AbstractActorCallback.class));
        Mockito.verify(this.optional).doAppend(Mockito.eq(this.message.getLogEventPack()), Mockito.any(AbstractActorCallback.class));
    }

    /**
     * A test to ensure that the endpoint receives a response even when there
     * are no log appenders that require delivery confirmation.
     *
     * @throws Exception
     */
    @Test
    public void withoutMandatoryLogAppendersTest() throws Exception {

        // No log appenders that require delivery confirmation
        this.logAppenders.add(this.optional);

        // Process the message
        ApplicationLogActorMessageProcessor messageProcessor = Mockito.spy(new ApplicationLogActorMessageProcessor(this.context, APPLICATION_TOKEN));
        messageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), this.message);

        // Nonetheless, a response has been sent
        Mockito.verify(this.optional).doAppend(Mockito.eq(this.message.getLogEventPack()), Mockito.any(AbstractActorCallback.class));
    }
}
