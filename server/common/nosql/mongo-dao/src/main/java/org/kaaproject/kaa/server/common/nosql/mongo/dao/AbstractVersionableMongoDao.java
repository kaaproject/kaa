package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.HasVersion;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

public abstract class AbstractVersionableMongoDao<T extends HasVersion, K> extends AbstractMongoDao<T, K> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractVersionableMongoDao.class);
    
    public T save(T dto) {
        try {
            mongoTemplate.save(dto);
            return dto;
        } catch (OptimisticLockingFailureException exception) {
            LOG.error("[{}] Can't update entity with version {}. Entity already changed!", getDocumentClass(), dto.getVersion());
            throw new KaaOptimisticLockingFailureException(
                    "Can't update entity with version " + dto.getVersion() + ". Entity already changed!");
        }
    }

}
