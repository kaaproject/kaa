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
#import "DefaultChannelManager.h"
#import "TransportProtocolIdHolder.h"
#import "GenericTransportInfo.h"
#import "KeyUtils.h"
#import "DefaultFailoverManager.h"
#import "TestsHelper.h"

@interface DefaultChannelManagerTest : XCTestCase

@property (nonatomic, strong) NSDictionary *supportedTypes;
@property (nonatomic, strong) id<ExecutorContext> executorContext;

@end

@implementation DefaultChannelManagerTest

- (void)setUp {
    self.supportedTypes =
    [NSDictionary dictionaryWithObjects:@[@(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                          @(CHANNEL_DIRECTION_UP),
                                          @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                          @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                          @(CHANNEL_DIRECTION_DOWN)]
                                forKeys:@[@( TRANSPORT_TYPE_PROFILE),
                                          @(TRANSPORT_TYPE_CONFIGURATION),
                                          @(TRANSPORT_TYPE_NOTIFICATION),
                                          @(TRANSPORT_TYPE_USER),
                                          @(TRANSPORT_TYPE_EVENT)]];
    self.executorContext = mockProtocol(@protocol(ExecutorContext));
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    [queue setMaxConcurrentOperationCount:1];
    [given([self.executorContext getSheduledExecutor]) willReturn:[queue underlyingQueue]];
}

- (void)testNullBootStrapServer {
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    @try {
        DefaultChannelManager *channel = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                bootstrapServers:nil
                                                                                         context:nil
                                                                                 failureDelegate:nil];
        XCTAssertNil(channel);
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"TestNullBootStrapServer succeed. Caught ChannelRuntimeException");
    }
}

- (void)testEmptyBootstrapServer {
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    @try {
        DefaultChannelManager *channel = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                bootstrapServers:[[NSDictionary alloc] init]
                                                                                         context:nil
                                                                                 failureDelegate:nil];
        XCTAssertNil(channel);
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testEmptyBootstrapServer succeed. Caught ChannelRuntimeException");
    }
}

- (void)testEmptyBootstrapManager {
    @try {
        DefaultChannelManager *channel = [[DefaultChannelManager alloc] initWithBootstrapManager:nil
                                                                                bootstrapServers:nil
                                                                                         context:nil
                                                                                 failureDelegate:nil];
        XCTAssertNil(channel);
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testEmptyBootstrapManager succeed. Caught ChannelRuntimeException");
    }
}

- (void)testAddHttpChannel {
    [KeyUtils generateKeyPair];
    NSDictionary *bootstrapServers = [NSDictionary dictionaryWithObject:@[[self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]]] forKey:[TransportProtocolIdHolder HTTPTransportID]];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getSupportedTransportTypes]) willReturn:self.supportedTypes];
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder HTTPTransportID]];
    [given([channel getServerType]) willReturn:@(SERVER_OPERATIONS)];
    [given([channel getId]) willReturn:@"mock_channel"];
    
    id<KaaInternalChannelManager> channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                          bootstrapServers:bootstrapServers
                                                                                                   context:nil
                                                                                           failureDelegate:nil];
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    [channelManager setFailoverManager:failoverManager];
    [channelManager addChannel:channel];
    [channelManager addChannel:channel];
    
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9999 publicKey:[KeyUtils getPublicKey]];
    [channelManager onTransportConnectionInfoUpdated:server];
    [verifyCount(failoverManager, times(1)) onServerChangedWithConnectionInfo:anything()];
    
    XCTAssertEqualObjects(channel, [channelManager getChannelById:@"mock_channel"]);
    XCTAssertEqualObjects(channel, [[channelManager getChannels] firstObject]);
    
    [channelManager removeChannel:channel];
    XCTAssertNil([channelManager getChannelById:@"mock_channel"]);
    XCTAssertTrue([[channelManager getChannels] count] == 0);
    
    [channelManager addChannel:channel];
    [verifyCount(failoverManager, times(2)) onServerChangedWithConnectionInfo:anything()];
    [verifyCount(channel, times(2)) setServer:server];
    [channelManager clearChannelList];
    XCTAssertTrue([[channelManager getChannels] count] == 0);
}

