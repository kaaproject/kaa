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

#import "AbstractLogCollector.h"
#import "DefaultLogUploadStrategy.h"
#import "MemLogStorage.h"
#import "LogFailoverCommand.h"
#import "KaaChannelManager.h"
#import "FailoverManager.h"
#import "KaaLogging.h"
#import "LogCollector.h"

#pragma clang diagnostic ignored "-Wprotocol"

#define TAG @"AbstractLogCollector >>>"

@interface AbstractLogCollector () <LogFailoverCommand>

@property (nonatomic, strong) id<LogUploadStrategy> strategy;
@property (nonatomic, strong) id<KaaChannelManager> channelManager;
@property (nonatomic, strong) id<LogTransport> transport;
@property (nonatomic, strong) id<FailoverManager> failoverManager;
@property (nonatomic, strong) NSMutableDictionary *timeouts; //<NSNumber<int32_t>, NSOperation> as key-value
@property (nonatomic, strong) NSLock *timeoutsLock;
@property (atomic) BOOL uploadCheckInProgress;
@property (nonatomic, strong) NSLock *uploadCheckLock;
@property (nonatomic, strong) NSObject *uploadCheckGuard;   //variable to sync
@property (nonatomic, weak) id<LogDeliveryDelegate> logDeliveryDelegate;
@property (nonatomic, strong) NSMutableDictionary *deliveryRunnerDictionary; //<NSNumber<int32_t>, NSArray<BucketRunner>> as key-value

- (void)checkDeliveryTimeoutForBucketId:(int32_t)bucketId;
- (void)processUploadDecision:(LogUploadStrategyDecision)decision;

@end

@interface TimeoutOperation : NSOperation

@property (nonatomic, weak) AbstractLogCollector *logCollector;
@property (nonatomic, strong) LogBucket *timeoutBucket;

- (instancetype)initWithLogCollector:(AbstractLogCollector *)logCollector bucket:(LogBucket *)bucket;

@end

@implementation AbstractLogCollector

- (instancetype)initWithTransport:(id<LogTransport>)transport
                  executorContext:(id<ExecutorContext>)executorContext
                   channelManager:(id<KaaChannelManager>)channelManager
                  failoverManager:(id<FailoverManager>)failoverManager {
    self = [super init];
    if (self) {
        self.strategy = [[DefaultLogUploadStrategy alloc] initWithDefaults];
        self.storage = [[MemLogStorage alloc] initWithBucketSize:[self.strategy getBatchSize] bucketRecordCount:[self.strategy getBatchCount]];
        self.channelManager = channelManager;
        self.transport = transport;
        _executorContext = executorContext;
        self.failoverManager = failoverManager;
        self.timeouts = [NSMutableDictionary dictionary];
        self.timeoutsLock = [[NSLock alloc] init];
        self.uploadCheckInProgress = NO;
        self.uploadCheckLock = [[NSLock alloc] init];
        self.uploadCheckGuard = [[NSObject alloc] init];
        self.logDeliveryDelegate = nil;
        self.bucketInfoDictionary = [NSMutableDictionary dictionary];
        self.deliveryRunnerDictionary = [NSMutableDictionary dictionary];
    }
    return self;
}

- (void)setLogDeliveryDelegate:(id<LogDeliveryDelegate>)logDeliveryDelegate {
    _logDeliveryDelegate = logDeliveryDelegate;
}

- (void)setStrategy:(id<LogUploadStrategy>)strategy {
    if (!strategy) {
        [NSException raise:NSInvalidArgumentException format:@"%@ Strategy is nil!", TAG];
    }
    _strategy = strategy;
    DDLogInfo(@"%@ New log upload strategy was set: %@", TAG, strategy);
}

- (void)setStorage:(id<LogStorage>)storage {
    if (!storage) {
        [NSException raise:NSInvalidArgumentException format:@"%@ Storage is nil!", TAG];
    }
    _storage = storage;
    DDLogInfo(@"%@ New log storage was set: %@", TAG, storage);
}

