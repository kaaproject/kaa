package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

import java.util.List;

public interface CTLService {

    CTLSchemaDto updateCTLSchemaScope(CTLSchemaDto ctlSchema);

    void removeCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    CTLSchemaDto findCTLSchemaById(String schemaId);

    CTLSchemaDto saveCTLSchema(CTLSchemaDto ctlSchemaDto);

    void removeCTLSchemaById(String schemaId);

    CTLSchemaDto findCTLSchemaByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    List<CTLSchemaDto> findCTLSchemasByApplicationId(String appId);

    List<CTLSchemaDto> findCTLSchemasByTenantId(String tenantId);

    List<CTLSchemaDto> findSystemCTLSchemas();

    CTLSchemaDto findLatestCTLSchemaByFqn(String fqn);

    List<CTLSchemaDto> findCTLSchemas();

}
