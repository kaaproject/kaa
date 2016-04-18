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

package org.kaaproject.kaa.server.common.dao;


import java.util.List;

import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

/**
 * The interface Log Schema service.
 */

public interface LogSchemaService {

    /**
     * Find log schemas by application id.
     *
     * @param applicationId the application id
     * @return the list of log schemas
     */
    List<LogSchemaDto> findLogSchemasByAppId(String applicationId);

    /**
     * Find all Log Schema versions for Application with specific id
     *
     * @param applicationId the id of Application
     * @return List of Log Schema versions
     */
    List<VersionDto> findLogSchemaVersionsByApplicationId(String applicationId);

    /**
     * Find log schema by id.
     *
     * @param id the log schema id
     * @return the log schema dto
     */
    LogSchemaDto findLogSchemaById(String id);

    /**
     * Find log schema by application id and version.
     *
     * @param appId the application id
     * @param schemaVersion the schema version
     * @return the log schema dto
     */
    LogSchemaDto findLogSchemaByAppIdAndVersion(String appId, int schemaVersion);

    /**
     * Save log schema.
     *
     * @param logSchemaDto the log schema dto
     * @return the log schema dto
     */
    LogSchemaDto saveLogSchema(LogSchemaDto logSchemaDto);

    /**
     * Remove log schemas by application id.
     *
     * @param applicationId the application id
     */
    void removeLogSchemasByAppId(String applicationId);

    /**
     * Remove log schema by id.
     *
     * @param id the id
     */
    void removeLogSchemaById(String id);
}
