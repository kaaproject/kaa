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

#import <XCTest/XCTest.h>

#define HC_SHORTHAND
#import <OCHamcrest/OCHamcrest.h>

#define MOCKITO_SHORTHAND
#import <OCMockito/OCMockito.h>

#import <Kaa/Kaa.h>
#import "TestsHelper.h"

@interface LogDeliveryDelegateImpl : NSObject <LogDeliveryDelegate>

@property (nonatomic) double scheduledBucketTimestamp;
@property (nonatomic) double bucketDeliveryDuration;

@end

@implementation LogDeliveryDelegateImpl

- (void)onLogDeliverySuccessWithBucketInfo:(BucketInfo *)bucketInfo {
    self.scheduledBucketTimestamp = bucketInfo.scheduledBucketTimestamp;
    self.bucketDeliveryDuration = bucketInfo.bucketDeliveryDuration;
}

- (void)onLogDeliveryFailureWithBucketInfo:(BucketInfo *)bucketInfo {
#pragma unused(bucketInfo)
    [NSException raise:NSInternalInconsistencyException format:@"Method is not expected to be called!"];
}

- (void)onLogDeliveryTimeoutWithBucketInfo:(BucketInfo *)bucketInfo {
#pragma unused(bucketInfo)
    [NSException raise:NSInternalInconsistencyException format:@"Method is not expected to be called!"];
}

@end

@interface LogDeliveryCallbackTests : XCTestCase

@property (nonatomic, strong) id<ExecutorContext> executorContext;
@property (nonatomic, strong) id<LogTransport> logTransport;
@property (nonatomic, strong) id<KaaChannelManager> channelManager;
@property (nonatomic, strong) id<FailoverManager> failoverManager;
@property (nonatomic, strong) id<LogUploadStrategy> strategy;
@property (nonatomic, strong) AbstractLogCollector *logCollector;

@end

@implementation LogDeliveryCallbackTests

- (void)setUp {
    [super setUp];
    self.executorContext = mockProtocol(@protocol(ExecutorContext));
    self.logTransport = mockProtocol(@protocol(LogTransport));
    self.channelManager = mockProtocol(@protocol(KaaChannelManager));
    self.failoverManager = mockProtocol(@protocol(FailoverManager));
    self.logCollector = [[AbstractLogCollector alloc] initWithTransport:self.logTransport
                                                        executorContext:self.executorContext
                                                         channelManager:self.channelManager
                                                        failoverManager:self.failoverManager];
    
    self.strategy = mockProtocol(@protocol(LogUploadStrategy));
    [given([self.strategy getMaxParallelUploads]) willReturn:@(10)];
    [self.logCollector setValue:self.strategy forKey:@"strategy"];
}

- (void)testSimpleCallbacksTriggering {
    
    id<LogDeliveryDelegate> delegate = mockProtocol(@protocol(LogDeliveryDelegate));
    [self.logCollector setLogDeliveryDelegate:delegate];
    
    [given([self.strategy getTimeout]) willReturn:@(0)];
    
    NSOperationQueue *callbackQueue = [[NSOperationQueue alloc] init];
    dispatch_queue_t schedulerQueue = dispatch_queue_create("scheduledExecutor", 0);
    [given([self.executorContext getCallbackExecutor]) willReturn:callbackQueue];
    [given([self.executorContext getSheduledExecutor]) willReturn:schedulerQueue];
    
    LogDeliveryStatus *status = [[LogDeliveryStatus alloc] init];
    status.requestId = 42;
    status.result = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 data:[NSArray arrayWithObject:status]]];
    
    BucketInfo *bucketInfo = [[BucketInfo alloc] initWithBucketId:42 logCount:1];
    self.logCollector.bucketInfoDictionary[@(bucketInfo.bucketId)] = bucketInfo;
    
    [self.logCollector onLogResponse:response];
    [NSThread sleepForTimeInterval:0.5];
    [verifyCount(delegate, times(1)) onLogDeliverySuccessWithBucketInfo:bucketInfo];
    

    status.result = SYNC_RESPONSE_RESULT_TYPE_FAILURE;
    self.logCollector.bucketInfoDictionary[@(bucketInfo.bucketId)] = bucketInfo;
    
    [self.logCollector onLogResponse:response];
    [NSThread sleepForTimeInterval:0.5];
    [verifyCount(delegate, times(1)) onLogDeliveryFailureWithBucketInfo:bucketInfo];
    
    
    id<LogStorage> storage = mockProtocol(@protocol(LogStorage));
    LogRecord *record = [[LogRecord alloc] initWithData:[NSMutableData data]];
    LogBucket *logBucket = [[LogBucket alloc] initWithBucketId:42 records:@[record]];
    [given([storage getNextBucket]) willReturn:logBucket];
    [self.logCollector setStorage:storage];
    self.logCollector.bucketInfoDictionary[@(bucketInfo.bucketId)] = bucketInfo;

    LogSyncRequest *request = mock([LogSyncRequest class]);
    [self.logCollector fillSyncRequest:request];
    [NSThread sleepForTimeInterval:0.5];
    [verifyCount(delegate, times(1)) onLogDeliveryTimeoutWithBucketInfo:bucketInfo];
}

- (void)testIfBucketInfoIsTimestamped {
    LogDeliveryDelegateImpl *delegate = [[LogDeliveryDelegateImpl alloc] init];
    [self.logCollector setLogDeliveryDelegate:delegate];
    
    NSOperationQueue *executor = [[NSOperationQueue alloc] init];
    [given([self.executorContext getCallbackExecutor]) willReturn:executor];
    
    LogDeliveryStatus *status = [[LogDeliveryStatus alloc] init];
    status.requestId = 42;
    status.result = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 data:[NSArray arrayWithObject:status]]];
    
    BucketInfo *bucketInfo = [[BucketInfo alloc] initWithBucketId:42 logCount:1];
    self.logCollector.bucketInfoDictionary[@(bucketInfo.bucketId)] = bucketInfo;
    
    [self.logCollector addDeliveryRunner:[[BucketRunner alloc] init] bucketInfo:bucketInfo];
    
    [self.logCollector onLogResponse:response];
    
    XCTAssertNotEqual(delegate.scheduledBucketTimestamp, 0);
    XCTAssertNotEqual(delegate.bucketDeliveryDuration, 0);
}

@end
