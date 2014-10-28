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
 * <p>Interface for log upload configuration.</p>
 *
 * <p>Describes threshold values for upload strategy to decide whether logs
 * should be sent or cleaned up.</p>
 *
 * <p>Reference implementation is  ({@link DefaultLogUploadConfiguration})
 * and used by default ({@link DefaultLogUploadStrategy}).</p>
 */
public interface LogUploadConfiguration {
    /**
     * <p>Retrieves a maximum size of log batch sends to the Operation server.</p>
     *
     * <p><b>NOTE: The upper bound is 32KB due a server limitations</b>
     * ({@link DefaultLogUploadConfiguration#MAX_BATCH_VOLUME}).</p>
     *
     * @return Size in bytes.
     */
    long getBatchVolume();

    /**
     * Retrieves a threshold value of memory occupied by added logs. If this
     * value is exceeded, log sending should be initiated.
     *
     * @return Size in bytes.
     */
    long getVolumeThreshold();

    /**
     * Maximum size of memory logs can occupy.
     *
     * @return Size in bytes.
     */
    long getMaximumAllowedVolume();
}
