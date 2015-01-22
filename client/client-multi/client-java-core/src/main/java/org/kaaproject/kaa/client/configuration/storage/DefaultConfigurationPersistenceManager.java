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

package org.kaaproject.kaa.client.configuration.storage;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.common.CommonToGeneric;
import org.kaaproject.kaa.client.configuration.ConfigurationHashContainer;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.configuration.ConfigurationRuntimeException;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationReceiver;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.schema.SchemaUpdatesReceiver;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link ConfigurationPersistenceManager} implementation
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultConfigurationPersistenceManager implements
        ConfigurationPersistenceManager, ConfigurationReceiver,
        SchemaUpdatesReceiver, ConfigurationHashContainer {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigurationPersistenceManager.class);

    private Schema schema;
    private ConfigurationStorage storage;
    private ConfigurationProcessor  processor;
    private boolean ignoreNextUpdate = false;
    private EndpointObjectHash configurationHash;
    private KaaClientState state;

    public DefaultConfigurationPersistenceManager() {

    }

    public DefaultConfigurationPersistenceManager(Schema schema, ConfigurationProcessor processor) {
        this.processor = processor;
        this.schema = schema;
    }

    public DefaultConfigurationPersistenceManager(KaaClientState state, ConfigurationProcessor processor) {
        this.processor = processor;
        this.state = state;
    }

    private void loadConfigurationFromStorage() throws IOException {
        if (state != null && state.isConfigurationVersionUpdated()) {
            LOG.info("Ignore loading configuration from storage: configuration version updated");
            return;
        }

        if (configurationHash == null) {
            ByteBuffer byteBuffer = storage.loadConfiguration();
            if (byteBuffer != null) {
                ignoreNextUpdate = true;
                processor.processConfigurationData(byteBuffer, true);
                configurationHash = EndpointObjectHash.fromSHA1(byteBuffer.array());
            }
        }
    }

    @Override
    public synchronized void onConfigurationUpdated(CommonRecord configuration) {
        if (!ignoreNextUpdate) {
            GenericArray<GenericRecord> deltaArray = new GenericData.Array<GenericRecord>(1, schema);
            GenericRecord deltaT = new GenericData.Record(schema.getElementType());
            deltaT.put("delta", CommonToGeneric.createRecord(configuration));
            deltaArray.add(deltaT);

            GenericAvroConverter<GenericArray<GenericRecord>> converter =
                    new GenericAvroConverter<GenericArray<GenericRecord>>(schema);

            byte[] byteArray = null;
            try {
                byteArray = converter.encode(deltaArray);
            } catch (IOException e) {
                throw new ConfigurationRuntimeException("Failed to encode configuration data");
            }
            configurationHash = EndpointObjectHash.fromSHA1(byteArray);
            if (storage != null) {
                ByteBuffer buffer = ByteBuffer.wrap(byteArray);
                storage.saveConfiguration(buffer);
            }
        } else {
            ignoreNextUpdate = false;
        }
    }

    @Override
    public synchronized void setConfigurationStorage(ConfigurationStorage storage) throws IOException {
        if (storage != null) {
            this.storage = storage;
            if (this.schema != null) {
                loadConfigurationFromStorage();
            } else {
                LOG.warn("Can't load configuration right now. Schema is null");
            }
        }
    }

    public void setConfigurationProcessor(ConfigurationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public synchronized void onSchemaUpdated(Schema schema) {
        if (schema != null) {
            this.schema = schema;
            if (this.storage != null) {
                try {
                    loadConfigurationFromStorage();
                } catch (IOException e) {
                    LOG.error("Failed to load the configuration data from the storage");
                    throw new ConfigurationRuntimeException("The configuration storage data is not applicable to the schema");
                }
            }
        }
    }

    @Override
    public EndpointObjectHash getConfigurationHash() {
        return configurationHash;
    }

}
