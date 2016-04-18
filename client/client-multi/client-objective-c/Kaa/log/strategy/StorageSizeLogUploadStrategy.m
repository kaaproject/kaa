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

#import "StorageSizeLogUploadStrategy.h"
#import "KaaLogging.h"

#define TAG @"StorageSizeLogUploadStrategy >>>"

@implementation StorageSizeLogUploadStrategy

- (instancetype)initWithVolumeThreshold:(int32_t)volumeThreshold {
    self = [super initWithDefaults];
    if (self) {
        [self setVolumeThreshold:volumeThreshold];
    }
    return self;
}

- (LogUploadStrategyDecision)checkUploadNeededForStorageStatus:(id<LogStorageStatus>)status {
    LogUploadStrategyDecision decision = LOG_UPLOAD_STRATEGY_DECISION_NOOP;
    int64_t currentConsumedVolume = [status getConsumedVolume];
    
    if (currentConsumedVolume >= self.volumeThreshold) {
        DDLogInfo(@"%@ Need to upload logs - current size: %lli, threshold: %i",
                  TAG, currentConsumedVolume, self.countThreshold);
        decision = LOG_UPLOAD_STRATEGY_DECISION_UPLOAD;
    }
    return decision;
}

@end
