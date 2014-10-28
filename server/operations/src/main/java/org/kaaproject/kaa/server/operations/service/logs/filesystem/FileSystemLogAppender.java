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

import java.util.List;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
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
public class FileSystemLogAppender extends LogAppender{

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogAppender.class);

    @Autowired
    private FileSystemLogEventService fileSystemLogEventService;

    private org.apache.log4j.Logger logger;
    private WriterAppender fileAppender;

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
        setName(name);
    }

    @Override
    public void close() {
        closed = true;
        if (fileAppender != null) {
            fileAppender.close();
        }
        LOG.debug("Stoped filesystem log appender.");
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            String path = logsRootPath + "/" + tenantDirName + "/" + applicationDirName;
            LOG.debug("[{}] appending {} logs to directory", path, logEventPack.getEvents().size());
            List<LogEventDto> dtos = generateLogEvent(logEventPack, header);

            LOG.debug("[{}] saving {} objects", path, dtos.size());
            fileSystemLogEventService.save(dtos, logger, fileAppender);
            LOG.debug("[{}] appended {} logs to directory", path, logEventPack.getEvents().size());
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    public void initLogAppender(LogAppenderDto appender) {
        LOG.debug("Starting initialize new instance of file system log appender");
        setName(appender.getName());
        initLogDirectories(appender);

        String appLogDir = logsRootPath + "/" + tenantDirName + "/" + applicationDirName;
        fileAppender = initAppender(appLogDir + "/application.log");
        logger = org.apache.log4j.Logger.getLogger(applicationDirName);
        fileSystemLogEventService.createUserAndGroup(appender, appLogDir);
    }

    private void createTenantLogDirectory(String tenantId) {
        tenantDirName = "tenant_" + tenantId;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName);
    }

    private void createApplicationLogDirectory(String applicationId) {
        applicationDirName = "application_" + applicationId;
        fileSystemLogEventService.createDirectory(logsRootPath + "/" + tenantDirName + "/" + applicationDirName);
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

    private void initLogDirectories(LogAppenderDto appender) {
        fileSystemLogEventService.createRootLogDirCommand(logsRootPath);
        createTenantLogDirectory(appender.getTenantId());
        createApplicationLogDirectory(appender.getApplicationId());
    }

}
