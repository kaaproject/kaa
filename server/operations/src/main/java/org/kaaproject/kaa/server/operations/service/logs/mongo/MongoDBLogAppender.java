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

package org.kaaproject.kaa.server.operations.service.logs.mongo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.LogEventService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class MongoDBLogAppender implements LogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBLogAppender.class);

    Map<String, ThreadLocal<GenericAvroConverter<GenericRecord>>> converters = new HashMap<>();

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LogEventService logEventService;

    private String appenderId;
    private String name;
    private String collectionName;

    private boolean closed = false;

    public MongoDBLogAppender() {
    }

    public MongoDBLogAppender(String name) {
        this.name = name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public String getAppenderId() {
        return appenderId;
    }

    public void setAppenderId(String appenderId) {
        this.appenderId = appenderId;
    }

    private GenericAvroConverter<GenericRecord> getConverter(String schema) {
        if (!converters.containsKey(schema)) {
            ThreadLocal<GenericAvroConverter<GenericRecord>> converter = new ThreadLocal<GenericAvroConverter<GenericRecord>>();
            converter.set(new GenericAvroConverter<GenericRecord>(schema));
            converters.put(schema, converter);
        }
        return converters.get(schema).get();
    }

    @Override
    public void doAppend(LogEventPack logEventPack) {
        if (!closed) {
            LOG.debug("[{}] appending {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
            List<LogEventDto> dtos = new ArrayList<>(logEventPack.getEvents().size());
            GenericAvroConverter<GenericRecord> converter = getConverter(logEventPack.getLogSchema().getSchema());
            try {
                for (LogEvent logEvent : logEventPack.getEvents()) {
                    GenericRecord decodedLog = converter.decodeBinary(logEvent.getLogData());
                    String encodedJsonLog = converter.endcodeToJson(decodedLog);
                    dtos.add(new LogEventDto(
                            logEventPack.getEndpointKey(),
                            logEventPack.getDateCreated(),
                            encodedJsonLog));
                }
            } catch (IOException e) {
                LOG.error("Unexpected IOException while decoding LogEvents", e);
            }
            LOG.debug("[{}] saving {} objects", collectionName, dtos.size());
            logEventService.save(dtos, collectionName);
            LOG.debug("[{}] appended {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", name);
        }
    }

    @Override
    public void init(LogAppenderDto appender) {
        createCollection(appender.getApplicationToken());
    }

    private void createCollection(String applicationToken) {
        if (collectionName == null) {
            collectionName = "logs_" + applicationToken;
            logEventService.createCollection(collectionName);
        } else {
            LOG.error("Appender is already initialized..");
        }
    }

}
