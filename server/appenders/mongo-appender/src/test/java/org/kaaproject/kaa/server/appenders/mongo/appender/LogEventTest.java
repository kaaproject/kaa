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

package org.kaaproject.kaa.server.appenders.mongo.appender;

import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;

public class LogEventTest {
    private static final String KEY = "key";
    private static final String HEADER_VALUE = "value";
    private static final Integer EVENT_VALUE = 5;
    private static final String HEADER = "{\"" + KEY  + "\":\"" + HEADER_VALUE + "\"}";
    private static final String EVENT = "{" + KEY  + ":" + EVENT_VALUE + "}";

    @Test
    public void basicLogEventTest() {
        LogEventDto logEventDto = new LogEventDto(HEADER, EVENT);
        LogEvent logEvent = new LogEvent(logEventDto, null, null);
        DBObject dBHeader = logEvent.getHeader();
        DBObject dbEvent = logEvent.getEvent();
        Assert.assertEquals(HEADER_VALUE, dBHeader.get(KEY));
        Assert.assertEquals(EVENT_VALUE, dbEvent.get(KEY));
    }
}
