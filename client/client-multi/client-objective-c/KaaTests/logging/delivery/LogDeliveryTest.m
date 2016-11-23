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

@import XCTest;

#import "EndpointGen.h"
#import "LogStorage.h"
#import "ExecutorContext.h"
#import "DefaultLogCollector.h"
#import "NSDate+Timestamp.h"

@interface LogDeliveryTest : XCTestCase

@end


@implementation LogDeliveryTest

- (void)testLogDeliveryTimeCalculation {
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
    
    AbstractLogCollector *logCollector = [[DefaultLogCollector alloc] initWithTransport:logTransport executorContext:executorContext channelManager:channelManager failoverManager:failoverManager];
    
    LogDeliveryStatus *status = [[LogDeliveryStatus alloc] init];
    status.result = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    status.requestId = 42;
    
    const NSInteger kIterCount = 10;
    
    NSMutableArray<BucketInfo *> *bucketInfoInstances = [NSMutableArray new];
    for (int i = 0; i < kIterCount; i++) {
        BucketInfo *bucketInfo = [[BucketInfo alloc] initWithBucketId:status.requestId logCount:1];
        bucketInfo.receivedResponseTime = [NSDate currentTimeInMilliseconds];
        [bucketInfoInstances addObject:bucketInfo];
    }
    
    logCollector.bucketInfoDictionary[@(status.requestId)] = bucketInfoInstances;
    
    for (NSInteger i = 0; i < kIterCount; i++) {
        BucketRunner *brunner = [[BucketRunner alloc] init];
        [logCollector addDeliveryRunner:brunner byBucketInfoKey:@([[bucketInfoInstances objectAtIndex:i] bucketId])];
    }
    
    [[executorContext getCallbackExecutor] addOperationWithBlock:^{
        for (NSInteger i = 0; i < kIterCount; i++) {
            [logCollector notifyOnSuccessDeliveryRunnersWithBucketInfo:bucketInfoInstances[i]];
        }
    }];
    [NSThread sleepForTimeInterval:0.1];
    
    for (NSInteger i = 0; i < kIterCount; i++) {
        XCTAssertLessThan(bucketInfoInstances[i].bucketDeliveryDuration, 1.f);
    }
}

@end

