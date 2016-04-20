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

package org.kaaproject.kaa.client.schema.storage;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.junit.Test;
import org.kaaproject.kaa.client.schema.SchemaProcessor;
import org.kaaproject.kaa.client.schema.storage.DefaultSchemaPersistenceManager;
import org.kaaproject.kaa.client.schema.storage.SchemaStorage;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;

public class DefaultSchemaPersistenceManagerTest {

    @Test
    public void testSchemaSaving() throws IOException {
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        final Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        SchemaProcessor processor = mock(SchemaProcessor.class);
        DefaultSchemaPersistenceManager manager = new DefaultSchemaPersistenceManager(processor);

        manager.setSchemaStorage(new SchemaStorage() {

            @Override
            public void saveSchema(ByteBuffer buffer) {
                byte [] expected = null;
                try {
                    expected = schema.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                assertArrayEquals(expected, buffer.array());
            }

            @Override
            public ByteBuffer loadSchema() {
                return null;
            }
        });

        manager.onSchemaUpdated(schema);
    }

    @Test
    public void testSchemaLoading() throws IOException {
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        final Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        DefaultSchemaPersistenceManager manager = new DefaultSchemaPersistenceManager();

        manager.setSchemaProcessor(new SchemaProcessor() {

            @Override
            public void loadSchema(ByteBuffer buffer) throws IOException {
                byte [] rawBuffer = null;
                try {
                    rawBuffer = schema.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                assertArrayEquals(rawBuffer, buffer.array());
            }

            @Override
            public Schema getSchema() {
                return null;
            }
        });

        manager.setSchemaStorage(new SchemaStorage() {

            @Override
            public void saveSchema(ByteBuffer buffer) {

            }

            @Override
            public ByteBuffer loadSchema() {
                byte [] rawBuffer = null;
                try {
                    rawBuffer = schema.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
                return buffer;
            }
        });
    }
}
