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

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;

/**
 * Server profile service
 */
public interface ServerProfileService {

    /**
     * Save server profile schema.
     *
     * @param dto the unsaved server profile schema.
     * @return the saved server profile schema.
     */
    ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto dto);

    /**
     * Find latest server profile schema for application with given identifier.
     *
     * @param appId the application identifier
     * @return the latest server profile schema.
     */
    ServerProfileSchemaDto findLatestServerProfileSchema(String appId);

    /**
     * Find server profile schema with given identifier.
     *
     * @param schemaId the server profile schema identifier.
     * @return the server profile schema.
     */
    ServerProfileSchemaDto findServerProfileSchema(String schemaId);

    /**
     * Find server profile schemas with given application identifier.
     *
     * @param appId the application identifier
     * @return the list of server profile schemas for corresponding application.
     */
    List<ServerProfileSchemaDto> findServerProfileSchemasByAppId(String appId);
    
    /**
     * Find server profile schema by application id and version.
     *
     * @param appId the application id
     * @param schemaVersion the schema version
     * @return the server profile schema dto
     */
    ServerProfileSchemaDto findServerProfileSchemaByAppIdAndVersion(String appId, int schemaVersion);

    /**
     * Remove server profile schema with given identifier.
     *
     * @param profileId the server profile schema identifier.
     */
    void removeServerProfileSchemaById(String profileId);

    /**
     * Remove server profile schemas for application with the given identifier.
     *
     * @param appId the application identifier
     */
    void removeServerProfileSchemaByAppId(String appId);

    /**
     * Save server profile data to endpoint profile.
     *
     * @param keyHash the endpoint key hash identifier.
     * @param version the server profile schema version
     * @param serverProfile server profile data in string representation.
     * @return the saved endpoint profile.
     */
    EndpointProfileDto saveServerProfile(byte[] keyHash, int version, String serverProfile);

}
