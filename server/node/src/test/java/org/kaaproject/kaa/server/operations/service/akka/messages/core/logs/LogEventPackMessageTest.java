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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.logs;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;

import akka.actor.ActorRef;

public class LogEventPackMessageTest {

    private static final int REQUEST_ID = 42;
    private static final String ENDPOINT_KEY = "endpointKey";
    private static final long DATE_CREATED = System.currentTimeMillis();
    private static final int LOG_SCHEMA_VERSION = 3;
    private static final LogSchema LOG_SCHEMA = new LogSchema(null, "");
    private static final List<LogEvent> EVENTS = new ArrayList<>();

    @Test
    public void nullLogEventPackTest() {
        LogEventPackMessage logEvent = new LogEventPackMessage(REQUEST_ID, ActorRef.noSender(), null);

        Assert.assertNull(logEvent.getLogEventPack());
    }

    @Test
    public void logEventPackTest() {
        BaseLogEventPack logEventPack1 = new BaseLogEventPack(null, System.currentTimeMillis(), 1, new ArrayList<LogEvent>());

        BaseLogEventPack logEventPack2 = new BaseLogEventPack(null, System.currentTimeMillis(), 2, new ArrayList<LogEvent>());

        LogEventPackMessage logEvent = new LogEventPackMessage(REQUEST_ID, ActorRef.noSender(), logEventPack1);

        Assert.assertEquals(logEventPack1, logEvent.getLogEventPack());
        Assert.assertNotEquals(logEventPack2, logEvent.getLogEventPack());
    }

    @Test
    public void logEventPackDataTest() {
        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", ENDPOINT_KEY, 1, "", 0, "");
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, DATE_CREATED, LOG_SCHEMA_VERSION, EVENTS);
        logEventPack.setLogSchema(LOG_SCHEMA);

        LogEventPackMessage logEvent = new LogEventPackMessage(REQUEST_ID, ActorRef.noSender(), logEventPack);

        Assert.assertEquals(ENDPOINT_KEY, logEvent.getEndpointKey());
        Assert.assertEquals(DATE_CREATED, logEvent.getDateCreated());
        Assert.assertEquals(LOG_SCHEMA_VERSION, logEvent.getLogSchemaVersion());
        Assert.assertEquals(LOG_SCHEMA, logEvent.getLogSchema());
        Assert.assertEquals(EVENTS, logEvent.getEvents());
    }
}
