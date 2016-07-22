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

package org.kaaproject.kaa.server.appenders.flume.appender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.flume.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeConfig;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeEventFormat;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseProfileInfo;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseSchemaInfo;

public class FlumeAvroEventBuilderTest {

    private static final int EVENTS_COUNT = 5;

    private static final int SCHEMA_VERSION = 10;

    private static final String SERVER_PROFILE_SCHEMA_FILE = "server-profile-schema.avsc";
    private static final String SERVER_PROFILE_CONTENT_FILE = "server-profile-content.json";

    private BaseLogEventPack eventPack;
    private FlumeEventBuilder eventBuilder;
    private String appToken;
    private EndpointProfileDataDto profileDto;

    @Before
    public void init() throws IOException {
        appToken = RandomStringUtils.randomNumeric(15);
        profileDto = new EndpointProfileDataDto("1", UUID.randomUUID().toString(), 1, "", 1, "");
        List<LogEvent> list = generateEvents();
        eventPack = generateEventPack(list);
    }

    private BaseLogEventPack generateEventPack(List<LogEvent> list) throws IOException {
        BaseLogEventPack eventPack = new BaseLogEventPack(profileDto, System.currentTimeMillis(), SCHEMA_VERSION, list);
        
        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setVersion(SCHEMA_VERSION);
        LogSchema logSchema = new LogSchema(schemaDto, "");
        eventPack.setLogSchema(logSchema);

        BaseSchemaInfo schemaInfo = new BaseSchemaInfo("1", getResourceAsString(SERVER_PROFILE_SCHEMA_FILE));
        String body = this.getResourceAsString(SERVER_PROFILE_CONTENT_FILE);
        eventPack.setServerProfile(new BaseProfileInfo(schemaInfo, body));


        return eventPack;
    }

    private String getResourceAsString(String path) throws IOException {
                URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        File file = null;
        if (url != null) {
            file = new File(url.getPath());
        }
        String result;
        BufferedReader br = null;
        if (file != null) {
            br = new BufferedReader(new FileReader(file));
        }
        try {
            StringBuilder sb = new StringBuilder();
            String line = null;
            if (br != null) {
                line = br.readLine();
            }
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            result = sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return result;
    }

    @Test
    public void generateFromAvroEventWithEmptyLogSchemaTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        FlumeConfig flumeConfig = new FlumeConfig();
        flumeConfig.setFlumeEventFormat(FlumeEventFormat.RECORDS_CONTAINER);
        flumeConfig.setIncludeClientProfile(false);
        flumeConfig.setIncludeServerProfile(false);
        eventBuilder.init(flumeConfig);
        eventPack.setLogSchema(null);
        List<Event> events = eventBuilder.generateEvents(eventPack, null, appToken);
        Assert.assertNull(events);
    }

    @Test
    public void generateFromAvroEventWithEmptyLogEventsTest() throws IOException {
        eventBuilder = new FlumeAvroEventBuilder();
        FlumeConfig flumeConfig = new FlumeConfig();
        flumeConfig.setFlumeEventFormat(FlumeEventFormat.RECORDS_CONTAINER);
        flumeConfig.setIncludeClientProfile(false);
        flumeConfig.setIncludeServerProfile(false);
        eventBuilder.init(flumeConfig);
        eventPack = generateEventPack(new ArrayList<LogEvent>());
        List<Event> events = eventBuilder.generateEvents(eventPack, null, appToken);
        Assert.assertNull(events);
    }

    @Test
    public void generateFromAvroEventRecordsContainerTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        FlumeConfig flumeConfig = new FlumeConfig();
        flumeConfig.setFlumeEventFormat(FlumeEventFormat.RECORDS_CONTAINER);
        flumeConfig.setIncludeClientProfile(false);
        flumeConfig.setIncludeServerProfile(true);
        eventBuilder.init(flumeConfig);
        List<Event> events = eventBuilder.generateEvents(eventPack, null, appToken);
        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());
    }

    @Test(expected = RuntimeException.class)
    public void generateFromAvroEventRecordsContainerWithoutClientProfileTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        FlumeConfig flumeConfig = new FlumeConfig();
        flumeConfig.setFlumeEventFormat(FlumeEventFormat.RECORDS_CONTAINER);
        flumeConfig.setIncludeClientProfile(true);
        flumeConfig.setIncludeServerProfile(true);
        eventBuilder.init(flumeConfig);
        List<Event> events = eventBuilder.generateEvents(eventPack, null, appToken);
        Assert.assertNull(events);
    }

    @Test
    public void generateFromAvroEventGenericTest() {
        eventBuilder = new FlumeAvroEventBuilder();
        FlumeConfig flumeConfig = new FlumeConfig();
        flumeConfig.setFlumeEventFormat(FlumeEventFormat.GENERIC);
        flumeConfig.setIncludeClientProfile(false);
        flumeConfig.setIncludeServerProfile(false);
        eventBuilder.init(flumeConfig);
        List<Event> events = eventBuilder.generateEvents(eventPack, null, appToken);
        Assert.assertNotNull(events);
        Assert.assertEquals(EVENTS_COUNT, events.size());
    }

    private List<LogEvent> generateEvents() {
        List<LogEvent> events = new ArrayList<>();
        for (int i = 0; i < EVENTS_COUNT; i++) {
            LogEvent logEvent = new LogEvent();
            logEvent.setLogData(new byte[] { 1, 2, 3, 4 });
            events.add(logEvent);
        }
        return events;
    }
}
