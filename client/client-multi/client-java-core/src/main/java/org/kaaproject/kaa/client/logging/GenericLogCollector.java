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

package org.kaaproject.kaa.client.logging;

/**
 * <p>
 * Root interface for a log collector.
 * </p>
 *
 * <p>
 * Adds new log record to a local storage.
 * </p>
 *
 * <p>
 * May be configured by setting user defined log record storage and log upload
 * strategy. Each of them may be set independently of others.
 * </p>
 *
 * <p>
 * Reference implementation of each module is provided.
 * </p>
 *
 * @see LogStorage
 * @see LogUploadStrategy
 * @see LogDeliveryListener
 */
public interface GenericLogCollector {
  /**
   * Set user implementation of a log storage.
   *
   * @param storage User-defined log storage object
   */
  void setStorage(LogStorage storage);

  /**
   * Set user implementation of a log upload strategy.
   *
   * @param strategy User-defined log upload strategy object.
   */
  void setStrategy(LogUploadStrategy strategy);

  /**
   * Set a listener which receives a delivery status of each log bucket.
   *
   * @param listener User-defined listener object.
   */
  void setLogDeliveryListener(LogDeliveryListener listener);

  /**
   * Stops and/or cleanup resources.
   */
  void stop();
}
