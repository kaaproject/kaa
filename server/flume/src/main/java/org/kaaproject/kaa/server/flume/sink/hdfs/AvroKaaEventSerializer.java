/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.server.flume.sink.hdfs;

import static org.apache.flume.serialization.AvroEventSerializerConfigurationConstants.COMPRESSION_CODEC;
import static org.apache.flume.serialization.AvroEventSerializerConfigurationConstants.DEFAULT_COMPRESSION_CODEC;
import static org.apache.flume.serialization.AvroEventSerializerConfigurationConstants.DEFAULT_SYNC_INTERVAL_BYTES;
import static org.apache.flume.serialization.AvroEventSerializerConfigurationConstants.SYNC_INTERVAL_BYTES;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.Configurable;
import org.apache.flume.serialization.EventSerializer;
import org.kaaproject.kaa.server.common.log.shared.RecordWrapperSchemaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvroKaaEventSerializer implements EventSerializer, Configurable,
        EventConstants {

    private static final Logger LOG = LoggerFactory.getLogger(AvroKaaEventSerializer.class);

    private final OutputStream out;
    
    private DatumReader<GenericRecord> datumReader;
    private BinaryDecoder binaryDecoder;
    
    private DatumWriter<Object> writer = null;
    private DataFileWriter<Object> dataFileWriter = null;
    private GenericRecord wrapperRecord;

    private int syncIntervalBytes;
    private String compressionCodec;
    private static Map<KaaSinkKey, Schema> schemaCache = new HashMap<KaaSinkKey, Schema>();

    private AvroSchemaSource schemaSource;

    private AvroKaaEventSerializer(OutputStream out) {
        this.out = out;
        this.schemaSource = new AvroSchemaSource();
    }

    @Override
    public void configure(Context context) {
        syncIntervalBytes = context.getInteger(SYNC_INTERVAL_BYTES,
                DEFAULT_SYNC_INTERVAL_BYTES);
        compressionCodec = context.getString(COMPRESSION_CODEC,
                DEFAULT_COMPRESSION_CODEC);
        schemaSource.configure(context);
    }

    @Override
    public void afterCreate() throws IOException {
        // no-op
    }

    @Override
    public void afterReopen() throws IOException {
        // impossible to initialize DataFileWriter without writing the schema?
        throw new UnsupportedOperationException(
                "Avro API doesn't support append");
    }

    @Override
    public void write(Event event) throws IOException {
        if (dataFileWriter == null) {
            initialize(event);
        }
        if (!(event instanceof KaaRecordEvent)) {
            throw new IOException("Not instance of KaaRecordEvent!");
        }
        KaaRecordEvent kaaRecordEvent = (KaaRecordEvent)event;
        
        binaryDecoder = DecoderFactory.get().binaryDecoder(kaaRecordEvent.getBody(), binaryDecoder);
        GenericRecord recordData = datumReader.read(null, binaryDecoder);
        
        wrapperRecord.put(RecordWrapperSchemaGenerator.RECORD_HEADER_FIELD, kaaRecordEvent.getRecordHeader());
        wrapperRecord.put(RecordWrapperSchemaGenerator.RECORD_DATA_FIELD, recordData);
        
        dataFileWriter.append(wrapperRecord);
    }

    private void initialize(Event event) throws IOException {
        Schema schema = null;
        Schema wrapperSchema = null;
        KaaSinkKey key = new KaaSinkKey(event.getHeaders());
        schema = schemaCache.get(key);
        if (schema == null) {
            try {
                schema = schemaSource.loadByKey(key);
            } catch (Exception e) {
                LOG.error("Unable to load schema by key {}", key);
                LOG.error("Caused by: ", e);
                throw new FlumeException("Could not find schema for event "
                        + event);
            }
            schemaCache.put(key, schema);
        }

        if (schema == null) {
            String schemaString = event.getHeaders().get(
                    AVRO_SCHEMA_LITERAL_HEADER);
            if (schemaString == null) {
                throw new FlumeException("Could not find schema for event "
                        + event);
            }
            schema = new Schema.Parser().parse(schemaString);
        }
        
        datumReader = new GenericDatumReader<GenericRecord>(schema);
        
        wrapperSchema = RecordWrapperSchemaGenerator.generateRecordWrapperSchema(schema.toString());

        writer = new GenericDatumWriter<Object>(wrapperSchema);
        dataFileWriter = new DataFileWriter<Object>(writer);

        dataFileWriter.setSyncInterval(syncIntervalBytes);

        try {
            CodecFactory codecFactory = CodecFactory
                    .fromString(compressionCodec);
            dataFileWriter.setCodec(codecFactory);
        } catch (AvroRuntimeException e) {
            LOG.warn("Unable to instantiate avro codec with name ("
                    + compressionCodec
                    + "). Compression disabled. Exception follows.", e);
        }

        dataFileWriter.create(wrapperSchema, out);
        wrapperRecord = new GenericData.Record(wrapperSchema);
    }

    @Override
    public void flush() throws IOException {
        if (dataFileWriter != null) {
            dataFileWriter.flush();
        }
    }

    @Override
    public void beforeClose() throws IOException {
        // no-op
    }

    @Override
    public boolean supportsReopen() {
        return false;
    }

    public static class Builder implements EventSerializer.Builder {

        @Override
        public EventSerializer build(Context context, OutputStream out) {
            AvroKaaEventSerializer writer = new AvroKaaEventSerializer(out);
            writer.configure(context);
            return writer;
        }

    }

}