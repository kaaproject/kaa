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

#import "DefaultLogUploadStrategy.h"
#import "KaaLogging.h"

#define TAG @"DefaultLogUploadStrategy >>>"

#define DEFAULT_UPLOAD_TIMEOUT          (2 * 60)
#define DEFAULT_UPLOAD_CHECK_PERIOD     (30)
#define DEFAULT_RETRY_PERIOD            (5 * 60)
#define DEFAULT_UPLOAD_VOLUME_THRESHOLD (8 * 1024)
#define DEFAULT_UPLOAD_COUNT_THRESHOLD  (64)
#define DEFAULT_BATCH_SIZE              (8 * 1024)
#define DEFAULT_BATCH_COUNT             (256)
#define DEFAULT_TIME_LIMIT              (5 * 60)
#define DEFAULT_UPLOAD_LOCKED           NO


@implementation DefaultLogUploadStrategy

- (instancetype)init {
    return [self initWithDefaults];
}

- (instancetype)initWithDefaults {
    self = [super init];
    if (self) {
        self.timeout = DEFAULT_UPLOAD_TIMEOUT;
        self.uploadCheckPeriod = DEFAULT_UPLOAD_CHECK_PERIOD;
        self.retryPeriod = DEFAULT_RETRY_PERIOD;
        self.volumeThreshold = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
        self.countThreshold = DEFAULT_UPLOAD_COUNT_THRESHOLD;
        self.batchSize = DEFAULT_BATCH_SIZE;
        self.batchCount = DEFAULT_BATCH_COUNT;
        self.timeLimit = DEFAULT_TIME_LIMIT;
        self.isUploadLocked = DEFAULT_UPLOAD_LOCKED;
        self.maxParallelUploads = INTMAX_MAX;
    }
    return self;
}

- (LogUploadStrategyDecision)isUploadNeededForStorageStatus:(id<LogStorageStatus>)status {
    LogUploadStrategyDecision decision;
    if (!self.isUploadLocked) {
        decision = [self checkUploadNeededForStorageStatus:status];
    } else {
        decision = LOG_UPLOAD_STRATEGY_DECISION_NOOP;
    }
    return decision;
}

- (LogUploadStrategyDecision)checkUploadNeededForStorageStatus:(id<LogStorageStatus>)status {
    LogUploadStrategyDecision decision = LOG_UPLOAD_STRATEGY_DECISION_NOOP;
    if ([status getConsumedVolume] >= self.volumeThreshold) {
        DDLogInfo(@"%@ Need to upload logs - current size: %lli, threshold: %i",
                  TAG, [status getConsumedVolume], self.volumeThreshold);
        decision = LOG_UPLOAD_STRATEGY_DECISION_UPLOAD;
    } else if ([status getRecordCount] >= self.countThreshold) {
        DDLogInfo(@"%@ Need to upload logs - current count: %lli, threshold: %i",
                  TAG, [status getRecordCount], self.countThreshold);
        decision = LOG_UPLOAD_STRATEGY_DECISION_UPLOAD;
    }
    return decision;
}

- (int64_t)getBatchSize {
    return _batchSize;
}

- (int32_t)getBatchCount {
    return _batchCount;
}

- (int32_t)getTimeout {
    return _timeout;
}

- (int32_t)getUploadCheckPeriod {
    return _uploadCheckPeriod;
}

- (int64_t)getMaxParallelUploads {
    return _maxParallelUploads;
}

- (void)onTimeoutForController:(id<LogFailoverCommand>)controller {
    //TODO: fix issue described in KAA-1040
    [controller retryLogUploadWithDelay:self.retryPeriod];
}

- (void)onFailureForController:(id<LogFailoverCommand>)controller errorCode:(LogDeliveryErrorCode)code {
    switch (code) {
        case LOG_DELIVERY_ERROR_CODE_NO_APPENDERS_CONFIGURED:
        case LOG_DELIVERY_ERROR_CODE_APPENDER_INTERNAL_ERROR:
        case LOG_DELIVERY_ERROR_CODE_REMOTE_CONNECTION_ERROR:
        case LOG_DELIVERY_ERROR_CODE_REMOTE_INTERNAL_ERROR:
            [controller retryLogUploadWithDelay:self.retryPeriod];
            break;
        default:
            break;
    }
}

- (void)lockUpload {
    _isUploadLocked = YES;
}

- (void)unlockUpload {
    _isUploadLocked = NO;
}

@end
