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
#import "KaaInternalChannelManager.h"
#import "IPTransportInfo.h"
#import "BootstrapTransport.h"
#import "DefaultBootstrapManager.h"
#import "TransportProtocolIdHolder.h"
#import "KeyUtils.h"
#import "KeyPair.h"
#import "DefaultFailoverManager.h"
#import "TestsHelper.h"
#import "DefaultFailoverStrategy.h"

#pragma mark - ChannelManagerMock

@interface ChannelManagerMock : NSObject <KaaInternalChannelManager>

@property (nonatomic) BOOL serverUpdated;
@property (nonatomic, strong) NSString *receivedURL;
@property (nonatomic) NSInteger callCounter;

@end

@implementation ChannelManagerMock

- (instancetype)init {
    self = [super init];
    if (self) {
        self.serverUpdated = NO;
        self.callCounter = 0;
    }
    return self;
}

- (void)setConnectivityChecker:(ConnectivityChecker *)checker {
#pragma unused(checker)
}

- (void)addChannel:(id<KaaDataChannel>)channel {
#pragma unused(channel)
}

- (void)removeChannel:(id<KaaDataChannel>)channel {
#pragma unused(channel)
}

- (NSArray *)getChannels {
    return nil;
}

- (id<KaaDataChannel>)getChannelById:(NSString *)channelId {
#pragma unused(channelId)
    return nil;
}

- (void)onServerFailedWithConnectionInfo:(id<TransportConnectionInfo>)server failoverStatus:(FailoverStatus)status {
#pragma unused(server, status)
}

- (void)setFailoverManager:(id<FailoverManager>)failoverManager {
#pragma unused(failoverManager)
}

- (void)onTransportConnectionInfoUpdated:(id<TransportConnectionInfo>)newServer {
    self.receivedURL = [[[IPTransportInfo alloc] initWithTransportInfo:newServer] getUrl];
    self.serverUpdated = YES;
    self.callCounter += 1;
}

- (void)clearChannelList {
}

- (void)setChannel:(id<KaaDataChannel>)channel withType:(TransportType)type {
#pragma unused(channel, type)
}

- (void)removeChannelById:(NSString *)channelId {
#pragma unused(channelId)
}

- (void)shutdown {
}

- (void)pause {
}

- (void)resume {
}

- (void)setOperationDemultiplexer:(id<KaaDataDemultiplexer>)demultiplexer {
#pragma unused(demultiplexer)
}

- (void)setOperationMultiplexer:(id<KaaDataMultiplexer>)multiplexer {
#pragma unused(multiplexer)
}

- (void)setBootstrapMultiplexer:(id<KaaDataMultiplexer>)multiplexer {
#pragma unused(multiplexer)
}

- (void)setBootstrapDemultiplexer:(id<KaaDataDemultiplexer>)demultiplexer {
#pragma unused(demultiplexer)
}

- (void)syncForTransportType:(TransportType)type {
#pragma unused(type)
}

- (void)syncAckForTransportType:(TransportType)type {
#pragma unused(type)
}

- (void)syncAll:(TransportType)type {
#pragma unused(type)
}

- (id<TransportConnectionInfo>)getActiveServerForType:(TransportType)type {
#pragma unused(type)
    return nil;
}

@end

#pragma mark - DefaultBootstrapManagerTest

@interface DefaultBootstrapManagerTest : XCTestCase

@property (nonatomic) BOOL exceptionCaught;

@end

@implementation DefaultBootstrapManagerTest

- (void)testReceiveOperationsServerList {
    id<BootstrapTransport> transport = mockProtocol(@protocol(BootstrapTransport));
    DefaultBootstrapManager *manager = [[DefaultBootstrapManager alloc] initWithTransport:transport
                                                                          executorContext:nil
                                                                          failureDelegate:nil];
    
    self.exceptionCaught = NO;
    @try {
        [manager receiveOperationsServerList];
        [manager useNextOperationsServerWithTransportId:[TransportProtocolIdHolder HTTPTransportID]
                                         failoverStatus:FailoverStatusNoConnectivity];
    }
    @catch (NSException *exception) {
        self.exceptionCaught = YES;
    }
    
    XCTAssertTrue(self.exceptionCaught);
    [manager receiveOperationsServerList];
    [verifyCount(transport, times(2)) sync];
}

