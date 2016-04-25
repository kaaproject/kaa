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

#import <XCTest/XCTest.h>
#import <Kaa/Kaa.h>

@interface TestFailoverStrategy : DefaultFailoverStrategy

@end

@implementation TestFailoverStrategy

- (FailoverDecision *)decisionOnFailoverStatus:(FailoverStatus)status {
#pragma unused(status)
    return [[FailoverDecision alloc] initWithFailoverAction:FailoverActionFailure];
}

@end


@interface TestClientProfileContainer : NSObject <ProfileContainer>

@end

@implementation TestClientProfileContainer

- (KAADummyProfile *)getProfile {
    return [[KAADummyProfile alloc] init];
}

@end

@interface AbstractKaaClient (BootstrapManager)

- (instancetype)initWithPlatformContext:(id<KaaClientPlatformContext>)context delegate:(id<KaaClientStateDelegate>)delegate bootstrapManager:(id<BootstrapManager>)bsManager;

@end

@implementation AbstractKaaClient (BootstrapManager)

- (instancetype)initWithPlatformContext:(id<KaaClientPlatformContext>)context delegate:(id<KaaClientStateDelegate>)delegate bootstrapManager:(id<BootstrapManager>)bsManager {
    self = [self initWithPlatformContext:context delegate:delegate];
    [self setValue:bsManager forKey:@"bootstrapManager"];
    return self;
}

@end

@interface KaaClientTest : XCTestCase

@property (nonatomic, strong) id<KaaClientPlatformContext> context;
@property (nonatomic, strong) KaaClientProperties *properties;
@property (nonatomic, strong) DefaultBootstrapManager *bootstrapManagerMock;
@property (nonatomic, strong) AbstractKaaClient *client;
@property (nonatomic, strong) id<KaaClientStateDelegate> delegate;

@end

@implementation KaaClientTest

- (void)setUp {
    [super setUp];
    self.context = mockProtocol(@protocol(KaaClientPlatformContext));
    self.properties = mock([KaaClientProperties class]);
    self.delegate = mockProtocol(@protocol(KaaClientStateDelegate));

    [given([self.context getBase64]) willReturn:[[CommonBase64 alloc] init]];
    [given([self.context getProperties]) willReturn:self.properties];
    [given([self.context getExecutorContext]) willReturn:[[SimpleExecutorContext alloc] init]];
    
    [given([self.properties bootstrapServers]) willReturn:[self buildDummyConnectionInfo]];
    [given([self.properties propertiesHash]) willReturn:[@"test" dataUsingEncoding:NSUTF8StringEncoding]];
    
    self.bootstrapManagerMock = mock([DefaultBootstrapManager class]);
    self.client = [[AbstractKaaClient alloc] initWithPlatformContext:self.context delegate:self.delegate bootstrapManager:self.bootstrapManagerMock];
    [self.client setProfileContainer:[[TestClientProfileContainer alloc] init]];
}

- (void)testBasicLifeCycle {
    [self.client start];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onStarted];
    [verifyCount(self.bootstrapManagerMock, times(1)) receiveOperationsServerList];
    
    [self.client pause];
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onPaused];
    
    [self.client resume];
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onResume];
    
    [self.client stop];
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onStopped];
}

- (void)testBasicStartBootstrapFailure {
    NSException *exception = [NSException exceptionWithName:@"TransportException" reason:@"cause" userInfo:nil];
    [givenVoid([self.bootstrapManagerMock receiveOperationsServerList]) willThrow:exception];
    [self.client start];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onStartFailureWithException:anything()];
    [verifyCount(self.bootstrapManagerMock, times(1)) receiveOperationsServerList];
    
    [self.client stop];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onStopped];
}

- (void)testFailureOnStart {
    NSException *exception = [NSException exceptionWithName:@"Exception" reason:@"cause" userInfo:nil];
    [givenVoid([self.bootstrapManagerMock receiveOperationsServerList]) willThrow:exception];
    
    [self.client start];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onStartFailureWithException:anything()];
}

- (void)testFailureOnStop {
    NSException *exception = [NSException exceptionWithName:@"RuntimeException" reason:@"cause" userInfo:nil];
    [self.client start];
    
    AbstractLogCollector *logCollector = mock([AbstractLogCollector class]);
    [givenVoid([logCollector stop]) willThrow:exception];
    [self.client setValue:logCollector forKey:@"logCollector"];
    [self.client stop];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onStopFailureWithException:anything()];
}

- (void)testOnPauseFailure {
    NSException *exception = [NSException exceptionWithName:@"RuntimeException" reason:@"cause" userInfo:nil];
    [self.client start];
    
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    [givenVoid([clientState persist]) willThrow:exception];
    [self.client setValue:clientState forKey:@"clientState"];
    [self.client pause];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onPauseFailureWithException:anything()];
}

- (void)testFailureOnResume {
    [self.client start];
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onStarted];
    
    [self.client pause];
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onPaused];
    
    id<KaaInternalChannelManager> channelManager = mockProtocol(@protocol(KaaInternalChannelManager));
    NSException *exception = [NSException exceptionWithName:@"RuntimeException" reason:@"cause" userInfo:nil];
    [givenVoid([channelManager resume]) willThrow:exception];
    [self.client setValue:channelManager forKey:@"channelManager"];
    [self.client resume];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount(self.delegate, times(1)) onResumeFailureWithException:anything()];
}

- (void)testDefaultFailureDelegate {
    [self.client setFailoverStrategy:[[TestFailoverStrategy alloc] init]];
    
    [self.client start];
    
    ProtocolVersionPair *protocolPair = [[ProtocolVersionPair alloc] initWithId:1 version:1];
    ProtocolMetaData *metaData = [[ProtocolMetaData alloc] initWithAccessPointId:1
                                                             protocolVersionInfo:protocolPair
                                                                  connectionInfo:nil];
    id<TransportConnectionInfo> connectionInfo = [[GenericTransportInfo alloc] initWithServerType:SERVER_BOOTSTRAP
                                                                                             meta:metaData];
    [[self.client getChannelManager] onServerFailedWithConnectionInfo:connectionInfo
                                                       failoverStatus:FailoverStatusBootstrapServersNotAvailable];
    
    [NSThread sleepForTimeInterval:0.5];
    
    [verifyCount(self.delegate, times(1)) onStopped];
}

#pragma mark - Supporting methods
- (NSDictionary *)buildDummyConnectionInfo {
    ProtocolVersionPair *protVersInfo = [[ProtocolVersionPair alloc] init];
    protVersInfo.id = 1;
    protVersInfo.version = 1;
    ProtocolMetaData *protMetadata = [[ProtocolMetaData alloc] init];
    protMetadata.accessPointId = 1;
    protMetadata.protocolVersionInfo = protVersInfo;
    protMetadata.connectionInfo = nil;
    NSMutableArray *connectionInfoArray = [NSMutableArray arrayWithObject:[[GenericTransportInfo alloc] initWithServerType:SERVER_BOOTSTRAP meta:protMetadata]];
    TransportProtocolId *tpId = [[TransportProtocolId alloc] initWithId:1 version:1];
    NSDictionary *dictionary = [NSDictionary dictionaryWithObject:connectionInfoArray forKey:tpId];
    return dictionary;
}
     
@end
