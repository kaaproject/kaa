/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import "DefaultFailoverStrategy.h"
#import "KaaLogging.h"

static  NSString *const logTag = @"DefaultFailoverStrategy >>>";

static const int64_t kDefaultBootstrapServersRetryPeriod = 2;
static const int64_t kDefaultOperationsServersRetryPeriod = 2;
static const int64_t kDefaultNoConnectivityRetryPeriod = 5;
static const int64_t kDefaultTimeUnit = TIME_UNIT_SECONDS;

@interface DefaultFailoverStrategy ()

@property (nonatomic) int64_t noConnectivityRetryPeriod;

@end

@implementation DefaultFailoverStrategy

@synthesize bootstrapServersRetryPeriod = _bootstrapServersRetryPeriod;
@synthesize operationsServersRetryPeriod = _operationsServersRetryPeriod;
@synthesize timeUnit = _timeUnit;

- (instancetype)init {
    return [self initWithBootstrapServersRetryPeriod:kDefaultBootstrapServersRetryPeriod
                        operationsServersRetryPeriod:kDefaultOperationsServersRetryPeriod
                           noConnectivityRetryPeriod:kDefaultNoConnectivityRetryPeriod
                                            timeUnit:kDefaultTimeUnit];
}

- (instancetype)initWithBootstrapServersRetryPeriod:(int64_t)bootstrapServersRetryPeriod
                       operationsServersRetryPeriod:(int64_t)operationsServersRetryPeriod
                          noConnectivityRetryPeriod:(int64_t)noConnectivityRetryPeriod
                                           timeUnit:(TimeUnit)timeUnit {
    self = [super init];
    if (self) {
        _bootstrapServersRetryPeriod = bootstrapServersRetryPeriod;
        _operationsServersRetryPeriod = operationsServersRetryPeriod;
        _noConnectivityRetryPeriod = noConnectivityRetryPeriod;
        _timeUnit = timeUnit;
    }
    return self;
}

- (void)onRecoverWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo {
    DDLogDebug(@"%@ SDK recovered after failover with connection info: %@", logTag, connectionInfo);
}

- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status {
    DDLogVerbose(@"%@ Producing failover decision for failover status: %i", logTag, status);
    switch (status) {
        case FAILOVER_STATUS_BOOTSTRAP_SERVERS_NA:
            return [[FailoverDecision alloc] initWithFailoverAction:FAILOVER_ACTION_RETRY
                                                        retryPeriod:self.bootstrapServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FAILOVER_STATUS_CURRENT_BOOTSTRAP_SERVER_NA:
            return [[FailoverDecision alloc] initWithFailoverAction:FAILOVER_ACTION_USE_NEXT_BOOTSTRAP
                                                        retryPeriod:self.bootstrapServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FAILOVER_STATUS_NO_OPERATION_SERVERS_RECEIVED:
            return [[FailoverDecision alloc] initWithFailoverAction:FAILOVER_ACTION_USE_NEXT_BOOTSTRAP
                                                        retryPeriod:self.bootstrapServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FAILOVER_STATUS_OPERATION_SERVERS_NA:
            return [[FailoverDecision alloc] initWithFailoverAction:FAILOVER_ACTION_RETRY
                                                        retryPeriod:self.operationsServersRetryPeriod
                                                           timeUnit:self.timeUnit];
        case FAILOVER_STATUS_NO_CONNECTIVITY:
            return [[FailoverDecision alloc] initWithFailoverAction:FAILOVER_ACTION_RETRY
                                                        retryPeriod:self.noConnectivityRetryPeriod
                                                           timeUnit:self.timeUnit];
        default:
            return [[FailoverDecision alloc] initWithFailoverAction:FAILOVER_ACTION_NOOP];
    }
}

@end
