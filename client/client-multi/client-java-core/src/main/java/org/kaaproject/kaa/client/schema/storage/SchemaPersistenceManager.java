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

import java.io.IOException;

/**
 * Manager that saves and loads the schema<br>
 * <br>
 * Provide {@link SchemaStorage} implementation instance to store updates for
 * configuration schema.
 * Once {@link SchemaPersistenceManager#setSchemaStorage(SchemaStorage)}
 * is called {@link SchemaStorage#loadSchema()} will be invoked to
 * load persisted configuration schema.<br>
 *
 * @author Yaroslav Zeygerman
 * @see SchemaStorage
 */
public interface SchemaPersistenceManager {

  /**
   * Provide storage object which is able to persist configuration schema.
   *
   * @param storage object that saves and loads schema data
   * @throws IOException the io exception
   * @see SchemaStorage
   */
  void setSchemaStorage(SchemaStorage storage) throws IOException;

}
