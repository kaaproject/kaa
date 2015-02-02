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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.mockito.Mockito;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class ApplicationLogActorTest {

    private static final int REQUEST_ID = 42;

    private static final int TEST_SCHEMA_VERSION = 1;

    private ApplicationLogActorMessageProcessor applicationLogActorMessageProcessor;

    private LogAppenderService logAppenderService;
    private ApplicationService applicationService;
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
        logAppenderService = mock(LogAppenderService.class);
        applicationService = mock(ApplicationService.class);
        applicationDto = mock(ApplicationDto.class);

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setMajorVersion(TEST_SCHEMA_VERSION);
        logSchema = new LogSchema(logSchemaDto);

        logAppenders = new ArrayList<>();
        logAppender = mock(LogAppender.class);
        logAppenders.add(logAppender);

        when(logAppenderService.getLogSchema(APPLICATION_ID, LOG_SCHEMA_VERSION)).thenReturn(logSchema);
        when(applicationService.findAppByApplicationToken(APP_TOKEN)).thenReturn(applicationDto);
        when(applicationDto.getId()).thenReturn(APPLICATION_ID);
        when(logAppenderService.getApplicationAppenders(APPLICATION_ID)).thenReturn(logAppenders);
        when(logAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);
    }

    @Test
    public void proccessLogSchemaVersionMessageHaveLogSchemaTest() throws Exception {
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(logAppenderService, applicationService, APP_TOKEN);

        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);
        when(logEventPackMessage.getLogSchema()).thenReturn(logSchema);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

        verify(logAppender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void proccessLogSchemaVersionNotSupported() throws Exception {
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(logAppenderService, applicationService, APP_TOKEN);

        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);
        when(logAppender.isSchemaVersionSupported(1)).thenReturn(Boolean.FALSE);
        when(logEventPackMessage.getLogSchema()).thenReturn(logSchema);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());
        when(logEventPackMessage.getOriginator()).thenReturn(ActorRef.noSender());
        when(logEventPackMessage.getRequestId()).thenReturn(REQUEST_ID);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

        verify(logAppender, Mockito.never()).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void proccessLogSchemaVersionLogShemasNoSchemaTest() throws Exception {
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(logAppenderService, applicationService, APP_TOKEN);

        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);
        when(logEventPackMessage.getLogSchemaVersion()).thenReturn(LOG_SCHEMA_VERSION);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

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
        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(logAppenderService, applicationService, APP_TOKEN);

        when(logAppenderService.getApplicationAppender(APPENDER_ID)).thenReturn(mockAppender);
        applicationLogActorMessageProcessor.processLogAppenderNotification(notification);

        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);
        when(logEventPackMessage.getLogSchema()).thenReturn(logSchema);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);

        verify(mockAppender).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));
    }

    @Test
    public void processUpdateLogAppenderNotificationTest() {
        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);
        when(logEventPackMessage.getLogSchema()).thenReturn(logSchema);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());
        when(logEventPackMessage.getOriginator()).thenReturn(ActorRef.noSender());
        when(logEventPackMessage.getRequestId()).thenReturn(REQUEST_ID);

        LogAppender mockAppender = mock(LogAppender.class);
        when(mockAppender.getName()).thenReturn("flume");
        when(mockAppender.getAppenderId()).thenReturn(APPENDER_ID);
        // new appender supports current log schema
        when(mockAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);
        // old appender does not support current log schema
        when(logAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.FALSE);

        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(logAppenderService, applicationService, APP_TOKEN);

        applicationLogActorMessageProcessor.processLogEventPack(Mockito.mock(ActorContext.class), logEventPackMessage);
        // check that log pack is not processed
        verify(logAppender, Mockito.never()).doAppend(Mockito.any(LogEventPack.class), Mockito.any(LogDeliveryCallback.class));

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
        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);
        when(logEventPackMessage.getLogSchema()).thenReturn(logSchema);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());

        LogAppender mockAppender = mock(LogAppender.class);
        when(mockAppender.getName()).thenReturn("flume");
        when(mockAppender.getAppenderId()).thenReturn(APPENDER_ID);
        // new appender supports current log schema
        when(logAppender.isSchemaVersionSupported(Mockito.anyInt())).thenReturn(Boolean.TRUE);

        applicationLogActorMessageProcessor = new ApplicationLogActorMessageProcessor(logAppenderService, applicationService, APP_TOKEN);

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

}
