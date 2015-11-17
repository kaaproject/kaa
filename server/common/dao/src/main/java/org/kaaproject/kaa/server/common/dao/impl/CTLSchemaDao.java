package org.kaaproject.kaa.server.common.dao.impl;

import java.util.List;

public interface CTLSchemaDao<T> extends SqlDao<T> {

    List<T> findSystemSchemas();

    List<T> findByTenantId(String tenantId);

    List<T> findByApplicationId(String appId);

    T findLatestByFqn(String fqn);

    T findByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    void removeByFqnAndVerAndTenantId(String fqn, Integer version, String tenantId);

    List<T> findDependentsSchemas(Long schemaId);

    List<T> findAvailableSchemas(String tenantId);
}
