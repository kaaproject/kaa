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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.appenders.cassandra.appender.gen.Level;
import org.kaaproject.kaa.server.appenders.cassandra.appender.gen.LogData;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraBatchType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraExecuteRequestType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraServer;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ClusteringElement;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ColumnMappingElement;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ColumnMappingElementType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ColumnType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.OrderType;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.springframework.test.util.ReflectionTestUtils;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class CassandraLogAppenderTest {

    public static final String KEY_SPACE_NAME = "kaa_test";

    @ClassRule
    public static CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("appender_test.cql", KEY_SPACE_NAME));

    private static final Random RANDOM = new Random();

    private LogAppender logAppender;
    private LogAppenderDto appenderDto;
    private CassandraConfig configuration;

    private RecordHeader header;

    private String appToken;
    private String endpointKeyHash;
    private EndpointProfileDataDto profileDto;

    private AvroByteArrayConverter<LogData> logDataConverter = new AvroByteArrayConverter<>(LogData.class);

    @Before
    public void beforeTest() throws IOException {
        endpointKeyHash = UUID.randomUUID().toString();
        profileDto = new EndpointProfileDataDto("1", endpointKeyHash, 1, "", "1", "");
        appToken = String.valueOf(RANDOM.nextInt(Integer.MAX_VALUE));

        appenderDto = new LogAppenderDto();
        appenderDto.setId("Test_id");
        appenderDto.setApplicationToken(appToken);
        appenderDto.setName("Test Name");
        appenderDto.setTenantId(String.valueOf(RANDOM.nextInt()));
        appenderDto.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.values()));
        appenderDto.setApplicationToken(appToken);

        header = new RecordHeader();
        header.setApplicationToken(appToken);
        header.setEndpointKeyHash(endpointKeyHash);
        header.setHeaderVersion(1);
        header.setTimestamp(System.currentTimeMillis());

        CassandraServer server = new CassandraServer("127.0.0.1", 9142);
        configuration = new CassandraConfig();
        configuration.setCassandraBatchType(CassandraBatchType.UNLOGGED);
        configuration.setKeySpace(KEY_SPACE_NAME);
        configuration.setTableNamePattern("logs_$app_token_$config_hash");
        configuration.setCassandraExecuteRequestType(CassandraExecuteRequestType.ASYNC);
        configuration.setCassandraServers(Arrays.asList(server));
        configuration.setCallbackThreadPoolSize(3);
        configuration.setExecutorThreadPoolSize(3);

        List<ColumnMappingElement> columnMapping = new ArrayList<ColumnMappingElement>();
        columnMapping.add(new ColumnMappingElement(ColumnMappingElementType.HEADER_FIELD, "endpointKeyHash", "endpointKeyHash",
                ColumnType.TEXT, true, false));
        columnMapping.add(new ColumnMappingElement(ColumnMappingElementType.EVENT_JSON, "", "event_json", ColumnType.TEXT, false, false));
        columnMapping.add(new ColumnMappingElement(ColumnMappingElementType.UUID, "", "binid", ColumnType.UUID, false, true));

        configuration.setColumnMapping(columnMapping);

        List<ClusteringElement> clusteringMapping = new ArrayList<ClusteringElement>();
        clusteringMapping.add(new ClusteringElement("binid", OrderType.DESC));
        configuration.setClusteringMapping(clusteringMapping);

        AvroByteArrayConverter<CassandraConfig> converter = new AvroByteArrayConverter<>(CassandraConfig.class);
        byte[] rawConfiguration = converter.toByteArray(configuration);
        appenderDto.setRawConfiguration(rawConfiguration);

        logAppender = new CassandraLogAppender();
        logAppender.init(appenderDto);
        logAppender.setApplicationToken(appToken);
    }

    @Test
    public void doAppendTest() throws IOException, InterruptedException {
        DeliveryCallback callback = new DeliveryCallback();
        logAppender.doAppend(generateLogEventPack(20), callback);
        Thread.sleep(3000);
        CassandraLogEventDao logEventDao = (CassandraLogEventDao) ReflectionTestUtils.getField(logAppender, "logEventDao");
        Session session = (Session) ReflectionTestUtils.getField(logEventDao, "session");
        ResultSet resultSet = session.execute(QueryBuilder.select().countAll()
                .from(KEY_SPACE_NAME, "logs_" + appToken + "_" + Math.abs(configuration.hashCode())));
        Row row = resultSet.one();
        Assert.assertEquals(20L, row.getLong(0));
        Assert.assertEquals(1, callback.getSuccessCount());
    }

    private BaseLogEventPack generateLogEventPack(int count) throws IOException {
        List<LogEvent> events = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            LogEvent event = new LogEvent();
            event.setLogData(logDataConverter.toByteArray(new LogData(Level.DEBUG, UUID.randomUUID().toString())));
            events.add(event);
        }
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, System.currentTimeMillis(), 2, events);
        
        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(String.valueOf(RANDOM.nextInt()));
        logSchemaDto.setId(String.valueOf(RANDOM.nextInt()));
        logSchemaDto.setCreatedTime(System.currentTimeMillis());
        logSchemaDto.setSchema(LogData.getClassSchema().toString());

        logEventPack.setLogSchema(new LogSchema(logSchemaDto));
        return logEventPack;
    }

    class DeliveryCallback implements LogDeliveryCallback {

        private AtomicInteger successCount = new AtomicInteger();

        @Override
        public void onSuccess() {
            successCount.incrementAndGet();
        }

        @Override
        public void onInternalError() {

        }

        @Override
        public void onConnectionError() {

        }

        @Override
        public void onRemoteError() {

        }

        public int getSuccessCount() {
            return successCount.get();
        }
    }
}
