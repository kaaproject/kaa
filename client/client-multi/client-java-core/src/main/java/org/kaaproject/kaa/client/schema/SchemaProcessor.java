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

import org.apache.avro.Schema;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Receives the data from stream and creates the schema object.
 *
 * @author Yaroslav Zeygerman
 */
public interface SchemaProcessor {

  /**
   * Loads new schema from the buffer.
   *
   * @param buffer schema buffer
   * @throws IOException in case of loading schema failure
   */
  void loadSchema(ByteBuffer buffer) throws IOException;

  /**
   * Retrieves current schema object.
   *
   * @return current schema.
   * @see org.apache.avro.Schema
   */
  Schema getSchema();
}
