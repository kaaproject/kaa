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
#import "LogStorage.h"
#import "DefaultLogUploadStrategy.h"
#import "AbstractLogCollector.h"
#import "EndpointGen.h"
#import "LogCollector.h"
#import "ExecutorContext.h"
#import "MemLogStorage.h"
#import "DefaultLogCollector.h"

@interface NoTimeoutLogCollector : DefaultLogCollector

- (void)checkDeliveryTimeoutForBucketId:(int32_t)bucketId;

@end

@implementation NoTimeoutLogCollector

- (void)checkDeliveryTimeoutForBucketId:(int32_t)bucketId {
#pragma unused(bucketId)
    //NOTE: method stub to avoid removing buckets from timeout tracking
}

@end

@interface TestLogStorageStatus : NSObject <LogStorageStatus>

@property (nonatomic) int64_t consumedVolume;
@property (nonatomic) int64_t recordCount;

@end

@implementation TestLogStorageStatus

- (instancetype)initWithConsumedVolume:(int64_t)consumedVolume recordCount:(int64_t)recordCount {
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

- (void)testNOOPDecision {
    DefaultLogUploadStrategy *strategy = [[DefaultLogUploadStrategy alloc] initWithDefaults];
    [strategy setBatchSize:20];
    [strategy setVolumeThreshold:60];
    [strategy setTimeout:300];
    TestLogStorageStatus *status = [[TestLogStorageStatus alloc] initWithConsumedVolume:30 recordCount:3];
    
    XCTAssertEqual(LOG_UPLOAD_STRATEGY_DECISION_NOOP, [strategy isUploadNeededForStorageStatus:status]);
}

- (void)testUpdateDecision {
    DefaultLogUploadStrategy *strategy = [[DefaultLogUploadStrategy alloc] initWithDefaults];
    [strategy setBatchSize:20];
    [strategy setVolumeThreshold:60];
    [strategy setTimeout:300];
    TestLogStorageStatus *status = [[TestLogStorageStatus alloc] initWithConsumedVolume:60 recordCount:3];
    
    XCTAssertEqual(LOG_UPLOAD_STRATEGY_DECISION_UPLOAD, [strategy isUploadNeededForStorageStatus:status]);
    
    status = [[TestLogStorageStatus alloc] initWithConsumedVolume:70 recordCount:3];
    XCTAssertEqual(LOG_UPLOAD_STRATEGY_DECISION_UPLOAD, [strategy isUploadNeededForStorageStatus:status]);
}

- (void)testFailureLogUploadCallback {
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    id<LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    id<LogUploadStrategy> strategy = mockProtocol(@protocol(LogUploadStrategy));
    
    AbstractLogCollector *logCollector = [[AbstractLogCollector alloc] initWithTransport:logTransport
                                                                         executorContext:executorContext
                                                                          channelManager:channelManager
                                                                         failoverManager:failoverManager];
    [logCollector setValue:strategy forKey:@"strategy"];
    
    NSOperationQueue *executor = [[NSOperationQueue alloc] init];
    [given([executorContext getCallbackExecutor]) willReturn:executor];
    
    LogDeliveryStatus *status = [[LogDeliveryStatus alloc] init];
    status.requestId = 42;
    status.result = SYNC_RESPONSE_RESULT_TYPE_FAILURE;
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 data:[NSArray arrayWithObject:status]]];
    
    logCollector.bucketInfoDictionary[@(42)] = [[BucketInfo alloc] initWithBucketId:42 logCount:1];
    [logCollector onLogResponse:response];
    
    [NSThread sleepForTimeInterval:0.001];
    [verifyCount(strategy, times(1)) onFailureForController:anything() errorCode:[((NSNumber *)status.errorCode.data) intValue]];
}

- (void)testMaxParallelLogUploadCountInSyncRequest {
    [self maxParallelUploadCountInSyncRequestHelper:0];
    [self maxParallelUploadCountInSyncRequestHelper:3];
    [self maxParallelUploadCountInSyncRequestHelper:5];
}

