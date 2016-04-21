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

package org.kaaproject.kaa.server.common.dao.impl;

import java.util.List;

/**
 * The interface CTL schema dao.
 *
 * @param <T> the model type parameter
 */
public interface CTLSchemaDao<T> extends SqlDao<T> {

    /**
     * Find CTL schemas available in the database.
     *
     * @return the list of CTL schemas available in the database.
     */
    List<T> findSystemSchemas();

    /**
     * Find available for tenant(include system scope) CTL schemas by given tenant identifier.
     *
     * @param tenantId the tenant identifier.
     * @return the list of available schemas.
     */
    List<T> findAvailableSchemasForTenant(String tenantId);

    /**
     * Find available for application(include system and tenant scope) CTL schemas by given tenant and application identifier.
     * 
     * @param tenantId the tenant identifier.
     * @param appId the application identifier.
     * @return the list of available schemas.
     */
    List<T> findAvailableSchemasForApplication(String tenantId, String appId);

    /**
     * Find CTL schema with the given meta info id and version.
     *
     * @param metaInfoId the id of meta info object.
     * @param version    the schema version.
     * @return the CTL schema with the given meta info id and version.
     */

    T findByMetaInfoIdAndVer(String metaInfoId, Integer version);

    /**
     * Find CTL schema with the given fully qualified name, version, tenant and application identifiers.
     *
     * @param fqn      the fully qualified name.
     * @param version  the schema version.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the CTL schema with the given fully qualified name, version, tenant and application identifiers.
     */
    
    T findByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId);

    /**
     * Find any CTL schema with the given fully qualified name, version, tenant and application identifiers.
     *
     * @param fqn      the fully qualified name.
     * @param version  the schema version.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the any CTL schema with the given fully qualified name, version, tenant and application identifiers.
     */
    T findAnyByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId);

    /**
     * Find the last version of CTL schema with the given fully qualified name, tenant and application identifiers.
     *
     * @param fqn the qualified name.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the latest version of CTL schema with the given fully qualified name, tenant and application identifiers.
     */
    T findLatestByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);

    /**
     * Find the last version of CTL schema with the given meta info id.
     *
     * @param metaInfoId the id of meta info object.
     * @return the latest version of  CTL schema with the given meta info id.
     */
    
    T findLatestByMetaInfoId(String metaInfoId);

    /**
     * Find all available versions of CTL schema with the given fully qualified name, tenant and application identifiers.
     *
     * @param fqn the qualified name.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the list of available versions of CTL schema with the given fully qualified name, tenant and application identifiers.
     */
    List<T> findAllByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);

    /**
     * Find all available versions of CTL schema with the given meta info id.
     *
     * @param metaInfoId the id of meta info object.
     * @return the list of available versions of CTL schema with the given meta info id.
     */
    List<T> findAllByMetaInfoId(String metaInfoId);

    /**
     * Find dependents CTL schemas from the schema with the given schema identifier.
     *
     * @param schemaId the schema identifier.
     * @return the list of dependents schemas from schema with given identifier.
     */
    List<T> findDependentSchemas(String schemaId);

}
