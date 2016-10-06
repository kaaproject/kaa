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

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaConfig;
import org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

public class KafkaLogEventDao implements LogEventDao {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaLogEventDao.class);

  private static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
  private static final String VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

  private static final Random RANDOM = new Random();

  private KafkaProducer<String, String> producer;
  private KafkaConfig configuration;
  private String topicName;
  private int partitionCount;

  /**
   * Instantiates a new KafkaLogEventDao.
   */
  public KafkaLogEventDao(KafkaConfig configuration) {
    if (configuration == null) {
      throw new IllegalArgumentException("Configuration shouldn't be null");
    }
    LOG.info("Init kafka log event dao...");
    this.configuration = configuration;
    this.topicName = configuration.getTopic();
    this.partitionCount = configuration.getPartitionCount();
    StringBuilder serverList = new StringBuilder();
    for (KafkaServer server : configuration.getKafkaServers()) {
      serverList.append(server.getHost() + ":" + server.getPort() + ",");
    }
    serverList = serverList.deleteCharAt(serverList.length() - 1);
    LOG.info("Init kafka cluster with property {}={}", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverList);
    Properties kafkaProperties = new Properties();
    kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverList.toString());
    LOG.info("Init kafka cluster with property {}={}", ProducerConfig.ACKS_CONFIG,
        configuration.getKafkaAcknowledgement());
    kafkaProperties.put(ProducerConfig.ACKS_CONFIG, parseAcknowledgement(configuration.getKafkaAcknowledgement()
        .name()));
    LOG.info("Init kafka cluster with property {}={}", ProducerConfig.BUFFER_MEMORY_CONFIG,
        configuration.getBufferMemorySize());
    kafkaProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, configuration.getBufferMemorySize());
    LOG.info("Init kafka cluster with property {}={}", ProducerConfig.COMPRESSION_TYPE_CONFIG,
        configuration.getKafkaCompression());
    kafkaProperties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, configuration.getKafkaCompression().name()
        .toLowerCase());
    LOG.info("Init kafka cluster with property {}={}", ProducerConfig.RETRIES_CONFIG, configuration.getRetries());
    kafkaProperties.put(ProducerConfig.RETRIES_CONFIG, configuration.getRetries());
    LOG.info("Init kafka cluster with property {}={}", ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
    kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
    LOG.info("Init kafka cluster with property {}={}", ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        VALUE_SERIALIZER);
    kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER);
    producer = new KafkaProducer<String, String>(kafkaProperties);
  }

  @Override
  public List<Future<RecordMetadata>> save(List<KafkaLogEventDto> logEventDtoList,
                                           GenericAvroConverter<GenericRecord> eventConverter, GenericAvroConverter<GenericRecord> headerConverter,
                                           Callback callback) throws IOException {
    List<Future<RecordMetadata>> results = new ArrayList<Future<RecordMetadata>>();
    LOG.info("[{}] Sending events to Kafka using {} key defining strategy", topicName, configuration
        .getKafkaKeyType().toString());
    for (KafkaLogEventDto dto : logEventDtoList) {
      ProducerRecord<String, String> recordToWrite;
      if (configuration.getUseDefaultPartitioner()) {
        recordToWrite = new ProducerRecord<String, String>(topicName, getKey(dto), formKafkaJson(dto,
            eventConverter, headerConverter));
      } else {
        recordToWrite = new ProducerRecord<String, String>(topicName, calculatePartitionId(dto), getKey(dto),
            formKafkaJson(dto, eventConverter, headerConverter));
      }
      results.add(producer.send(recordToWrite, callback));
    }
    return results;
  }

  @Override
  public void close() {
    LOG.info("Close connection to kafka cluster.");
    if (producer != null) {
      producer.close();
    }
  }

  private int calculatePartitionId(KafkaLogEventDto eventDto) {
    return eventDto.hashCode() % partitionCount;
  }

  private String parseAcknowledgement(String record) {
    switch (record) {
      case "ALL":
        return "all";
      case "ZERO":
        return "0";
      case "ONE":
        return "1";
      case "TWO":
        return "2";
      default:
        return "";
    }
  }

  private String formKafkaJson(KafkaLogEventDto dto, GenericAvroConverter<GenericRecord> eventConverter,
                               GenericAvroConverter<GenericRecord> headerConverter) throws IOException {
    String eventJson = eventConverter.encodeToJson(dto.getEvent());
    String headerJson = headerConverter.encodeToJson(dto.getHeader());
    StringBuilder result = new StringBuilder("{");
    if (headerJson != null && !headerJson.isEmpty()) {
      result.append("\"header\":" + headerJson + ",");
    }
    result.append("\"event\":" + eventJson + "}");
    return result.toString();
  }

  private String getKey(KafkaLogEventDto dto) {
    switch (configuration.getKafkaKeyType()) {
      case ENDPOINTHASHKEY:
        return dto.getHeader().getEndpointKeyHash();
      case UUID:
        return new UUID(System.currentTimeMillis(), RANDOM.nextLong()).toString();
      case HASH:
        return "" + dto.hashCode();
      default:
        return null;
    }
  }
}
