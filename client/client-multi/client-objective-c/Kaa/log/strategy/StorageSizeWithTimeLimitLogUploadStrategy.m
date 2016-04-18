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

#import "StorageSizeWithTimeLimitLogUploadStrategy.h"
#import "KaaLogging.h"

#define TAG @"StorageSizeWithTimeLimitLogUploadStrategy >>>"

@implementation StorageSizeWithTimeLimitLogUploadStrategy


- (instancetype)init {
    self = [super initWithDefaults];
    if (self) {
        [self setLastUploadTime:[[NSDate date] timeIntervalSince1970] * 1000];
    }
    return self;
}

- (instancetype) initWithThresholdVolume:(int32_t)volumeThreshold timeLimit:(int64_t)timeLimit timeUnit:(TimeUnit)timeUnit {
    self = [self init];
    if (self) {
        [self setVolumeThreshold:volumeThreshold];
        [self setUploadCheckPeriod:(int32_t)[TimeUtils convertValue:timeLimit fromTimeUnit:timeUnit toTimeUnit:TIME_UNIT_SECONDS]];
    }
    return self;
}

- (LogUploadStrategyDecision) checkUploadNeededForStorageStatus:(id<LogStorageStatus>)status {
    LogUploadStrategyDecision decision = LOG_UPLOAD_STRATEGY_DECISION_NOOP;
    int64_t currentTime = [[NSDate date] timeIntervalSince1970] * 1000;
    int64_t currentConsumedVolume = [status getConsumedVolume];
    
    if (currentConsumedVolume >= self.volumeThreshold) {
        DDLogInfo(@"%@ Need to upload logs - current size: %lli, threshold: %i",
                  TAG, currentConsumedVolume, self.volumeThreshold);
        decision = LOG_UPLOAD_STRATEGY_DECISION_UPLOAD;
        self.lastUploadTime = currentTime;
    } else if (((currentTime - self.lastUploadTime) / 1000) >= self.uploadCheckPeriod) {
        DDLogInfo(@"%@ Need to upload logs - current count: %lli, lastUploadedTime: %lli, timeLimit: %i",
                  TAG, [status getRecordCount], self.lastUploadTime, self.uploadCheckPeriod);
        decision = LOG_UPLOAD_STRATEGY_DECISION_UPLOAD;
        self.lastUploadTime = currentTime;
    }
    return decision;
}

@end