- (void)testAddBootstrapChannel {
    [KeyUtils generateKeyPair];
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    NSDictionary *bootstrapServers = [NSDictionary dictionaryWithObject:@[server] forKey:[TransportProtocolIdHolder HTTPTransportID]];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getSupportedTransportTypes]) willReturn:self.supportedTypes];
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder HTTPTransportID]];
    [given([channel getServerType]) willReturn:@(SERVER_BOOTSTRAP)];
    [given([channel getId]) willReturn:@"mock_channel"];
    
    id<KaaChannelManager> channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                  bootstrapServers:bootstrapServers
                                                                                           context:nil
                                                                                   failureDelegate:nil];
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    [channelManager setFailoverManager:failoverManager];
    [channelManager addChannel:channel];
    
    [verifyCount(failoverManager, times(1)) onServerChangedWithConnectionInfo:anything()];
    XCTAssertEqualObjects(channel, [channelManager getChannelById:@"mock_channel"]);
    XCTAssertEqualObjects(channel, [[channelManager getChannels] firstObject]);
    
    [channelManager removeChannel:channel];
    XCTAssertNil([channelManager getChannelById:@"mock_channel"]);
    XCTAssertTrue([[channelManager getChannels] count] == 0);
    
    [channelManager addChannel:channel];
    [verifyCount(channel, times(2)) setServer:server];
}

- (void)testOperationServerFailed {
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getSupportedTransportTypes]) willReturn:self.supportedTypes];
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder HTTPTransportID]];
    [given([channel getId]) willReturn:@"mock_channel"];
    
    id<KaaInternalChannelManager> channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                          bootstrapServers:[self getDefaultBootstrapServers]
                                                                                                   context:nil
                                                                                           failureDelegate:nil];
    [channelManager addChannel:channel];
    
    id<TransportConnectionInfo> opServer = [self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9999 publicKey:[KeyUtils getPublicKey]];
    [channelManager onTransportConnectionInfoUpdated:opServer];
    
    [channelManager onServerFailedWithConnectionInfo:opServer failoverStatus:FailoverStatusNoConnectivity];
    [verifyCount(bootstrapManager, times(1)) useNextOperationsServerWithTransportId:[TransportProtocolIdHolder HTTPTransportID]
                                                                     failoverStatus:FailoverStatusNoConnectivity];
}

- (void)testBootstrapServerFailed {
    [KeyUtils generateKeyPair];
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    id<TransportConnectionInfo> server1 = [self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost2" port:9889 publicKey:[KeyUtils getPublicKey]];
    NSDictionary *bootstrapServers = [NSDictionary dictionaryWithObject:@[server, server1] forKey:[TransportProtocolIdHolder HTTPTransportID]];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getSupportedTransportTypes]) willReturn:self.supportedTypes];
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder HTTPTransportID]];
    [given([channel getServerType]) willReturn:@(SERVER_BOOTSTRAP)];
    [given([channel getId]) willReturn:@"mock_channel"];
    
    id<ExecutorContext> executorContext = mockProtocol(@protocol(ExecutorContext));
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    [queue setMaxConcurrentOperationCount:1];
    [given([executorContext getSheduledExecutor]) willReturn:[queue underlyingQueue]];
    id<KaaChannelManager> channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                  bootstrapServers:bootstrapServers
                                                                                           context:executorContext
                                                                                   failureDelegate:nil];
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    [channelManager setFailoverManager:failoverManager];
    
    [channelManager addChannel:channel];
    
    [verifyCount(failoverManager, times(1)) onServerChangedWithConnectionInfo:anything()];
    
    [channelManager onServerFailedWithConnectionInfo:server failoverStatus:FailoverStatusCurrentBootstrapServerNotAvailable];
    
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        [NSThread sleepForTimeInterval:1];
        [verifyCount(channel, times(1)) setServer:server1];
    }];
    [verifyCount(failoverManager, times(1)) decisionOnFailoverStatus:FailoverStatusCurrentBootstrapServerNotAvailable];
}


