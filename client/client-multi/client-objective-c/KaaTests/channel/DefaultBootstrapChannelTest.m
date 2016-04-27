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
#import <Foundation/Foundation.h>
#import "DefaultBootstrapChannel.h"
#import "AbstractHttpChannel.h"
#import "KaaClientState.h"
#import "GenericTransportInfo.h"
#import "TransportProtocolIdHolder.h"
#import "KeyUtils.h"
#import "TestsHelper.h"
#import "TransportProtocolIdHolder.h"

@interface HttpClientMockWithEncoder : HttpClientMock

@end

@implementation HttpClientMockWithEncoder

- (MessageEncoderDecoder *)getEncoderDecoder {
    MessageEncoderDecoder *crypt = mock([MessageEncoderDecoder class]);
    @try {
        [given([crypt decodeData:anything()]) willReturn:[TestsHelper getData]];
    }
    @catch (NSException *exception) {
        NSLog(@"Error decoding data: %@, reason: %@", exception.name, exception.reason);
    }
    return crypt;
}

@end


@interface DefaultBootStrapChannelMock : DefaultBootstrapChannel

@property (nonatomic) NSInteger wantedNumberOfInvocations;

- (instancetype)initWithClient:(AbstractKaaClient *)client
                         state:(id<KaaClientState>)state
               failoverManager:(id<FailoverManager>)manager
     wantedNumberOfInvocations:(NSInteger)wantedNumberOfInvocations;

@end

@implementation DefaultBootStrapChannelMock

- (instancetype)initWithClient:(AbstractKaaClient *)client
                         state:(id<KaaClientState>)state
               failoverManager:(id<FailoverManager>)manager
     wantedNumberOfInvocations:(NSInteger)wantedNumberOfInvocations {
    self = [super initWithClient:client state:state failoverManager:manager];
    self.wantedNumberOfInvocations = wantedNumberOfInvocations;
    return self;
}

- (NSOperationQueue *)createExecutor {
    return [super createExecutor];
}

@end


@interface DefaultBootstrapChannelTest : XCTestCase

@end

@implementation DefaultBootstrapChannelTest

- (void)testChannelGetters {
    AbstractKaaClient *client = mock([AbstractKaaClient class]);
    id<KaaClientState> state = mockProtocol(@protocol(KaaClientState));
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    id<KaaDataChannel> channel = [[DefaultBootstrapChannel alloc] initWithClient:client state:state failoverManager:failoverManager];
    
    NSDictionary *transportTypes = [channel getSupportedTransportTypes];
    
    XCTAssertEqualObjects(transportTypes[@(TRANSPORT_TYPE_BOOTSTRAP)], @(CHANNEL_DIRECTION_BIDIRECTIONAL));
    XCTAssertTrue([[TransportProtocolIdHolder HTTPTransportID] isEqual:[channel getTransportProtocolId]]);
    XCTAssertTrue([@"default_bootstrap_channel" isEqualToString:[channel getId]]);
}

- (void)testChannelSync {
    id<KaaChannelManager> manager = mockProtocol(@protocol(KaaChannelManager));
    AbstractHttpClient *httpClient = [[HttpClientMockWithEncoder alloc] init];
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    
    [KeyUtils generateKeyPair];
    AbstractKaaClient *client = mock([AbstractKaaClient class]);
    [given([client createHttpClientWithURLString:anything() privateKeyRef:nil publicKeyRef:nil remoteKey:anything()]) willReturn:httpClient];
    [given([client getChannelManager]) willReturn:manager];
    
    id<KaaClientState> state = mockProtocol(@protocol(KaaClientState));
    id<KaaDataMultiplexer> multiplexer = mockProtocol(@protocol(KaaDataMultiplexer));
    id<KaaDataDemultiplexer> demultiplexer = mockProtocol(@protocol(KaaDataDemultiplexer));
    DefaultBootStrapChannelMock *channel = [[DefaultBootStrapChannelMock alloc] initWithClient:client state:state failoverManager:failoverManager wantedNumberOfInvocations:2];
    
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder TCPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    [channel setServer:server];

    [channel syncForTransportType:TRANSPORT_TYPE_BOOTSTRAP];
    [channel setDemultiplexer:demultiplexer];
    [channel setDemultiplexer:nil];
    [channel syncForTransportType:TRANSPORT_TYPE_BOOTSTRAP];
    [channel setMultiplexer:multiplexer];
    [channel setMultiplexer:nil];
    [channel syncForTransportType:TRANSPORT_TYPE_CONFIGURATION];
    [channel syncForTransportType:TRANSPORT_TYPE_BOOTSTRAP];

    [NSThread sleepForTimeInterval:1];
    [verifyCount([channel getDemultiplexer], times(channel.wantedNumberOfInvocations)) processResponse:[TestsHelper getData]];
    [verifyCount([channel getMultiplexer], times(channel.wantedNumberOfInvocations)) compileRequestForTypes:anything()];
}

- (void)testShutDown {
    id<KaaChannelManager> manager = mockProtocol(@protocol(KaaChannelManager));
    AbstractHttpClient *httpClient = mock([AbstractHttpClient class]);
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    
    AbstractKaaClient *client = mock([AbstractKaaClient class]);
    [given([client createHttpClientWithURLString:anything() privateKeyRef:[KeyUtils getPrivateKeyRef] publicKeyRef:[KeyUtils getPublicKeyRef] remoteKey:anything()]) willReturn:httpClient];
    [given([client getChannelManager]) willReturn:manager];
    
    id<KaaClientState> state = mockProtocol(@protocol(KaaClientState));
    id<KaaDataMultiplexer> multiplexer = mockProtocol(@protocol(KaaDataMultiplexer));
    id<KaaDataDemultiplexer> demultiplexer = mockProtocol(@protocol(KaaDataDemultiplexer));
    DefaultBootStrapChannelMock *channel = [[DefaultBootStrapChannelMock alloc] initWithClient:client state:state failoverManager:failoverManager wantedNumberOfInvocations:0];
    [channel setMultiplexer:multiplexer];
    [channel setDemultiplexer:demultiplexer];
    [channel shutdown];
    
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_BOOTSTRAP transportProtocolId:[TransportProtocolIdHolder TCPTransportID] host:@"localhost" port:9889 publicKey:[KeyUtils getPublicKey]];
    [channel setServer:server];
    
    [channel syncForTransportType:TRANSPORT_TYPE_BOOTSTRAP];
    [channel syncAll];
    
    [NSThread sleepForTimeInterval:1];
    [verifyCount([channel getDemultiplexer], times(channel.wantedNumberOfInvocations)) processResponse:[TestsHelper getData]];
    [verifyCount([channel getMultiplexer], times(channel.wantedNumberOfInvocations)) compileRequestForTypes:anything()];
}

#pragma mark - Supporting methods

- (id<TransportConnectionInfo>) createTestServerInfoWithServerType:(ServerType)serverType transportProtocolId:(TransportProtocolId *)TPid host:(NSString *)host port:(uint32_t)port publicKey:(NSData *)publicKey {
    ProtocolMetaData *md = [TestsHelper buildMetaDataWithTransportProtocolId:TPid host:host port:port publicKey:publicKey];
    return  [[GenericTransportInfo alloc] initWithServerType:serverType meta:md];
}

@end
