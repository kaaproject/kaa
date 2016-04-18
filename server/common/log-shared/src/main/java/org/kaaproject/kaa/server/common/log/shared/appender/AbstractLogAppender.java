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

package org.kaaproject.kaa.server.common.log.shared.appender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LogAppender.
 */
public abstract class AbstractLogAppender<T extends SpecificRecordBase> implements LogAppender {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLogAppender.class);

    /** The Constant LOG_HEADER_VERSION. */
    private static final int LOG_HEADER_VERSION = 1;

    /** The appender id. */
    private String appenderId;

    /** The name. */
    private String name;

    /** The application token. */
    private String applicationToken;

    /** The header. */
    private List<LogHeaderStructureDto> header;

    private int minSchemaVersion, maxSchemaVersion;

    private boolean confirmDelivery;

    /** The converters. */
    Map<String, GenericAvroConverter<GenericRecord>> converters = new HashMap<>();

    private final Class<T> configurationClass;

    public AbstractLogAppender(Class<T> configurationClass) {
        this.configurationClass = configurationClass;
    }

    /**
     * Log in <code>LogAppender</code> specific way.
     * 
     * @param logEventPack  the pack of Log Events
     * @param header        the header
     * @param listener      the listener
     */
    public abstract void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener);

    /**
     * Change parameters of log appender.
     * 
     * @param appender      the appender
     * @param configuration the configuration
     */

    protected abstract void initFromConfiguration(LogAppenderDto appender, T configuration);

    public void initLogAppender(LogAppenderDto appender) {
        this.minSchemaVersion = appender.getMinLogSchemaVersion();
        this.maxSchemaVersion = appender.getMaxLogSchemaVersion();
        this.confirmDelivery = appender.isConfirmDelivery();
        byte[] rawConfiguration = appender.getRawConfiguration();
        try {
            AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(configurationClass);
            T configuration = converter.fromByteArray(rawConfiguration);
            initFromConfiguration(appender, configuration);
        } catch (IOException e) {
            LOG.error("Unable to parse configuration for appender '" + getName() + "'", e);
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
    public String getAppenderId() {
        return appenderId;
    }

    @Override
    public void setAppenderId(String appenderId) {
        this.appenderId = appenderId;
    }

    /**
     * Gets the application token.
     * 
     * @return the applicationToken
     */
    public String getApplicationToken() {
        return applicationToken;
    }

    @Override
    public void setApplicationToken(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    /**
     * Gets the header.
     * 
     * @return the header
     */
    public List<LogHeaderStructureDto> getHeader() {
        return header;
    }

    /**
     * Sets the header.
     * 
     * @param header
     *            the new header
     */
    public void setHeader(List<LogHeaderStructureDto> header) {
        this.header = header;
    }

    @Override
    public void init(LogAppenderDto appender) {
        this.header = appender.getHeaderStructure();
        initLogAppender(appender);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, LogDeliveryCallback listener) {
        if (logEventPack != null) {
            doAppend(logEventPack, generateHeader(logEventPack), listener);
        } else {
            LOG.warn("Can't append log events. LogEventPack object is null.");
        }
    }

    @Override
    public boolean isSchemaVersionSupported(int version) {
        return minSchemaVersion <= version && version <= maxSchemaVersion;
    }

    @Override
    public boolean isDeliveryConfirmationRequired() {
        return confirmDelivery;
    }

    /**
     * Generate log event.
     * 
     * @param logEventPack
     *            the log event pack
     * @param header
     *            the header
     * @return the list
     * @throws IOException the io exception
     */
    protected List<LogEventDto> generateLogEvent(LogEventPack logEventPack, RecordHeader header) throws IOException {
        LOG.debug("Generate LogEventDto objects from LogEventPack [{}] and header [{}]", logEventPack, header);
        List<LogEventDto> events = new ArrayList<>(logEventPack.getEvents().size());
        GenericAvroConverter<GenericRecord> eventConverter = getConverter(logEventPack.getLogSchema().getSchema());
        GenericAvroConverter<GenericRecord> headerConverter = getConverter(header.getSchema().toString());
        try {
            for (LogEvent logEvent : logEventPack.getEvents()) {
                LOG.debug("Convert log events [{}] to dto objects.", logEvent);
                if (logEvent == null | logEvent.getLogData() == null) {
                    continue;
                }
                LOG.trace("Avro record converter [{}] with log data [{}]", eventConverter, logEvent.getLogData());
                GenericRecord decodedLog = eventConverter.decodeBinary(logEvent.getLogData());
                LOG.trace("Avro header record converter [{}]", headerConverter);
                String encodedJsonLogHeader = headerConverter.encodeToJson(header);
                String encodedJsonLog = eventConverter.encodeToJson(decodedLog);
                events.add(new LogEventDto(encodedJsonLogHeader, encodedJsonLog));
            }
        } catch (IOException e) {
            LOG.error("Unexpected IOException while decoding LogEvents", e);
            throw e;
        }
        return events;
    }

    /**
     * Gets the converter.
     * 
     * @param schema
     *            the schema
     * @return the converter
     */
    private GenericAvroConverter<GenericRecord> getConverter(String schema) {
        LOG.trace("Get converter for schema [{}]", schema);
        GenericAvroConverter<GenericRecord> genAvroConverter = converters.get(schema);
        if (genAvroConverter == null) {
            LOG.trace("Create new converter for schema [{}]", schema);
            genAvroConverter = new GenericAvroConverter<GenericRecord>(schema);
            converters.put(schema, genAvroConverter);
        }
        LOG.trace("Get converter [{}] from map.", genAvroConverter);
        return genAvroConverter;
    }

    /**
     * Generate header.
     * 
     * @param logEventPack
     *            the log event pack
     * @return the log header
     */
    private RecordHeader generateHeader(LogEventPack logEventPack) {
        RecordHeader logHeader = null;
        if (header != null) {
            logHeader = new RecordHeader();
            for (LogHeaderStructureDto field : header) {
                switch (field) {
                case KEYHASH:
                    logHeader.setEndpointKeyHash(logEventPack.getEndpointKey());
                    break;
                case TIMESTAMP:
                    logHeader.setTimestamp(System.currentTimeMillis());
                    break;
                case TOKEN:
                    logHeader.setApplicationToken(applicationToken);
                    break;
                case VERSION:
                    logHeader.setHeaderVersion(LOG_HEADER_VERSION);
                    break;
                case LSVERSION:
                    logHeader.setLogSchemaVersion(logEventPack.getLogSchema().getVersion());
                    break;
                default:
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Current header field [{}] doesn't support", field);
                    }
                    break;
                }
            }
        }
        return logHeader;
    }

}
