package org.kaaproject.kaa.server.appenders.cassandra.appender;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.appenders.cassandra.appender.gen.Level;
import org.kaaproject.kaa.server.appenders.cassandra.appender.gen.LogData;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraBatchType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraExecuteRequestType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraServer;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CassandraLogAppenderTest {

    public static final String KEY_SPACE_NAME = "kaa_test";

    @ClassRule
    public static CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("appender_test.cql", KEY_SPACE_NAME));

    private static final Random RANDOM = new Random();

    private LogAppender logAppender;
    private LogAppenderDto appenderDto;
    private CassandraConfig configuration;

    private LogEventPack logEventPack;
    private RecordHeader header;

    private String appToken;
    private String endpointKeyHash;

    private AvroByteArrayConverter<LogData> logDataConverter = new AvroByteArrayConverter<>(LogData.class);
    private AvroByteArrayConverter<CassandraConfig> converter = new AvroByteArrayConverter<>(CassandraConfig.class);

    @Before
    public void beforeTest() throws IOException {
        endpointKeyHash = UUID.randomUUID().toString();
        appToken = String.valueOf(RANDOM.nextInt(Integer.MAX_VALUE));

        appenderDto = new LogAppenderDto();
        appenderDto.setId("Test_id");
        appenderDto.setApplicationToken(appToken);
        appenderDto.setName("Test Name");
        appenderDto.setTenantId(String.valueOf(RANDOM.nextInt()));
        appenderDto.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.values()));

        header = new RecordHeader();
        header.setApplicationToken(appToken);
        header.setEndpointKeyHash(endpointKeyHash);
        header.setHeaderVersion(1);
        header.setTimestamp(System.currentTimeMillis());

        logEventPack = new LogEventPack();
        logEventPack.setDateCreated(System.currentTimeMillis());
        logEventPack.setEndpointKey(endpointKeyHash);

        CassandraServer server = new CassandraServer("127.0.0.1", 9142);
        configuration = new CassandraConfig();
        configuration.setCassandraBatchType(CassandraBatchType.UNLOGGED);
        configuration.setKeySpace(KEY_SPACE_NAME);
        configuration.setCassandraExecuteRequestType(CassandraExecuteRequestType.ASYNC);
        configuration.setCassandraServers(Arrays.asList(server));

        AvroByteArrayConverter<CassandraConfig> converter = new AvroByteArrayConverter<>(CassandraConfig.class);
        byte[] rawConfiguration = converter.toByteArray(configuration);
        appenderDto.setRawConfiguration(rawConfiguration);

        logAppender = new CassandraLogAppender();
        logAppender.init(appenderDto);
    }

    @Test
    public void doAppendTest() throws IOException {
        logAppender.doAppend(generateLogEventPack(20), new LogDeliveryCallback() {
            @Override
            public void onSuccess() {

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
        });
        CassandraLogEventDao logEventDao = (CassandraLogEventDao) ReflectionTestUtils.getField(logAppender, "logEventDao");
        Session session = (Session) ReflectionTestUtils.getField(logEventDao, "session");
        ResultSet resultSet = session.execute(QueryBuilder.select().countAll().from(KEY_SPACE_NAME, "logs_" + appToken));
        Row row = resultSet.one();
        Assert.assertEquals(20L, row.getLong(0));
    }

    private LogEventPack generateLogEventPack(int count) throws IOException {
        LogEventPack logEventPack = new LogEventPack();
        List<LogEvent> events = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            LogEvent event = new LogEvent();
            event.setLogData(logDataConverter.toByteArray(new LogData(Level.DEBUG, UUID.randomUUID().toString())));
            events.add(event);
        }
        logEventPack.setDateCreated(System.currentTimeMillis());
        logEventPack.setEndpointKey(endpointKeyHash);

        logEventPack.setLogSchemaVersion(2);
        logEventPack.setEvents(events);
        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(String.valueOf(RANDOM.nextInt()));
        logSchemaDto.setId(String.valueOf(RANDOM.nextInt()));
        logSchemaDto.setCreatedTime(System.currentTimeMillis());
        logSchemaDto.setSchema(LogData.getClassSchema().toString());

        logEventPack.setLogSchema(new LogSchema(logSchemaDto));
        return logEventPack;
    }

}
