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

    private long batchVolume = 0;
    private long maximumAllowedVolume = 0;
    private long volumeThreshold = 0;
    private long logUploadTimeout = 0;

    private DefaultLogUploadConfiguration() {}

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

    @Override
    public long getLogUploadTimeout() {
        return logUploadTimeout;
    }

    public static class Builder {
        public static final int UPLOAD_TIMEOUT = 2 * 60;

        public static final int MAX_STORAGE_SIZE = 1024 * 1024;

        public static final int BATCH_VOLUME = 8 * 1024;

        public static final int SINK_THRESHOLD = 4 * BATCH_VOLUME;
        
        DefaultLogUploadConfiguration config = new DefaultLogUploadConfiguration();

        public Builder setBatchVolume(long volume) {
            config.batchVolume = volume;
            return this;
        }

        public Builder setMaximumAllowedVolume(long volume) {
            config.maximumAllowedVolume = volume;
            return this;
        }

        public Builder setVolumeThreshold(long volume) {
            config.volumeThreshold = volume;
            return this;
        }

        public Builder setLogUploadTimeout(long timeout) {
            config.logUploadTimeout = timeout;
            return this;
        }

        public DefaultLogUploadConfiguration build() {
            if (config.batchVolume > MAX_BATCH_VOLUME) {
                throw new IllegalArgumentException("Log batch should be less than " + MAX_BATCH_VOLUME + " KB");
            }

            if (config.volumeThreshold > config.maximumAllowedVolume) {
                throw new IllegalArgumentException("Maximum allowed volume should be greater than volume threshold");
            }

            if (config.logUploadTimeout <= 0) {
                throw new IllegalArgumentException("To small log upload timeout: " + config.logUploadTimeout);
            }

            return config;
        }
    }
}
