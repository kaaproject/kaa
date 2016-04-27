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

package org.kaaproject.kaa.server.appenders.file.appender;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.file.config.gen.FileConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemLogAppender extends AbstractLogAppender<FileConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogAppender.class);

    private FileSystemLogEventService fileSystemLogEventService;
    private FileSystemLogger logger;

    private String logsRootPath;

    private String tenantDirName;
    private String applicationDirName;

    private boolean closed = false;

    public FileSystemLogAppender() {
        super(FileConfig.class);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
        if (!closed) {
            try {
                String path = logsRootPath + "/" + tenantDirName + "/" + applicationDirName;
                LOG.debug("[{}] appending {} logs to directory", path, logEventPack.getEvents().size());
                List<String> dtos = eventsToStrings(generateLogEvent(logEventPack, header));
                LOG.debug("[{}] saving {} objects", path, dtos.size());
                for (String event : dtos) {
                    logger.append(event);
                }
                LOG.debug("[{}] appended {} logs to directory", path, logEventPack.getEvents().size());
                listener.onSuccess();
            } catch (Exception e) {
                LOG.error(MessageFormat.format("[{0}] Attempted to append logs failed", getName()), e);
                listener.onInternalError();
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            listener.onInternalError();
        }
    }

    private List<String> eventsToStrings(List<LogEventDto> dtos) {
        List<String> events = new ArrayList<>();
        for (LogEventDto logEventDto : dtos) {
            String event = new StringBuilder("{\"Log Header\": \"").append(logEventDto.getHeader()).append("\", \"Event\": ")
                    .append(logEventDto.getEvent()).append("}").toString();
            events.add(event);
        }
        return events;
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appenderDto, FileConfig configuration) {
        LOG.debug("Initializing new instance of file system log appender");
        try {
            logsRootPath = configuration.getLogsRootPath();
            if (fileSystemLogEventService == null) {
                fileSystemLogEventService = new FileSystemLogEventServiceImpl();
            }
            if (logger == null) {
                logger = new LogbackFileSystemLogger();
            }
            initLogDirectories(appenderDto);
            logger.init(appenderDto, configuration, Paths.get(logsRootPath, tenantDirName, applicationDirName, "application.log"));
            fileSystemLogEventService.createUserAndGroup(appenderDto, configuration,
                    Paths.get(logsRootPath, tenantDirName, applicationDirName).toAbsolutePath().toString());
        } catch (Exception e) {
            LOG.error("Failed to init file system log appender: ", e);
        }
    }

    private void createTenantLogDirectory(String tenantId) {
        tenantDirName = "tenant_" + tenantId;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName);
    }

    private void createApplicationLogDirectory(String applicationToken) {
        applicationDirName = "application_" + applicationToken;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName + "/" + applicationDirName);
    }

    private void initLogDirectories(LogAppenderDto appender) {
        fileSystemLogEventService.createRootLogDirCommand(logsRootPath);
        createTenantLogDirectory(appender.getTenantId());
        createApplicationLogDirectory(appender.getApplicationToken());
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (logger != null) {
                try {
                    logger.close();
                } catch (IOException e) {
                    LOG.warn("IO Exception catched: ", e);
                }
            }
            fileSystemLogEventService = null;
            logger = null;
        }
        LOG.debug("Stoped filesystem log appender.");
    }

}
