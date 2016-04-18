/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.kaaproject.kaa.common.dto.HasVersion;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
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
        } catch (DuplicateKeyException exception) {
            LOG.error("[{}] Can't insert entity. Entity already exists!", getDocumentClass());
            throw new KaaOptimisticLockingFailureException("Can't insert entity. Entity already exists!");
        }
    }

}
