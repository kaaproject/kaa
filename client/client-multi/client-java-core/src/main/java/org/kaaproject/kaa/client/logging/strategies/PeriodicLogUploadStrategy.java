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

import java.util.concurrent.TimeUnit;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Issue log upload each timeLimit timeUnit units.
 */
public class PeriodicLogUploadStrategy extends DefaultLogUploadStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(PeriodicLogUploadStrategy.class);
  protected long lastUploadTime = System.currentTimeMillis();

  public PeriodicLogUploadStrategy(long timeLimit, TimeUnit timeUnit) {
    setUploadCheckPeriod((int) timeUnit.toSeconds(timeLimit));
  }

  @Override
  protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
    LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

    long currentTime = System.currentTimeMillis();

    if (((currentTime - lastUploadTime) / 1000) >= uploadCheckPeriod) {
      LOG.info("Need to upload logs - current count: {}, lastUploadedTime: {}, timeLimit: {} sec",
              status.getRecordCount(), lastUploadTime, uploadCheckPeriod);
      decision = LogUploadStrategyDecision.UPLOAD;
      lastUploadTime = currentTime;
    }

    return decision;
  }
}
