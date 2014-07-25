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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;

/**
 * The interface FileSystemLogEventService service.
 */
public interface FileSystemLogEventService {

    /**
     * Create Directory with specific path
     *
     * @param path the path to directory
     */
    void createDirectory(String path);

    /**
     * Create log user and log group and give them permissions to access
     *  logs of application with specific id
     *
     * @param applicationId the application id
     * @param path the path to logs directory
     */
    public void createUserAndGroup(String applicationId, String path);

    /**
     * Save LogEventPacks.
     *
     * @param logEventPackDtos the domain objects
     * @param logger the logger which will save logs
     */
    void save(List<LogEventDto> logEventPackDtos, Logger logger, WriterAppender fileAppender);

    /**
     * Remove all objects from directory.
     *
     * @param path the path to directory
     */
    void removeAll(String path);

    /**
     * Create root log directory on kaa server.
     *
     * @param logsRootPath the path to root log directory
     */
    void createRootLogDirCommand(String logsRootPath);
}
