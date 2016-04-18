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

package org.kaaproject.kaa.server.appenders.oraclenosql.appender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import oracle.kv.FaultException;
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
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.oraclenosql.config.gen.KvStoreNode;
import org.kaaproject.kaa.server.appenders.oraclenosql.config.gen.OracleNoSqlConfig;
import org.kaaproject.kaa.server.common.log.shared.RecordWrapperSchemaGenerator;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleNoSqlLogAppender extends AbstractLogAppender<OracleNoSqlConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(OracleNoSqlLogAppender.class);

    private boolean closed = false;

    private KVStore kvStore;
    private String username;
    private GenericAvroBinding binding;
    private GenericRecord wrapperRecord;
    private BinaryDecoder binaryDecoder;
    private DatumReader<GenericRecord> datumReader;

    public OracleNoSqlLogAppender() {
        super(OracleNoSqlConfig.class);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
        if (!closed) {
            if (kvStore != null) {
                LOG.debug("[{}] appending {} logs to Oracle NoSQL kvStore", this.getApplicationToken(), logEventPack.getEvents().size());
                try {
                    doAppendGenericAvro(logEventPack, header);
                    listener.onSuccess();
                } catch (FaultException e) {
                    LOG.error("Unable to append logs due to remote exception!", e);
                    listener.onRemoteError();
                } catch (Exception e) {
                    LOG.error("Unable to append logs!", e);
                    listener.onInternalError();
                }
            } else {
                LOG.info("[{}] Attempted to append to empty kvStore.", getName());
                listener.onInternalError();
            }
        } else {
            LOG.info("[{}] Attempted to append to closed appender.", getName());
            listener.onInternalError();
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, OracleNoSqlConfig configuration) {
        try {
            kvStore = initKvStore(configuration);
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

        List<String> majorPath = Arrays.asList(getApplicationToken(), logEventPack.getLogSchema().getVersion() + "",
                logEventPack.getEndpointKey(), System.currentTimeMillis() + "");

        int counter = 0;

        for (LogEvent event : logEventPack.getEvents()) {
            binaryDecoder = DecoderFactory.get().binaryDecoder(event.getLogData(), binaryDecoder);
            try {
                recordData = datumReader.read(recordData, binaryDecoder);
            } catch (IOException e) {
                LOG.error("[{}] Unable to read log event!", e);
                throw e;
            }
            wrapperRecord.put(RecordWrapperSchemaGenerator.RECORD_HEADER_FIELD, header);
            wrapperRecord.put(RecordWrapperSchemaGenerator.RECORD_DATA_FIELD, recordData);

            Key key = Key.createKey(majorPath, Arrays.asList((counter++) + ""));

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
            AvroSchemaMetadata metadata = new AvroSchemaMetadata(AvroSchemaStatus.ACTIVE, System.currentTimeMillis(), username,
                    getHostName());

            AddSchemaOptions options = new AddSchemaOptions(evolve, true);

            AddSchemaResult result = avroDdl.addSchema(metadata, schemaText, options, KVVersion.CURRENT_VERSION);

            LOG.info("[{}] Uploaded new schema to store, extra message [{}].", getName(), result.getExtraMessage());
        }
    }

    private boolean checkSchemaUploaded(AvroDdl avroDdl, SchemaSummary summary, String schemaText) {
        SchemaDetails details = avroDdl.getSchemaDetails(summary.getId());
        if (details.getText().equals(schemaText)) {
            return true;
        } else if (summary.getPreviousVersion() != null) {
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
        if (!closed) {
            closed = true;
            if (kvStore != null) {
                kvStore.close();
                kvStore = null;
            }
        }
        LOG.debug("Stopped Oracle NoSQL log appender.");
    }

    private KVStore initKvStore(OracleNoSqlConfig configuration) throws Exception {
        List<KvStoreNode> kvStoreNodes = configuration.getKvStoreNodes();
        String[] helperHostPorts = new String[kvStoreNodes.size()];
        for (int i = 0; i < kvStoreNodes.size(); i++) {
            KvStoreNode node = kvStoreNodes.get(i);
            helperHostPorts[i] = node.getHost() + ":" + node.getPort();
        }

        KVStoreConfig config = new KVStoreConfig(configuration.getStoreName(), helperHostPorts);

        Properties securityProperties = new Properties();
        if (configuration.getUsername() != null) {
            username = configuration.getUsername();
            securityProperties.put(KVSecurityConstants.AUTH_USERNAME_PROPERTY, configuration.getUsername());
        } else {
            username = "";
        }
        if (configuration.getWalletDir() != null) {
            securityProperties.put(KVSecurityConstants.AUTH_WALLET_PROPERTY, configuration.getWalletDir());
        }
        if (configuration.getPwdFile() != null) {
            securityProperties.put(KVSecurityConstants.AUTH_PWDFILE_PROPERTY, configuration.getPwdFile());
        }
        if (configuration.getSecurityFile() != null) {
            securityProperties.put(KVSecurityConstants.SECURITY_FILE_PROPERTY, configuration.getSecurityFile());
        }
        if (configuration.getTransport() != null) {
            securityProperties.put(KVSecurityConstants.TRANSPORT_PROPERTY, configuration.getTransport());
        }
        if (configuration.getSsl() != null) {
            securityProperties.put(KVSecurityConstants.SSL_TRANSPORT_NAME, configuration.getSsl());
        }
        if (configuration.getSslCipherSuites() != null) {
            securityProperties.put(KVSecurityConstants.SSL_CIPHER_SUITES_PROPERTY, configuration.getSslCipherSuites());
        }
        if (configuration.getSslProtocols() != null) {
            securityProperties.put(KVSecurityConstants.SSL_PROTOCOLS_PROPERTY, configuration.getSslProtocols());
        }
        if (configuration.getSslHostnameVerifier() != null) {
            securityProperties.put(KVSecurityConstants.SSL_HOSTNAME_VERIFIER_PROPERTY, configuration.getSslHostnameVerifier());
        }
        if (configuration.getSslTrustStore() != null) {
            securityProperties.put(KVSecurityConstants.SSL_TRUSTSTORE_FILE_PROPERTY, configuration.getSslTrustStore());
        }
        if (configuration.getSslTrustStoreType() != null) {
            securityProperties.put(KVSecurityConstants.SSL_TRUSTSTORE_TYPE_PROPERTY, configuration.getSslTrustStoreType());
        }
        config.setSecurityProperties(securityProperties);

        KVStore kvStore = KVStoreFactory.getStore(config);

        return kvStore;
    }

}
