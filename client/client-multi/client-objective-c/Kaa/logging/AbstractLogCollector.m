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

#import "AbstractLogCollector.h"
#import "DefaultLogUploadStrategy.h"
#import "MemLogStorage.h"
#import "LogFailoverCommand.h"
#import "KaaChannelManager.h"
#import "FailoverManager.h"
#import "KaaLogging.h"
#import "LogCollector.h"

#define TAG @"AbstractLogCollector >>>"

@interface AbstractLogCollector () <LogFailoverCommand>

@property (nonatomic,strong) id<LogUploadStrategy> strategy;
@property (nonatomic,strong) id<KaaChannelManager> channelManager;
@property (nonatomic,strong) id<LogTransport> transport;
@property (nonatomic,strong) id<FailoverManager> failoverManager;
@property (nonatomic,strong) NSMutableDictionary *timeouts;
@property (nonatomic,strong) NSLock *timeoutsLock;
@property (atomic) BOOL uploadCheckInProgress;
@property (nonatomic,strong) NSLock *uploadCheckLock;
@property (nonatomic,strong) NSObject *uploadCheckGuard;   //variable to sync
@property (nonatomic,weak) id<LogDeliveryDelegate> logDeliveryDelegate;

- (void)checkDeliveryTimeout:(int32_t)bucketId;
- (void)processUploadDecision:(LogUploadStrategyDecision)decision;

@end

@interface TimeoutOperation : NSOperation

@property (nonatomic) int64_t timeout;
@property (nonatomic,weak) AbstractLogCollector *logCollector;
@property (nonatomic,strong) LogBlock *timeoutGroup;

- (instancetype)initWithLogCollector:(AbstractLogCollector *)logCollector timeout:(int64_t)timeout andGroup:(LogBlock *)group;

@end

@implementation AbstractLogCollector

- (instancetype)initWith:(id<LogTransport>)transport
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
        self.bucketRunnerDictionary = [NSMutableDictionary dictionary];
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
    LogBlock *group = nil;
    if ([[self.storage getStatus] getRecordCount] == 0) {
        DDLogDebug(@"%@ Log storage is empty", TAG);
        return;
    }
    if (![self isUploadAllowed]) {
        return;
    }
    group = [self.storage getRecordBlock];
    if (group) {
        NSArray *recordList = group.logRecords;
        if ([recordList count] > 0) {
            DDLogVerbose(@"%@ Sending %li log records", TAG, (long)[recordList count]);
            NSMutableArray *logs = [NSMutableArray array];
            for (LogRecord *record in recordList) {
                LogEntry *logEntry = [[LogEntry alloc] init];
                logEntry.data = [NSData dataWithData:record.data];
                [logs addObject:logEntry];
            }
            request.requestId = group.blockId;
            request.logEntries = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0 andData:logs];
            
            DDLogInfo(@"%@ Adding following bucket id [%i] for timeout tracking", TAG, group.blockId);
            NSOperation *timeoutOperation = [[TimeoutOperation alloc] initWithLogCollector:self
                                                                                   timeout:[self.strategy getTimeout]
                                                                                  andGroup:group];

            [self.timeoutsLock lock];
            [self.timeouts setObject:timeoutOperation forKey:[NSNumber numberWithLong:group.blockId]];
            [self.timeoutsLock unlock];
        }
    } else {
        DDLogWarn(@"%@ Log group is nil: log group size is too small", TAG);
    }
}

