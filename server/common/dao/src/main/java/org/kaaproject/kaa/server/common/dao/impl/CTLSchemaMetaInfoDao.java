package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;

public interface CTLSchemaMetaInfoDao<T> extends SqlDao<T> {

    CTLSchemaMetaInfo incrementCount(CTLSchemaMetaInfo metaInfo);

    T findByFqnAndVersion(String fqn, Integer version);
}
