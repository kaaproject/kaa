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

import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;

/**
 * <p>
 * Interface for log upload strategy.
 * </p>
 *
 * <p>
 * Used by log collector on each adding of the new log record in order to check
 * whether to send logs to server or clean up local storage.
 * </p>
 *
 * <p>
 * Reference implementation used by default ({@link DefaultLogUploadStrategy}).
 * </p>
 */
public interface LogUploadStrategy {
  /**
   * Retrieves log upload decision based on current storage status and defined
   * upload configuration.
   *
   * @param status Log storage status
   * @return Upload decision ({@link LogUploadStrategyDecision})
   */
  LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status);

  /**
   * Maximum time to wait log delivery response.
   *
   * @return Time in seconds.
   */
  int getTimeout();

  /**
   * If there are records in storage we need to periodically check isUploadNeeded method. This is
   * useful if client want to upload logs on certain timing conditions instead of log storage
   * checks.
   *
   * @return Time in seconds
   */
  int getUploadCheckPeriod();

  /**
   * Returns max parallel upload count.
   *
   * @return Max parallel upload count
   */
  int getMaxParallelUploads();

  /**
   * Handles timeout of log delivery.
   *
   * @param controller the controller
   */
  void onTimeout(LogFailoverCommand controller);

  /**
   * Handles failure of log delivery.
   *
   * @param controller the controller
   * @param code       the code
   */
  void onFailure(LogFailoverCommand controller, LogDeliveryErrorCode code);
}