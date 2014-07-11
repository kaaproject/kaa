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

package org.kaaproject.kaa.server.operations.service.logs;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

public class LogEventPackTest { 
    
    private static final String ENDPOINT_KEY = "endpoint key";
    private static final long DATE_CREATED = System.currentTimeMillis();  
    private static final LogSchema LOG_SCHEMA = new LogSchema(new LogSchemaDto());
    private static final int VERSION = 3;
    private static final List<LogEvent> EVENTS = new ArrayList<>();
    
    @Test
    public void basicLogEventPackTest() {        
        LogEventPack logEventPack1 = new LogEventPack(ENDPOINT_KEY, DATE_CREATED, LOG_SCHEMA, EVENTS);
        logEventPack1.setLogSchemaVersion(VERSION);
        LogEventPack logEventPack2 = new LogEventPack();
        
        Assert.assertEquals(ENDPOINT_KEY, logEventPack1.getEndpointKey());
        Assert.assertEquals(DATE_CREATED, logEventPack1.getDateCreated());
        Assert.assertEquals(LOG_SCHEMA, logEventPack1.getLogSchema());
        Assert.assertEquals(VERSION, logEventPack1.getLogSchemaVersion());
        Assert.assertEquals(EVENTS, logEventPack1.getEvents());
        
        Assert.assertNull(logEventPack2.getEndpointKey());
        Assert.assertEquals(0L, logEventPack2.getDateCreated());
        Assert.assertNull(logEventPack2.getLogSchema());
        Assert.assertEquals(0, logEventPack2.getLogSchemaVersion());
        Assert.assertNull(logEventPack2.getEvents());
        
        logEventPack2.setEndpointKey(ENDPOINT_KEY);
        logEventPack2.setDateCreated(DATE_CREATED);
        logEventPack2.setLogSchema(LOG_SCHEMA);
        logEventPack2.setLogSchemaVersion(VERSION);
        logEventPack2.setEvents(EVENTS);  
        
        Assert.assertEquals(logEventPack1.getEndpointKey(), logEventPack2.getEndpointKey());
        Assert.assertEquals(logEventPack1.getDateCreated(), logEventPack2.getDateCreated());
        Assert.assertEquals(logEventPack1.getLogSchema(), logEventPack2.getLogSchema());
        Assert.assertEquals(logEventPack1.getLogSchemaVersion(), logEventPack2.getLogSchemaVersion());
        Assert.assertEquals(logEventPack1.getEvents(), logEventPack2.getEvents());
    }
}
