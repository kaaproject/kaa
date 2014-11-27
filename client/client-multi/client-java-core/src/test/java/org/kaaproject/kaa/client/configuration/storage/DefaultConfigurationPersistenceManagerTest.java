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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.junit.Test;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.client.configuration.manager.DefaultConfigurationManager;
import org.kaaproject.kaa.client.configuration.manager.DefaultConfigurationManagerTest;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.client.configuration.storage.DefaultConfigurationPersistenceManager;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class DefaultConfigurationPersistenceManagerTest {

    public static byte [] serializeDelta(GenericRecord delta, Schema schema) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        DatumWriter<GenericArray> writer = new GenericDatumWriter<GenericArray>(schema);
        GenericArray<GenericRecord> deltaArray = new GenericData.Array<GenericRecord>(1, schema);

        GenericRecord deltaT = new GenericData.Record(schema.getElementType());
        deltaT.put("delta", delta);
        deltaArray.add(deltaT);
        try {
            writer.write(deltaArray, encoder);
            encoder.flush();
        } catch (IOException e) {
        }
        return stream.toByteArray();
    }

    @Test
    public void testConfigurationSaving() throws IOException {
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        final Schema complexSchema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        ConfigurationProcessor processor = mock(ConfigurationProcessor.class);
        DefaultConfigurationManager configManager = new DefaultConfigurationManager();
        DefaultConfigurationPersistenceManager persistenceManager = new DefaultConfigurationPersistenceManager(complexSchema, processor);

        final GenericRecord complexDelta = new GenericData.Record(
                DefaultConfigurationManagerTest.getDeltaSchemaByFullName(
                        complexSchema, "org.kaa.config.testT"));
        DefaultConfigurationManagerTest.fillComplexFullResyncDelta(complexDelta);
        configManager.onDeltaReceived(0, complexDelta, true);

        persistenceManager.setConfigurationStorage(new ConfigurationStorage() {

            @Override
            public void saveConfiguration(ByteBuffer buffer) {
                assertArrayEquals(serializeDelta(complexDelta, complexSchema), buffer.array());
            }

            @Override
            public ByteBuffer loadConfiguration() {
                return null;
            }
        });

        CommonRecord rootRecord = configManager.getConfiguration();
        persistenceManager.onConfigurationUpdated(rootRecord);

        schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/arrayFieldsDeltaSchema.json");
        final Schema arraySchema = new Schema.Parser().parse(new File(schemaUrl.getPath()));
        persistenceManager.onSchemaUpdated(arraySchema);
        final GenericRecord arrayDelta = new GenericData.Record(
                DefaultConfigurationManagerTest.getDeltaSchemaByFullName(
                        arraySchema, "org.kaa.config.testT"));
        DefaultConfigurationManagerTest.fillArrayFullResyncDelta(arrayDelta);
        configManager = new DefaultConfigurationManager();
        configManager.onDeltaReceived(0, arrayDelta, true);
        rootRecord = configManager.getConfiguration();

        persistenceManager.setConfigurationStorage(new ConfigurationStorage() {

            @Override
            public void saveConfiguration(ByteBuffer buffer) {
                assertArrayEquals(serializeDelta(arrayDelta, arraySchema), buffer.array());
            }

            @Override
            public ByteBuffer loadConfiguration() {
                return null;
            }
        });

        persistenceManager.onConfigurationUpdated(rootRecord);
    }

    @Test
    public void testConfigurationLoading() throws IOException {
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        final Schema complexSchema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        ConfigurationProcessor processor = mock(ConfigurationProcessor.class);
        DefaultConfigurationPersistenceManager persistenceManager = new DefaultConfigurationPersistenceManager();
        persistenceManager.setConfigurationProcessor(processor);
        persistenceManager.onSchemaUpdated(complexSchema);

        GenericRecord complexDelta = new GenericData.Record(
                DefaultConfigurationManagerTest.getDeltaSchemaByFullName(
                        complexSchema, "org.kaa.config.testT"));
        DefaultConfigurationManagerTest.fillComplexFullResyncDelta(complexDelta);
        byte [] rawBuffer = serializeDelta(complexDelta, complexSchema);
        final ByteBuffer complexBuffer = ByteBuffer.wrap(rawBuffer);
        EndpointObjectHash complexHash = EndpointObjectHash.fromSHA1(rawBuffer);

        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        when(storage.loadConfiguration()).thenReturn(complexBuffer);
        persistenceManager.setConfigurationStorage(storage);
        persistenceManager.onConfigurationUpdated(mock(CommonRecord.class));

        assertTrue(complexHash.equals(persistenceManager.getConfigurationHash()));

        ConfigurationStorage nullStorage = mock(ConfigurationStorage.class);
        when(nullStorage.loadConfiguration()).thenReturn(null);
        persistenceManager.setConfigurationStorage(nullStorage);
        verify(processor, times(1)).processConfigurationData(complexBuffer, true);
    }

    @Test
    public void testConfigurationLoadingWithoutSchema() throws IOException {
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        final Schema complexSchema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        ConfigurationProcessor processor = mock(ConfigurationProcessor.class);
        DefaultConfigurationPersistenceManager persistenceManager = new DefaultConfigurationPersistenceManager();
        persistenceManager.setConfigurationProcessor(processor);

        GenericRecord complexDelta = new GenericData.Record(
                DefaultConfigurationManagerTest.getDeltaSchemaByFullName(
                        complexSchema, "org.kaa.config.testT"));
        DefaultConfigurationManagerTest.fillComplexFullResyncDelta(complexDelta);
        byte [] rawBuffer = serializeDelta(complexDelta, complexSchema);
        final ByteBuffer complexBuffer = ByteBuffer.wrap(rawBuffer);
        EndpointObjectHash complexHash = EndpointObjectHash.fromSHA1(rawBuffer);

        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        when(storage.loadConfiguration()).thenReturn(complexBuffer);
        persistenceManager.setConfigurationStorage(storage);
        persistenceManager.onSchemaUpdated(complexSchema);

        assertTrue(complexHash.equals(persistenceManager.getConfigurationHash()));

    }
}