- (void)testSingleBootstrapServerFailed {
    [KeyUtils generateKeyPair];
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    NSDictionary *bootstrapServers = [NSDictionary dictionaryWithObject:@[server] forKey:[TransportProtocolIdHolder HTTPTransportID]];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getSupportedTransportTypes]) willReturn:self.supportedTypes];
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder HTTPTransportID]];
    [given([channel getServerType]) willReturn:@(SERVER_BOOTSTRAP)];
    [given([channel getId]) willReturn:@"mock_channel"];
    
    id<KaaChannelManager> channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                  bootstrapServers:bootstrapServers
                                                                                           context:nil
                                                                                   failureDelegate:nil];
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    [channelManager setFailoverManager:failoverManager];
    [channelManager addChannel:channel];
    
    [verifyCount(failoverManager, times(1)) onServerChangedWithConnectionInfo:anything()];
    
    [channelManager onServerFailedWithConnectionInfo:server failoverStatus:FailoverStatusCurrentBootstrapServerNotAvailable];
}

- (void)testRemoveHttpLpChannel {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];

    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    
    NSDictionary *typesForChannel2 =
    [NSDictionary dictionaryWithObjects:@[@(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                          @(CHANNEL_DIRECTION_UP),
                                          @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                          @(CHANNEL_DIRECTION_DOWN)]
                                forKeys:@[@( TRANSPORT_TYPE_PROFILE),
                                          @(TRANSPORT_TYPE_CONFIGURATION),
                                          @(TRANSPORT_TYPE_NOTIFICATION),
                                          @(TRANSPORT_TYPE_EVENT)]];
    id<KaaDataChannel> channel1 = mockProtocol(@protocol(KaaDataChannel));
    [given([channel1 getSupportedTransportTypes]) willReturn:typesForChannel2];
    [given([channel1 getTransportProtocolId]) willReturn:[TransportProtocolIdHolder HTTPTransportID]];
    [given([channel1 getServerType]) willReturn:@(SERVER_OPERATIONS)];
    [given([channel1 getId]) willReturn:@"mock_channel1"];
    
    id<KaaDataChannel> channel2 = mockProtocol(@protocol(KaaDataChannel));
    [given([channel2 getSupportedTransportTypes]) willReturn:self.supportedTypes];
    [given([channel2 getTransportProtocolId]) willReturn:[TransportProtocolIdHolder HTTPTransportID]];
    [given([channel2 getServerType]) willReturn:@(SERVER_OPERATIONS)];
    [given([channel2 getId]) willReturn:@"mock_channel2"];
    
    id<KaaDataChannel> channel3 = mockProtocol(@protocol(KaaDataChannel));
    [given([channel3 getSupportedTransportTypes]) willReturn:typesForChannel2];
    [given([channel3 getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel3 getServerType]) willReturn:@(SERVER_OPERATIONS)];
    [given([channel3 getId]) willReturn:@"mock_channel3"];
    
    id<KaaInternalChannelManager> channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                          bootstrapServers:bootstrapServers
                                                                                                   context:nil
                                                                                           failureDelegate:nil];
    
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    [channelManager setFailoverManager:failoverManager];
    
    [channelManager addChannel:channel1];
    [channelManager addChannel:channel2];
    
    id<TransportConnectionInfo> opServer = [self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9999 publicKey:[KeyUtils getPublicKey]];
    
    [channelManager onTransportConnectionInfoUpdated:opServer];

    id<TransportConnectionInfo> opServer2 = [self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    
    [channelManager onTransportConnectionInfoUpdated:opServer2];
    
    [verifyCount(channel1, times(1)) setServer:opServer];
    [verifyCount(channel2, times(1)) setServer:opServer2];
    
    [channelManager removeChannel:channel2];
    
    id<TransportConnectionInfo> opServer3 = [self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:[TransportProtocolIdHolder TCPTransportID] host:@"localhost" port:9009 publicKey:[KeyUtils getPublicKey]];
    [channelManager addChannel:channel3];
    [channelManager onTransportConnectionInfoUpdated:opServer3];
    
    [verifyCount(channel3, times(1)) setServer:opServer3];
}

