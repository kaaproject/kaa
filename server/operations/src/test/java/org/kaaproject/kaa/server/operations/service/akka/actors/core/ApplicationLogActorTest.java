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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Operation;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.kaaproject.kaa.server.operations.service.logs.LogSchema;
import org.kaaproject.kaa.server.operations.service.logs.flume.FlumeLogAppender;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("unchecked")
public class ApplicationLogActorTest {

    private ApplicationLogActor applicationLogActor;

    private LogAppenderService logAppenderService;

    private Map<Integer, LogSchema> logSchemas;
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
        applicationLogActor = mock(ApplicationLogActor.class);

        logSchema = new LogSchema(new LogSchemaDto());
        logSchemas = new HashMap<>();
        logAppenders = new ArrayList<>();
        logAppender = mock(LogAppender.class);
        ReflectionTestUtils.setField(applicationLogActor, "applicationToken", APP_TOKEN);

        ReflectionTestUtils.setField(applicationLogActor, "logSchemas", logSchemas);
        ReflectionTestUtils.setField(applicationLogActor, "logAppenders", logAppenders);
        ReflectionTestUtils.setField(applicationLogActor, "logAppenderService", logAppenderService);
    }


    @Test
    public void proccessLogSchemaVersionMessageHaveLogSchemaTest() throws Exception {
        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);

        logAppenders.add(logAppender);

        when(logEventPackMessage.getLogSchema()).thenReturn(logSchema);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());

        ReflectionTestUtils.invokeMethod(applicationLogActor, "processLogEventPack", logEventPackMessage);

        verify(logAppender).doAppend(logEventPackMessage.getLogEventPack());
    }

    @Test
    public void proccessLogSchemaVersionLogShemasHaveSchemaTest() throws Exception {
        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);

        logSchemas.put(LOG_SCHEMA_VERSION, logSchema);
        logAppenders.add(logAppender);

        when(logEventPackMessage.getLogSchemaVersion()).thenReturn(LOG_SCHEMA_VERSION);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());

        ReflectionTestUtils.invokeMethod(applicationLogActor, "processLogEventPack", logEventPackMessage);

        verify(logAppender).doAppend(logEventPackMessage.getLogEventPack());
    }

    @Test
    public void proccessLogSchemaVersionNoSchemaTest() throws Exception {
        LogEventPackMessage logEventPackMessage = mock(LogEventPackMessage.class);
        ReflectionTestUtils.setField(applicationLogActor, "applicationId", APPLICATION_ID);

        logAppenders.add(logAppender);

        when(logEventPackMessage.getLogSchemaVersion()).thenReturn(LOG_SCHEMA_VERSION);
        when(logEventPackMessage.getLogEventPack()).thenReturn(new LogEventPack());
        when(logAppenderService.getLogSchema(APPLICATION_ID, LOG_SCHEMA_VERSION)).thenReturn(logSchema);

        ReflectionTestUtils.invokeMethod(applicationLogActor, "processLogEventPack", logEventPackMessage);

        verify(logAppender).doAppend(logEventPackMessage.getLogEventPack());
        Assert.assertEquals(1, logSchemas.size());
    }

    @Test

    public void processAddLogAppenderNotificationTest() {
        FlumeLogAppender flumeAppender = new FlumeLogAppender();
        flumeAppender.setName("Flume");
        flumeAppender.setAppenderId(APPENDER_ID);

        Notification notification = new Notification();
        notification.setAppenderId(APPENDER_ID);
        notification.setAppId(APPLICATION_ID);
        notification.setOp(Operation.ADD_LOG_APPENDER);

        List<LogAppender> appenders = mock(List.class);
        ReflectionTestUtils.setField(applicationLogActor, "logAppenders", appenders);
        when(logAppenderService.getApplicationAppender(APPENDER_ID, APP_TOKEN)).thenReturn(flumeAppender);
        ReflectionTestUtils.invokeMethod(applicationLogActor, "processLogAppenderNotification", notification);

        verify(logAppenderService, times(1)).getApplicationAppender(APPENDER_ID, APP_TOKEN);
        verify(appenders, times(1)).add(any(LogAppender.class));
        ReflectionTestUtils.setField(applicationLogActor, "logAppenders", logAppenders);
    }

    @Test
    public void processUpdateLogAppenderNotificationTest() {
        FlumeLogAppender flumeAppender = mock(FlumeLogAppender.class);
        when(flumeAppender.getName()).thenReturn("Flume");
        when(flumeAppender.getAppenderId()).thenReturn(APPENDER_ID);

        Notification notification = new Notification();
        notification.setAppenderId(APPENDER_ID);
        notification.setAppId(APPLICATION_ID);
        notification.setOp(Operation.UPDATE_LOG_APPENDER);

        List<LogAppender> appenders = mock(List.class);
        ReflectionTestUtils.setField(applicationLogActor, "logAppenders", appenders);
        when(appenders.size()).thenReturn(1);
        when(appenders.get(any(Integer.class))).thenReturn(flumeAppender);
        when(logAppenderService.getApplicationAppender(APPENDER_ID, APP_TOKEN)).thenReturn(flumeAppender);

        ReflectionTestUtils.invokeMethod(applicationLogActor, "processLogAppenderNotification", notification);

        verify(logAppenderService, times(1)).getApplicationAppender(APPENDER_ID, APP_TOKEN);
        verify(appenders, times(1)).remove(0);
        verify(appenders, times(1)).add(any(LogAppender.class));

        ReflectionTestUtils.setField(applicationLogActor, "logAppenders", logAppenders);
    }

    @Test
    public void processRemoveLogAppenderNotificationTest() {
        FlumeLogAppender flumeAppender = mock(FlumeLogAppender.class);
        when(flumeAppender.getName()).thenReturn("Flume");
        when(flumeAppender.getAppenderId()).thenReturn(APPENDER_ID);

        Notification notification = new Notification();
        notification.setAppenderId(APPENDER_ID);
        notification.setAppId(APPLICATION_ID);
        notification.setOp(Operation.REMOVE_LOG_APPENDER);

        List<LogAppender> appenders = mock(List.class);
        ReflectionTestUtils.setField(applicationLogActor, "logAppenders", appenders);
        when(appenders.size()).thenReturn(1);
        when(appenders.get(any(Integer.class))).thenReturn(flumeAppender);
        when(logAppenderService.getApplicationAppender(APPENDER_ID, APP_TOKEN)).thenReturn(flumeAppender);

        ReflectionTestUtils.invokeMethod(applicationLogActor, "processLogAppenderNotification", notification);

        verify(appenders, times(1)).remove(0);
        ReflectionTestUtils.setField(applicationLogActor, "logAppenders", logAppenders);
    }

}