- (void)onLogResponse:(LogSyncResponse *)response {
    @synchronized (self) {
        if (response.deliveryStatuses && response.deliveryStatuses.branch == KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0) {
            BOOL isAlreadyScheduled = NO;
            NSArray *deliveryStatuses = response.deliveryStatuses.data;
            __weak typeof(self) weakSelf = self;
            for (LogDeliveryStatus *status in deliveryStatuses) {
                if (status.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
                    [self.storage removeRecordBlock:status.requestId];
                    BucketInfo *bucketInfo = [self.bucketInfoDictionary objectForKey:[NSNumber numberWithInt:status.requestId]];
                    BucketRunner *currentRunner = [self.bucketRunnerDictionary objectForKey:[NSNumber numberWithInt:status.requestId]];
                    [currentRunner setValue:bucketInfo];
                    if (self.logDeliveryDelegate) {
                        [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                            [weakSelf.logDeliveryDelegate onLogDeliverySuccess:bucketInfo];
                        }];
                    }
                    [self.bucketInfoDictionary removeObjectForKey:[NSNumber numberWithInt:status.requestId]];
                } else {
                    [self.storage notifyUploadFailed:status.requestId];
                    [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
                        [weakSelf.strategy onFailure:weakSelf errorCode:[((NSNumber *)status.errorCode.data) intValue]];
                        if (weakSelf.logDeliveryDelegate) {
                            [weakSelf.logDeliveryDelegate onLogDeliveryFailure:[weakSelf.bucketInfoDictionary objectForKey:[NSNumber numberWithInt:status.requestId]]];
                        }
                    }];
                    isAlreadyScheduled = YES;
                }
                DDLogInfo(@"%@ Removing bucket id from timeouts: %i", TAG, status.requestId);
                [self.timeoutsLock lock];
                NSNumber *key = [NSNumber numberWithLong:status.requestId];
                NSOperation *timeout = [self.timeouts objectForKey:key];
                if (timeout) {
                    [self.timeouts removeObjectForKey:key];
                    [timeout cancel];
                }
                [self.timeoutsLock unlock];
            }
            if (!isAlreadyScheduled) {
                [self processUploadDecision:[self.strategy isUploadNeeded:[self.storage getStatus]]];
            }
        }
    }
}

- (void)stop {
    DDLogDebug(@"%@ Closing storage", TAG);
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
            DDLogVerbose(@"%@ Scheduling upload check with timeout: %li", TAG, (long)[self.strategy getUploadCheckPeriod]);
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

- (void)checkDeliveryTimeout:(int32_t)bucketId {
    DDLogDebug(@"%@ Checking for a delivery timeout of the bucket with id: [%i]", TAG, bucketId);
    [self.timeoutsLock lock];
    NSOperation *timeout = [self.timeouts objectForKey:[NSNumber numberWithLong:bucketId]];
    if (timeout) {
        [self.timeouts removeObjectForKey:[NSNumber numberWithLong:bucketId]];
    }
    [self.timeoutsLock unlock];
    
    if (timeout) {
        DDLogInfo(@"%@ Log delivery timeout detected for the bucket with id: [%i]", TAG, bucketId);
        [self.storage notifyUploadFailed:bucketId];
        __weak typeof(self)weakSelf = self;
        [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
            [weakSelf.strategy onTimeout:weakSelf];
            if (weakSelf.logDeliveryDelegate) {
                [weakSelf.logDeliveryDelegate onLogDeliveryTimeout:[self.bucketInfoDictionary objectForKey:[NSNumber numberWithInt:bucketId]]];
            }
        }];
        [timeout cancel];
    } else {
        DDLogVerbose(@"%@ No log delivery timeout for the bucket with id [%li] was detected", TAG, (long)bucketId);
    }
}

- (void)uploadIfNeeded {
    [self processUploadDecision:[self.strategy isUploadNeeded:[self.storage getStatus]]];
}

- (void)switchAccessPoint {
    id<TransportConnectionInfo> server = [self.channelManager getActiveServer:TRANSPORT_TYPE_LOGGING];
    if (server) {
        [self.failoverManager onServerFailed:server];
    } else {
        DDLogWarn(@"%@ Failed to switch Operation server. No channel is used for logging transport", TAG);
    }
}

- (void)retryLogUpload {
    [self uploadIfNeeded];
}

- (void)retryLogUpload:(int32_t)delay {
    __weak typeof(self)weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delay * NSEC_PER_SEC)), [self.executorContext getSheduledExecutor], ^{
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

- (instancetype)initWithLogCollector:(AbstractLogCollector *)logCollector timeout:(int64_t)timeout andGroup:(LogBlock *)group {
    self = [super init];
    if (self) {
        self.logCollector = logCollector;
        self.timeout = timeout;
        self.timeoutGroup = group;
    }
    return self;
}

- (void)main {
    if (self.isFinished && self.isCancelled) {
        DDLogDebug(@"%@ Timeout check worker for block: %i was interrupted before start", TAG, [self.timeoutGroup blockId]);
        return;
    }
    
    [NSThread sleepForTimeInterval:self.timeout];
    
    if (!self.isFinished && !self.isCancelled) {
        [self.logCollector checkDeliveryTimeout:[self.timeoutGroup blockId]];
        
    } else {
        DDLogDebug(@"%@ Timeout check worker for block: %i was interrupted after delay", TAG, [self.timeoutGroup blockId]);
    }
}

@end
