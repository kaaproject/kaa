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

package org.kaaproject.kaa.client.configuration;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.client.schema.SchemaUpdatesReceiver;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of {@link ConfigurationProcessor} using avro decoding mechanisms.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultConfigurationProcessor implements
    ConfigurationProcessor, DecodedDeltaObservable,
    SchemaUpdatesReceiver, ConfigurationProcessedObservable {

  private final List<GenericDeltaReceiver> onDeltaReceived = new LinkedList<>();
  private final List<ConfigurationProcessedObserver> onProcessed = new LinkedList<>();
  private Schema schema;

  public DefaultConfigurationProcessor() {

  }

  @Override
  public synchronized void processConfigurationData(ByteBuffer buffer, boolean fullResync)
          throws IOException {
    if (buffer != null) {
      if (schema == null) {
        throw new ConfigurationRuntimeException(
                "Can't process configuration update. Schema is null");
      }
      GenericAvroConverter<GenericArray<GenericRecord>> converter =
              new GenericAvroConverter<>(schema);
      GenericArray<GenericRecord> deltaArray = converter.decodeBinary(buffer.array());

      for (GenericRecord delta : deltaArray) {
        GenericRecord record = (GenericRecord) delta.get("delta");
        int index = delta.getSchema().getField("delta").schema().getTypes().indexOf(
                record.getSchema());
        for (GenericDeltaReceiver subscriber : onDeltaReceived) {
          subscriber.onDeltaReceived(index, record, fullResync);
        }
      }

      for (ConfigurationProcessedObserver callback : onProcessed) {
        callback.onConfigurationProcessed();
      }
    }
  }

  @Override
  public synchronized void onSchemaUpdated(Schema schema) {
    if (schema != null) {
      this.schema = schema;
    }
  }

  @Override
  public void subscribeForUpdates(GenericDeltaReceiver receiver) {
    if (receiver != null && !onDeltaReceived.contains(receiver)) {
      onDeltaReceived.add(receiver);
    }
  }

  @Override
  public void unsubscribeFromUpdates(GenericDeltaReceiver receiver) {
    if (receiver != null) {
      onDeltaReceived.remove(receiver);
    }
  }

  @Override
  public void addOnProcessedCallback(ConfigurationProcessedObserver callback) {
    if (callback != null && !onProcessed.contains(callback)) {
      onProcessed.add(callback);
    }
  }

  @Override
  public void removeOnProcessedCallback(ConfigurationProcessedObserver callback) {
    if (callback != null) {
      onProcessed.remove(callback);
    }
  }

}
