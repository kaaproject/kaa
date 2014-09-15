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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.common.dto.logs.avro.FileAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import scala.collection.mutable.StringBuilder;

@Service
@Transactional
public class FileSystemLogEventServiceImpl implements FileSystemLogEventService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogEventServiceImpl.class);
    private static final String DEFAULT_SYSTEM_USER = "kaa";

    @Value("#{properties[tmp_keys]}")
    private String tmpKeys;

    @Override
    public void createDirectory(String path) {
        LOG.debug("Starting create directory with path: {}", path);
        File directory = new File(path);
        if (!directory.exists()) {
            boolean result = directory.mkdir();
            LOG.debug("Creating directory result: {}", result);
        } else {
            LOG.debug("Directory/File with path: {} already exist", path);
        }
    }

    @Override
    public void createUserAndGroup(LogAppenderDto appender, String path) {
        LogAppenderParametersDto propertiesDto = appender.getProperties();
        if (propertiesDto != null) {
            FileAppenderParametersDto parametersDto = (FileAppenderParametersDto) propertiesDto.getParameters();
            if (parametersDto != null) {
                String applicationId = appender.getApplicationId();
                LOG.debug("Starting create user and group for application with id: {}", applicationId);
                String userName = "kaa_log_user_" + appender.getApplicationToken();
                String groupName = "kaa_log_group_" + appender.getApplicationToken();
                String publicKey = parametersDto.getSshKey();

                File tmpKeyFile = null;
                try {
                    createDirectory(tmpKeys);
                    tmpKeyFile = new File(tmpKeys + "/" + applicationId + "_pub.key");
                    PrintWriter out = new PrintWriter(tmpKeyFile);
                    out.write(publicKey);
                    out.close();
                    String command = new StringBuilder("sudo /usr/lib/kaa-operations/bin/kaa-operations create_user ")
                                    .append(userName).append(" ")
                                    .append(groupName).append(" ")
                                    .append(path).append(" ")
                                    .append(tmpKeyFile.getAbsolutePath()).append(" ").toString();
                    LOG.info("Executing system command: {}", command);
                    Process process = Runtime.getRuntime().exec(command);
                    process.waitFor();
                } catch (IOException | InterruptedException e) {
                    LOG.error("Unexpected exception occurred while executing create_user script", e);
                } finally {
                    if (tmpKeyFile != null) {
                        tmpKeyFile.delete();
                    }
                }
            }
        }
    }

    @Override
    public void createRootLogDirCommand(String logsRootPath) {
        LOG.info("Create root log directory...");
        try {
            String command = new StringBuilder("sudo /usr/lib/kaa-operations/bin/kaa-operations create_root_log_dir ")
                    .append(logsRootPath).append(" ")
                    .append(DEFAULT_SYSTEM_USER).append(" ").toString();
            LOG.info("Executing system command: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LOG.error("Can't execute create_root_log_dir script.", e);
        }
    }

    @Override
    public void save(List<LogEventDto> logEventPackDtos,
            org.apache.log4j.Logger logger, WriterAppender fileAppender) {
        LOG.debug("Starting saving {} logs", logEventPackDtos.size());
        List<String> events = new ArrayList<>();
        for (LogEventDto logEventDto : logEventPackDtos) {
            String event = new StringBuilder("{\"Log Header\": \"")
                    .append(logEventDto.getHeader())
                    .append("\", \"Event\": ")
                    .append(logEventDto.getEvent())
                    .append("}").toString();
            events.add(event);
        }
        LOG.debug("Starting writing {} logs to file", logEventPackDtos.size());
        for (String event : events) {
            fileAppender.doAppend(new LoggingEvent(null, logger, null, event, null));
        }
        LOG.debug("{} logs already saved to file", logEventPackDtos.size());
    }

    @Override
    public void removeAll(String path) {
        LOG.debug("Starting delete directory with path: {}", path);
        File directory = new File(path);
        try {
            FileUtils.deleteDirectory(directory);
            LOG.debug("Directory was successfully deleted");
        } catch (IOException e) {
            LOG.error("Unable to delete directory with path: {}", path);
        }
    }
}
