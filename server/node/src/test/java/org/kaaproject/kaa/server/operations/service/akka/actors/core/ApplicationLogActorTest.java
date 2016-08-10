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

import akka.actor.ActorContext;
import akka.actor.ActorRef;
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
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationLogActorTest {

    private static final String CLIENT_PROFILE_CTL_SCHEMA_ID = "144";
    
    private static final String SERVER_PROFILE_CTL_SCHEMA_ID = "173";

    private static final int CLIENT_SCHEMA_VERSION = 42;
    
    private static final int SERVER_SCHEMA_VERSION = 33;

    private static final int REQUEST_ID = 42;

    private static final int TEST_SCHEMA_VERSION = 1;

    private ApplicationLogActorMessageProcessor applicationLogActorMessageProcessor;

    private AkkaContext context;
    private LogAppenderService logAppenderService;
    private ApplicationService applicationService;
    private CacheService cacheService;
    private CTLService ctlService;
    private ApplicationDto applicationDto;

    private List<LogAppender> logAppenders;

    private LogSchema logSchema;
    private LogAppender logAppender;

    private static final String APPLICATION_ID = "application_id";
    private static final String APPENDER_ID = "appender_id";
    private static final String APP_TOKEN = "app_token";

    private static final int LOG_SCHEMA_VERSION = 1;

    @Before
    public void before() throws Exception {
        context = mock(AkkaContext.class);
        logAppenderService = mock(LogAppenderService.class);
        applicationService = mock(ApplicationService.class);
        cacheService = mock(CacheService.class);
        applicationDto = mock(ApplicationDto.class);
        ctlService = mock(CTLService.class);

        when(context.getCacheService()).thenReturn(cacheService);
        when(context.getApplicationService()).thenReturn(applicationService);
        when(context.getLogAppenderService()).thenReturn(logAppenderService);
        when(context.getCtlService()).thenReturn(ctlService);

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setVersion(TEST_SCHEMA_VERSION);
        logSchema = new LogSchema(logSchemaDto, "");

        logAppenders = new ArrayList<>();
        logAppender = mock(LogAppender.class);
        logAppenders.add(logAppender);

        when(logAppenderService.getLogSchema(APPLICATION_ID, LOG_SCHEMA_VERSION)).thenReturn(logSchema);
        when(applicationService.findAppByApplicationToken(APP_TOKEN)).thenReturn(applicationDto);
        when(applicationDto.getId()).thenReturn(APPLICATION_ID);
        when(logAppenderService.getApplicationAppenders(APPLICATION_ID)).thenReturn(logAppenders);
        when(logAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);

        EndpointProfileSchemaDto profileSchemaDto = new EndpointProfileSchemaDto();
        profileSchemaDto.setId("" + CLIENT_SCHEMA_VERSION);
        profileSchemaDto.setCtlSchemaId(CLIENT_PROFILE_CTL_SCHEMA_ID);
        
        ServerProfileSchemaDto serverProfileSchemaDto = new ServerProfileSchemaDto();
        serverProfileSchemaDto.setId("" + SERVER_SCHEMA_VERSION);
        serverProfileSchemaDto.setCtlSchemaId(SERVER_PROFILE_CTL_SCHEMA_ID);
        
        when(cacheService.getProfileSchemaByAppAndVersion(new AppVersionKey(APP_TOKEN, CLIENT_SCHEMA_VERSION)))
                .thenReturn(profileSchemaDto);
        when(cacheService.getServerProfileSchemaByAppAndVersion(new AppVersionKey(APP_TOKEN, SERVER_SCHEMA_VERSION)))
        .thenReturn(serverProfileSchemaDto);
        
        CTLSchemaDto profileCtlSchemaDto = new CTLSchemaDto();
        profileCtlSchemaDto.setId(CLIENT_PROFILE_CTL_SCHEMA_ID);

        CTLSchemaDto serverProfileCtlSchemaDto = new CTLSchemaDto();
        serverProfileCtlSchemaDto.setId(SERVER_PROFILE_CTL_SCHEMA_ID);

        when(cacheService.getCtlSchemaById(CLIENT_PROFILE_CTL_SCHEMA_ID)).thenReturn(profileCtlSchemaDto);
        when(cacheService.getCtlSchemaById(SERVER_PROFILE_CTL_SCHEMA_ID)).thenReturn(serverProfileCtlSchemaDto);
        
        when(ctlService.flatExportAsString(profileCtlSchemaDto)).thenReturn("ClientProfileSchema");
        when(ctlService.flatExportAsString(serverProfileCtlSchemaDto)).thenReturn("ServerProfileSchema");
    }

    @Test
    public void proccessLogSchemaVersionMessageHaveLogSchemaTest() throws Exception {
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(context, APP_TOKEN);

        LogEventPackMessage logEventPackMessage = buildTestMessage(LOG_SCHEMA_VERSION);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

        verify(logAppender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void proccessLogSchemaVersionNotSupported() throws Exception {
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(context, APP_TOKEN);

        LogEventPackMessage logEventPackMessage = buildTestMessage(LOG_SCHEMA_VERSION, logSchema);
        when(logAppender.isSchemaVersionSupported(1)).thenReturn(Boolean.FALSE);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

        verify(logAppender, Mockito.never()).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void proccessLogSchemaVersionLogShemasNoSchemasTest() throws Exception {
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(context, APP_TOKEN);

        LogEventPackMessage logEventPackMessage = buildTestMessage(LOG_SCHEMA_VERSION);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

        verify(cacheService).getProfileSchemaByAppAndVersion(new AppVersionKey(APP_TOKEN, CLIENT_SCHEMA_VERSION));
        verify(logAppenderService).getLogSchema(APPLICATION_ID, LOG_SCHEMA_VERSION);
        verify(logAppender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void processAddLogAppenderNotificationTest() {
        LogAppender mockAppender = mock(LogAppender.class);
        when(mockAppender.getName()).thenReturn("flume");
        when(mockAppender.getAppenderId()).thenReturn(APPENDER_ID);
        when(mockAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);

        Notification notification = new Notification();
        notification.setAppenderId(APPENDER_ID);
        notification.setAppId(APPLICATION_ID);
        notification.setOp(Operation.ADD_LOG_APPENDER);

        logAppenders.clear();
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(context, APP_TOKEN);

        when(logAppenderService.getApplicationAppender(APPENDER_ID)).thenReturn(mockAppender);
        applicationLogActorMessageProcessor.processLogAppenderNotification(notification);

        LogEventPackMessage logEventPackMessage = buildTestMessage(LOG_SCHEMA_VERSION, logSchema);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

        verify(mockAppender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void processUpdateLogAppenderNotificationTest() {
        LogEventPackMessage logEventPackMessage = buildTestMessage(LOG_SCHEMA_VERSION, logSchema);

        LogAppender mockAppender = mock(LogAppender.class);
        when(mockAppender.getName()).thenReturn("flume");
        when(mockAppender.getAppenderId()).thenReturn(APPENDER_ID);
        // new appender supports current log schema
        when(mockAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);
        // old appender does not support current log schema
        when(logAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.FALSE);

        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(context, APP_TOKEN);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);
        // check that log pack is not processed
        verify(logAppender, Mockito.never()).doAppend(Mockito.any(BaseLogEventPack.class), Mockito.any(LogDeliveryCallback.class));

        Notification notification = new Notification();
        notification.setAppenderId(APPENDER_ID);
        notification.setAppId(APPLICATION_ID);
        notification.setOp(Operation.UPDATE_LOG_APPENDER);

        when(logAppenderService.getApplicationAppender(APPENDER_ID)).thenReturn(mockAppender);
        applicationLogActorMessageProcessor.processLogAppenderNotification(notification);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);
        // check that log pack is processed
        verify(mockAppender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void processRemoveLogAppenderNotificationTest() {
        LogEventPackMessage logEventPackMessage = buildTestMessage(LOG_SCHEMA_VERSION, logSchema);

        LogAppender mockAppender = mock(LogAppender.class);
        when(mockAppender.getName()).thenReturn("flume");
        when(mockAppender.getAppenderId()).thenReturn(APPENDER_ID);
        // new appender supports current log schema
        when(logAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);

        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(context, APP_TOKEN);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);
        // check that log pack is not processed
        verify(logAppender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));

        Notification notification = new Notification();
        notification.setAppenderId(APPENDER_ID);
        notification.setAppId(APPLICATION_ID);
        notification.setOp(Operation.REMOVE_LOG_APPENDER);

        when(logAppenderService.getApplicationAppender(APPENDER_ID)).thenReturn(mockAppender);
        applicationLogActorMessageProcessor.processLogAppenderNotification(notification);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);
        // check that log pack is processed
        verify(mockAppender, Mockito.never()).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    public LogEventPackMessage buildTestMessage(int logSchemaVersion) {
        return buildTestMessage(logSchemaVersion, null);
    }

    public LogEventPackMessage buildTestMessage(int logSchemaVersion, LogSchema logSchema) {
        return new LogEventPackMessage(REQUEST_ID, ActorRef.noSender(), getTestPack(logSchemaVersion, logSchema));
    }

    public BaseLogEventPack getTestPack(int logSchemaVersion, LogSchema logSchema) {
        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", "EndpointKey", CLIENT_SCHEMA_VERSION, "",
                SERVER_SCHEMA_VERSION, "");
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, System.currentTimeMillis(), logSchemaVersion,
                new ArrayList<LogEvent>());
        logEventPack.setLogSchema(logSchema);
        return logEventPack;
    }
}
