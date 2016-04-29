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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeConfig;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeEventFormat;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordData;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlumeAvroEventBuilder extends FlumeEventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeAvroEventBuilder.class);

    private static final String AVRO_SCHEMA_HEADER_LITERAL = "flume.avro.schema.literal";

    private static final String CLIENT_PROFILE_NOT_SET = "Client profile is not set!";
    private static final String SERVER_PROFILE_NOT_SET = "Server profile is not set!";

    private FlumeEventFormat flumeEventFormat;
    private boolean includeClientProfile;
    private boolean includeServerProfile;

    @Override
    public void init(FlumeConfig configuration) {
        flumeEventFormat = configuration.getFlumeEventFormat();
        includeClientProfile = configuration.getIncludeClientProfile();
        includeServerProfile = configuration.getIncludeServerProfile();
    }

    @Override
    public List<Event> generateEvents(String appToken, LogSchema schema, List<LogEvent> logEvents,
                                      ProfileInfo clientProfile, ProfileInfo serverProfile, RecordHeader header) {
        LOG.debug("Build flume events with appToken [{}], schema version [{}], events: [{}] and header [{}].", appToken, schema.getVersion(), logEvents, header);
        List<Event> events = null;
        switch (flumeEventFormat) {
        case RECORDS_CONTAINER:
            Event event = generateRecordsContainerEvent(appToken, schema, logEvents, clientProfile, serverProfile, header);
            if (event != null) {
                events = Collections.singletonList(event);
            }
            break;
        case GENERIC:
            events = generateGenericEvent(schema, logEvents);
            break;
        default:
            break;
        }
        return events;
    }

    private Event generateRecordsContainerEvent(String appToken, LogSchema schema, List<LogEvent> logEvents,
                                                ProfileInfo clientProfile, ProfileInfo serverProfile, RecordHeader header) {
        if (clientProfile == null && includeClientProfile) {
            LOG.error("Can't  generate records container event. " + CLIENT_PROFILE_NOT_SET);
            throw new RuntimeException(CLIENT_PROFILE_NOT_SET);
        }

        if (serverProfile == null && includeServerProfile) {
            LOG.error("Can't  generate records container event. " + SERVER_PROFILE_NOT_SET);
            throw new RuntimeException(SERVER_PROFILE_NOT_SET);
        }

        Event event = null;
        RecordData logData = new RecordData();
        logData.setSchemaVersion(schema.getVersion());
        logData.setApplicationToken(appToken);
        logData.setRecordHeader(header);

        if (includeClientProfile) {
            if (clientProfile != null) {
                logData.setClientProfileBody(clientProfile.getBody());
                logData.setClientSchemaId(clientProfile.getSchemaId());
            }
        }

        if (includeServerProfile) {
            if (serverProfile != null) {
                logData.setServerProfileBody(serverProfile.getBody());
                logData.setServerSchemaId(serverProfile.getSchemaId());
            }
        }

        List<ByteBuffer> bytes = new ArrayList<>(logEvents.size());

        for (LogEvent logEvent : logEvents) {
            bytes.add(ByteBuffer.wrap(logEvent.getLogData()));
        }

        logData.setEventRecords(bytes);
        EncoderFactory factory = EncoderFactory.get();

        GenericDatumWriter<RecordData> writer = new GenericDatumWriter<>(logData.getSchema());
        LOG.debug("Convert load data [{}] to bytes.", logData);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        factory.binaryEncoder(baos, null);
        BinaryEncoder encoder = factory.binaryEncoder(baos, null);
        try {
            writer.write(logData, encoder);
            encoder.flush();
            event = EventBuilder.withBody(baos.toByteArray());
        } catch (IOException e) {
            LOG.warn("Can't convert avro object {} to binary. Exception catched: {}", logData, e);
        }
        LOG.trace("Build flume event with array body [{}]", baos);
        return event;
    }

    private List<Event> generateGenericEvent(LogSchema schema, List<LogEvent> logEvents) {
        List<Event> events = new ArrayList<>();
        for (LogEvent logEvent : logEvents) {
            Event event = EventBuilder.withBody(logEvent.getLogData());
            event.getHeaders().put(AVRO_SCHEMA_HEADER_LITERAL, schema.getSchema());
            events.add(event);
        }
        return events;
    }
}