- (void)fillSyncRequest:(LogSyncRequest *)request {
    if (![self isUploadAllowed]) {
        return;
    }

    LogBucket *bucket = [self.storage getNextBucket];
    if (!bucket || [bucket.logRecords count] == 0) {
        DDLogVerbose(@"%@ No logs to send", TAG);
        return;
    }
    
    DDLogVerbose(@"%@ Sending %li log records", TAG, (long)[bucket.logRecords count]);
    NSMutableArray *logs = [NSMutableArray array];
    for (LogRecord *record in bucket.logRecords) {
        [logs addObject:[[LogEntry alloc] initWithData:[NSData dataWithData:record.data]]];
    }
    request.requestId = bucket.bucketId;
    request.logEntries = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0 data:logs];
    
    DDLogInfo(@"%@ Adding following bucket id [%i] for timeout tracking", TAG, bucket.bucketId);
    NSOperation *timeoutOperation = [[TimeoutOperation alloc] initWithLogCollector:self bucket:bucket];
    
    dispatch_time_t delay = dispatch_time(DISPATCH_TIME_NOW, (int64_t)([self.strategy getTimeout] * NSEC_PER_SEC));
    dispatch_after(delay, [self.executorContext getSheduledExecutor], ^{
        [timeoutOperation start];
    });
    
    [self.timeoutsLock lock];
    self.timeouts[@(bucket.bucketId)] = timeoutOperation;
    [self.timeoutsLock unlock];
}

- (void)onLogResponse:(LogSyncResponse *)response {
    @synchronized (self) {
        if (response.deliveryStatuses && response.deliveryStatuses.branch == KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0) {
            BOOL isAlreadyScheduled = NO;
            NSArray *deliveryStatuses = response.deliveryStatuses.data;
            __weak typeof(self) weakSelf = self;
            for (LogDeliveryStatus *status in deliveryStatuses) {
                
                NSNumber *key = @(status.requestId);
                
                __block BucketInfo *bucketInfo = self.bucketInfoDictionary[key];
                
                if (bucketInfo) {
                    [self.bucketInfoDictionary removeObjectForKey:key];

                    if (status.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
                        [self.storage removeBucketWithId:status.requestId];
                        
                        [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                            [weakSelf notifyOnSuccessDeliveryRunnersWithBucketInfo:bucketInfo];
                        }];
                        
                        if (self.logDeliveryDelegate) {
                            [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                                [weakSelf.logDeliveryDelegate onLogDeliverySuccessWithBucketInfo:bucketInfo];
                            }];
                        }

                    } else {
                        [self.storage rollbackBucketWithId:status.requestId];
                        
                        [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                            LogDeliveryErrorCode errorCode = [(NSNumber *)status.errorCode.data intValue];
                            [weakSelf.strategy onFailureForController:weakSelf errorCode:errorCode];
                        }];
                        
                        if (self.logDeliveryDelegate) {
                            [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                                [weakSelf.logDeliveryDelegate onLogDeliveryFailureWithBucketInfo:bucketInfo];
                            }];
                        }
                        
                        isAlreadyScheduled = YES;
                    }
                } else {
                    DDLogWarn(@"%@ Can't process log response: no bucket info for id: %i", TAG, status.requestId);
                }
                
                DDLogInfo(@"%@ Removing bucket id from timeouts: %i", TAG, status.requestId);
                [self.timeoutsLock lock];
                
                NSOperation *timeout = self.timeouts[key];
                if (timeout) {
                    [self.timeouts removeObjectForKey:key];
                    [timeout cancel];
                }
                [self.timeoutsLock unlock];
            }
            
            if (!isAlreadyScheduled) {
                [self processUploadDecision:[self.strategy isUploadNeededForStorageStatus:[self.storage getStatus]]];
            }
        }
    }
}

- (void)stop {
    [self.storage close];
    DDLogDebug(@"%@ Clearing timeouts map", TAG);
    for (NSOperation *timeout in self.timeouts.allValues) {
        [timeout cancel];
    }
    [self.timeouts removeAllObjects];
}

- (void)processUploadDecision:(LogUploadStrategyDecision)decision {
    switch (decision) {
        case LOG_UPLOAD_STRATEGY_DECISION_UPLOAD:
            if ([self isUploadAllowed]) {
                [self.transport sync];
            }            break;
        case LOG_UPLOAD_STRATEGY_DECISION_NOOP:
            if ([self.strategy getUploadCheckPeriod] > 0 && [[self.storage getStatus] getRecordCount] > 0) {
                [self scheduleUploadCheck];
            }
            break;
        default:
            break;
    }
}

- (void)scheduleUploadCheck {
    DDLogVerbose(@"%@ Attempt to execute upload check: %i", TAG, self.uploadCheckInProgress);
    @synchronized(self.uploadCheckGuard) {
        if (!self.uploadCheckInProgress) {
            DDLogVerbose(@"%@ Scheduling upload check with timeout: %i", TAG, [self.strategy getUploadCheckPeriod]);
            self.uploadCheckInProgress = YES;
            __weak typeof(self)weakSelf = self;
            dispatch_time_t delay = dispatch_time(DISPATCH_TIME_NOW, (int64_t)([self.strategy getUploadCheckPeriod] * NSEC_PER_SEC));
            dispatch_after(delay, [self.executorContext getSheduledExecutor], ^{
                
                @synchronized(self.uploadCheckGuard) {
                    weakSelf.uploadCheckInProgress = NO;
                }
                
                [weakSelf uploadIfNeeded];
            });
        } else {
            DDLogVerbose(@"%@ Upload check is already scheduled!", TAG);
        }
    }
}

