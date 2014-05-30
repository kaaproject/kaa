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

import java.util.List;


/**
 * The interface Configuration dao.
 *
 * @param <T> the type parameter
 */
public interface ConfigurationDao<T> extends Dao<T> {


    /**
     * Find configuration by application id and version.
     *
     * @param applicationId the application id
     * @param version the configuration schema version
     * @return the configuration object
     */
    T findConfigurationByAppIdAndVersion(String applicationId, int version);

    /**
     * Find configurations by endpoint group id. and major version.
     *
     * @param endpointGroupId the endpoint group id
     * @param version the configuration schema version
     * @return configuration
     */
    T findConfigurationByEndpointGroupIdAndVersion(String endpointGroupId, int version);

    /**
     * Find configurations by application id.
     *
     * @param applicationId the application id
     * @return the list of configurations
     */
    List<T> findActiveByApplicationId(String applicationId);

    /**
     * Find all configurations by application id.
     *
     * @param applicationId the application id
     * @return the list of configuration
     */
    List<T> findAllByApplicationId(String applicationId);

    /**
     *  Find latest by configuration schema id and endpoint group id.
     *
     * @param schemaId the schema id
     * @param groupId the group id
     * @return latest active configuration
     */
    T findLatestActiveBySchemaIdAndGroupId(String schemaId, String groupId);

    /**
     * Find inactive configuration by configuration schema id.
     *
     * @param configurationSchemaId the configuration schema id
     * @return the configuration
     */
    T findInactiveByConfigurationSchemaId(String configurationSchemaId);

    /**
     * Find inactive configuration by configuration schema id and endpoint group id.
     *
     * @param schemaId the schema id
     * @param groupId the group id
     * @return the configuration
     */
    T findInactiveBySchemaIdAndGroupId(String schemaId, String groupId);

    /**
     * Find all configurations by configuration schema id.
     *
     * @param configurationSchemaId the configuration schema id
     * @return the list of configuration
     */
    List<T> findAllByConfigurationSchemaId(String configurationSchemaId);

    /**
     * Find active configuration by configuration schema id.
     *
     * @param configurationSchemaId the configuration schema id
     * @return the list of configuration
     */
    List<T> findActiveByConfigurationSchemaId(String configurationSchemaId);

    /**
     * Find all configurations by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list of configurations
     */
    List<T> findAllByEndpointGroupId(String endpointGroupId);

    /**
     * Find active configurations by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list of configuration
     */
    List<T> findActiveByEndpointGroupId(String endpointGroupId);

    /**
     * Find actual configurations by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list of configuration
     */
    List<T> findActualByEndpointGroupId(String endpointGroupId);

    /**
     * Find actual configurations by group id and schema id.
     *
     * @param schemaId the schema id
     * @param groupId the group id
     * @return the list of configurations
     */
    List<T> findActualBySchemaIdAndGroupId(String schemaId, String groupId);

    /**
     * Find latest deprecated configuration for endpoint group and profile schema.
     *
     * @param schemaId the schema id
     * @param groupId  the group id
     * @return the configuration
     */
    T findLatestDeprecated(String schemaId, String groupId);

    /**
     * Remove configurations by configuration schema id.
     *
     * @param configurationSchemaId the configuration schema id
     */
    void removeByConfigurationSchemaId(String configurationSchemaId);

    /**
     * Remove by application id.
     *
     * @param applicationId the application id
     */
    void removeByApplicationId(String applicationId);

    /**
     * Remove configuration by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     */
    void removeByEndpointGroupId(String endpointGroupId);

    /**
     * Activate configuration by id.
     *
     * @param id the configuration id
     * @param activatedUsername the activated username
     * @return the active configuration
     */
    T activate(String id, String activatedUsername);

    /**
     * Deactivate configuration by id.
     *
     * @param id the configuration id
     * @param deactivatedUsername the deactivated username
     * @return the deactivated configuration
     */
    T deactivate(String id, String deactivatedUsername);

    /**
     * Deactivate old configuration by configuration schema id and endpoint group id.
     *
     * @param schemaId the schema id
     * @param groupId the group id
     * @param deactivatedUsername the deactivated username
     * @return the deactivated configuration
     */
    T deactivateOldConfiguration(String schemaId, String groupId, String deactivatedUsername);
}
