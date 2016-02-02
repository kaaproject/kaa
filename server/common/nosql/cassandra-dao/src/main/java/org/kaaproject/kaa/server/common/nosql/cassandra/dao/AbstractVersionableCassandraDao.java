package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.kaaproject.kaa.common.dto.HasVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Statement;

public abstract class AbstractVersionableCassandraDao<T extends HasVersion, K> extends AbstractCassandraDao<T, K> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractVersionableCassandraDao.class);
    
    public T save(T entity) {
        if (entity.getVersion() == null) {
            entity.setVersion(0l);
            LOG.debug("Save entity {}", entity);
            Statement saveStatement = getSaveQuery(entity);
            saveStatement.setConsistencyLevel(getWriteConsistencyLevel());
            execute(saveStatement);
            return entity;
        } else {
            LOG.debug("Update entity {}", entity);
            return updateLocked(entity);
        }
    }

    protected abstract T updateLocked(T entity);
}
