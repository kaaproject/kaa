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

package org.kaaproject.kaa.server.common.dao;


import java.util.Collection;
import java.util.List;

import org.kaaproject.kaa.common.dto.ChangeProfileFilterNotification;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;

/**
 * The interface Profile service.
 */
public interface ProfileService {

    /**
     * Find profile schemas by application id.
     *
     * @param applicationId the application id
     * @return the list of profile schemas
     */
    List<ProfileSchemaDto> findProfileSchemasByAppId(String applicationId);

    /**
     * Find schemas versions by application id.
     *
     * @param applicationId the application id
     * @return the list of <code>SchemaDto</code> objects
     */
    List<SchemaDto> findProfileSchemaVersionsByAppId(String applicationId);

    /**
     * Find profile schema by id.
     *
     * @param id the profile schema id
     * @return the profile schema dto
     */
    ProfileSchemaDto findProfileSchemaById(String id);

    /**
     * Save profile schema.
     *
     * @param profileSchemaDto the profile schema dto
     * @return the profile schema dto
     */
    ProfileSchemaDto saveProfileSchema(ProfileSchemaDto profileSchemaDto);

    /**
     * Remove profile schemas by application id.
     *
     * @param applicationId the application id
     */
    void removeProfileSchemasByAppId(String applicationId);

    /**
     * Remove profile schema by id.
     *
     * @param id the id
     */
    void removeProfileSchemaById(String id);

    /**
     * Find all profile filter records by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @param includeDeprecated the include deprecated filters
     * @return the collection of profile filters
     */
    Collection<StructureRecordDto<ProfileFilterDto>> findAllProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated);

    /**
     * Find profile filter record by schema id and endpoint group id.
     *
     * @param schemaId the schema id
     * @param endpointGroupId the endpoint group id
     * @return the structure record dto
     */
    StructureRecordDto<ProfileFilterDto> findProfileFilterRecordBySchemaIdAndEndpointGroupId(String schemaId, String endpointGroupId);

    /**
     * Find all vacant profile schemas versions by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list of schema versions
     */
    List<SchemaDto> findVacantSchemasByEndpointGroupId(String endpointGroupId);

    /**
     * Find profile filter by id.
     *
     * @param id the id
     * @return the profile filter dto
     */
    ProfileFilterDto findProfileFilterById(String id);

    /**
     * Save profile filter.
     *
     * @param profileFilterDto the profile filter dto
     * @return the profile filter dto
     */
    ProfileFilterDto saveProfileFilter(ProfileFilterDto profileFilterDto);

    /**
     * Activate profile filter.
     *
     * @param id the inactive profile filter id
     * @param activatedUsername the activated username
     * @return the active profile filter dto
     */
    ChangeProfileFilterNotification activateProfileFilter(String id, String activatedUsername);

    /**
     * Deactivate profile filter.
     *
     * @param id the id
     * @param deactivatedUsername the deactivated username
     * @return the change profile filter notification
     */
    ChangeProfileFilterNotification deactivateProfileFilter(String id, String deactivatedUsername);

    /**
     * Delete profile filter record.
     *
     * @param schemaId the profile schema id
     * @param groupId the endpoint group id
     * @param deactivatedUsername the deactivated username
     * @return the change profile filter notification
     */
    ChangeProfileFilterNotification deleteProfileFilterRecord(String schemaId, String groupId, String deactivatedUsername);

    /**
     * Remove profile filters by profile schema id.
     *
     * @param profileSchemaId the profile schema id
     */
    void removeProfileFiltersByProfileSchemaId(String profileSchemaId);

    /**
     * Find profile filter by application id and version.
     *
     * @param appId the application id
     * @param schemaVersion the schema version
     * @return the list of profile filters
     */
    List<ProfileFilterDto> findProfileFilterByAppIdAndVersion(String appId, int schemaVersion);

    /**
     * Find profile schema by application id and version.
     *
     * @param appId the application id
     * @param schemaVersion the schema version
     * @return the profile schema dto
     */
    ProfileSchemaDto findProfileSchemaByAppIdAndVersion(String appId, int schemaVersion);

    /**
     * Find profile filter by profile schema id and endpoint group id.
     * @param schemaId the profile schema id
     * @param groupId the endpoint group id
     * @return found profile filter
     */
    ProfileFilterDto findLatestFilterBySchemaIdAndGroupId(String schemaId, String groupId);
}
