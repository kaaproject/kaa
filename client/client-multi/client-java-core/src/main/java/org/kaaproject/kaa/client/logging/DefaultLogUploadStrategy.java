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

package org.kaaproject.kaa.client.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of @see LogUploadStrategy
 */
public class DefaultLogUploadStrategy implements LogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogUploadStrategy.class);
    
    public DefaultLogUploadStrategy() {
    }

    @Override
    public LogUploadStrategyDecision isUploadNeeded(LogUploadConfiguration configuration, LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;
        long consumedVolume = status.getConsumedVolume();

        if (consumedVolume >= configuration.getMaximumAllowedVolume()) {
            LOG.info("Need to clean up log storage - current size: {}, max: {}"
                    , consumedVolume, configuration.getMaximumAllowedVolume());
            decision = LogUploadStrategyDecision.CLEANUP;
        } else if (consumedVolume >= configuration.getVolumeThreshold()) {
            LOG.info("Need to upload logs - current size: {}, max: {}"
                    , consumedVolume, configuration.getMaximumAllowedVolume());
            decision = LogUploadStrategyDecision.UPLOAD;
        }

        return decision;
    }
}