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

#import "DefaultFailoverManager.h"
#import "KaaLogging.h"
#import "DefaultFailoverStrategy.h"
#import "KaaExceptions.h"

static NSString *const logTag = @"DefaultFailoverManager >>>";

static const int64_t kDefaultFailureResolutionTimeout = 10;
static const TimeUnit kDefaultTimeUnit = TIME_UNIT_SECONDS;


@interface Resolution : NSOperation

@property (nonatomic, weak) DefaultFailoverManager *failoverManager;
@property (nonatomic, weak) id<TransportConnectionInfo> info;

- (instancetype)initWithManager:(DefaultFailoverManager *)manager info:(id<TransportConnectionInfo>)info;

@end

@interface AccessPointIdResolution : NSObject

@property (nonatomic, readonly) int accessPointId;
@property (nonatomic) int64_t resolutionTimeMillis;
@property (nonatomic, strong) Resolution *resolution;

- (instancetype)initWithAccessId:(int)accessId resolution:(Resolution *)resolution;

@end

@interface DefaultFailoverManager ()

@property (nonatomic, strong) id<FailoverStrategy> failoverStrategy;
@property (nonatomic) int64_t failureResolutionTimeout;
@property (nonatomic) TimeUnit  timeUnit;

@property (nonatomic, strong) id<KaaChannelManager> kaaChannelMgr;
@property (nonatomic, strong) id<ExecutorContext> executorContext;

@property (nonatomic, strong) NSMutableDictionary *resolutionProgressMap;

- (void)cancelCurrentFailResolution:(AccessPointIdResolution *)resolution;

@end

@implementation DefaultFailoverManager

- (instancetype)initWithChannelManager:(id<KaaChannelManager>)channelMgr context:(id<ExecutorContext>)context {
    return [self initWithChannelManager:channelMgr
                                context:context
                       failoverStrategy:[[DefaultFailoverStrategy alloc] init]
               failureResolutionTimeout:kDefaultFailureResolutionTimeout
                               timeUnit:kDefaultTimeUnit];
}

- (instancetype)initWithChannelManager:(id<KaaChannelManager>)channelMgr
                               context:(id<ExecutorContext>)context
                      failoverStrategy:(id<FailoverStrategy>)failoverStrategy
              failureResolutionTimeout:(int64_t)failureResolutionTimeout
                              timeUnit:(TimeUnit)timeUnit {
    self = [super init];
    if (self) {
        self.kaaChannelMgr = channelMgr;
        self.executorContext = context;
        self.failoverStrategy = failoverStrategy;
        self.failureResolutionTimeout = failureResolutionTimeout;
        self.timeUnit = timeUnit;
        
        self.resolutionProgressMap = [NSMutableDictionary dictionary];
    }
    return self;
}

- (void)onServerFailedWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo failoverStatus:(FailoverStatus)status {
    
    if (!connectionInfo) {
        DDLogWarn(@"%@ Server failed, but connection info is nil, can't resolve", logTag);
        return;
    } else {
        DDLogInfo(@"%@ Server [%i, %i] failed", logTag, [connectionInfo serverType], [connectionInfo accessPointId]);
    }
    
    @synchronized(self) {
        int64_t currentResolutionTime = -1;
        AccessPointIdResolution *pointResolution = self.resolutionProgressMap[@([connectionInfo serverType])];
        if (pointResolution != nil) {
            currentResolutionTime = pointResolution.resolutionTimeMillis;
            if (pointResolution.accessPointId == [connectionInfo accessPointId]
                && pointResolution.resolution != nil
                && ([[NSDate date] timeIntervalSince1970] * 1000) < currentResolutionTime) {
                DDLogDebug(@"%@ Resolution is in progress for %@ server", logTag, connectionInfo);
                return;
            } else if (pointResolution.resolution != nil) {
                DDLogVerbose(@"%@ Cancelling old resolution: %@", logTag, pointResolution);
                [self cancelCurrentFailResolution:pointResolution];
            }
        }
        
        DDLogVerbose(@"%@ Next fail resolution will be available in [delay:%lli timeunit:%i]",
                     logTag, self.failureResolutionTimeout, self.timeUnit);
        
        Resolution *resolution = [[Resolution alloc] initWithManager:self info:connectionInfo];
        
        int64_t secondsTimeout = [TimeUtils convertValue:self.failureResolutionTimeout
                                            fromTimeUnit:self.timeUnit
                                              toTimeUnit:TIME_UNIT_SECONDS];
        dispatch_time_t delay = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(secondsTimeout * NSEC_PER_SEC));
        dispatch_after(delay, [self.executorContext getSheduledExecutor], ^{
            [resolution start];
        });
        
        [self.kaaChannelMgr onServerFailedWithConnectionInfo:connectionInfo failoverStatus:status];
        
        int64_t updatedResolutionTime = pointResolution != nil ?  pointResolution.resolutionTimeMillis : currentResolutionTime;
        AccessPointIdResolution *newPointResolution =
        [[AccessPointIdResolution alloc] initWithAccessId:[connectionInfo accessPointId] resolution:resolution];
        
        if (updatedResolutionTime != currentResolutionTime) {
            newPointResolution.resolutionTimeMillis = updatedResolutionTime;
        }
        
        self.resolutionProgressMap[@([connectionInfo serverType])] = newPointResolution;
    }
}

