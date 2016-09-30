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

/**
 * Sends notifications with decoded configuration.
 *
 * @author Yaroslav Zeygerman
 */
public interface DecodedDeltaObservable {

  /**
   * Subscribes new receiver for decoded data updates.
   *
   * @param receiver receiver to get decoded configuration updates
   * @see GenericDeltaReceiver
   */
  void subscribeForUpdates(GenericDeltaReceiver receiver);

  /**
   * Unsubscribes receiver from decoded data updates.
   *
   * @param receiver receiver to be unsubscribed from configuration updates
   * @see GenericDeltaReceiver
   */
  void unsubscribeFromUpdates(GenericDeltaReceiver receiver);

}
