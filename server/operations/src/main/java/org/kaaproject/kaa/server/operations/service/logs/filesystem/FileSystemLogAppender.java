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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.kaaproject.kaa.server.operations.service.logs.filesystem.loggers.FileSystemLogger;
import org.kaaproject.kaa.server.operations.service.logs.filesystem.loggers.LogbackFileSystemLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import scala.collection.mutable.StringBuilder;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class FileSystemLogAppender extends AbstractLogAppender {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogAppender.class);

    @Autowired
    private FileSystemLogEventService fileSystemLogEventService;
    @Autowired
    private FileSystemLogger logger;
    
    @Value("#{properties[logs_root_dir]}")
    private String logsRootPath;

    private String tenantDirName;
    private String applicationDirName;

    private boolean closed = false;

    public FileSystemLogAppender() {
        logger = new LogbackFileSystemLogger();
    }

    public FileSystemLogAppender(String name) {
        this();
        setName(name);
    }

    @Override
    public void close() {
        closed = true;
        if (logger != null) {
            try {
                logger.close();
            } catch (IOException e) {
                LOG.warn("IO Exception");
            }
        }
        LOG.debug("Stoped filesystem log appender.");
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            String path = logsRootPath + "/" + tenantDirName + "/" + applicationDirName;
            LOG.debug("[{}] appending {} logs to directory", path, logEventPack.getEvents().size());
            List<String> dtos = eventsToStrings(generateLogEvent(logEventPack, header));
            LOG.debug("[{}] saving {} objects", path, dtos.size());
            for(String event : dtos){
                logger.append(event);
            }
            LOG.debug("[{}] appended {} logs to directory", path, logEventPack.getEvents().size());
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    protected List<String> eventsToStrings(List<LogEventDto> dtos) {
        List<String> events = new ArrayList<>();
        for (LogEventDto logEventDto : dtos) {
            String event = new StringBuilder("{\"Log Header\": \"").append(logEventDto.getHeader())
                    .append("\", \"Event\": ").append(logEventDto.getEvent()).append("}").toString();
            events.add(event);
        }
        return events;
    }

    @Override
    public void initLogAppender(LogAppenderDto appenderDto) {
        LOG.debug("Starting initialize new instance of file system log appender");
        setName(appenderDto.getName());
        initLogDirectories(appenderDto);
        logger.init(appenderDto, Paths.get(logsRootPath, tenantDirName, applicationDirName, "application.log"));
        fileSystemLogEventService.createUserAndGroup(appenderDto,
                Paths.get(logsRootPath, tenantDirName, applicationDirName).toAbsolutePath().toString());
    }

    private void createTenantLogDirectory(String tenantId) {
        tenantDirName = "tenant_" + tenantId;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName);
    }

    private void createApplicationLogDirectory(String applicationId) {
        applicationDirName = "application_" + applicationId;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName + "/" + applicationDirName);
    }

    private void initLogDirectories(LogAppenderDto appender) {
        fileSystemLogEventService.createRootLogDirCommand(logsRootPath);
        createTenantLogDirectory(appender.getTenantId());
        createApplicationLogDirectory(appender.getApplicationId());
    }
}
