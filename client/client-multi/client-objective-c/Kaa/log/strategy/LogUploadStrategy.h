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

#ifndef Kaa_LogUploadStrategy_h
#define Kaa_LogUploadStrategy_h

#import <Foundation/Foundation.h>
#import "LogStorage.h"
#import "EndpointGen.h"
#import "LogFailoverCommand.h"

/**
 * Describes all possible decisions for a log upload strategy.
 */
typedef enum {
    /**
     * Do nothing except adding log record to a storage.
     */
    LOG_UPLOAD_STRATEGY_DECISION_NOOP,
    /**
     * Kaa SDK should initiate log upload on the Operation server.
     */
    LOG_UPLOAD_STRATEGY_DECISION_UPLOAD
    
} LogUploadStrategyDecision;


/**
 * Interface for log upload strategy.
 *
 * Used by log collector on each adding of the new log record in order to check
 * whether to send logs to server or clean up local storage.
 *
 * Reference implementation used by default < DefaultLogUploadStrategy >
 */
@protocol LogUploadStrategy

/**
 * Retrieves log upload decision based on current storage status and defined
 * upload configuration.
 *
 * @return Upload decision.
 */
- (LogUploadStrategyDecision)isUploadNeededForStorageStatus:(id<LogStorageStatus>)status;

/**
 * Retrieves maximum size of the report pack
 * that will be delivered in single request to server.
 * @return Size of the batch
 */
- (int64_t)getBatchSize;

/**
 * Retrieves maximum count of the records in report pack
 * that will be delivered in single request to server.
 * @return Size of the batch
 */
- (int32_t)getBatchCount;

/**
 * Maximum time to wait log delivery response.
 * @returns Time in seconds.
 */
- (int32_t)getTimeout;

/**
 * If there are records in storage we need to periodically check isUploadNeeded method.
 * This is useful if client want to upload logs on certain timing conditions instead of log storage checks
 * @return Time in seconds
 */
- (int32_t)getUploadCheckPeriod;

/**
 * @return Number of maximum parallel uploads.
 */
- (int64_t)getMaxParallelUploads;

/**
 * Handles timeout of log delivery
 */
- (void)onTimeoutForController:(id<LogFailoverCommand>)controller;

/**
 * Handles failure of log delivery
 */
- (void)onFailureForController:(id<LogFailoverCommand>)controller errorCode:(LogDeliveryErrorCode)code;

@end

#endif