- (void)testConnectivityChecker {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    DefaultChannelManager *channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:nil
                                                                                    failureDelegate:nil];
    
    TransportProtocolId *type = [TransportProtocolIdHolder TCPTransportID];
    id<KaaDataChannel> channel1 = mockProtocol(@protocol(KaaDataChannel));
    [given([channel1 getTransportProtocolId]) willReturn:type];
    [given([channel1 getId]) willReturn:@"Channel1"];
    id<KaaDataChannel> channel2 = mockProtocol(@protocol(KaaDataChannel));
    [given([channel2 getTransportProtocolId]) willReturn:type];
    [given([channel2 getId]) willReturn:@"Channel2"];
    
    [channelManager addChannel:channel1];
    [channelManager addChannel:channel2];
    
    ConnectivityChecker *checker = mock([ConnectivityChecker class]);
    
    [channelManager setConnectivityChecker:checker];
    
    [verifyCount(channel1, times(1)) setConnectivityChecker:checker];
    [verifyCount(channel2, times(1)) setConnectivityChecker:checker];
    
    id<KaaDataChannel> channel3 = mockProtocol(@protocol(KaaDataChannel));
    [given([channel3 getTransportProtocolId]) willReturn:type];
    [given([channel3 getId]) willReturn:@"Channel3"];
    
    [channelManager addChannel:channel3];
    [verifyCount(channel3, times(1)) setConnectivityChecker:checker];
}

- (void)testUpdateForSpecifiedTransport {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    DefaultChannelManager *channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:nil
                                                                                    failureDelegate:nil];
    
    NSDictionary *types = [NSDictionary dictionaryWithObjects:@[@(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                                                @(CHANNEL_DIRECTION_UP)]
                                                      forKeys:@[@(TRANSPORT_TYPE_CONFIGURATION),
                                                                @(TRANSPORT_TYPE_LOGGING)]];
    
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel getSupportedTransportTypes]) willReturn:types];
    [given([channel getId]) willReturn:@"channel1"];
    
    id<KaaDataChannel> channel2 = mockProtocol(@protocol(KaaDataChannel));
    [given([channel2 getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel2 getSupportedTransportTypes]) willReturn:types];
    [given([channel2 getId]) willReturn:@"channel2"];
    
    [channelManager addChannel:channel2];
    [channelManager setChannel:channel withType:TRANSPORT_TYPE_LOGGING];
    [channelManager setChannel:nil withType:TRANSPORT_TYPE_LOGGING];
    [channelManager removeChannelById:[channel2 getId]];
}

- (void)testNegativeUpdateForSpecifiedTransport {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    DefaultChannelManager *channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:nil
                                                                                    failureDelegate:nil];
    
    NSDictionary *types =
    [NSDictionary dictionaryWithObjects:@[@(CHANNEL_DIRECTION_DOWN),
                                          @(CHANNEL_DIRECTION_UP)]
                                forKeys:@[@(TRANSPORT_TYPE_CONFIGURATION),
                                          @(TRANSPORT_TYPE_LOGGING)]];
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel getSupportedTransportTypes]) willReturn:types];
    @try {
        [channelManager setChannel:channel withType:TRANSPORT_TYPE_CONFIGURATION];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testNegativeUpdateForSpecifiedTransport succeed. Caught KaaInvalidChannelException");
    }
}

