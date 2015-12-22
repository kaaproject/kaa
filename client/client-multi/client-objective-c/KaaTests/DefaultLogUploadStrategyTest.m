/*
 * Copyright 2014-2015 CyberVision, Inc.
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
#import "LogStorage.h"
#import "DefaultLogUploadStrategy.h"
#import "AbstractLogCollector.h"
#import "EndpointGen.h"
#import "LogCollector.h"
#import "ExecutorContext.h"
#import "MemLogStorage.h"

@interface TestLogStorageStatus : NSObject <LogStorageStatus>

@property (nonatomic) int64_t consumedVolume;
@property (nonatomic) int64_t recordCount;

@end

@implementation TestLogStorageStatus

- (instancetype)initWithConsumedVolume:(int64_t)consumedVolume andRecordCount:(int64_t)recordCount {
    self = [super init];
    if (self) {
        self.consumedVolume = consumedVolume;
        self.recordCount = recordCount;
    }
    return self;
}

- (int64_t)getConsumedVolume {
    return self.consumedVolume;
}

- (int64_t)getRecordCount {
    return self.recordCount;
}

@end


@interface DefaultLogUploadStrategyTest : XCTestCase

@end

@implementation DefaultLogUploadStrategyTest

- (void) testNOOPDecision {
    DefaultLogUploadStrategy *strategy = [[DefaultLogUploadStrategy alloc] initWithDefaults];
    [strategy setBatchSize:20];
    [strategy setVolumeThreshold:60];
    [strategy setTimeout:300];
    TestLogStorageStatus *status = [[TestLogStorageStatus alloc] initWithConsumedVolume:30 andRecordCount:3];
    
    XCTAssertEqual(LOG_UPLOAD_STRATEGY_DECISION_NOOP, [strategy isUploadNeeded:status]);
}

- (void) testUpdateDecision {
    DefaultLogUploadStrategy *strategy = [[DefaultLogUploadStrategy alloc] initWithDefaults];
    [strategy setBatchSize:20];
    [strategy setVolumeThreshold:60];
    [strategy setTimeout:300];
    TestLogStorageStatus *status = [[TestLogStorageStatus alloc] initWithConsumedVolume:60 andRecordCount:3];
    
    XCTAssertEqual(LOG_UPLOAD_STRATEGY_DECISION_UPLOAD, [strategy isUploadNeeded:status]);
    
    status = [[TestLogStorageStatus alloc] initWithConsumedVolume:70 andRecordCount:3];
    XCTAssertEqual(LOG_UPLOAD_STRATEGY_DECISION_UPLOAD, [strategy isUploadNeeded:status]);
}

- (void)testSuccessLogUploadCallback {
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    id<LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    id<LogUploadStrategy> strategy = mockProtocol(@protocol(LogUploadStrategy));
    
    AbstractLogCollector *logCollector = [[AbstractLogCollector alloc] initWith:logTransport executorContext:executorContext channelManager:channelManager failoverManager:failoverManager];
    [logCollector setValue:strategy forKey:@"strategy"];
    
    NSOperationQueue *executor = [[NSOperationQueue alloc] init];
    [given([executorContext getCallbackExecutor]) willReturn:executor];
    
    LogDeliveryStatus *status = [[LogDeliveryStatus alloc] init];
    status.requestId = 42;
    status.result = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 andData:[NSArray arrayWithObject:status]]];
    
    [logCollector onLogResponse:response];
    
    [NSThread sleepForTimeInterval:0.001];
    [verifyCount(strategy, times(1)) onSuccessLogUpload:status.requestId];
}

- (void)testFailureLogUploadCallback {
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    id<LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    id<LogUploadStrategy> strategy = mockProtocol(@protocol(LogUploadStrategy));
    
    AbstractLogCollector *logCollector = [[AbstractLogCollector alloc] initWith:logTransport executorContext:executorContext channelManager:channelManager failoverManager:failoverManager];
    [logCollector setValue:strategy forKey:@"strategy"];
    
    NSOperationQueue *executor = [[NSOperationQueue alloc] init];
    [given([executorContext getCallbackExecutor]) willReturn:executor];
    
    LogDeliveryStatus *status = [[LogDeliveryStatus alloc] init];
    status.requestId = 42;
    status.result = SYNC_RESPONSE_RESULT_TYPE_FAILURE;
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 andData:[NSArray arrayWithObject:status]]];
    
    [logCollector onLogResponse:response];
    
    [NSThread sleepForTimeInterval:0.001];
    [verifyCount(strategy, times(1)) onFailure:anything() errorCode:[((NSNumber *)status.errorCode.data) intValue]];
}

@end
