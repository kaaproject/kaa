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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;

import java.util.List;

import org.kaaproject.kaa.common.dto.HasVersion;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Update.Assignments;

public abstract class AbstractVersionableCassandraDao<T extends HasVersion, K> extends AbstractCassandraDao<T, K> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractVersionableCassandraDao.class);
    
    public T save(T entity) {
        if (entity.getVersion() == null) {
            entity.setVersion(0l);
            LOG.debug("Save entity {}", entity);
            return insertLocked(entity);
        } else {
            LOG.debug("Update entity {}", entity);
            return updateLocked(entity);
        }
    }
    
    private Clause[] buildKeyClauses(CassandraEntityMapper<T> entityMapper, T entity) {
        List<String> keyColumns = entityMapper.getKeyColumnNames();
        Clause[] clauses = new Clause[keyColumns.size()];
        for (int i=0;i<keyColumns.size();i++) {
            String columnName = keyColumns.get(i);
            clauses[i] = eq(columnName, entityMapper.getColumnValueForName(columnName, entity, cassandraClient));
        }
        return clauses;
    }
    
    private T updateLocked(T entity) {
        long version = (entity.getVersion() == null) ? 0l : entity.getVersion();
        Assignments assigns = update(getColumnFamilyName()).onlyIf(eq(OPT_LOCK, version)).with(set(OPT_LOCK, version + 1));
        CassandraEntityMapper<T> entityMapper = CassandraEntityMapper.getEntityMapperForClass(getColumnFamilyClass(), cassandraClient);
        for (String name : entityMapper.getNonKeyColumnNames()) {
            if (!name.equals(OPT_LOCK)) {
                Assignment assignment = set(name, entityMapper.getColumnValueForName(name, entity, cassandraClient));
                assigns = assigns.and(assignment);
            }
        }
        Clause[] whereClauses = buildKeyClauses(entityMapper, entity);
        Update.Where query = assigns.where(whereClauses[0]);
        if (whereClauses.length > 1) {
            for (int i = 1; i < whereClauses.length; i++) {
                query = query.and(whereClauses[i]);
            }
        }
        query.setConsistencyLevel(getWriteConsistencyLevel());
        ResultSet res = execute(query);
        if (!res.wasApplied()) {
            LOG.error("[{}] Can't update entity with version {}. Entity already changed!", getColumnFamilyClass(), version);
            throw new KaaOptimisticLockingFailureException("Can't update entity with version " + version + ". Entity already changed!");
        } else {
            Select.Where where = select().from(getColumnFamilyName()).where(whereClauses[0]);
            if (whereClauses.length > 1) {
                for (int i = 1; i < whereClauses.length; i++) {
                    where = where.and(whereClauses[i]);
                }
            }
            return findOneByStatement(where);
        }
    }
    
    private T insertLocked(T entity) {
        Insert insert = insertInto(getColumnFamilyName()).ifNotExists();
        CassandraEntityMapper<T> entityMapper = CassandraEntityMapper.getEntityMapperForClass(getColumnFamilyClass(), cassandraClient);
        for (String name : entityMapper.getKeyColumnNames()) {
            insert.value(name, entityMapper.getColumnValueForName(name, entity, cassandraClient));
        }
        for (String name : entityMapper.getNonKeyColumnNames()) {
            insert.value(name, entityMapper.getColumnValueForName(name, entity, cassandraClient));
        }
        insert.setConsistencyLevel(getWriteConsistencyLevel());
        ResultSet res = execute(insert);
        if (!res.wasApplied()) {
            LOG.error("[{}] Can't insert entity. Entity already exists!", getColumnFamilyClass());
            throw new KaaOptimisticLockingFailureException("Can't insert entity. Entity already exists!");
        } else {
            Clause[] whereClauses = buildKeyClauses(entityMapper, entity);
            Select.Where where = select().from(getColumnFamilyName()).where(whereClauses[0]);
            if (whereClauses.length > 1) {
                for (int i = 1; i < whereClauses.length; i++) {
                    where = where.and(whereClauses[i]);
                }
            }
            return findOneByStatement(where);
        }
    }

}
