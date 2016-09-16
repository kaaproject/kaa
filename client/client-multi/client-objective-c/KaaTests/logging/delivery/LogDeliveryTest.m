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
    
    const NSInteger runnersCount = 5;
    
    __block BucketInfo *bucketInfo = [[BucketInfo alloc] initWithBucketId:status.requestId logCount:1];
    logCollector.bucketInfoDictionary[@(status.requestId)] = bucketInfo;
    for (NSInteger i = 0; i < runnersCount; i++) {
	    BucketRunner *brunner = [[BucketRunner alloc] init];
	    [logCollector addDeliveryRunner:brunner bucketInfo:bucketInfo];
    }
    
    bucketInfo.receivedResponseTime = [NSDate currentTimeInMilliseconds];
    
    [[executorContext getCallbackExecutor] addOperationWithBlock:^{
        [logCollector notifyOnSuccessDeliveryRunnersWithBucketInfo:bucketInfo];
    }];
    [NSThread sleepForTimeInterval:0.1];
    
    for (int i = 0; i < [([logCollector getDeliveryRunnerDictionary])[@(status.requestId)] count]; i++) {
	    BucketRunner *arunner = ([logCollector getDeliveryRunnerDictionary])[@(status.requestId)][i];
	    @try {
	        [[[NSOperationQueue alloc] init] addOperationWithBlock:^{
	            BucketInfo *bucketInfo = [arunner getValue];
                NSLog(@"arunner: %@", arunner);
	            NSLog(@"Received log record delivery info. Bucket Id [%d]. Record delivery time [%f ms]", bucketInfo.bucketId, bucketInfo.bucketDeliveryDuration);
	        }];
	    }
	    @catch (NSException *exception) {
	        NSLog(@"Exception was caught while waiting for callback");
	    }
    }
    [NSThread sleepForTimeInterval:0.1];
}

@end