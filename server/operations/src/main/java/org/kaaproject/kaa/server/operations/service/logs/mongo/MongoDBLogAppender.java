package org.kaaproject.kaa.server.operations.service.logs.mongo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.LogEventService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MongoDBLogAppender implements LogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBLogAppender.class);

    Map<String, ThreadLocal<GenericAvroConverter<GenericRecord>>> converters = new HashMap<>();

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LogEventService logEventService;

    private String name;
    private String collectionName;

    private boolean closed = false;

    public MongoDBLogAppender(String name) {
        this.name = name;
    }

    @Override
    public void setTenantId(String tenantId) {
    }

    @Override
    public void setApplicationId(String applicationId) {
        if (collectionName == null) {
            collectionName = "logs_" + applicationService.findAppById(applicationId).getApplicationToken();
            logEventService.createCollection(collectionName);
        } else {
            LOG.error("Appender is already initialized..");
        }
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
            LOG.info("Can't append logs caused by Appender closed state is [{}] ", closed);
        }
    }

    @Override
    public LogAppender copy() {
        MongoDBLogAppender copy = new MongoDBLogAppender(name);
        copy.applicationService = this.applicationService;
        copy.logEventService = this.logEventService;
        return copy;
    }

    @Override
    public void init(String logAppenderName, String tenantId, String applicationId) {
        setName(logAppenderName);
        setApplicationId(applicationId);
    }

}
