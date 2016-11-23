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

import org.apache.avro.Schema;
import org.kaaproject.kaa.client.schema.SchemaProcessor;
import org.kaaproject.kaa.client.schema.SchemaRuntimeException;
import org.kaaproject.kaa.client.schema.SchemaUpdatesReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Default {@link SchemaPersistenceManager} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultSchemaPersistenceManager implements
    SchemaPersistenceManager, SchemaUpdatesReceiver {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultSchemaPersistenceManager.class);

  private SchemaProcessor processor;
  private SchemaStorage storage;
  private boolean ignoreNextUpdate = false;

  public DefaultSchemaPersistenceManager() {

  }

  public DefaultSchemaPersistenceManager(SchemaProcessor processor) {
    this.processor = processor;
  }

  @Override
  public synchronized void onSchemaUpdated(Schema schema) {
    if (!ignoreNextUpdate) {
      try {
        byte[] schemaBuffer = schema.toString().getBytes("UTF-8");
        ByteBuffer buffer = ByteBuffer.wrap(schemaBuffer);
        if (storage != null) {
          storage.saveSchema(buffer);
        }
      } catch (UnsupportedEncodingException ex) {
        LOG.error("Failed to save schema: ", ex);
        throw new SchemaRuntimeException("Failed to save schema");
      }
    } else {
      ignoreNextUpdate = false;
    }
  }

  @Override
  public synchronized void setSchemaStorage(SchemaStorage storage) throws IOException {
    this.storage = storage;
    if (processor.getSchema() == null) {
      ByteBuffer schemaBuffer = storage.loadSchema();
      if (schemaBuffer != null) {
        ignoreNextUpdate = true;
        processor.loadSchema(schemaBuffer);
      }
    }
  }

  public void setSchemaProcessor(SchemaProcessor processor) {
    this.processor = processor;
  }

}
