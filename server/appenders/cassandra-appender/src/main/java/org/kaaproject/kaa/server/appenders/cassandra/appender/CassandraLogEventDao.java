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

package org.kaaproject.kaa.server.appenders.cassandra.appender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraBatchType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraCompression;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraCredential;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraServer;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraSocketOption;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraWriteConsistencyLevel;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ClusteringElement;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ColumnMappingElement;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class CassandraLogEventDao implements LogEventDao {

    private static final String $CONFIG_HASH = "$config_hash";

    private static final String $APP_TOKEN = "$app_token";

    private static final Logger LOG = LoggerFactory.getLogger(CassandraLogEventDao.class);

    private static final String TABLE_NAME = "$table_name";
    private static final String KEYSPACE_NAME = "$keyspace_name";
    private static final String COLUMNS_DEFINITION = "$columns_definition";
    private static final String PRIMARY_KEY_DEFINITION = "$primary_key_definition";
    private static final String CLUSTERING_DEFINITION = "$clustering_definition";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $keyspace_name.$table_name ("
            + "$columns_definition PRIMARY KEY ( $primary_key_definition )) $clustering_definition;";

    private static final String ABSENT_CLIENT_PROFILE_ERROR = "Client profile is not set!";
    private static final String ABSENT_SERVER_PROFILE_ERROR = "Server profile is not set!";

    private final ConcurrentMap<String, ThreadLocal<SimpleDateFormat>> dateFormatMap = new ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>();

    private Cluster cluster;
    private Session session;
    private CassandraBatchType batchType;
    private ConsistencyLevel writeConsistencyLevel;
    private String keyspaceName;
    private CassandraConfig configuration;

    public CassandraLogEventDao(CassandraConfig configuration) throws UnknownHostException {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration shouldn't be null");
        }
        LOG.info("Init cassandra log event dao...");
        this.configuration = configuration;
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

    @Override
    public String createTable(String appToken) {
        // We need to add hash code of configuration in order to make sure that
        // we are working with up-to-date instance of the table.
        String tableName = toFullTableName(appToken);
        LOG.info("Create table {} in cassandra keyspace {}", tableName, keyspaceName);
        String createTableStmt = CREATE_TABLE.replace(TABLE_NAME, tableName);
        createTableStmt = createTableStmt.replace(KEYSPACE_NAME, keyspaceName);
        createTableStmt = createTableStmt.replace(COLUMNS_DEFINITION, generateColumnsDefinition(configuration));
        createTableStmt = createTableStmt.replace(PRIMARY_KEY_DEFINITION, generatePrimaryKeyDefinition(configuration));
        createTableStmt = createTableStmt.replace(CLUSTERING_DEFINITION, generateClusteringDefinition(configuration));
        LOG.info("Executing table creation stmt {}", createTableStmt);
        getSession().execute(createTableStmt);
        return tableName;
    }

    private String toFullTableName(String appToken) {
        String tableName = configuration.getTableNamePattern();
        tableName = tableName.replace($APP_TOKEN, appToken);
        tableName = tableName.replace($CONFIG_HASH, Integer.toString(Math.abs(configuration.hashCode())));
        return tableName;
    }

    private static String generateColumnsDefinition(CassandraConfig configuration) {
        List<ColumnMappingElement> mappings = configuration.getColumnMapping();
        StringBuilder sb = new StringBuilder();
        for (ColumnMappingElement element : mappings) {
            sb.append(element.getColumnName());
            sb.append(" ");
            sb.append(element.getColumnType().name().toLowerCase());
            sb.append(" , ");
        }
        return sb.toString();
    }

    private static String generatePrimaryKeyDefinition(CassandraConfig configuration) {
        List<ColumnMappingElement> mappings = configuration.getColumnMapping();
        StringBuilder partitionKey = new StringBuilder();
        StringBuilder clusteringKey = new StringBuilder();
        for (ColumnMappingElement element : mappings) {
            if (element.getPartitionKey()) {
                if (partitionKey.length() > 0) {
                    partitionKey.append(",");
                }
                partitionKey.append(element.getColumnName());
            }
            if (element.getClusteringKey()) {
                if (clusteringKey.length() > 0) {
                    clusteringKey.append(" , ");
                }
                clusteringKey.append(element.getColumnName());
            }
        }
        String primaryKey = "";
        if (partitionKey.length() > 0) {
            primaryKey += "(" + partitionKey + ")";
        }
        if (clusteringKey.length() > 0) {
            primaryKey += " , " + clusteringKey;
        }
        return primaryKey;
    }

    private static String generateClusteringDefinition(CassandraConfig configuration) {
        List<ClusteringElement> mapping = configuration.getClusteringMapping();
        if (mapping != null && mapping.size() > 0) {
            StringBuilder columnList = new StringBuilder();
            for (ClusteringElement element : mapping) {
                if (columnList.length() > 0) {
                    columnList.append(" , ");
                }
                columnList.append(element.getColumnName());
                columnList.append(" ");
                columnList.append(element.getOrder());
            }
            return "WITH CLUSTERING ORDER BY (" + columnList.toString() + ")";
        } else {
            return "";
        }
    }

    @Override
    public List<CassandraLogEventDto> save(List<CassandraLogEventDto> logEventDtoList, String tableName,
            GenericAvroConverter<GenericRecord> eventConverter, GenericAvroConverter<GenericRecord> headerConverter,
            GenericAvroConverter<GenericRecord> clientProfileConverter, GenericAvroConverter<GenericRecord> serverProfileConverter,
            String clientProfileJson, String serverProfileJson)
            throws IOException {
        LOG.debug("Execute bath request for cassandra table {}", tableName);
        executeBatch(prepareQuery(logEventDtoList, tableName, eventConverter, headerConverter,
                clientProfileConverter, serverProfileConverter, clientProfileJson, serverProfileJson));
        return logEventDtoList;
    }

    @Override
    public ListenableFuture<ResultSet> saveAsync(List<CassandraLogEventDto> logEventDtoList, String tableName,
            GenericAvroConverter<GenericRecord> eventConverter, GenericAvroConverter<GenericRecord> headerConverter,
            GenericAvroConverter<GenericRecord> clientProfileConverter, GenericAvroConverter<GenericRecord> serverProfileConverter,
            String clientProfileJson, String serverProfileJson)
            throws IOException {
        LOG.debug("Execute async bath request for cassandra table {}", tableName);
        return executeBatchAsync(prepareQuery(logEventDtoList, tableName, eventConverter, headerConverter,
                clientProfileConverter, serverProfileConverter, clientProfileJson, serverProfileJson));
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

    private Insert[] prepareQuery(List<CassandraLogEventDto> logEventDtoList, String collectionName,
            GenericAvroConverter<GenericRecord> eventConverter, GenericAvroConverter<GenericRecord> headerConverter,
            GenericAvroConverter<GenericRecord> clientProfileConverter, GenericAvroConverter<GenericRecord> serverProfileConverter,
            String clientProfileJson, String serverProfileJson)
            throws IOException {
        String reuseTsValue = null;
        Insert[] insertArray = new Insert[logEventDtoList.size()];

        // Process client profile data
        GenericRecord clientProfile = null;
        ByteBuffer clientProfileBinary = null;
        if (clientProfileConverter != null) {
            clientProfile = clientProfileConverter.decodeJson(clientProfileJson);
            clientProfileBinary = ByteBuffer.wrap(clientProfileConverter.encode(clientProfile));
        }

        // Process server profile data
        GenericRecord serverProfile = null;
        ByteBuffer serverProfileBinary = null;
        if (serverProfileConverter != null) {
            serverProfile = serverProfileConverter.decodeJson(serverProfileJson);
            serverProfileBinary = ByteBuffer.wrap(serverProfileConverter.encode(serverProfile));
        }

        for (int i = 0; i < logEventDtoList.size(); i++) {
            CassandraLogEventDto dto = logEventDtoList.get(i);
            Insert insert = QueryBuilder.insertInto(keyspaceName, collectionName);
            for (ColumnMappingElement element : configuration.getColumnMapping()) {
                switch (element.getType()) {
                case HEADER_FIELD:
                    insert.value(element.getColumnName(),
                            formatField(element.getColumnType(), dto.getHeader().get(element.getValue())));
                    break;
                case EVENT_FIELD:
                    insert.value(element.getColumnName(),
                            formatField(element.getColumnType(), dto.getEvent().get(element.getValue())));
                    break;
                case CLIENT_FIELD:
                    if (clientProfile != null) {
                        insert.value(element.getColumnName(), formatField(element.getColumnType(), clientProfile.get(element.getValue())));
                    } else {
                        throw new RuntimeException(ABSENT_CLIENT_PROFILE_ERROR);
                    }
                    break;
                case SERVER_FIELD:
                    if (serverProfile != null) {
                        insert.value(element.getColumnName(), formatField(element.getColumnType(), serverProfile.get(element.getValue())));
                    } else {
                        throw new RuntimeException(ABSENT_SERVER_PROFILE_ERROR);
                    }
                    break;
                case HEADER_JSON:
                    insert.value(element.getColumnName(), headerConverter.encodeToJson(dto.getHeader()));
                    break;
                case HEADER_BINARY:
                    insert.value(element.getColumnName(), ByteBuffer.wrap(headerConverter.encode(dto.getHeader())));
                    break;
                case EVENT_JSON:
                    insert.value(element.getColumnName(), eventConverter.encodeToJson(dto.getEvent()));
                    break;
                case EVENT_BINARY:
                    insert.value(element.getColumnName(), ByteBuffer.wrap(eventConverter.encode(dto.getEvent())));
                    break;
                case CLIENT_JSON:
                    if (clientProfileJson != null) {
                        insert.value(element.getColumnName(), clientProfileJson);
                    }
                    else {
                        throw new RuntimeException(ABSENT_CLIENT_PROFILE_ERROR);
                    }
                    break;
                case CLIENT_BINARY:
                    if (clientProfileBinary != null) {
                        insert.value(element.getColumnName(), clientProfileBinary);
                    } else {
                        throw new RuntimeException(ABSENT_CLIENT_PROFILE_ERROR);
                    }
                    break;
                case SERVER_JSON:
                    if (serverProfileJson != null) {
                        insert.value(element.getColumnName(), serverProfileJson);
                    }
                    else {
                        throw new RuntimeException(ABSENT_SERVER_PROFILE_ERROR);
                    }
                case SERVER_BINARY:
                    if (serverProfileBinary != null) {
                        insert.value(element.getColumnName(), clientProfileBinary);
                    } else {
                        throw new RuntimeException(ABSENT_SERVER_PROFILE_ERROR);
                    }
                    break;
                case UUID:
                    insert.value(element.getColumnName(), UUID.randomUUID());
                    break;
                case TS:
                    reuseTsValue = formatTs(reuseTsValue, element);
                    insert.value(element.getColumnName(), reuseTsValue);
                    break;
                }
            }

            // Here we get ttl parameter from config and add it to insert query
            insert.using(QueryBuilder.ttl(configuration.getDataTTL()));

            insertArray[i] = insert;

        }
        return insertArray;
    }

    private String formatTs(String tsValue, ColumnMappingElement element) {
        if (tsValue == null) {
            long ts = System.currentTimeMillis();
            final String pattern = element.getValue();
            if (pattern == null || pattern.isEmpty()) {
                tsValue = ts + "";
            } else {
                ThreadLocal<SimpleDateFormat> formatterTL = dateFormatMap.get(pattern);
                if (formatterTL == null) {
                    formatterTL = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    dateFormatMap.putIfAbsent(pattern, formatterTL);
                }
                SimpleDateFormat formatter = formatterTL.get();
                if (formatter == null) {
                    formatter = new SimpleDateFormat(pattern);
                    formatterTL.set(formatter);
                }
                tsValue = formatter.format(new Date(ts));
            }
        }
        return tsValue;
    }

    private Object formatField(ColumnType type, Object elementValue) {
        if (type == ColumnType.TEXT && elementValue != null) {
            return elementValue.toString();
        } else {
            return elementValue;
        }
    }
}
