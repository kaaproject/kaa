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

package org.kaaproject.kaa.server.appenders.kafka.appender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.appenders.kafka.appender.gen.Level;
import org.kaaproject.kaa.server.appenders.kafka.appender.gen.LogData;
import org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaAcknowledgement;
import org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaCompression;
import org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaConfig;
import org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaServer;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;

public class KafkaLogAppenderTest {

    public static final String TOPIC_NAME = "kaa_test";
    private static final Random RANDOM = new Random();

    private LogAppender logAppender;
    private LogAppenderDto appenderDto;
    private KafkaConfig configuration;

    private RecordHeader header;

    private String appToken;
    private String endpointKeyHash;

    private AvroByteArrayConverter<LogData> logDataConverter = new AvroByteArrayConverter<>(LogData.class);

    private BaseLogEventPack generateLogEventPack(int count) throws IOException {
        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", endpointKeyHash, 1, "", 1, "");
        List<LogEvent> events = new ArrayList<>(count);
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, System.currentTimeMillis(), 2, events);
        for (int i = 0; i < count; i++) {
            LogEvent event = new LogEvent();
            event.setLogData(logDataConverter.toByteArray(new LogData(Level.DEBUG, UUID.randomUUID().toString())));
            events.add(event);
        }
        LogSchemaDto logSchemaDto = new LogSchemaDto();
        logSchemaDto.setApplicationId(String.valueOf(RANDOM.nextInt()));
        logSchemaDto.setId(String.valueOf(RANDOM.nextInt()));
        logSchemaDto.setCreatedTime(System.currentTimeMillis());

        logEventPack.setLogSchema(new LogSchema(logSchemaDto, LogData.getClassSchema().toString()));
        return logEventPack;
    }

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
        appenderDto.setApplicationToken(appToken);

        header = new RecordHeader();
        header.setApplicationToken(appToken);
        header.setEndpointKeyHash(endpointKeyHash);
        header.setHeaderVersion(1);
        header.setTimestamp(System.currentTimeMillis());

        configuration = new KafkaConfig();
        List<KafkaServer> servers = new ArrayList<KafkaServer>();
        servers.add(new KafkaServer("localhost", 9092));
        configuration.setKafkaServers(servers);
        configuration.setKafkaAcknowledgement(KafkaAcknowledgement.ONE);
        configuration.setTopic(TOPIC_NAME);
        configuration.setBufferMemorySize(33554432L);
        configuration.setKafkaCompression(KafkaCompression.NONE);
        configuration.setRetries(0);
        configuration.setExecutorThreadPoolSize(10);
        configuration.setPartitionCount(1);
        configuration.setUseDefaultPartitioner(true);

        AvroByteArrayConverter<KafkaConfig> converter = new AvroByteArrayConverter<>(KafkaConfig.class);
        byte[] rawConfiguration = converter.toByteArray(configuration);
        appenderDto.setRawConfiguration(rawConfiguration);

        logAppender = new KafkaLogAppender();
        logAppender.init(appenderDto);
        logAppender.setApplicationToken(appToken);
    }


    @Test
    @Ignore
    public void doAppendTest() throws IOException, InterruptedException {
        DeliveryCallback callback = new DeliveryCallback();
        logAppender.doAppend(generateLogEventPack(20), callback);
        Thread.sleep(3000);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY, "range");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(props);
        consumer.subscribe(TOPIC_NAME);
        consumer.poll(100);
        consumer.close();
        Assert.assertEquals(20, callback.getSuccessCount());
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
