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

/**
 * Sends notifications with new schema object
 *
 * @author Yaroslav Zeygerman.
 */
public interface SchemaObservable {

  /**
   * Subscribes new receiver for schema updates.
   *
   * @param receiver receiver to get schema updates
   */
  void subscribeForSchemaUpdates(SchemaUpdatesReceiver receiver);

  /**
   * Unsubscribes receiver from schema updates.
   *
   * @param receiver receiver to be unsubscribed from schema updates
   */
  void unsubscribeFromSchemaUpdates(SchemaUpdatesReceiver receiver);

}
