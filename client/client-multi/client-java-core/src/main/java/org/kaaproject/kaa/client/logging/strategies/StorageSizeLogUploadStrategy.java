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

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Start log upload when there storage size is >= volumeThreshold bytes.
 */
public class StorageSizeLogUploadStrategy extends DefaultLogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(StorageSizeLogUploadStrategy.class);

    public StorageSizeLogUploadStrategy() { }

    public StorageSizeLogUploadStrategy(int volumeThreshold){
        this.volumeThreshold = volumeThreshold;
    }

    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        if(status.getConsumedVolume() >= volumeThreshold){
            LOG.info("Need to upload logs - current size: {}, threshold: {}", status.getConsumedVolume(), volumeThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
        }

        return decision;
    }

    public long getVolumeThreshold() {
        return volumeThreshold;
    }

    public void setVolumeThreshold(int volumeThreshold) {
        this.volumeThreshold = volumeThreshold;
    }
}
