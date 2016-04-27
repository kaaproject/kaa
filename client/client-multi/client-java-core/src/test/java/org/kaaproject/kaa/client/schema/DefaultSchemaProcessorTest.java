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

package org.kaaproject.kaa.client.schema;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;
import org.kaaproject.kaa.client.schema.DefaultSchemaProcessor;
import org.kaaproject.kaa.client.schema.SchemaUpdatesReceiver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

public class DefaultSchemaProcessorTest {

    @Test
    public void testUpdates() throws IOException {
        SchemaUpdatesReceiver receiver = mock(SchemaUpdatesReceiver.class);
        DefaultSchemaProcessor processor = new DefaultSchemaProcessor();
        processor.subscribeForSchemaUpdates(receiver);
        processor.subscribeForSchemaUpdates(receiver);
        processor.subscribeForSchemaUpdates(null);

        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        FileInputStream inStream = new FileInputStream(new File(schemaUrl.getPath()));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        IOUtils.copy(inStream, outStream);
        String schemaStr = new String(outStream.toByteArray(), "UTF-8");
        Schema schema = new Schema.Parser().parse(schemaStr);

        processor.loadSchema(ByteBuffer.wrap(schemaStr.getBytes()));
        processor.loadSchema(null);
        processor.unsubscribeFromSchemaUpdates(receiver);
        processor.unsubscribeFromSchemaUpdates(null);
        processor.loadSchema(ByteBuffer.wrap(schemaStr.getBytes()));

        verify(receiver, times(1)).onSchemaUpdated(eq(schema));
        assertEquals(schema, processor.getSchema());
    }

}
