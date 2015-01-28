package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.client.CassandraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class AbstractCassandraDao<T, K> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCassandraDao.class);

    /**
     * Cassandra client classes.
     */
    @Autowired
    private CassandraClient cassandraClient;
    @Value("#{cassandra_properties[read_consistency_level]}")
    private Integer readConsistencyLevel;
    @Value("#{cassandra_properties[write_consistency_level]}")
    private Integer writeConsistencyLevel;

    private Session session;

    protected abstract Class<?> getColumnFamilyClass();

    protected abstract String getColumnFamilyName();

    private Session getSession() {
        if (session == null) {
            session = cassandraClient.getSession();
        }
        return session;
    }

    protected Mapper<?> getMapper(Class<?> clazz) {
        return cassandraClient.getMapper(clazz);
    }

    protected Mapper<?> getMapper() {
        return cassandraClient.getMapper(getColumnFamilyClass());
    }

    protected List<T> findListByStatement(Statement statement) {
        List<T> list = Collections.emptyList();
        ResultSet resultSet = getSession().execute(statement);
        Result result = getMapper().map(resultSet);
        if (result != null) {
            list = result.all();
        }
        return list;
    }

    protected T findOneByStatement(Statement statement) {
        T object = null;
        ResultSet resultSet = getSession().execute(statement);
        Result result = getMapper().map(resultSet);
        if (result != null) {
            object = (T) result.one();
        }
        return object;
    }

    protected <V> Statement getSaveQuery(V dto, Class<?> clazz) {
        Mapper<V> mapper = (Mapper<V>) getMapper(clazz);
        return mapper.saveQuery(dto);
    }

    protected Statement getSaveQuery(T dto) {
        Mapper<T> mapper = (Mapper<T>) getMapper(getColumnFamilyClass());
        return mapper.saveQuery(dto);
    }

    public T save(T dto) {
        LOG.info("Save entity {}", dto);
        Mapper mapper = getMapper();
        mapper.save(dto);
        return dto;
    }

    protected void executeBatch(BatchStatement batch) {
        LOG.info("Execute cassandra batch {}", batch);
        ResultSet resultSet = getSession().execute(batch);
        LOG.info("Executed batch {}", resultSet);
    }

    protected void executeBatch(BatchStatement.Type type, Statement... statements) {
        LOG.info("Execute cassandra batch {} with type {} ", statements, type);
        BatchStatement batchStatement = new BatchStatement(type);
        for (Statement statement : statements) {
            batchStatement.add(statement);
        }
        ResultSet resultSet = getSession().execute(batchStatement);
        LOG.info("Result of batch execution is {}", resultSet);
    }

    protected ResultSet execute(Statement statement) {
        LOG.info("Execute cassandra batch {}", statement);
        return getSession().execute(statement);
    }

    public <V> V save(V dto, Class<?> clazz) {
        LOG.debug("Save entity of {} class", clazz.getName());
        Mapper mapper = getMapper(clazz);
        mapper.save(dto);
        return dto;
    }

    public List<T> find() {
        LOG.debug("Get all entities from column family {}", getColumnFamilyName());
        return findListByStatement(QueryBuilder.select().all().from(getColumnFamilyName()));
    }

    public T findById(K key) {
        return (T) getMapper().get(key);
    }

    public void removeAll() {
        Delete delete = QueryBuilder.delete().all().from(getColumnFamilyName());
        LOG.debug("Remove all request: {}", delete.toString());
        session.execute(delete);
    }

    public void removeById(K key) {
        getMapper().delete(key);
    }

    protected String getStringId() {
        return UUID.randomUUID().toString();
    }

    protected <V> List<Statement> getSaveQueryList(V dto, Class<?> clazz) {
        return null;
    }

    protected ConsistencyLevel getReadConsistencyLevel() {
        ConsistencyLevel defaultConsistencyLevel = ConsistencyLevel.ANY;
        if (readConsistencyLevel != null) {
            defaultConsistencyLevel = ConsistencyLevel.values()[readConsistencyLevel];
        }
        return defaultConsistencyLevel;
    }

    protected ConsistencyLevel getWriteConsistencyLevel() {
        ConsistencyLevel defaultConsistencyLevel = ConsistencyLevel.ANY;
        if (writeConsistencyLevel != null) {
            defaultConsistencyLevel = ConsistencyLevel.values()[writeConsistencyLevel];
        }
        return defaultConsistencyLevel;
    }
}