package org.kaaproject.kaa.server.common.dao.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import org.kaaproject.kaa.server.common.dao.cassandra.client.CassandraClient;
import org.kaaproject.kaa.server.common.dao.impl.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCassandraDao<T> implements Dao<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCassandraDao.class);

    /**
     * Cassandra client classes.
     */
    @Autowired
    private CassandraClient cassandraClient;
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
        ResultSet resultSet = session.execute(statement);
        Result result = getMapper().map(resultSet);
        if (result != null) {
            object = (T) result.one();
        }
        return object;
    }

    protected <V> Statement getSaveQuery(V dto, Class<?> clazz) {
        Mapper<V> mapper= (Mapper<V>) getMapper(clazz);
        return mapper.saveQuery(dto);
    }

    protected Statement getSaveQuery(T dto) {
        Mapper<T> mapper = (Mapper<T>) getMapper(getColumnFamilyClass());
        return mapper.saveQuery(dto);
    }

    @Override
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
        LOG.info("Execute cassandra batch {}", statements);
        BatchStatement batchStatement = new BatchStatement(type);
        for (Statement statement : statements) {
            batchStatement.add(statement);
        }
        ResultSet resultSet = getSession().execute(batchStatement);
        LOG.info("Executed batch {}", resultSet);
    }

    @Override
    public T persist(T dto) {
        return save(dto);
    }

    @Override
    public <V> V save(V dto, Class<?> clazz) {
        LOG.debug("Save entity of {} class", clazz.getName());
        Mapper mapper = getMapper(clazz);
        mapper.save(dto);
        return dto;
    }

    @Override
    public List<T> find() {
        return Collections.emptyList();
    }

    @Override
    public T findById(String id) {
        return (T) getMapper().get(id);
    }

    @Override
    public T findById(String id, boolean lazy) {
        return (T) getMapper().get(id);
    }

    @Override
    public void removeAll() {
        Delete delete = QueryBuilder.delete().all().from(getColumnFamilyName());
        LOG.debug("Remove all request: {}", delete.toString());
        session.execute(delete);
    }

    @Override
    public void removeById(String id) {
        getMapper().delete(id);
    }
}