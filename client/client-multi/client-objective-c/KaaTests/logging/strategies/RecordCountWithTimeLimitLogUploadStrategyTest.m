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

#define HC_SHORTHAND
#import <OCHamcrest/OCHamcrest.h>

#define MOCKITO_SHORTHAND
#import <OCMockito/OCMockito.h>

#import <XCTest/XCTest.h>
#import "RecordCountWithTimeLimitLogUploadStrategy.h"

@interface RecordCountWithTimeLimitLogUploadStrategyTest : XCTestCase

@end

@implementation RecordCountWithTimeLimitLogUploadStrategyTest

- (void)testLessThanRecordThresholdCount {
    int32_t thresholdCount = 5;
    int32_t uploadCheckPeriod = 2;//2 sec
    
    id<LogStorageStatus> logStorageStatus = mockProtocol(@protocol(LogStorageStatus));
    [given([logStorageStatus getRecordCount]) willReturnLong:(int64_t)(thresholdCount - 1)];
    
    RecordCountWithTimeLimitLogUploadStrategy *strategy = [[RecordCountWithTimeLimitLogUploadStrategy alloc] initWithCountThreshold:thresholdCount timeLimit:uploadCheckPeriod timeUnit:TIME_UNIT_SECONDS];
    
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_NOOP);
}

- (void)testEqualToRecordThresholdCount {
    int32_t thresholdCount = 5;
    int32_t uploadCheckPeriod = 2;//2 sec
    
    id<LogStorageStatus> logStorageStatus = mockProtocol(@protocol(LogStorageStatus));
    [given([logStorageStatus getRecordCount]) willReturnLong:(int64_t)(thresholdCount)];
    
    RecordCountWithTimeLimitLogUploadStrategy *strategy = [[RecordCountWithTimeLimitLogUploadStrategy alloc] initWithCountThreshold:thresholdCount timeLimit:uploadCheckPeriod timeUnit:TIME_UNIT_SECONDS];
    
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_UPLOAD);
}

- (void)testGreaterThanRecordThresholdCount {
    int32_t thresholdCount = 5;
    int32_t uploadCheckPeriod = 2; //2 sec
    
    id<LogStorageStatus> logStorageStatus = mockProtocol(@protocol(LogStorageStatus));
    [given([logStorageStatus getRecordCount]) willReturnLong:(int64_t)(thresholdCount + 1)];
    
    RecordCountWithTimeLimitLogUploadStrategy *strategy = [[RecordCountWithTimeLimitLogUploadStrategy alloc] initWithCountThreshold:thresholdCount timeLimit:uploadCheckPeriod timeUnit:TIME_UNIT_SECONDS];
    
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_UPLOAD);
}

- (void)testUploadAfterSomeTime {
    int32_t thresholdCount = 5;
    int32_t uploadCheckPeriod = 2; //2 sec
    
    id<LogStorageStatus> logStorageStatus = mockProtocol(@protocol(LogStorageStatus));
    [given([logStorageStatus getRecordCount]) willReturnLong:(int64_t)0];
    
    RecordCountWithTimeLimitLogUploadStrategy *strategy = [[RecordCountWithTimeLimitLogUploadStrategy alloc] initWithCountThreshold:thresholdCount timeLimit:uploadCheckPeriod timeUnit:TIME_UNIT_SECONDS];
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_NOOP);
    
    [NSThread sleepForTimeInterval:uploadCheckPeriod/2];
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_NOOP);
    
    [NSThread sleepForTimeInterval:uploadCheckPeriod/2];
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_UPLOAD);
    
    [NSThread sleepForTimeInterval:uploadCheckPeriod/2];
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_NOOP);
    
    [NSThread sleepForTimeInterval:uploadCheckPeriod/2];
    XCTAssertEqual([strategy isUploadNeededForStorageStatus:logStorageStatus], LOG_UPLOAD_STRATEGY_DECISION_UPLOAD);
}

@end
