/*
 * Copyright 2014 CyberVision, Inc.
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
 * Start log upload when there is countThreshold records in storage or records are stored for more then timeLimit TimeUnit units.
 */
public class RecordCountWithTimeLimitLogUploadStrategy extends DefaultLogUploadStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(RecordCountWithTimeLimitLogUploadStrategy.class);
    protected long lastUploadTime;
    protected TimeUnit timeUnit;

    public RecordCountWithTimeLimitLogUploadStrategy() {
        timeUnit = TimeUnit.SECONDS;
    }

    public RecordCountWithTimeLimitLogUploadStrategy(int countThreshold, long timeLimit, TimeUnit timeUnit) {
        this.countThreshold = countThreshold;
        this.timeLimit = timeLimit;
        this.timeUnit = timeUnit;
    }

    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        long currentTime = System.currentTimeMillis();

        if(status.getRecordCount() == countThreshold){
            LOG.info("Need to upload logs - current count: {}, threshold: {}", status.getRecordCount(), countThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
            lastUploadTime = currentTime;
        }else if(lastUploadTime != 0 && (currentTime - lastUploadTime) >= timeUnit.toMillis(timeLimit)){
            LOG.info("Need to upload logs - current count: {}, lastUploadedTime: {}, timeLimit: {}", status.getRecordCount(), lastUploadTime, timeLimit);
            decision = LogUploadStrategyDecision.UPLOAD;
            lastUploadTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        }

        return decision;
    }

    public long getCountThreshold() {
        return countThreshold;
    }

    public void setCountThreshold(int countThreshold) {
        this.countThreshold = countThreshold;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