- (void)testShutdown {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    DefaultChannelManager *channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:nil
                                                                                    failureDelegate:nil];
    
    NSDictionary *types =
    [NSDictionary dictionaryWithObjects:@[@(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                          @(CHANNEL_DIRECTION_UP)]
                                forKeys:@[@(TRANSPORT_TYPE_CONFIGURATION),
                                          @(TRANSPORT_TYPE_LOGGING)]];
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel getSupportedTransportTypes]) willReturn:types];
    [given([channel getId]) willReturn:@"channel1"];
    
    [channelManager addChannel:channel];
    
    [channelManager shutdown];
    [channelManager onServerFailedWithConnectionInfo:nil failoverStatus:FailoverStatusBootstrapServersNotAvailable];
    [channelManager onTransportConnectionInfoUpdated:nil];
    [channelManager addChannel:nil];
    [channelManager setChannel:nil withType:0];
    [channelManager setConnectivityChecker:nil];
    [verifyCount(channel, times(1)) shutdown];
}

- (void)testPauseAfterAdd {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    DefaultChannelManager *channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:nil
                                                                                    failureDelegate:nil];
    
    NSDictionary *types =
    [NSDictionary dictionaryWithObject:@(CHANNEL_DIRECTION_BIDIRECTIONAL)
                                forKey:@(TRANSPORT_TYPE_CONFIGURATION)];
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel getSupportedTransportTypes]) willReturn:types];
    [given([channel getId]) willReturn:@"channel1"];

    [channelManager pause];
    [channelManager addChannel:channel];
    [verifyCount(channel, times(1)) pause];
}

- (void)testPauseAfterSet {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    DefaultChannelManager *channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:nil
                                                                                    failureDelegate:nil];
    
    NSDictionary *types =
    [NSDictionary dictionaryWithObject:@(CHANNEL_DIRECTION_BIDIRECTIONAL)
                                forKey:@(TRANSPORT_TYPE_CONFIGURATION)];
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel getSupportedTransportTypes]) willReturn:types];
    [given([channel getId]) willReturn:@"channel1"];
    
    [channelManager pause];
    [channelManager setChannel:channel withType:TRANSPORT_TYPE_CONFIGURATION];
    [verifyCount(channel, times(1)) pause];
}

- (void)testResume {
    NSDictionary *bootstrapServers = [self getDefaultBootstrapServers];
    
    id<BootstrapManager> bootstrapManager = mockProtocol(@protocol(BootstrapManager));
    DefaultChannelManager *channelManager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:nil
                                                                                    failureDelegate:nil];
    
    NSDictionary *types =
    [NSDictionary dictionaryWithObject:@(CHANNEL_DIRECTION_BIDIRECTIONAL)
                                forKey:@(TRANSPORT_TYPE_CONFIGURATION)];
    id<KaaDataChannel> channel = mockProtocol(@protocol(KaaDataChannel));
    [given([channel getTransportProtocolId]) willReturn:[TransportProtocolIdHolder TCPTransportID]];
    [given([channel getSupportedTransportTypes]) willReturn:types];
    [given([channel getId]) willReturn:@"channel1"];
    
    [channelManager pause];
    [channelManager addChannel:channel];
    [channelManager resume];
    
    [verifyCount(channel, times(1)) pause];
    [verifyCount(channel, times(1)) resume];
}

#pragma mark - Supporting methods

- (NSDictionary *)getDefaultBootstrapServers {
    [KeyUtils generateKeyPair];
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder HTTPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    NSDictionary *dictionary = [NSDictionary dictionaryWithObject:@[server] forKey:[TransportProtocolIdHolder HTTPTransportID]];
    return dictionary;
}

- (id<TransportConnectionInfo>)createTestServerInfoWithServerType:(ServerType)serverType transportProtocolId:(TransportProtocolId *)TPid host:(NSString *)host port:(int32_t)port publicKey:(NSData *)publicKey {
    ProtocolMetaData *md = [TestsHelper buildMetaDataWithTransportProtocolId:TPid host:host port:port publicKey:publicKey];
    return  [[GenericTransportInfo alloc] initWithServerType:serverType meta:md];
}

@end
