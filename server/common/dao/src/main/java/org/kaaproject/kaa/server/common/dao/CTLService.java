package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;

import java.util.List;

/**
 * Common type library service.
 */
public interface CTLService {

    /**
     * This method
     *
     * @param ctlSchema the ctlSchema object
     * @return CTLSchemaDto the updated object
     */
    CTLSchemaDto updateCTLSchema(CTLSchemaDto ctlSchema);

    /**
     * This method
     *
     * @param fqn
     * @param version
     * @param tenantId
     */
    void removeCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    /**
     * This method
     *
     * @param schemaId
     * @return
     */
    CTLSchemaDto findCTLSchemaById(String schemaId);

    /**
     * This method
     *
     * @param ctlSchemaDto
     * @return
     */
    CTLSchemaDto saveCTLSchema(CTLSchemaDto ctlSchemaDto);

    /**
     * This method
     *
     * @param schemaId
     */
    void removeCTLSchemaById(String schemaId);

    /**
     * This method
     *
     * @param fqn
     * @param version
     * @param tenantId
     * @return
     */
    CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    /**
     * This method
     *
     * @param appId
     * @return
     */
    List<CTLSchemaDto> findCTLSchemasByApplicationId(String appId);

    /**
     * This method
     *
     * @param tenantId
     * @return
     */
    List<CTLSchemaDto> findCTLSchemasByTenantId(String tenantId);

    /**
     * This method
     *
     * @return
     */
    List<CTLSchemaDto> findSystemCTLSchemas();

    /**
     * This method
     *
     * @return
     */
    List<CTLSchemaMetaInfoDto> findSystemCTLSchemasMetaInfo();

    /**
     * This method
     *
     * @param fqn
     * @return
     */
    CTLSchemaDto findLatestCTLSchemaByFqn(String fqn);

    /**
     * This method
     *
     * @return
     */
    List<CTLSchemaDto> findCTLSchemas();


    /**
     * This method
     *
     * @param appId
     * @return
     */
    List<CTLSchemaMetaInfoDto> findCTLSchemasMetaInfoByApplicationId(String appId);

    /**
     * This method
     *
     * @param tenantId
     * @return
     */
    List<CTLSchemaMetaInfoDto> findCTLSchemasMetaInfoByTenantId(String tenantId);


    /**
     * This method
     *
     * @param tenantId
     * @return the list of
     */
    List<CTLSchemaDto> findAvailableCTLSchemas(String tenantId);

    /**
     * This method
     *
     * @param tenantId
     * @return
     */
    List<CTLSchemaMetaInfoDto> findAvailableCTLSchemasMetaInfo(String tenantId);

    /**
     * @param schemaId
     * @return
     */
    List<CTLSchemaDto> findCTLSchemaDependents(String schemaId);

    /**
     * @param fqn
     * @param version
     * @param tenantId
     * @return
     */
    List<CTLSchemaDto> findCTLSchemaDependents(String fqn, Integer version, String tenantId);


}
