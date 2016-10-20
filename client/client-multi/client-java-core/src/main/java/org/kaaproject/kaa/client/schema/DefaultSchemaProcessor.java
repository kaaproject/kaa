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
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of @{link SchemaProcessor} using the avro schema.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultSchemaProcessor implements SchemaProcessor, SchemaObservable {
  private final List<SchemaUpdatesReceiver> subscribers = new LinkedList<SchemaUpdatesReceiver>();
  private Schema schema;

  public DefaultSchemaProcessor() {

  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public void loadSchema(ByteBuffer buffer) throws IOException {
    if (buffer != null) {
      String schemaString = new String(buffer.array(), "UTF-8");
      schema = new Schema.Parser().parse(schemaString);
      for (SchemaUpdatesReceiver subscriber : subscribers) {
        subscriber.onSchemaUpdated(schema);
      }
    }
  }

  @Override
  public void subscribeForSchemaUpdates(SchemaUpdatesReceiver receiver) {
    if (receiver != null && !subscribers.contains(receiver)) {
      subscribers.add(receiver);
    }
  }

  @Override
  public void unsubscribeFromSchemaUpdates(SchemaUpdatesReceiver receiver) {
    if (receiver != null) {
      subscribers.remove(receiver);
    }
  }

}
