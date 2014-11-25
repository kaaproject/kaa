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

package org.kaaproject.kaa.server.appenders.flume.appender;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.flume.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;

public class FlumeAvroEventBuilderTest {

    private static final int SCHEMA_VERSION = 10;

    private LogEventPack eventPack;
    private FlumeEventBuilder eventBuilder;
    private String appToken;

    @Before
    public void init() {
        appToken = RandomStringUtils.randomNumeric(15);
        eventPack = new LogEventPack();
        List<LogEvent> list = generateEvents();

        eventPack.setEvents(list);
        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setMajorVersion(SCHEMA_VERSION);
        LogSchema logSchema = new LogSchema(schemaDto);
        eventPack.setLogSchema(logSchema);
        eventPack.setLogSchemaVersion(SCHEMA_VERSION);
    }

    @Test
    public void generateFromAvroEventWithEmptyLogSchemaTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        eventPack.setLogSchema(null);
        Event event = eventBuilder.generateEvent(eventPack, null, appToken);
        Assert.assertNull(event);
    }

    @Test
    public void generateFromAvroEventWithEmptyLogEventsTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        eventPack.setEvents(new ArrayList<LogEvent>());
        Event event = eventBuilder.generateEvent(eventPack, null, appToken);
        Assert.assertNull(event);
    }

    @Test
    public void generateFromAvroEventTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        Event event = eventBuilder.generateEvent(eventPack, null, appToken);
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
