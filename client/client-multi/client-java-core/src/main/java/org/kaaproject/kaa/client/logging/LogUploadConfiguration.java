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
 * Interface for log upload configuration.
 *
 * Describe threshold values needed to upload strategy to decide whether logs
 * should be sent or clean up.
 * 
 * Reference implementation is present (@see DefaultLogUploadConfiguration) 
 * and use by default (@see DefaultLogUploadStrategy).
 */
public interface LogUploadConfiguration {
    long getBatchVolume();
    long getVolumeThreshold();
    long getMaximumAllowedVolume();
}
