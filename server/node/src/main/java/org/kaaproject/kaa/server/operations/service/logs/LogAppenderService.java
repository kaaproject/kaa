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

package org.kaaproject.kaa.server.operations.service.logs;

import java.util.List;

import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;

/**
 * Service that return appenders list.
 *
 */
public interface LogAppenderService {

    /**
     * Return List of all Appenders available for Application with specific application id.
     *
     * @param applicationId the application id of Application
     * @return List of all Appenders available for Application
     */
    List<LogAppender> getApplicationAppenders(String applicationId);
    
    /**
     * Return List of all Appenders available for Application and log schema version.
     *
     * @param applicationId the application id of Application
     * @param schemaVersion the log schema version
     * @return List of all Appenders available for Application and log schema version
     */
    List<LogAppender> getApplicationAppendersByLogSchemaVersion(String applicationId, int schemaVersion);

    /**
     * Gets the log schema.
     *
     * @param applicationId the application id
     * @param logSchemaVersion the log schema version
     * @return the log schema
     */
    LogSchema getLogSchema(String applicationId, int logSchemaVersion);

    /**
     * Gets the application appender.
     *
     * @param appenderId the appender id
     * @return the application appender
     */
    LogAppender getApplicationAppender(String appenderId);

}
