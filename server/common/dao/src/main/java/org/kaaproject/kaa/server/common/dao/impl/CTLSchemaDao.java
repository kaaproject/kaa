package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchema;

public interface CTLSchemaDao<T> extends SqlDao<T> {
    CTLSchema findByFqnAndVersion(String fqn, Integer version);
}
