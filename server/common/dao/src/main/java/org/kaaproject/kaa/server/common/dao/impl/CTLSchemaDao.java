/*
 * Copyright 2015 CyberVision, Inc.
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
     * Find CTL schemas with the given tenant identifier with tenant scope.
     *
     * @param tenantId the tenant identifier.
     * @return the list of CTL schemas with given tenant identifier.
     */
    List<T> findTenantSchemasByTenantId(String tenantId);

    /**
     * Find CTL schemas with the given application identifier.
     *
     * @param appId the application identifier.
     * @return the list of CTL schemas with given application identifier.
     */
    List<T> findByApplicationId(String appId);

    /**
     * Find the last version of CTL schema with the given fully qualified name.
     *
     * @param fqn the qualified name.
     * @return the latest version of CTL schema with the given fully qualified name.
     */
    T findLatestByFqn(String fqn);

    /**
     * Find CTL schema with the given fully qualified name, version and tenant identifier.
     *
     * @param fqn      the fully qualified name.
     * @param version  the schema version.
     * @param tenantId the tenant identifier.
     * @return the CTL schema with the given fully qualified name, version and tenant identifier.
     */
    T findByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);
    
    /**
     * Remove CTL schemas with the given fully qualified name, version and tenant identifier.
     *
     * @param fqn      the fully qualified name.
     * @param version  the schema version.
     * @param tenantId the tenant identifier.
     */
    void removeByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    /**
     * Find dependents CTL schemas from the schema with the given schema identifier.
     *
     * @param schemaId the schema identifier.
     * @return the list of dependents schemas from schema with given identifier.
     */
    List<T> findDependentSchemas(String schemaId);

    /**
     * Find available for tenant(include system scope) CTL schemas by given tenant identifier.
     *
     * @param tenantId the tenant identifier.
     * @return the list of available schemas.
     */
    List<T> findAvailableSchemas(String tenantId);
    
}
