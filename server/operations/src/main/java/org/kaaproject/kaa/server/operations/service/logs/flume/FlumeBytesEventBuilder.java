/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.logs.flume;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlumeBytesEventBuilder extends FlumeEventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeBytesEventBuilder.class);

    public static final int HEADER_SIZE_IN_BYTES = 4;
    public static final int APP_TOKEN_SIZE_IN_BYTES = 8;
    public static final int SCHEMA_VERSION_SIZE_IN_BYTES = 2;
    public static final int EVENT_COUNT_SIZE_IN_BYTES = 4;
    public static final int PAYLOAD_SIZE_IN_BYTES = 4;
    public static final int METADATA_SIZE_IN_BYTES = HEADER_SIZE_IN_BYTES + APP_TOKEN_SIZE_IN_BYTES + SCHEMA_VERSION_SIZE_IN_BYTES + EVENT_COUNT_SIZE_IN_BYTES
            + PAYLOAD_SIZE_IN_BYTES;
    public static final int EMPTY_HEADER = 0;

    @Override
    public Event generateEvent(String appToken, int schemaVersion, List<LogEvent> logEvents, RecordHeader header) {
        int payloadSize = 0;
        List<ByteBuffer> bytes = new ArrayList<>();

        for (LogEvent current : logEvents) {
            byte[] eventData = current.getLogData();
            int eventDataLenght = eventData.length + EVENT_COUNT_SIZE_IN_BYTES;
            bytes.add(getLogEventBuffer(eventDataLenght, eventData));
            payloadSize += eventDataLenght;
        }
        long token = getAppToken(appToken);
        ByteBuffer metaData = generateMetada(token, (short) schemaVersion, logEvents.size(), payloadSize);

        assert metaData.capacity() == METADATA_SIZE_IN_BYTES;
        LOG.trace("Generate metada {}.", Arrays.toString(metaData.array()));

        return getFlumeEvent(payloadSize, metaData, bytes);
    }

    private Event getFlumeEvent(int size, ByteBuffer metadata, List<ByteBuffer> events) {
        ByteBuffer eventPayload = ByteBuffer.allocate(size + METADATA_SIZE_IN_BYTES);
        eventPayload.put(metadata.array());
        LOG.trace("Print payload with metadata {}", Arrays.toString(eventPayload.array()));

        for (ByteBuffer temp : events) {
            eventPayload.put(temp.array());
            LOG.trace("Get log event data {}", Arrays.toString(temp.array()));
            LOG.trace("Print payload with event {}", Arrays.toString(eventPayload.array()));
        }

        return EventBuilder.withBody(eventPayload.array());
    }

    private ByteBuffer generateMetada(long appToken, short schemaVersion, int eventCount, int payloadSize) {
        ByteBuffer metaData = ByteBuffer.allocate(METADATA_SIZE_IN_BYTES);
        metaData.putInt(EMPTY_HEADER);
        metaData.putLong(appToken);
        metaData.putShort(schemaVersion);
        metaData.putInt(eventCount);
        metaData.putInt(payloadSize);
        return metaData;
    }

    private ByteBuffer getLogEventBuffer(int lenght, byte[] body) {
        ByteBuffer event = ByteBuffer.allocate(lenght);
        event.putInt(body.length);
        event.put(body);
        return event;
    }

    private Long getAppToken(String token) {
        Long t = null;
        try {
            t = Long.valueOf(token);
        } catch (NumberFormatException e) {
            LOG.warn("Can't convert string application token {} to Long format.", token);
        }
        return t;
    }

}