- (void)checkDeliveryTimeoutForBucketId:(int32_t)bucketId {
    DDLogDebug(@"%@ Checking for a delivery timeout of the bucket with id: [%i]", TAG, bucketId);
    [self.timeoutsLock lock];
    NSOperation *timeout = self.timeouts[@(bucketId)];
    if (timeout) {
        [self.timeouts removeObjectForKey:@(bucketId)];
    }
    [self.timeoutsLock unlock];
    
    if (timeout) {
        DDLogInfo(@"%@ Log delivery timeout detected for the bucket with id: [%i]", TAG, bucketId);
        [self.storage rollbackBucketWithId:bucketId];
        
        __weak typeof(self)weakSelf = self;
        [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
            [weakSelf.strategy onTimeoutForController:weakSelf];
        }];
        if (self.logDeliveryDelegate) {
            [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                BucketInfo *bucket = weakSelf.bucketInfoDictionary[@(bucketId)];
                [weakSelf.logDeliveryDelegate onLogDeliveryTimeoutWithBucketInfo:bucket];
            }];
        }
        [timeout cancel];
    } else {
        DDLogVerbose(@"%@ No log delivery timeout for the bucket with id [%i] was detected", TAG, bucketId);
    }
}

- (void)uploadIfNeeded {
    [self processUploadDecision:[self.strategy isUploadNeededForStorageStatus:[self.storage getStatus]]];
}

- (void)addDeliveryRunner:(BucketRunner *)runner bucketInfo:(BucketInfo *)bucketInfo {
    @synchronized(self.deliveryRunnerDictionary) {
        NSNumber *bucketKey = @(bucketInfo.bucketId);
        
        NSMutableArray *deliveryRunners = self.deliveryRunnerDictionary[bucketKey];
        if (!deliveryRunners) {
            deliveryRunners = [NSMutableArray array];
            self.deliveryRunnerDictionary[bucketKey] = deliveryRunners;
        }
        
        [deliveryRunners addObject:runner];
    }
}

- (void)notifyOnSuccessDeliveryRunnersWithBucketInfo:(BucketInfo *)bucketInfo {
    @synchronized(self.deliveryRunnerDictionary) {
        NSNumber *bucketKey = @(bucketInfo.bucketId);
        
        NSMutableArray *deliveryRunners = self.deliveryRunnerDictionary[bucketKey];
        if (deliveryRunners) {
            for (BucketRunner *runner in deliveryRunners) {
                [runner setValue:bucketInfo];
            }
            [self.deliveryRunnerDictionary removeObjectForKey:bucketKey];
        }
    }
}

- (void)switchAccessPoint {
    id<TransportConnectionInfo> server = [self.channelManager getActiveServerForType:TRANSPORT_TYPE_LOGGING];
    if (server) {
        [self.failoverManager onServerFailedWithConnectionInfo:server failoverStatus:FailoverStatusOperationsServersNotAvailable];
    } else {
        DDLogWarn(@"%@ Failed to switch Operation server. No channel is used for logging transport", TAG);
    }
}

- (void)retryLogUpload {
    [self uploadIfNeeded];
}

- (void)retryLogUploadWithDelay:(int32_t)delay {
    __weak typeof(self)weakSelf = self;
    dispatch_time_t dispatchDelay = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delay * NSEC_PER_SEC));
    dispatch_after(dispatchDelay, [self.executorContext getSheduledExecutor], ^{
        [weakSelf uploadIfNeeded];
    });
}

- (BOOL)isUploadAllowed {
    if (self.timeouts.count >= [self.strategy getMaxParallelUploads]) {
        DDLogDebug(@"%@ Ignore log upload: too much pending requests. Max allowed: %lld", TAG, [self.strategy getMaxParallelUploads]);
        return NO;
    }
    return YES;
}

@end

@implementation TimeoutOperation

- (instancetype)initWithLogCollector:(AbstractLogCollector *)logCollector bucket:(LogBucket *)bucket {
    self = [super init];
    if (self) {
        self.logCollector = logCollector;
        self.timeoutBucket = bucket;
    }
    return self;
}

- (void)main {
    
    if (!self.isFinished && !self.isCancelled) {
        [self.logCollector checkDeliveryTimeoutForBucketId:self.timeoutBucket.bucketId];
    } else {
        DDLogDebug(@"%@ Timeout operation for bucket: %i was interrupted", TAG, self.timeoutBucket.bucketId);
    }
}

@end
