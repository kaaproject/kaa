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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.logs;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;

public class LogEventPackMessageTest {
    
    private static final String ENDPOINT_KEY = "endpointKey";
    private static final long DATE_CREATED = System.currentTimeMillis();
    private static final int LOG_SCHEMA_VERSION = 3;
    private static final LogSchema LOG_SCHEMA = new LogSchema(null);
    private static final List<LogEvent> EVENTS = new ArrayList<>();
    
    @Test
    public void nullLogEventPackTest() {
        LogEventPackMessage logEvent = new LogEventPackMessage(null);
        
        Assert.assertNull(logEvent.getLogEventPack());
    }
    
    @Test
    public void logEventPackTest() {
        LogEventPack logEventPack1 = new LogEventPack();
        
        LogEventPack logEventPack2 = new LogEventPack();
        
        LogEventPackMessage logEvent = new LogEventPackMessage(logEventPack1);
        
        Assert.assertEquals(logEventPack1, logEvent.getLogEventPack());
        Assert.assertNotEquals(logEventPack2, logEvent.getLogEventPack());
    }
    
    @Test
    public void logEventPackDataTest() {
        LogEventPack logEventPack = new LogEventPack(ENDPOINT_KEY, DATE_CREATED, LOG_SCHEMA, EVENTS);
        logEventPack.setLogSchemaVersion(LOG_SCHEMA_VERSION);
        
        LogEventPackMessage logEvent = new LogEventPackMessage(logEventPack);
        
        Assert.assertEquals(ENDPOINT_KEY, logEvent.getEndpointKey());
        Assert.assertEquals(DATE_CREATED, logEvent.getDateCreated());
        Assert.assertEquals(LOG_SCHEMA_VERSION, logEvent.getLogSchemaVersion());
        Assert.assertEquals(LOG_SCHEMA, logEvent.getLogSchema());
        Assert.assertEquals(EVENTS, logEvent.getEvents());
        
        LogSchema logSchema = new LogSchema(new LogSchemaDto());
        
        logEvent.setLogSchema(logSchema);
        
        Assert.assertEquals(logSchema, logEvent.getLogSchema());
        Assert.assertNotEquals(LOG_SCHEMA, logEvent.getLogSchema());
    }
}
