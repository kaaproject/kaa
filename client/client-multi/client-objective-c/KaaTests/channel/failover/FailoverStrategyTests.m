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

@interface CustomFailoverStrategy : DefaultFailoverStrategy

@property (nonatomic) int wantedNumberOfInvocationsOnRecover;
@property (nonatomic) int wantedNumberOfInvocationsOnFailure;

@end

@implementation CustomFailoverStrategy

- (void)onRecoverWithConnectionInfo:(id<TransportConnectionInfo>)connectionInfo {
    [super onRecoverWithConnectionInfo:connectionInfo];
    self.wantedNumberOfInvocationsOnRecover++;
}

- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status {
    self.wantedNumberOfInvocationsOnFailure++;
    return [super decisionOnFailoverStatus:status];
}

@end

@interface NewCustomFailoverStrategy : DefaultFailoverStrategy

@end

@implementation NewCustomFailoverStrategy

- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status {
#pragma unused(status)
    return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionUseNextBootstrap];
}

@end

@interface FailoverStrategyTests : XCTestCase

@property (nonatomic, strong) id<FailoverManager> failoverManager;
@property (nonatomic, strong) CustomFailoverStrategy *failoverStrategy;

@end

@implementation FailoverStrategyTests

- (void)setUp {
    [super setUp];
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<ExecutorContext> context = mockProtocol(@protocol(ExecutorContext));
    self.failoverStrategy = [[CustomFailoverStrategy alloc] init];
    self.failoverManager = [[DefaultFailoverManager alloc] initWithChannelManager:channelManager
                                                                          context:context
                                                                 failoverStrategy:self.failoverStrategy
                                                         failureResolutionTimeout:1 timeUnit:TIME_UNIT_MILLISECONDS];
}

- (void)testChangeStrategyAtRuntime {
    id<KaaClient> kaaClient = mockProtocol(@protocol(KaaClient));
    
    [givenVoid([kaaClient setFailoverStrategy:anything()]) willDo:^id(NSInvocation *invocation) {
        id<FailoverStrategy> strategy = [invocation mkt_arguments][0];
        [self.failoverManager setFailoverStrategy:strategy];
        return nil;
    }];
    
    FailoverStatus singleFailoverStatus = FailoverStatusOperationsServersNotAvailable;
    
    FailoverDecision *primaryFailoverDecision = [self.failoverManager decisionOnFailoverStatus:singleFailoverStatus];
    
    [kaaClient setFailoverStrategy:[[NewCustomFailoverStrategy alloc] init]];
    
    FailoverDecision *secondaryFailoverDecision = [self.failoverManager decisionOnFailoverStatus:singleFailoverStatus];
    
    XCTAssertNotEqual(primaryFailoverDecision.failoverAction, secondaryFailoverDecision.failoverAction);
}

- (void)testBasicFailoverStrategy {
    FailoverStatus incomingStatus = FailoverStatusBootstrapServersNotAvailable;
    XCTAssertNotNil([self.failoverManager decisionOnFailoverStatus:incomingStatus]);
    XCTAssertEqual(self.failoverStrategy.wantedNumberOfInvocationsOnFailure, 1);
    
    id<TransportConnectionInfo> connectionInfo = mockProtocol(@protocol(TransportConnectionInfo));
    [self.failoverManager onServerConnectedWithConnectionInfo:connectionInfo];
    XCTAssertEqual(self.failoverStrategy.wantedNumberOfInvocationsOnRecover, 1);
}

@end
