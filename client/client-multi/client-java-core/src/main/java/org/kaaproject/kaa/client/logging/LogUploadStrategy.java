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
 * Interface for log upload strategy.
 * Used by log collector on each adding of the new log record in order to check
 * whether to send logs to server or clean up local storage.
 *
 * Reference implementation used by default (@see DefaultLogUploadStrategy).
 */
public interface LogUploadStrategy {
    /**
     * Retrieves log upload decision based on current storage 
     * status and defined upload configuration.
     * 
     * @param configuration Log upload configuration (@see LogUploadConfiguration)
     * @param status Log storage status
     * @return Upload decision (@see LogUploadStrategyDecision)
     */
    LogUploadStrategyDecision isUploadNeeded(LogUploadConfiguration configuration, LogStorageStatus status);
}