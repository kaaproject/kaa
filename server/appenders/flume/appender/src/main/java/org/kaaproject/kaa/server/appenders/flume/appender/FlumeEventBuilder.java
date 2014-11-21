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

import java.util.List;

import org.apache.flume.Event;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Interface FlumeEventBuilder.
 */
public abstract class FlumeEventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeEventBuilder.class);

    public abstract Event generateEvent(String appToken, int schemaVersion, List<LogEvent> logEvents, RecordHeader header);

    /**
     * This method generate flume event from own data structure <code>LogEventPack</code>
     *
     * @param eventPack the event pack
     * @param header the record header
     * @param appToken the application token
     * @return the build flume event
     */
    public Event generateEvent(LogEventPack eventPack, RecordHeader header, String appToken) {
        LOG.debug("Build flume event object from LogEventPack object {}", eventPack);
        Event event = null;
        LogSchema schema = eventPack.getLogSchema();
        if (schema != null) {
            List<LogEvent> events = eventPack.getEvents();
            if (events != null && !events.isEmpty()) {
                event = generateEvent(appToken, schema.getVersion(), events, header);
            } else {
                LOG.warn("Can't build flume event. Empty Log events.");
            }
        } else {
            LOG.warn("Can't build flume event. LogSchema object is null.");
        }
        return event;
    }

}
