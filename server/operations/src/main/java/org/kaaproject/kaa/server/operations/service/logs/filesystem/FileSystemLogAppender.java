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

package org.kaaproject.kaa.server.operations.service.logs.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class FileSystemLogAppender implements LogAppender{

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogAppender.class);

    Map<String, ThreadLocal<GenericAvroConverter<GenericRecord>>> converters = new HashMap<>();

    @Autowired
    private FileSystemLogEventService fileSystemLogEventService;

    private org.apache.log4j.Logger logger;
    private WriterAppender fileAppender;

    private String appenderId;
    private String name;

    @Value("#{properties[date_pattern]}")
    private String datePattern;
    @Value("#{properties[layout_pattern]}")
    private String layoutPattern;
    @Value("#{properties[logs_root_dir]}")
    private String logsRootPath;

    private String tenantDirName;
    private String applicationDirName;

    private boolean closed = false;

    public FileSystemLogAppender() {
    }

    public FileSystemLogAppender(String name) {
        this.name = name;
    }

    @Override
    public String getAppenderId() {
        return appenderId;
    }

    public void setAppenderId(String appenderId) {
        this.appenderId = appenderId;
    }

    public void setTenantId(String tenantId) {
        tenantDirName = "tenant_" + tenantId;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName);
    }

    public void setApplicationId(String applicationId) {
        applicationDirName = "application_" + applicationId;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName + "/" + applicationDirName);
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
        if (fileAppender != null) {
            fileAppender.close();
        }
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
            // TODO Auto-generated method stub
            String path = logsRootPath + "/" + tenantDirName + "/" + applicationDirName;
            LOG.debug("[{}] appending {} logs to directory", path, logEventPack.getEvents().size());
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
            LOG.debug("[{}] saving {} objects", path, dtos.size());
            fileSystemLogEventService.save(dtos, logger, fileAppender);
            LOG.debug("[{}] appended {} logs to directory", path, logEventPack.getEvents().size());
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", name);
        }
    }

    private WriterAppender initAppender(String path) {
        LOG.debug("Starting initialize rolling file appender");
        DailyRollingFileAppender fileAppender = new DailyRollingFileAppender();
        fileAppender.setFile(path);
        fileAppender.setDatePattern(datePattern);
        fileAppender.setAppend(true);
        fileAppender.setLayout(new PatternLayout(layoutPattern));
        fileAppender.activateOptions();
        return fileAppender;
    }

    private org.apache.log4j.Logger initLogger(String name) {
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
        return logger;
    }

    @Override
    public void init(LogAppenderDto appender) {
        LOG.debug("Starting initialize new instance of file system log appender");
        String appId = appender.getApplicationId();

        setName(appender.getName());
        fileSystemLogEventService.createRootLogDirCommand(logsRootPath);

        setTenantId(appender.getTenantId());
        setApplicationId(appId);
        fileAppender = initAppender(logsRootPath + "/" + tenantDirName + "/" + applicationDirName + "/application.log");
        logger = initLogger(applicationDirName);
        fileSystemLogEventService.createUserAndGroup(appId,
                logsRootPath + "/" + tenantDirName + "/" + applicationDirName);
    }

}
