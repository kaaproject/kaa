/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

/**
 * A service to manage Common Type Library schemas.
 *
 * @since v0.8.0
 */
public interface CTLService {

    /**
     * Saves the given CTL schema to the database.
     *
     * @param schema A CTL schema to save
     *
     * @return The saved CTL schema
     */
    CTLSchemaDto saveCTLSchema(CTLSchemaDto schema);

    /**
     * Removes a CTL schema with the given identifier from the database.
     *
     * @param id The identifier of a CTL schema to remove
     */
    void removeCTLSchemaById(String id);

    /**
     * Removes a CTL schema of the given tenant with the given fully qualified
     * name and version number from the database.
     *
     * @param fqn A fully qualified CTL schema name
     * @param version A CTL schema version number
     * @param tenantId A tenant identifier
     */
    void removeCTLSchemaByFqnAndVersionAndTenantId(String fqn, int version, String tenantId);

    /**
     * Returns a CTL schema with the given identifier.
     *
     * @param id A CTL schema identifier
     *
     * @return A CTL schema with the given identifier
     */
    CTLSchemaDto findCTLSchemaById(String id);

    /**
     * Returns a CTL schema of the given tenant with the given fully qualified
     * name and version number.
     *
     * @param fqn A fully qualified CTL schema name
     * @param version A CTL schema version number
     * @param tenantId A tenant identifier
     *
     * @return A CTL schema with the given fully qualified name and version
     *         number
     */
    CTLSchemaDto findCTLSchemaByFqnAndVersionAndTenantId(String fqn, int version, String tenantId);

    /**
     * Returns CTL schemas available in the database.
     *
     * @return CTL schemas available in the database
     */
    List<CTLSchemaDto> findCTLSchemas();

    /**
     * Returns CTL schemas of a tenant with the given identifier.
     *
     * @param tenantId A tenant identifier
     *
     * @return CTL schemas of a tenant with the given identifier
     */
    List<CTLSchemaDto> findCTLSchemasByTenantId(String tenantId);

    /**
     * Returns CTL schemas of an application with the given identifier.
     *
     * @param applicationId An application identifier
     *
     * @return CTL schemas of an application with the given identifier
     */
    List<CTLSchemaDto> findCTLSchemasByApplicationId(String applicationId);

    /**
     * Returns {@link org.kaaproject.kaa.common.dto.ctl.CTLSchemaScope#SYSTEM}
     * scoped CTL schemas.
     *
     * @return {@link org.kaaproject.kaa.common.dto.ctl.CTLSchemaScope#SYSTEM}
     *         scoped CTL schemas
     */
    List<CTLSchemaDto> findSystemCTLSchemas();

    List<CTLSchemaDto> findCTLSchemaDependents(String schemaId);

    List<CTLSchemaDto> findCTLSchemaDependents(String fqn, int version, String tenantId);
}
