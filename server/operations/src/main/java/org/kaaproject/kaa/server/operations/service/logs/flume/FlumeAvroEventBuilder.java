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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.kaaproject.kaa.server.common.flume.shared.avro.gen.LogData;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FlumeAvroEventBuilder extends FlumeEventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeAvroEventBuilder.class);

    @Override
    public Event generateEvent(String appToken, int schemaVersion, List<LogEvent> logEvents) {
        LOG.debug("Build flume event with appToken [{}], schema version [{}] and events [{}].", appToken, schemaVersion, logEvents);

        Event event = null;
        LogData logData = new LogData();
        logData.setSchemaVersion(schemaVersion);
        logData.setApplicationToken(appToken);

        List<ByteBuffer> bytes = new ArrayList<>(logEvents.size());

        for (LogEvent logEvent : logEvents) {
            bytes.add(ByteBuffer.wrap(logEvent.getLogData()));
        }

        logData.setLogEvents(bytes);
        EncoderFactory factory = EncoderFactory.get();

        GenericDatumWriter<LogData> writer = new GenericDatumWriter<>(logData.getSchema());
        LOG.debug("Convert load data [{}] to bytes.", logData);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        factory.binaryEncoder(baos, null);
        BinaryEncoder encoder = factory.binaryEncoder(baos, null);
        try {
            writer.write(logData, encoder);
            encoder.flush();
            event = EventBuilder.withBody(baos.toByteArray());
        } catch (IOException e) {
            LOG.warn("Can't convert avro object {} to binary.", logData);
        }
        LOG.trace("Build flume event with array body [{}]", baos);
        return event;
    }

}
