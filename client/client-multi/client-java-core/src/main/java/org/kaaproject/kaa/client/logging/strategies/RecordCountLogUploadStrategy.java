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

package org.kaaproject.kaa.client.logging.strategies;

import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Start log upload when there is countThreshold records in storage.
 */
public class RecordCountLogUploadStrategy extends DefaultLogUploadStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(RecordCountLogUploadStrategy.class);

  public RecordCountLogUploadStrategy(int countThreshold) {
    setCountThreshold(countThreshold);
  }

  @Override
  protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
    long currentRecordCount = status.getRecordCount();
    LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

    if (currentRecordCount >= countThreshold) {
      LOG.info("Need to upload logs - current count: {}, threshold: {}",
              currentRecordCount, countThreshold);
      decision = LogUploadStrategyDecision.UPLOAD;
    }

    return decision;
  }
}
