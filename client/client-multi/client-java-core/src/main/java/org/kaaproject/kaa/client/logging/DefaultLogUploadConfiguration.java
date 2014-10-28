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

/**
 * Reference implementation for {@link LogUploadConfiguration}.
 */
public class DefaultLogUploadConfiguration implements LogUploadConfiguration {
    public static final long MAX_BATCH_VOLUME = 32 * 1024; // Framework limitation

    private final long batchVolume;
    private final long maximumAllowedVolume;
    private final long volumeThreshold;

    public DefaultLogUploadConfiguration(long batchVolume, long volumeThreshold, long maximumAllowedVolume) {
        if (batchVolume > MAX_BATCH_VOLUME) {
            throw new IllegalArgumentException("Log batch should be less than " + MAX_BATCH_VOLUME + " KB");
        }

        if (volumeThreshold > maximumAllowedVolume) {
            throw new IllegalArgumentException("Maximum allowed volume should be greater than volume threshold");
        }

        this.batchVolume = batchVolume;
        this.maximumAllowedVolume = maximumAllowedVolume;
        this.volumeThreshold = volumeThreshold;
    }

    @Override
    public long getBatchVolume() {
        return batchVolume;
    }

    @Override
    public long getMaximumAllowedVolume() {
        return maximumAllowedVolume;
    }

    @Override
    public long getVolumeThreshold() {
        return volumeThreshold;
    }
}
