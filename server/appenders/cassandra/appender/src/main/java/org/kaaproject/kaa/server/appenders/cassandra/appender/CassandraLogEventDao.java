/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.appenders.cassandra.appender;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraBatchType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraCompression;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraCredential;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraServer;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraSocketOption;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraWriteConsistencyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

public class CassandraLogEventDao implements LogEventDao {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraLogEventDao.class);

    private static final String ID_COLUMN = "id";
    private static final String HEADER_COLUMN = "header";
    private static final String EVENT_COLUMN = "event";

    private static final String TABLE_NAME = "$table_name";
    private static final String KEYSPACE_NAME = "$keyspace_name";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $keyspace_name.$table_name (" + ID_COLUMN + " text, "
            + HEADER_COLUMN + " text, " + EVENT_COLUMN + " text, PRIMARY KEY (id) );";

    private final Random RANDOM = new Random();

    private Cluster cluster;
    private Session session;
    private CassandraBatchType batchType;
    private ConsistencyLevel writeConsistencyLevel;
    private String keyspaceName;

    public CassandraLogEventDao(CassandraConfig configuration) throws UnknownHostException {
        LOG.info("Init cassandra log event dao...");
        if (configuration != null) {
            keyspaceName = configuration.getKeySpace();
            List<InetSocketAddress> clusterNodes = new ArrayList<>();
            List<CassandraServer> nodes = configuration.getCassandraServers();
            for (CassandraServer node : nodes) {
                clusterNodes.add(new InetSocketAddress(InetAddress.getByName(node.getHost()), node.getPort()));
            }

            Cluster.Builder builder = Cluster.builder().addContactPointsWithPorts(clusterNodes);
            LOG.info("Init cassandra cluster with nodes {}", Arrays.toString(clusterNodes.toArray()));

            CassandraCredential cc = configuration.getCassandraCredential();
            if (cc != null) {
                builder.withCredentials(cc.getUser(), cc.getPassword());
                LOG.trace("Init cassandra cluster with username {} and password {}", cc.getUser(), cc.getPassword());
            }

            CassandraSocketOption option = configuration.getCassandraSocketOption();
            if (option != null) {
                SocketOptions so = new SocketOptions();
                if (option.getSoLinger() != null) {
                    so.setSoLinger(option.getSoLinger());
                }
                if (option.getKeepAlive() != null) {
                    so.setKeepAlive(option.getKeepAlive());
                }
                if (option.getReuseAddress()) {
                    so.setReuseAddress(option.getReuseAddress());
                }
                if (option.getTcpNoDelay() != null) {
                    so.setTcpNoDelay(option.getTcpNoDelay());
                }
                if (option.getConnectionTimeout() != null) {
                    so.setConnectTimeoutMillis(option.getConnectionTimeout());
                }
                if (option.getReadTimeout() != null) {
                    so.setReadTimeoutMillis(option.getReadTimeout());
                }
                if (option.getReceiveBufferSize() != null) {
                    so.setReceiveBufferSize(option.getReceiveBufferSize());
                }
                if (option.getSendBufferSize() != null) {
                    so.setSendBufferSize(option.getSendBufferSize());
                }
                builder.withSocketOptions(so);
                LOG.trace("Init cassandra cluster with socket options {}", option);
            }

            CassandraWriteConsistencyLevel ccLevel = configuration.getCassandraWriteConsistencyLevel();
            if (ccLevel != null) {
                writeConsistencyLevel = ConsistencyLevel.valueOf(ccLevel.name());
                LOG.trace("Init cassandra cluster with consistency level {}", ccLevel.name());
            }
            CassandraCompression cassandraCompression = configuration.getCassandraCompression();
            if (cassandraCompression != null) {
                builder.withCompression(ProtocolOptions.Compression.valueOf(cassandraCompression.name()));
                LOG.trace("Init cassandra cluster with compression {}", cassandraCompression.name());
            }
            batchType = configuration.getCassandraBatchType();
            cluster = builder.build();
        }
    }

    @Override
    public void createTable(String tableName) {
        LOG.info("Create table {} in cassandra keyspace {}", tableName, keyspaceName);
        String createTableQuery = CREATE_TABLE.replace(TABLE_NAME, tableName);
        createTableQuery = createTableQuery.replace(KEYSPACE_NAME, keyspaceName);
        LOG.debug("Execute query {}", createTableQuery);
        getSession().execute(createTableQuery);
    }

    @Override
    public List<LogEventDto> save(List<LogEventDto> logEventDtoList, String tableName) {
        LOG.debug("Execute bath request for cassandra table {}", tableName);
        executeBatch(prepareQuery(logEventDtoList, tableName));
        return logEventDtoList;
    }

    @Override
    public ListenableFuture<ResultSet> saveAsync(List<LogEventDto> logEventDtoList, String tableName) {
        LOG.debug("Execute async bath request for cassandra table {}", tableName);
        return executeBatchAsync(prepareQuery(logEventDtoList, tableName));
    }

    @Override
    public void removeAll(String tableName) {
        LOG.info("Truncate all data from table {}", tableName);
        getSession().execute(QueryBuilder.truncate(keyspaceName, tableName));
    }

    @Override
    public void close() {
        LOG.info("Close connection to cassandra cluster.");
        if (cluster != null) {
            cluster.close();
        }
    }

    private Session getSession() {
        if (session == null || session.isClosed()) {
            session = cluster.newSession();
        }
        return session;
    }

    private ConsistencyLevel getWriteConsistencyLevel() {
        if (writeConsistencyLevel == null) {
            writeConsistencyLevel = ConsistencyLevel.ONE;
        }
        return writeConsistencyLevel;
    }

    private void executeBatch(RegularStatement... statement) {
        getSession().execute(prepareBatch(statement));
    }

    private ResultSetFuture executeBatchAsync(RegularStatement... statement) {
        return getSession().executeAsync(prepareBatch(statement));
    }

    private String getId(String id) {
        if (id == null || id.length() == 0) {
            id = new UUID(System.currentTimeMillis(), RANDOM.nextLong()).toString();
        }
        return id;
    }

    private Batch prepareBatch(RegularStatement... statement) {
        Batch batch;
        if (batchType != null && batchType.equals(CassandraBatchType.UNLOGGED)) {
            batch = QueryBuilder.unloggedBatch(statement);
        } else {
            batch = QueryBuilder.batch(statement);
        }
        batch.setConsistencyLevel(getWriteConsistencyLevel());
        return batch;
    }

    private Insert[] prepareQuery(List<LogEventDto> logEventDtoList, String collectionName) {
        Insert[] insertArray = new Insert[logEventDtoList.size()];
        for (int i = 0; i < logEventDtoList.size(); i++) {
            LogEventDto dto = logEventDtoList.get(i);
            insertArray[i] = QueryBuilder.insertInto(keyspaceName, collectionName)
                    .value(ID_COLUMN, getId(dto.getId()))
                    .value(HEADER_COLUMN, dto.getHeader())
                    .value(EVENT_COLUMN, dto.getEvent());

        }
        return insertArray;
    }
}
