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

package org.kaaproject.kaa.server.appenders.oraclenosql.appender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import oracle.kv.KVSecurityConstants;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.KVVersion;
import oracle.kv.Key;
import oracle.kv.Operation;
import oracle.kv.OperationFactory;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;
import oracle.kv.impl.api.avro.AvroDdl;
import oracle.kv.impl.api.avro.AvroDdl.AddSchemaOptions;
import oracle.kv.impl.api.avro.AvroDdl.AddSchemaResult;
import oracle.kv.impl.api.avro.AvroDdl.SchemaDetails;
import oracle.kv.impl.api.avro.AvroDdl.SchemaSummary;
import oracle.kv.impl.api.avro.AvroSchemaMetadata;
import oracle.kv.impl.api.avro.AvroSchemaStatus;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.kaaproject.kaa.server.common.log.shared.RecordWrapperSchemaGenerator;
import org.kaaproject.kaa.server.common.log.shared.appender.CustomLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleNoSqlLogAppender extends CustomLogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(OracleNoSqlLogAppender.class);

    /**
     * Configuration properties constants
     */
    private static final String STORE_NAME = "storeName";
    private static final String HELPER_HOST_PORT = "helperHostPort";

    /**
     * Default values
     */
    private static final String DEFAULT_STORE_NAME = "kvstore";
    private static final String DEFAULT_HELPER_HOST_PORT = "localhost:5000";
    private static final String HELPER_HOST_PORT_SEPARATOR = ",";

    private boolean closed = false;

    private KVStore kvStore;
    private String username;
    private GenericAvroBinding binding;
    private GenericRecord wrapperRecord;
    private BinaryDecoder binaryDecoder;
    private DatumReader<GenericRecord> datumReader;

    public OracleNoSqlLogAppender() {
        super();
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            if (kvStore != null) {
                LOG.debug("[{}] appending {} logs to Oracle NoSQL kvStore", this.getApplicationToken(), logEventPack.getEvents()
                        .size());
                try {
                    doAppendGenericAvro(logEventPack, header);
                } catch (Exception e) {
                    LOG.error("Unable to append logs!", e);
                }
            } else {
                LOG.info("[{}] Attempted to append to empty kvStore.", getName());
            }
        } else {
            LOG.info("[{}] Attempted to append to closed appender.", getName());
        }
    }
    
    @Override
    protected void initFromProperties(Properties properties) {
        try {
            kvStore = initKvStore(properties);
        } catch (Exception e) {
            LOG.error("Failed to init kvStore: ", e);
        }
    }
    
    private void doAppendGenericAvro(LogEventPack logEventPack, RecordHeader header) throws Exception {
        if (binding == null) {
            initialize(logEventPack);
        }
        
        GenericRecord recordData = null;
        
        OperationFactory of = kvStore.getOperationFactory();
        ArrayList<Operation> opList = new ArrayList<Operation>();
        
        List<String> majorPath = Arrays.asList(getApplicationToken(), 
                                               logEventPack.getLogSchemaVersion()+"", 
                                               logEventPack.getEndpointKey(), 
                                               System.currentTimeMillis()+"");

        int counter = 0;

        for (LogEvent event : logEventPack.getEvents()) {
            binaryDecoder = DecoderFactory.get().binaryDecoder(event.getLogData(), binaryDecoder);
            try {
                recordData = datumReader.read(recordData, binaryDecoder);
            } catch (IOException e) {
                LOG.error("[{}] Unable to read log event!", e);
                continue;
            }
            wrapperRecord.put(RecordWrapperSchemaGenerator.RECORD_HEADER_FIELD, header);
            wrapperRecord.put(RecordWrapperSchemaGenerator.RECORD_DATA_FIELD, recordData);
            
            Key key = Key.createKey(majorPath,
                                    Arrays.asList((counter++)+""));
            
            opList.add(of.createPut(key, binding.toValue(wrapperRecord)));
        }
        
        kvStore.execute(opList);
    }
    
    private void initialize(LogEventPack logEventPack) throws Exception {
        try {
            Schema recordWrapperSchema = RecordWrapperSchemaGenerator.generateRecordWrapperSchema(logEventPack.getLogSchema().getSchema());
            checkSchemaUploaded(recordWrapperSchema);
            AvroCatalog avroCatalog = kvStore.getAvroCatalog();
            binding = avroCatalog.getGenericBinding(recordWrapperSchema);
            Schema userSchema = new Schema.Parser().parse(logEventPack.getLogSchema().getSchema());
            datumReader = new GenericDatumReader<GenericRecord>(userSchema);
            wrapperRecord = new GenericData.Record(recordWrapperSchema);
        } catch (Exception e) {
            LOG.error("[{}] Unable to initialize parameters for log event pack.", getName());
            throw e;
        }
        
    }
    
    private void checkSchemaUploaded(Schema schema) {
        String schemaText = schema.toString(true);
        AvroDdl avroDdl = new AvroDdl(kvStore);
        SortedMap<String, SchemaSummary> schemaSummaries = avroDdl.getSchemaSummaries(false);
        boolean uploaded = false;
        boolean evolve = schemaSummaries.containsKey(schema.getFullName());
        
        if (evolve) {
            for (String key : schemaSummaries.keySet()) {
                SchemaSummary summary = schemaSummaries.get(key);
                if (checkSchemaUploaded(avroDdl, summary, schemaText)) {
                    uploaded = true;
                    break;
                }
            }
        }
        
        if (!uploaded) {
            AvroSchemaMetadata metadata = new AvroSchemaMetadata(AvroSchemaStatus.ACTIVE, 
                    System.currentTimeMillis(), username, getHostName());
            
            AddSchemaOptions options =
                    new AddSchemaOptions(evolve, true);
            
            AddSchemaResult result = avroDdl.addSchema(metadata, schemaText, options, KVVersion.CURRENT_VERSION);
            
            LOG.info("[{}] Uploaded new schema to store, extra message [{}].", getName(), result.getExtraMessage());
        }
    }
    
    private boolean checkSchemaUploaded(AvroDdl avroDdl, SchemaSummary summary, String schemaText) {
        SchemaDetails details = avroDdl.getSchemaDetails(summary.getId());
        if (details.getText().equals(schemaText)) {
            return true;
        }
        else if (summary.getPreviousVersion() != null) {
            return checkSchemaUploaded(avroDdl, summary.getPreviousVersion(), schemaText);
        }
        return false;
    }
    
    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "";
        }
    }


    @Override
    public void close() {
        closed = true;
        kvStore.close();
        LOG.debug("Stopped Oracle NoSQL log appender.");
    }

    private KVStore initKvStore(Properties properties) throws Exception {
        String storeName = properties.getProperty(STORE_NAME, DEFAULT_STORE_NAME);
        String helperHostPort = properties.getProperty(HELPER_HOST_PORT, DEFAULT_HELPER_HOST_PORT);
        String[] helperHostPorts = helperHostPort.split(HELPER_HOST_PORT_SEPARATOR);
        
        KVStoreConfig config = new KVStoreConfig(storeName, helperHostPorts);
        config.setSecurityProperties(properties);
        
        username = properties.getProperty(KVSecurityConstants.AUTH_USERNAME_PROPERTY, "");
        
        KVStore kvStore = KVStoreFactory.getStore(config);
        
        return kvStore;
    }
 
}