- (void)testMaxParallelLogUploadCountInDecision {
    [self maxParallelUploadCountInDecisionHelper:0];
    [self maxParallelUploadCountInDecisionHelper:3];
    [self maxParallelUploadCountInDecisionHelper:5];
}

- (void)maxParallelUploadCountInSyncRequestHelper:(int64_t)maxParallelUploads {
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    id<LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    
    NSOperationQueue *executor = [[NSOperationQueue alloc] init];
    NSOperationQueue *apiExecutor = [[NSOperationQueue alloc] init];
    dispatch_queue_t schedulerQueue = dispatch_queue_create("scheduler", 0);
    [given([executorContext getCallbackExecutor]) willReturn:executor];
    [given([executorContext getApiExecutor]) willReturn:apiExecutor];
    [given([executorContext getSheduledExecutor]) willReturn:schedulerQueue];
    
    AbstractLogCollector *logCollector = [[NoTimeoutLogCollector alloc] initWithTransport:logTransport executorContext:executorContext channelManager:channelManager failoverManager:failoverManager];
    DefaultLogUploadStrategy *strategy = mock([DefaultLogUploadStrategy class]);
    [given([strategy getMaxParallelUploads]) willReturnLong:maxParallelUploads];
    [logCollector setValue:strategy forKey:@"strategy"];
    
    LogSyncRequest *request = mock([LogSyncRequest class]);
    
    NSMutableArray *statuses = [NSMutableArray array];
    
    for (int i = 0; i < maxParallelUploads; i++) {
        [logCollector addLogRecord:[[KAADummyLog alloc] init]];
        [NSThread sleepForTimeInterval:0.1];
        [logCollector fillSyncRequest:request];
        [statuses addObject:[[LogDeliveryStatus alloc] initWithRequestId:[request requestId] result:SYNC_RESPONSE_RESULT_TYPE_SUCCESS errorCode:[KAAUnion unionWithBranch:1]]];
    }
    
    [logCollector addLogRecord:[[KAADummyLog alloc] init]];
    [NSThread sleepForTimeInterval:0.1];
    [logCollector fillSyncRequest:request];
    [verifyCount(request, times(maxParallelUploads)) setLogEntries:anything()];
    
    if (statuses.count == 0 && maxParallelUploads == 0)
        return;
    
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 data:statuses]];
    [logCollector onLogResponse:response];
    
    [logCollector fillSyncRequest:request];
    [verifyCount(request, times(maxParallelUploads + 1)) setLogEntries:anything()];
}

- (void)maxParallelUploadCountInDecisionHelper:(int64_t)maxParallelUploads {
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    id<LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    
    NSOperationQueue *executor = [[NSOperationQueue alloc] init];
    NSOperationQueue *apiExecutor = [[NSOperationQueue alloc] init];
    dispatch_queue_t schedulerQueue = dispatch_queue_create("scheduler", 0);
    [given([executorContext getCallbackExecutor]) willReturn:executor];
    [given([executorContext getApiExecutor]) willReturn:apiExecutor];
    [given([executorContext getSheduledExecutor]) willReturn:schedulerQueue];

    AbstractLogCollector *logCollector = [[NoTimeoutLogCollector alloc] initWithTransport:logTransport executorContext:executorContext channelManager:channelManager failoverManager:failoverManager];
    DefaultLogUploadStrategy *strategy = mock([DefaultLogUploadStrategy class]);
    [given([strategy isUploadNeededForStorageStatus:anything()]) willReturnInt:LOG_UPLOAD_STRATEGY_DECISION_UPLOAD];
    [given([strategy getMaxParallelUploads]) willReturnLong:maxParallelUploads];
    [logCollector setValue:strategy forKey:@"strategy"];
    
    LogSyncRequest *request = mock([LogSyncRequest class]);
    
    NSMutableArray *statuses = [NSMutableArray array];
    
    for (int i = 0; i < maxParallelUploads; i++) {
        [logCollector addLogRecord:[[KAADummyLog alloc] init]];
        [NSThread sleepForTimeInterval:0.1];
        [logCollector fillSyncRequest:request];
    }
    
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 data:statuses]];
    [logCollector onLogResponse:response];
    [verifyCount(logTransport, times(maxParallelUploads)) sync];
}


@end
