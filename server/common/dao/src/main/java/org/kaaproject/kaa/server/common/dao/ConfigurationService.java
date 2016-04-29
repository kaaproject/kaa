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


import java.util.Collection;
import java.util.List;

import org.kaaproject.kaa.common.dto.ChangeConfigurationNotification;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.VersionDto;

/**
 * The interface Configuration service.
 */
public interface ConfigurationService {

    /**
     * Find latest configuration by application id and configuration major version.
     *
     * @param applicationId the application id
     * @param version the major version of configuration
     * @return the configuration dto
     */
    ConfigurationDto findConfigurationByAppIdAndVersion(String applicationId, int version);

    /**
     * Find configuration by group id and configuration major version.
     *
     * @param endpointGroupId the endpoint group id
     * @param version the version
     * @return the found configuration object
     */
    ConfigurationDto findConfigurationByEndpointGroupIdAndVersion(String endpointGroupId, int version);

    /**
     * Find default configuration by configuration schema id.
     *
     * @param schemaId the schema id
     * @return the configuration dto
     */
    ConfigurationDto findDefaultConfigurationBySchemaId(String schemaId);

    /**
     * Find configuration by id.
     *
     * @param id the id
     * @return the configuration dto
     */
    ConfigurationDto findConfigurationById(String id);

    /**
     * Find all configuration records by group id.
     *
     * @param endpointGroupId the endpoint group id
     * @param includeDeprecated the include deprecated
     * @return the list of structure records
     */
    Collection<ConfigurationRecordDto> findAllConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated);

    /**
     * Find configuration record by schema id and group id.
     *
     * @param schemaId the schema id
     * @param endpointGroupId the endpoint group id
     * @return the structure record dto
     */
    ConfigurationRecordDto findConfigurationRecordBySchemaIdAndEndpointGroupId(String schemaId, String endpointGroupId);

    /**
     * Find all vacant configuration schemas by group id.
     * Will be returned schema list where isn't related active or inactive configurations.
     *
     * @param endpointGroupId the group id
     * @return the list of schema objects
     */
    List<VersionDto> findVacantSchemasByEndpointGroupId(String endpointGroupId);

    /**
     * Save configuration. Configuration
     *
     * @param configurationDto the configuration dto
     * @return the configuration dto
     */
    ConfigurationDto saveConfiguration(ConfigurationDto configurationDto);

    /**
     * Activate configuration. Activate existing inactive configuration.
     * After that last active configuration will be deactivated.
     * Also will be added information to history {@link HistoryService#saveHistory(HistoryDto)}}
     *
     * @param id the string id
     * @param activatedUsername the activated username
     * @return the change configuration notification
     */
    ChangeConfigurationNotification activateConfiguration(String id, String activatedUsername);


    /**
     * Deactivate configuration. Deactivate existing active configuration
     * and delete if exists inactive.
     *
     * @param id the id
     * @param deactivatedUsername the deactivated username
     * @return the change configuration notification
     */
    ChangeConfigurationNotification deactivateConfiguration(String id, String deactivatedUsername);

    /**
     * Delete configuration record.
     *
     * @param schemaId the configuration schema id
     * @param groupId the endpoint group id
     * @param deactivatedUsername the deactivated username
     * @return the change configuration notification
     */
    ChangeConfigurationNotification deleteConfigurationRecord(String schemaId, String groupId, String deactivatedUsername);

    /**
     * Find configurations by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list
     */
    List<ConfigurationDto> findConfigurationsByEndpointGroupId(String endpointGroupId);

    /**
     * Find conf schemas by application id.
     *
     * @param applicationId the application id
     * @return the list
     */
    List<ConfigurationSchemaDto> findConfSchemasByAppId(String applicationId);

    /**
     * Find configuration schema versions by application id.
     *
     * @param applicationId the application id
     * @return the list
     */
    List<VersionDto> findConfigurationSchemaVersionsByAppId(String applicationId);

    /**
     * Find latest configuration schema by application id and schema version.
     *
     * @param applicationId the application id
     * @param version the version
     * @return the configuration schema dto
     */
    ConfigurationSchemaDto findConfSchemaByAppIdAndVersion(String applicationId, int version);

    /**
     * Save Configuration schema. Please see {@link ConfigurationService#saveConfSchema(ConfigurationSchemaDto, String)}
     *
     * @param configurationSchema the configuration schema
     * @param groupId group id for generated default configuration based on schema
     * @return the configuration schema dto
     */
    ConfigurationSchemaDto saveConfSchema(ConfigurationSchemaDto configurationSchema, String groupId);

    /**
     * Save Configuration schema.
     * <p>
     * During saving new configuration schema will be generated:
     * <ul>
     * <li>Base Schema</li>
     * <li>Override Schema</li>
     * <li>Protocol Schema</li>
     * <li>Base Data - Configuration attached to default group</li>
     * </ul>
     * After that will added information to history {@link HistoryService#saveHistory(HistoryDto historyDto)}
     *
     * @param configurationSchema the configuration schema
     * @return the configuration schema dto
     */
    ConfigurationSchemaDto saveConfSchema(ConfigurationSchemaDto configurationSchema);

    /**
     * Find configuration schema by id.
     *
     * @param id the id
     * @return the configuration schema dto
     */
    ConfigurationSchemaDto findConfSchemaById(String id);

    /**
     * Remove configuration schemas by application id.
     *
     * @param appId the application id
     */
    void removeConfSchemasByAppId(String appId);

}
