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

package org.kaaproject.kaa.server.operations.service.logs.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import scala.collection.mutable.StringBuilder;

@Service
@Transactional
public class FileSystemLogEventServiceImpl implements FileSystemLogEventService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLogEventServiceImpl.class);
    private static final String DEFAULT_SYSTEM_USER = "kaa";

    @Autowired
    private ApplicationService applicationService;

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
    public void createUserAndGroup(String applicationId, String path) {
        LOG.debug("Starting create user and group for application with id: {}", applicationId);
        ApplicationDto dto = applicationService.findAppById(applicationId);
        String userName = "kaa_log_user_" + dto.getApplicationToken();
        String groupName = "kaa_log_group_" + dto.getApplicationToken();
        String publicKey = dto.getPublicKey();

        File tmpKeyFile = null;
        try {
            createDirectory(tmpKeys);
            tmpKeyFile = new File(tmpKeys + "/" +applicationId+"_pub.key");
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
        } catch (IOException e1) {
            LOG.error("Unexpected exception occurred while executing create_user script", e1);
        } catch (InterruptedException e1) {
            LOG.error("InterruptedException while waiting for create_user script executing", e1);
        } finally{
            if(tmpKeyFile != null){
                tmpKeyFile.delete();
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
            String event = new StringBuilder("{\"Date created\": \"")
                    .append(logEventDto.getDateCreated())
                    .append("\", \"Endpoint key\": \"")
                    .append(logEventDto.getEndpointKey())
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
