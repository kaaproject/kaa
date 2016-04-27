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
import java.util.List;
import java.util.concurrent.Future;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;

public interface LogEventDao {

    List<Future<RecordMetadata>> save(List<KafkaLogEventDto> logEventDtoList,
            GenericAvroConverter<GenericRecord> eventConverter, GenericAvroConverter<GenericRecord> headerConverter,
            Callback callback) throws IOException;

    void close();
}