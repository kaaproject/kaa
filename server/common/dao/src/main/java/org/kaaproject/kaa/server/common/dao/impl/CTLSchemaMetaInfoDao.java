package org.kaaproject.kaa.server.common.dao.impl;

import java.util.List;

public interface CTLSchemaMetaInfoDao<T> extends SqlDao<T> {

    T incrementCount(T metaInfo);

    T findByFqnAndVersion(String fqn, Integer version);

    List<T> findSystemSchemaMetaInfo();
}
