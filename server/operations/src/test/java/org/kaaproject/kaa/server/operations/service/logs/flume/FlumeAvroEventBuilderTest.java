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

package org.kaaproject.kaa.server.operations.service.logs.flume;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.flume.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class FlumeAvroEventBuilderTest {

    private static final String APP_ID = "100";
    private static final int SCHEMA_VERSION = 10;

    private static final int COUNT = 5;

    private LogEventPack eventPack;
    private FlumeEventBuilder eventBuilder;
    private ApplicationDto app;
    private ApplicationService applicationService;
    private String appToken;

    @Before
    public void init() {
        app = new ApplicationDto();
        app.setId(APP_ID);
        appToken = RandomStringUtils.randomNumeric(15);
        app.setApplicationToken(appToken);
        eventPack = new LogEventPack();
        List<LogEvent> list = generateEvents();

        eventPack.setEvents(list);
        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setApplicationId(APP_ID);
        schemaDto.setMajorVersion(SCHEMA_VERSION);
        LogSchema logSchema = new LogSchema(schemaDto);
        eventPack.setLogSchema(logSchema);
        eventPack.setLogSchemaVersion(SCHEMA_VERSION);
        applicationService = mock(ApplicationService.class);
    }

    @Test
    public void generateFromBytesEventTest() {
        eventBuilder = new FlumeBytesEventBuilder();
        ReflectionTestUtils.setField(eventBuilder, "applicationService", applicationService);
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(app);
        Event event = eventBuilder.generateEvent(eventPack, null);
        Assert.assertEquals(FlumeBytesEventBuilder.METADATA_SIZE_IN_BYTES + COUNT * 8, event.getBody().length);
        eventBuilder.generateEvent(eventPack, null);
        Mockito.verify(applicationService, Mockito.times(1)).findAppById(APP_ID);
    }

    @Test
    public void generateFromAvroEventWithEmptyLogSchemaTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        ReflectionTestUtils.setField(eventBuilder, "applicationService", applicationService);
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(app);
        eventPack.setLogSchema(null);
        Event event = eventBuilder.generateEvent(eventPack, null);
        Assert.assertNull(event);
    }

    @Test
    public void generateFromAvroEventWithEmptyLogEventsTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        ReflectionTestUtils.setField(eventBuilder, "applicationService", applicationService);
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(app);
        eventPack.setEvents(new ArrayList<LogEvent>());
        Event event = eventBuilder.generateEvent(eventPack, null);
        Assert.assertNull(event);
    }

    @Test
    public void generateFromAvroEventTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        ReflectionTestUtils.setField(eventBuilder, "applicationService", applicationService);
        Mockito.when(applicationService.findAppById(APP_ID)).thenReturn(app);
        Event event = eventBuilder.generateEvent(eventPack, null);
        Assert.assertNotNull(event);
    }

    private List<LogEvent> generateEvents() {
        List<LogEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LogEvent logEvent = new LogEvent();
            logEvent.setLogData(new byte[] { 1, 2, 3, 4 });
            events.add(logEvent);
        }
        return events;
    }
}
