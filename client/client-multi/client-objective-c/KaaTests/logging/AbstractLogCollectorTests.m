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

#import "LogStorage.h"
#import "DefaultLogUploadStrategy.h"
#import "AbstractLogCollector.h"
#import "EndpointGen.h"
#import "LogCollector.h"
#import "ExecutorContext.h"
#import "MemLogStorage.h"
#import "DefaultLogCollector.h"
#import "BucketInfo.h"

@interface AbstractLogCollectorTests : XCTestCase

@end

@implementation AbstractLogCollectorTests

- (void)testBucketInfoCleanUpAfterResponse {
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
    status.result = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    LogSyncResponse *response = [[LogSyncResponse alloc] initWithDeliveryStatuses:[KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 data:[NSArray arrayWithObject:status]]];
    
    logCollector.bucketInfoDictionary[@(42)] = [[BucketInfo alloc] initWithBucketId:42 logCount:1];
    logCollector.bucketInfoDictionary[@(13)] = [[BucketInfo alloc] initWithBucketId:13 logCount:1];

    [logCollector onLogResponse:response];
    
    XCTAssertEqual(logCollector.bucketInfoDictionary.count, 1);
}

@end
