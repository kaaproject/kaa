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

import com.datastax.driver.core.ResultSet;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.DataMappingElement;

import java.io.IOException;
import java.util.List;

public interface LogEventDao {

    void createTable(String collectionName, DataMappingElement mappingElement);

    ListenableFuture<List<ResultSet>> save(List<CassandraLogEventDto> logEventDtoList,
                                           GenericAvroConverter<GenericRecord> eventConverter, GenericAvroConverter<GenericRecord> headerConverter,
                                           GenericAvroConverter<GenericRecord> clientProfileConverter, GenericAvroConverter<GenericRecord> serverProfileConverter,
                                           String clientProfileJson, String serverProfileJson) throws IOException;

    void removeAll(String collectionName);

    void close();
}
