package org.kaaproject.kaa.server.common.dao.impl;

public interface CTLSchemaMetaInfoDao<T> extends SqlDao<T> {
    T findByFqnAndVersion(String fqn, Integer version);
}
