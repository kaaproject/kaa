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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.flume.Event;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.kaaproject.kaa.server.operations.service.logs.LogSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Interface FlumeEventBuilder.
 */
public abstract class FlumeEventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeEventBuilder.class);

    private Map<String, String> applications = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationService applicationService;

    public abstract Event generateEvent(String appToken, int schemaVersion, List<LogEvent> logEvents);

    /**
     * This method generate flume event from own data structure <code>LogEventPack</code>
     *
     * @param eventPack the event pack
     * @return the build flume event
     */
    public Event generateEvent(LogEventPack eventPack) {
        LOG.debug("Build flume event object from LogEventPack object {}", eventPack);
        Event event = null;
        if (eventPack != null) {
            LogSchema schema = eventPack.getLogSchema();
            if (schema != null) {
                List<LogEvent> events = eventPack.getEvents();
                if (events != null && !events.isEmpty()) {
                    String appId = schema.getApplicationId();
                    String token = getAppToken(appId);
                    LOG.debug("Fetched application token {} for application id {}", token, appId);
                    event = generateEvent(token, schema.getVersion(), eventPack.getEvents());
                } else {
                    LOG.warn("Can't build flume event. Empty Log events.");
                }
            } else {
                LOG.warn("Can't build flume event. LogSchema object is null.");
            }
        } else {
            LOG.warn("Can't build flume event. LogEventPack object is null.");
        }
        return event;
    }

    private String getAppToken(String appId) {
        String token = applications.get(appId);
        if (token == null) {
            ApplicationDto app = applicationService.findAppById(appId);
            if (app != null) {
                token = app.getApplicationToken();
                applications.put(appId, token);
            }
        }
        return token;
    }

}