- (void)testOperationsServerInfoRetrieving {
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    DefaultBootstrapManager *manager = [[DefaultBootstrapManager alloc] initWithTransport:nil
                                                                          executorContext:executorContext
                                                                          failureDelegate:nil];
    
    self.exceptionCaught = NO;
    
    @try {
        [manager useNextOperationsServerWithTransportId:[TransportProtocolIdHolder HTTPTransportID]
                                         failoverStatus:FailoverStatusNoConnectivity];
    }
    @catch (NSException *exception) {
        self.exceptionCaught = YES;
    }
    XCTAssertTrue(self.exceptionCaught);
    
    id<BootstrapTransport> transport = mockProtocol(@protocol(BootstrapTransport));
    
    //Generating pseudo bootstrap key
    [KeyUtils generateKeyPair];
    ProtocolMetaData *md = [TestsHelper buildMetaDataWithTransportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    NSArray *array = [NSArray arrayWithObject:md];
    
    NSOperationQueue *opQue = [[NSOperationQueue alloc] init];
    [opQue setMaxConcurrentOperationCount:1];
    ChannelManagerMock *channelManager = [[ChannelManagerMock alloc] init];
    [given([executorContext getSheduledExecutor]) willReturn:[opQue underlyingQueue]];
    id<FailoverStrategy> strategy = [[DefaultFailoverStrategy alloc] initWithBootstrapServersRetryPeriod:1 operationsServersRetryPeriod:1 noConnectivityRetryPeriod:1 timeUnit:TIME_UNIT_MILLISECONDS];
    DefaultFailoverManager *failoverManager = [[DefaultFailoverManager alloc] initWithChannelManager:channelManager context:executorContext failoverStrategy:strategy failureResolutionTimeout:1 timeUnit:TIME_UNIT_MILLISECONDS];
    
    [manager setChannelManager:channelManager];
    [manager setFailoverManager:failoverManager];
    [manager setTransport:transport];
    [manager onProtocolListUpdated:array];
    [manager useNextOperationsServerWithTransportId:[TransportProtocolIdHolder HTTPTransportID]
                                     failoverStatus:FailoverStatusNoConnectivity];
    
    XCTAssertTrue(channelManager.serverUpdated);
    XCTAssertEqualObjects(@"http://localhost:9889", [channelManager receivedURL]);
    
    [manager useNextOperationsServerByAccessPointId:(int32_t)[@"some.name" hash]];
    XCTAssertEqual(1, channelManager.callCounter);
}

- (void)testUseServerByDNSName {
    DefaultBootstrapManager *manager = [[DefaultBootstrapManager alloc] initWithTransport:nil
                                                                          executorContext:nil
                                                                          failureDelegate:nil];
    
    ChannelManagerMock *channelManager = [[ChannelManagerMock alloc] init];
    [manager setChannelManager:channelManager];
    
    id<BootstrapTransport> transport = mockProtocol(@protocol(BootstrapTransport));
    [manager setTransport:transport];
    
    //Generating pseudo bootstrap key
    [KeyUtils generateKeyPair];
    ProtocolMetaData *md = [TestsHelper buildMetaDataWithTransportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    NSArray *array = [NSArray arrayWithObject:md];
    
    [manager onProtocolListUpdated:array];
    XCTAssertEqualObjects(@"http://localhost:9889", [channelManager receivedURL]);
    
    [manager useNextOperationsServerByAccessPointId:(int32_t)[@"localhost2:9889" hash]];
    [verifyCount(transport, times(1)) sync];
    
    md = [TestsHelper buildMetaDataWithTransportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost2" port:9889 publicKey:[KeyUtils getPublicKey]];
    array = [NSArray arrayWithObject:md];
    
    [manager onProtocolListUpdated:array];
    XCTAssertEqualObjects(@"http://localhost2:9889", [channelManager receivedURL]);
    XCTAssertTrue(channelManager.serverUpdated);
}

@end
