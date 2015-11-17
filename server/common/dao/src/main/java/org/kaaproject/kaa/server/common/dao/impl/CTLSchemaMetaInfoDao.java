package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.server.common.dao.model.sql.CTLSchemaMetaInfo;

import java.util.List;

public interface CTLSchemaMetaInfoDao<T> extends SqlDao<T> {

    T incrementCount(T metaInfo);

    T findByFqnAndVersion(String fqn, Integer version);

    List<T> findSystemSchemaMetaInfo();

    T updateScope(CTLSchemaMetaInfo ctlSchema);
}
