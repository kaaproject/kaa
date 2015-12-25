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

package org.kaaproject.kaa.server.common.dao;

import org.apache.avro.Schema;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.file.FileData;

import java.util.List;

/**
 * Common type library service.
 */
public interface CTLService {
    
    String DEFAULT_SYSTEM_EMPTY_SCHEMA_FQN = "org.kaaproject.kaa.schema.system.EmptyData";
    int DEFAULT_SYSTEM_EMPTY_SCHEMA_VERSION = 1;
    
    /**
     * Get or create empty CTL schema with system scope.
     *
     * @param createdUsername
     *            the name of user.
     * @return CTLSchemaDto the empty schema with system scope.
     */
    CTLSchemaDto getOrCreateEmptySystemSchema(String createdUsername);

    /**
     * Update existing CTL schema by the given CTL schema object.
     *
     * @param ctlSchema
     *            the CTL schema object.
     * @return CTLSchemaDto the updated object.
     */
    CTLSchemaDto updateCTLSchema(CTLSchemaDto ctlSchema);

    /**
     * Remove a CTL schema of the given tenant with the given fully qualified
     * name and version number.
     *
     * @param fqn
     *            the fully qualified name.
     * @param version
     *            the schema version.
     * @param tenantId
     *            the tenant identifier.
     */
    void removeCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    /**
     * Find a CTL schema with the given identifier.
     *
     * @param schemaId
     *            the CTL schema identifier.
     * @return CTLSchemaDto the CTL schema with the given identifier.
     */
    CTLSchemaDto findCTLSchemaById(String schemaId);

    /**
     * Saves the given CTL schema to the database.
     *
     * @param ctlSchemaDto
     *            the CTL schema to save.
     * @return CTLSchemaDto the saved CTL schema.
     */
    CTLSchemaDto saveCTLSchema(CTLSchemaDto ctlSchemaDto);

    /**
     * Remove a CTL schema with the given identifier.
     *
     * @param schemaId
     *            the CTL schema identifier.
     */
    void removeCTLSchemaById(String schemaId);

    /**
     * Find a CTL schema of the given tenant with the given fully qualified name
     * and version number.
     *
     * @param fqn
     *            the fully qualified name.
     * @param version
     *            the CTL schema version.
     * @param tenantId
     *            the tenant identifier.
     * @return the CTL schema with the given fully qualified name and version
     *         number.
     */
    CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    /**
     * Find system CTL schemas available in the database.
     *
     * @return the list of available system CTL schemas in the database.
     */
    List<CTLSchemaDto> findSystemCTLSchemas();

    /**
     * Find system meta information of CTL schemas available in the database.
     *
     * @return the list of available system CTL meta information in the
     *         database.
     */
    List<CTLSchemaMetaInfoDto> findSystemCTLSchemasMetaInfo();

    /**
     * Find the last version of CTL schema with the given fully qualified name.
     *
     * @param fqn
     *            the fully qualified name.
     * @return the latest version of CTL schema with the given fully qualified
     *         name.
     */
    CTLSchemaDto findLatestCTLSchemaByFqn(String fqn);

    /**
     * Find CTL schemas available in the database.
     *
     * @return the list of available CTL schemas in the database.
     */
    List<CTLSchemaDto> findCTLSchemas();

    /**
     * Find meta information of CTL schemas with the given application
     * identifier.
     *
     * @param appId
     *            the application identifier.
     * @return the list of meta information of CTL schemas with application
     *         identifier.
     */
    List<CTLSchemaMetaInfoDto> findCTLSchemasMetaInfoByApplicationId(String appId);
    
    /**
     * Find meta information of CTL schemas with the given tenant identifier with tenant scope.
     *
     * @param tenantId
     *            the tenant identifier.
     * @return the list of meta information of CTL schemas with tenant
     *         identifier.
     */
    List<CTLSchemaMetaInfoDto> findTenantCTLSchemasMetaInfoByTenantId(String tenantId);
    
    /**
     * Find available CTL schemas meta information schemas for tenant(include
     * system scope) with the given tenant identifier.
     *
     * @param tenantId
     *            the tenant identifier.
     * @return the list of available meta information of CTL schemas for tenant
     *         with given identifier.
     */
    List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfo(String tenantId);
    
    /**
     * Find the dependents CTL schemas from CTL schema with the given schema
     * identifier
     *
     * @param schemaId
     *            the schema identifier.
     * @return the list of dependents CTL schemas from CTL schema with the given
     *         identifier.
     */
    List<CTLSchemaDto> findCTLSchemaDependents(String schemaId);

    /**
     * Find the dependents CTL schemas from CTL schema with the given tenant,
     * given fully qualified name and version number.
     *
     * @param fqn
     *            the fully qualified name.
     * @param version
     *            the schema version.
     * @param tenantId
     *            the tenant identifier.
     * @return the list of dependents CTL schemas from CTL schema with the given
     *         tenant identifier, fully qualified name and version.
     */
    List<CTLSchemaDto> findCTLSchemaDependents(String fqn, Integer version, String tenantId);

    /**
     * Exports the body of a CTL schema.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return A file containing the body of a CTL schema
     */
    FileData shallowExport(CTLSchemaDto schema);

    /**
     * Exports the body of a CTL schema with all dependencies inline,
     * recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return A string containing the body of a CTL schema with all
     *         dependencies inline, recursively
     */
    String flatExportAsString(CTLSchemaDto schema);

    /**
     * Exports the body of a CTL schema with all dependencies inline,
     * recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return A Schema object containing the body of a CTL schema with all
     *         dependencies inline, recursively
     */
    Schema flatExportAsSchema(CTLSchemaDto schema);

    /**
     * Exports the body of a CTL schema with all dependencies inline,
     * recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return A file containing the body of a CTL schema with all dependencies
     *         inline, recursively
     */
    FileData flatExport(CTLSchemaDto schema);

    /**
     * Exports the body of a CTL schema with all dependencies as different
     * files, recursively.
     * 
     * @param schema
     *            A CTL schema to export
     * 
     * @return An archive containing the body of a CTL schema as a file and all
     *         dependencies as different files, recursively.
     */
    FileData deepExport(CTLSchemaDto schema);
}
