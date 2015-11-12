package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

import java.util.List;

public interface CTLSchemaDao<T> extends SqlDao<T> {

    List<T> findSystemSchemas();

    List<T> findByTenantId(String tenantId);

    List<T> findByApplicationId(String appId);

    T findLatestByFqn(String fqn);

    T findByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    T updateScope(CTLSchemaDto ctlSchema);

    void removeByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);
}
