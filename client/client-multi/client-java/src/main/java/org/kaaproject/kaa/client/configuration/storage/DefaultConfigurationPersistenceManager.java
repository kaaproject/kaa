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
import org.kaaproject.kaa.client.schema.SchemaUpdatesReceiver;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

/**
 * Default {@link ConfigurationPersistenceManager} implementation
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultConfigurationPersistenceManager implements
        ConfigurationPersistenceManager, ConfigurationReceiver,
        SchemaUpdatesReceiver, ConfigurationHashContainer {

    private Schema schema;
    private ConfigurationStorage storage;
    private ConfigurationProcessor  processor;
    private boolean ignoreNextUpdate = false;
    private EndpointObjectHash configurationHash;

    public DefaultConfigurationPersistenceManager() {

    }

    public DefaultConfigurationPersistenceManager(Schema schema, ConfigurationProcessor processor) {
        this.processor = processor;
        this.schema = schema;
    }

    public DefaultConfigurationPersistenceManager(ConfigurationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public synchronized void onConfigurationUpdated(CommonRecord configuration) {
        if (!ignoreNextUpdate) {
            GenericArray<GenericRecord> deltaArray = new GenericData.Array<GenericRecord>(1, schema);
            GenericRecord deltaT = new GenericData.Record(schema.getElementType());
            deltaT.put("delta", CommonToGeneric.createRecord(configuration));
            deltaArray.add(deltaT);

            GenericAvroConverter<GenericArray<GenericRecord>> converter = new GenericAvroConverter<GenericArray<GenericRecord>>(schema);

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
        this.storage = storage;
        if (configurationHash == null) {
            ByteBuffer byteBuffer = storage.loadConfiguration();
            if (byteBuffer != null) {
                ignoreNextUpdate = true;
                processor.processConfigurationData(byteBuffer, true);
                configurationHash = EndpointObjectHash.fromSHA1(byteBuffer.array());
            }
        }
    }

    public void setConfigurationProcessor(ConfigurationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void onSchemaUpdated(Schema schema) {
        this.schema = schema;
    }

    @Override
    public EndpointObjectHash getConfigurationHash() {
        return configurationHash;
    }

}
