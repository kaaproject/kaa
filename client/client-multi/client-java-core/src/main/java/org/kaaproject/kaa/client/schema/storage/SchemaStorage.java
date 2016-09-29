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

import java.nio.ByteBuffer;

/**
 * Interface for object to save and to load schema<br>
 * <br>
 * Provide implementation instance of this interface to save and load configuration
 * schema.<br>
 * <br>
 * Schema storage can be added using {@link SchemaPersistenceManager}
 * accessed through {@link org.kaaproject.kaa.client.KaaClient} interface.<br>
 * <pre>
 * {@code
 * class FileConfigurationSchemaStorage implements SchemaStorage {
 *     public void saveSchema(ByteBuffer buffer) {
 *         ...
 *     }
 *     public ByteBuffer loadSchema() {
 *         ...
 *     }
 * }
 * ...
 * // Assuming Kaa instance is created
 * KaaClient kaaClient = kaa.getClient();
 *
 * SchemaStorage schemaStorage = new FileConfigurationSchemaStorage();
 * kaaClient.getSchemaPersistenceManager().setSchemaStorage(schemaStorage);
 * }
 * </pre>
 *
 * @author Yaroslav Zeygerman
 * @see SchemaPersistenceManager
 */
public interface SchemaStorage {

  /**
   * Saves schema.
   *
   * @param buffer buffer with schema
   */
  void saveSchema(ByteBuffer buffer);

  /**
   * Loads schema.
   *
   * @return buffer with loaded schema, or null if schema is empty
   */
  ByteBuffer loadSchema();

}
