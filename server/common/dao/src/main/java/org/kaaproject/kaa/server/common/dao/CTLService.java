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

import org.apache.avro.Schema;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.file.FileData;

/**
 * Common type library service.
 */
public interface CTLService {
    
    public static final String DEFAULT_SYSTEM_EMPTY_SCHEMA_FQN = "org.kaaproject.kaa.schema.system.EmptyData";
    public static final int DEFAULT_SYSTEM_EMPTY_SCHEMA_VERSION = 1;
    
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
     * Update existing CTL schema meta info scope by the given CTL schema meta info object.
     *
     * @param ctlSchemaMetaInfo
     *            the CTL schema meta info object.
     * @return CTLSchemaMetaInfoDto the updated CTL schema meta info object.
     */
    CTLSchemaMetaInfoDto updateCTLSchemaMetaInfoScope(CTLSchemaMetaInfoDto ctlSchemaMetaInfo);

    /**
     * Find CTL schema meta infos which are the application level siblings to the CTL 
     * of the given fully qualified name, tenant and application identifiers.
     *
     * @param fqn     the fully qualified.
     * @param tenantId the tenant identifier.
     * @param applicationId the application identifier.
     * @return the CTL schema meta information objects which are the siblings to the given CTL.
     */
    List<CTLSchemaMetaInfoDto> findSiblingsByFqnTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);
    
    /**
     * Remove a CTL schema of the given tenant or application with the given fully qualified
     * name and version number.
     *
     * @param fqn
     *            the fully qualified name.
     * @param version
     *            the schema version.
     * @param tenantId
     *            the tenant identifier.
     * @param applicationId
     *            the application identifier.
     */
    void removeCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId);

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
     * Find CTL schema with the given meta info id and version.
     *
     * @param metaInfoId the id of meta info object.
     * @param version    the schema version.
     * @return the CTL schema with the given meta info id and version.
     */    
    CTLSchemaDto findByMetaInfoIdAndVer(String metaInfoId, Integer version);
    
    /**
     * Find a CTL schema of the given tenant or application with the given fully qualified name
     * and version number.
     *
     * @param fqn
     *            the fully qualified name.
     * @param version
     *            the CTL schema version.
     * @param tenantId
     *            the tenant identifier.
     * @param applicationId
     *            the application identifier.
     * @return the CTL schema with the given fully qualified name and version
     *         number.
     */
    CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId);

    /**
     * Find any CTL schema of the given tenant or application with the given fully qualified name
     * and version number.
     *
     * @param fqn
     *            the fully qualified name.
     * @param version
     *            the CTL schema version.
     * @param tenantId
     *            the tenant identifier.
     * @param applicationId
     *            the application identifier.
     * @return the any CTL schema with the given fully qualified name and version
     *         number.
     */
    CTLSchemaDto findAnyCTLSchemaByFqnAndVerAndTenantIdAndApplicationId(String fqn, Integer version, String tenantId, String applicationId);
    
    /**
     * Find system CTL schemas available in the database.
     *
     * @return the list of available system CTL schemas in the database.
     */
    List<CTLSchemaDto> findSystemCTLSchemas();
    
    /**
     * Find system CTL schemas meta info available in the database.
     *
     * @return the list of available system CTL schemas meta info in the database.
     */
    List<CTLSchemaMetaInfoDto> findSystemCTLSchemasMetaInfo();
    
    /**
     * Find available CTL schemas meta info for tenant(include
     * system scope) with the given tenant identifier.
     *
     * @param tenantId
     *            the tenant identifier.
     * @return the list of available CTL schemas meta info for tenant
     *         with given identifier.
     */
    List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfoForTenant(String tenantId);
    
    /**
     * Find available CTL schemas meta info for application(include
     * system and tenant scope) with the given tenant and application identifier.
     *
     * @param tenantId
     *            the tenant identifier.
     * @param applicationId
     *            the application identifier.
     * @return the list of available CTL schemas meta info for application
     *         with given identifier.
     */
    List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfoForApplication(String tenantId, String applicationId);

    /**
     * Find the last version of CTL schema with the given fully qualified name, tenant and application identifier.
     *
     * @param fqn
     *            the fully qualified name.
     * @param tenantId
     *            the tenant identifier.
     * @param applicationId
     *            the application identifier.
     * @return the latest version of CTL schema with the given fully qualified
     *         name, tenant and application identifier.
     */
    CTLSchemaDto findLatestCTLSchemaByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);
    
    /**
     * Find the last version of CTL schema with the given meta info id.
     *
     * @param metaInfoId the id of meta info object.
     * @return the latest version of  CTL schema with the given meta info id.
     */    
    CTLSchemaDto findLatestByMetaInfoId(String metaInfoId);
    
    /**
     * Find all available versions of CTL schema with the given fully qualified name, tenant and application identifier.
     *
     * @param fqn
     *            the fully qualified name.
     * @param tenantId
     *            the tenant identifier.
     * @param applicationId
     *            the application identifier.
     * @return the list of available versions of CTL schema with the given fully qualified
     *         name, tenant and application identifier.
     */
    List<CTLSchemaDto> findAllCTLSchemasByFqnAndTenantIdAndApplicationId(String fqn, String tenantId, String applicationId);

    /**
     * Find CTL schemas available in the database.
     *
     * @return the list of available CTL schemas in the database.
     */
    List<CTLSchemaDto> findCTLSchemas();
        
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
     * Find the dependents CTL schemas from CTL schema with the given tenant, application, 
     * fully qualified name and version number.
     *
     * @param fqn
     *            the fully qualified name.
     * @param version
     *            the schema version.
     * @param tenantId
     *            the tenant identifier.
     * @param applicationId
     *            the application identifier.
     * @return the list of dependents CTL schemas from CTL schema with the given
     *         tenant, application, fully qualified name and version.
     */
    List<CTLSchemaDto> findCTLSchemaDependents(String fqn, Integer version, String tenantId, String applicationId);

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