- (void)onServerChangedWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo {
    
    if (connectionInfo == nil) {
        DDLogWarn(@"%@ Server has changed, but its connection info is nil, can't resolve", logTag);
        return;
    } else {
        DDLogVerbose(@"%@ Server [%i, %i] has changed", logTag, [connectionInfo serverType], [connectionInfo accessPointId]);
    }
    
    @synchronized(self) {
        NSNumber *serverTypeKey = @([connectionInfo serverType]);
        AccessPointIdResolution *pointResolution = self.resolutionProgressMap[serverTypeKey];
        if (pointResolution == nil) {
            AccessPointIdResolution *newPointResolution =
            [[AccessPointIdResolution alloc] initWithAccessId:[connectionInfo accessPointId] resolution:nil];
            self.resolutionProgressMap[serverTypeKey] = newPointResolution;
        } else if (pointResolution.accessPointId != [connectionInfo accessPointId]) {
            if (pointResolution.resolution != nil) {
                DDLogVerbose(@"%@ Cancelling fail resolution: %@", logTag, pointResolution);
                [self cancelCurrentFailResolution:pointResolution];
            }
            AccessPointIdResolution *newPointResolution =
            [[AccessPointIdResolution alloc] initWithAccessId:[connectionInfo accessPointId] resolution:nil];
            self.resolutionProgressMap[serverTypeKey] = newPointResolution;
        } else {
            DDLogDebug(@"%@ Same server [%@] is used, nothing has changed", logTag, connectionInfo);
        }
    }
}

- (void)onServerConnectedWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo {
    
    DDLogVerbose(@"%@ Server %@ has connected", logTag, connectionInfo);
    if (connectionInfo == nil) {
        DDLogWarn(@"%@ Server connection info is nil, can't resolve", logTag);
        return;
    }
    
    [self.failoverStrategy onRecoverWithConnectionInfo:connectionInfo];
    
    @synchronized(self) {
        AccessPointIdResolution *pointResolution = self.resolutionProgressMap[@([connectionInfo serverType])];
        if (pointResolution == nil) {
            DDLogVerbose(@"%@ Server hasn't been set (failover resolution has happened), new server %@ can't be connected",
                         logTag, connectionInfo);
        } else if (pointResolution.resolution != nil && pointResolution.accessPointId == [connectionInfo accessPointId]) {
            DDLogVerbose(@"%@ Cancelling fail resolution: %@", logTag, pointResolution);
            [self cancelCurrentFailResolution:pointResolution];
        } else if (pointResolution.resolution != nil) {
            DDLogDebug(@"%@ Connection for outdated accessPointId: %i was received - ignoring. New accessPointId: %i",
                       logTag, [connectionInfo accessPointId], pointResolution.accessPointId);
        } else {
            DDLogVerbose(@"%@ There is no current resolution in progress, connected to the same server: %@",
                         logTag, connectionInfo);
        }
    }
}

- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status {
    
    DDLogVerbose(@"%@ Applying failover decision for status: %i", logTag, status);

    @synchronized(self) {
        AccessPointIdResolution *accessPointIdResolution = nil;
        int64_t resolutionTime = [[NSDate date] timeIntervalSince1970] * 1000;

        switch (status) {
            case FailoverStatusBootstrapServersNotAvailable:
            case FailoverStatusCurrentBootstrapServerNotAvailable:
                accessPointIdResolution = self.resolutionProgressMap[@(SERVER_BOOTSTRAP)];
                resolutionTime += [TimeUtils convertValue:[self.failoverStrategy bootstrapServersRetryPeriod]
                                             fromTimeUnit:[self.failoverStrategy timeUnit]
                                               toTimeUnit:TIME_UNIT_MILLISECONDS];
                break;
            case FailoverStatusNoOperationsServersReceived:
                accessPointIdResolution = self.resolutionProgressMap[@(SERVER_BOOTSTRAP)];
                break;
            case FailoverStatusOperationsServersNotAvailable:
                accessPointIdResolution = self.resolutionProgressMap[@(SERVER_OPERATIONS)];
                resolutionTime += [TimeUtils convertValue:[self.failoverStrategy operationsServersRetryPeriod]
                                             fromTimeUnit:[self.failoverStrategy timeUnit]
                                               toTimeUnit:TIME_UNIT_MILLISECONDS];
                break;
            case FailoverStatusNoConnectivity:
            case FailoverStatusEndpointCredentialsRevoked:
            case FailoverStatusEndpointVerificationFailed:
                break;
        }
        
        if (accessPointIdResolution != nil) {
            accessPointIdResolution.resolutionTimeMillis = resolutionTime;
        }
        
        return [self.failoverStrategy decisionOnFailoverStatus:status];
    }
}

- (void)setFailoverStrategy:(id<FailoverStrategy>)failoverStrategy {
    if (failoverStrategy == nil) {
        [NSException raise:KaaRuntimeException format:@"Failover strategy can't be nil!"];
    }
    
    _failoverStrategy = failoverStrategy;
}

- (void)cancelCurrentFailResolution:(AccessPointIdResolution *)pointResolution {
    if (pointResolution.resolution != nil) {
        [pointResolution.resolution cancel];
        pointResolution.resolution = nil;
    } else {
        DDLogVerbose(@"%@ Current resolution is nil, can't cancel", logTag);
    }
}

@end



@implementation Resolution

- (instancetype)initWithManager:(DefaultFailoverManager *)manager info:(id<TransportConnectionInfo>)info {
    self = [super init];
    if (self) {
        self.failoverManager = manager;
        self.info = info;
    }
    return self;
}

- (void)main {
    if (!self.isCancelled || !self.isFinished) {
        DDLogDebug(@"%@ Removing server %@ from resolution map for type: %i", logTag, self.info, [self.info serverType]);
        [self.failoverManager.resolutionProgressMap removeObjectForKey:@([self.info serverType])];
    }
}

@end



@implementation AccessPointIdResolution

- (instancetype)initWithAccessId:(int)accessId resolution:(Resolution *)resolution {
    self = [super init];
    if (self) {
        _accessPointId = accessId;
        self.resolution = resolution;
        self.resolutionTimeMillis = NSIntegerMax;
    }
    return self;
}

- (NSUInteger)hash {
    NSUInteger result = self.accessPointId;
    return 31 * result + (self.resolution != nil ? [self.resolution hash] : 0);
}

- (BOOL)isEqual:(id)object {
    if ([self isEqual:object]) {
        return YES;
    }
    if (!object || ![object isKindOfClass:[self class]]) {
        return false;
    }
    AccessPointIdResolution *pointResolution = (AccessPointIdResolution *)object;
    if (self.accessPointId != pointResolution.accessPointId) {
        return NO;
    }
    if (self.resolution != nil
        ? ![self.resolution isEqual:pointResolution.resolution]
        : pointResolution.resolution != nil) {
        return NO;
    }
    return YES;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"AccessPointIdResolution [accessPointId:%i resolutionTime:%lld resolution:%@]",
            self.accessPointId, self.resolutionTimeMillis, self.resolution];
}

@end
