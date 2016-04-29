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

import java.util.List;

import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;

import akka.actor.ActorRef;

/**
 * The Class LogEventPackMessage.
 */
public class LogEventPackMessage {

    private final int requestId;

    private final ActorRef originator;

    /** Log Event Pack. */
    private final BaseLogEventPack logEventPack;

    /**
     * Instantiates a new log event pack message.
     *
     * @param requestId     the request id
     * @param originator    the originator
     * @param logEventPack  the log event pack
     */
    public LogEventPackMessage(int requestId, ActorRef originator, BaseLogEventPack logEventPack) {
        this.requestId = requestId;
        this.originator = originator;
        this.logEventPack = logEventPack;
    }

    public int getRequestId() {
        return requestId;
    }

    public ActorRef getOriginator() {
        return originator;
    }

    public BaseLogEventPack getLogEventPack() {
        return logEventPack;
    }

    public String getEndpointKey() {
        return logEventPack.getEndpointKey();
    }

    public long getDateCreated() {
        return logEventPack.getDateCreated();
    }

    public int getLogSchemaVersion() {
        return logEventPack.getLogSchemaVersion();
    }

    public List<LogEvent> getEvents() {
        return logEventPack.getEvents();
    }

    public LogSchema getLogSchema() {
        return logEventPack.getLogSchema();
    }
}
