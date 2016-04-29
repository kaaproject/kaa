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

#import <Foundation/Foundation.h>
#import "LogTransport.h"
#import "ExecutorContext.h"
#import "LogStorage.h"
#import "KaaChannelManager.h"
#import "LogCollector.h"

/**
 * Designed to be abstraction layer for LogCollector.
 */
@interface AbstractLogCollector : NSObject <LogProcessor,LogCollector>

@property (nonatomic, strong, readonly) id<ExecutorContext> executorContext;
@property (nonatomic, strong, readonly) id<LogStorage> storage;
@property (nonatomic, strong) NSMutableDictionary *bucketInfoDictionary; // <NSNumber<int32_t>, BucketInfo> as Key-Value

- (instancetype)initWithTransport:(id<LogTransport>)transport
                  executorContext:(id<ExecutorContext>)executorContext
                   channelManager:(id<KaaChannelManager>)channelManager
                  failoverManager:(id<FailoverManager>)failoverManager;

- (void)scheduleUploadCheck;

- (void)uploadIfNeeded;

- (void)addDeliveryRunner:(BucketRunner *)runner bucketInfo:(BucketInfo *)bucketInfo;

- (void)notifyOnSuccessDeliveryRunnersWithBucketInfo:(BucketInfo *)bucketInfo;

@end
