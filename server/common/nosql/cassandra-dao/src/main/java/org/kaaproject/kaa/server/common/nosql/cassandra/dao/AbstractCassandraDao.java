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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.client.CassandraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCassandraDao<T, K> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractCassandraDao.class);
  private static final String KAA = "kaa";

  /**
   * Cassandra client classes.
   */
  @Autowired
  protected CassandraClient cassandraClient;

  @Value("#{cassandra_properties[read_consistency_level]}")
  private String readConsistencyLevel;
  @Value("#{cassandra_properties[write_consistency_level]}")
  private String writeConsistencyLevel;
  @Value("#{cassandra_properties[batch_type]}")
  private String batchType;

  private Session session;

  protected abstract Class<T> getColumnFamilyClass();

  protected abstract String getColumnFamilyName();

  protected Session getSession() {
    if (session == null) {
      session = cassandraClient.getSession();
    }
    return session;
  }

  protected Mapper<T> getMapper(Class<T> clazz) {
    return cassandraClient.getMapper(clazz);
  }

  protected Mapper<T> getMapper() {
    return cassandraClient.getMapper(getColumnFamilyClass());
  }

  protected List<T> findListByStatement(Statement statement) {
    List<T> list = Collections.emptyList();
    if (statement != null) {
      statement.setConsistencyLevel(getReadConsistencyLevel());
      ResultSet resultSet = getSession().execute(statement);
      Result<T> result = getMapper().map(resultSet);
      if (result != null) {
        list = result.all();
      }
    }
    return list;
  }

  protected UserType getUserType(String userType) {
    return getSession().getCluster().getMetadata().getKeyspace(KAA).getUserType(userType);
  }

  protected T findOneByStatement(Statement statement) {
    T object = null;
    if (statement != null) {
      statement.setConsistencyLevel(getReadConsistencyLevel());
      ResultSet resultSet = getSession().execute(statement);
      Result<T> result = getMapper().map(resultSet);
      if (result != null) {
        object = result.one();
      }
    }
    return object;
  }

  protected Statement getSaveQuery(T dto) {
    Mapper<T> mapper = (Mapper<T>) getMapper(getColumnFamilyClass());
    return mapper.saveQuery(dto);
  }

  /**
   * Save entity in cassandra database.
   *
   * @param object is entity for saving
   * @return saved entity
   */
  public T save(T object) {
    LOG.debug("Save entity {}", object);
    Statement saveStatement = getSaveQuery(object);
    saveStatement.setConsistencyLevel(getWriteConsistencyLevel());
    execute(saveStatement);
    return object;
  }

  protected void executeBatch(BatchStatement batch) {
    LOG.debug("Execute cassandra batch {}", batch);
    batch.setConsistencyLevel(getWriteConsistencyLevel());
    ResultSet resultSet = getSession().execute(batch);
    LOG.debug("Executed batch {}", resultSet);
  }

  protected void executeBatch(Statement... statements) {
    LOG.debug("Execute cassandra list of statements");
    if (LOG.isDebugEnabled()) {
      LOG.debug("Execute cassandra statements {} ", Arrays.toString(statements));
    }
    BatchStatement batchStatement = new BatchStatement(getBatchType());
    for (Statement statement : statements) {
      batchStatement.add(statement);
    }
    executeBatch(batchStatement);
  }

  protected ResultSet execute(Statement statement, ConsistencyLevel consistencyLevel) {
    LOG.debug("Execute cassandra statement {}", statement);
    statement.setConsistencyLevel(
        consistencyLevel == null ? ConsistencyLevel.ONE : consistencyLevel);
    return getSession().execute(statement);
  }

  protected ResultSet execute(Statement statement) {
    return execute(statement, statement.getConsistencyLevel());
  }

  /**
   * Get all entities from cassandra database.
   * @return <code>List</code> of entities
   */
  public List<T> find() {
    LOG.debug("Get all entities from column family {}", getColumnFamilyName());
    return findListByStatement(
        QueryBuilder.select()
            .all().from(getColumnFamilyName())
            .setConsistencyLevel(getReadConsistencyLevel()));
  }

  public T findById(K key) {
    LOG.debug("Get entity by key {}", key);
    return (T) getMapper().get(key);
  }

  /**
   * Remove all entities from cassandra database.
   */
  public void removeAll() {
    Statement truncate = QueryBuilder.truncate(getColumnFamilyName())
        .setConsistencyLevel(getWriteConsistencyLevel());
    session.execute(truncate);
  }

  public void removeById(K key) {
    getMapper().delete(key);
  }

  protected ConsistencyLevel getReadConsistencyLevel() {
    ConsistencyLevel defaultConsistencyLevel = ConsistencyLevel.ONE;
    if (readConsistencyLevel != null) {
      ConsistencyLevel cl = ConsistencyLevel.valueOf(readConsistencyLevel);
      if (cl != null) {
        defaultConsistencyLevel = cl;
      }
    }
    return defaultConsistencyLevel;
  }

  protected ConsistencyLevel getWriteConsistencyLevel() {
    ConsistencyLevel defaultConsistencyLevel = ConsistencyLevel.ONE;
    if (writeConsistencyLevel != null) {
      ConsistencyLevel cl = ConsistencyLevel.valueOf(writeConsistencyLevel);
      if (cl != null) {
        defaultConsistencyLevel = cl;
      }
    }
    return defaultConsistencyLevel;
  }

  protected BatchStatement.Type getBatchType() {
    BatchStatement.Type type = BatchStatement.Type.LOGGED;
    if (batchType != null && BatchStatement.Type.UNLOGGED.name().equalsIgnoreCase(batchType)) {
      type = BatchStatement.Type.UNLOGGED;
    }
    return type;
  }
}