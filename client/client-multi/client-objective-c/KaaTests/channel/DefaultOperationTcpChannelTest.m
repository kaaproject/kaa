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
#import "DefaultOperationTcpChannel.h"
#import "KAASocket.h"
#import "KaaClientState.h"
#import "KeyUtils.h"
#import "AvroBytesConverter.h"
#import "GenericTransportInfo.h"
#import "TransportProtocolIdHolder.h"
#import "KAATcpSyncResponse.h"
#import "IPTransportInfo.h"
#import "KAATcpPingResponse.h"
#import "KAATcpDisconnect.h"
#import "TestsHelper.h"

#pragma mark - MockedOperationTcpChannel

@interface MockedOperationTcpChannel : DefaultOperationTcpChannel

@property (nonatomic, strong) KAASocket *socketMock;
@property (nonatomic, strong) NSInputStream *inputStream;
@property (nonatomic, strong) NSOutputStream *outputStream;

@end

@implementation MockedOperationTcpChannel

- (instancetype)initWithClientState:(id<KaaClientState>)state failoverManager:(id<FailoverManager>)failoverMgr {
    self = [super initWithClientState:state failoverManager:failoverMgr failureDelegate:nil];
    if (self) {
        CFReadStreamRef readStream = NULL;
        CFWriteStreamRef writeStream = NULL;
        CFStreamCreateBoundPair(NULL, &readStream, &writeStream, 4096);
        
        self.inputStream = (__bridge_transfer NSInputStream *)readStream;
        self.outputStream = (__bridge_transfer NSOutputStream *)writeStream;
        
        self.socketMock = mock([KAASocket class]);
        
        [given([self.socketMock input]) willReturn:self.inputStream];
        [given([self.socketMock output]) willReturn:self.outputStream];
        
        [self.inputStream open];
        [self.outputStream open];
    }
    return self;
}

- (KAASocket *)createSocket {
    return self.socketMock;
}

@end

#pragma mark - DefaultOperationTcpChannelTest

@interface DefaultOperationTcpChannelTest : XCTestCase

@end

@implementation DefaultOperationTcpChannelTest


- (void)testDefaultOperationTcpChannel {
    id<KaaClientState> state = mockProtocol(@protocol(KaaClientState));
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    id<KaaDataChannel> tcpchannel = [[DefaultOperationTcpChannel alloc] initWithClientState:state
                                                                            failoverManager:failoverManager
                                                                            failureDelegate:nil];
    XCTAssertNotNil([tcpchannel getId]);
    XCTAssertNotNil([tcpchannel getSupportedTransportTypes]);
    XCTAssertNotEqual(0, [[tcpchannel getSupportedTransportTypes] count]);
}

/**
 * The issue behind this test is that stream delegate methods don't get triggered after mocking KAASocket
 */
- (void)DISABLED_testSync {
    KeyPair *clientKeys = [KeyUtils generateKeyPair];
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    [given([clientState privateKey]) willReturnStruct:[clientKeys getPrivateKeyRef] objCType:@encode(SecKeyRef)];
    [given([clientState publicKey]) willReturnStruct:[clientKeys getPublicKeyRef] objCType:@encode(SecKeyRef)];
    
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    MockedOperationTcpChannel *tcpChannel = [[MockedOperationTcpChannel alloc] initWithClientState:clientState failoverManager:failoverManager];
    
    AvroBytesConverter *requestCreator = [[AvroBytesConverter alloc] init];
    id<KaaDataMultiplexer> multiplexer = mockProtocol(@protocol(KaaDataMultiplexer));
    [given([multiplexer compileRequestForTypes:anything()])willReturn:[requestCreator toBytes:[[SyncRequest alloc] init]]];
    id<KaaDataDemultiplexer> demultiplexer = mockProtocol(@protocol(KaaDataDemultiplexer));
    
    [tcpChannel setMultiplexer:multiplexer];
    [tcpChannel setDemultiplexer:demultiplexer];
    [tcpChannel syncForTransportType:TRANSPORT_TYPE_USER];    // will cause call to KaaDataMultiplexer.compileRequest(...) after "CONNECT" messsage
    [tcpChannel syncForTransportType:TRANSPORT_TYPE_PROFILE];
    
    [KeyUtils generateKeyPair];
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:[TransportProtocolIdHolder TCPTransportID] host:@"localhost" port:9009 publicKey:[KeyUtils getPublicKey]];
    
    [tcpChannel setServer:server withKeyPair:clientKeys];
    uint8_t rawConnackChar[] = {0x20, 0x02, 0x00, 0x01};
    [tcpChannel.outputStream write:rawConnackChar maxLength:sizeof(rawConnackChar)];
    
    SyncResponse *response = [[SyncResponse alloc] init];
    [response setStatus:SYNC_RESPONSE_RESULT_TYPE_SUCCESS];
    NSData *kaatcpsyncrespData = [self getNewKAATcpSyncResponseWithResponse:response];
    [tcpChannel.outputStream write:[kaatcpsyncrespData bytes] maxLength:[kaatcpsyncrespData length]];
    
    [NSThread sleepForTimeInterval:1]; // sleep a bit to let the message to be received
    [tcpChannel syncForTransportType:TRANSPORT_TYPE_USER]; // causes call to KaaDataMultiplexer.compileRequest(...) for "KAA_SYNC" messsage
    [verifyCount(multiplexer, times(2)) compileRequestForTypes:anything()];
    
    [tcpChannel syncForTransportType:TRANSPORT_TYPE_EVENT];
    [verifyCount(multiplexer, times(3)) compileRequestForTypes:anything()];
    [verifyCount(tcpChannel.socketMock, times(3)) output];
    
    [tcpChannel.outputStream write:[[[[KAATcpPingResponse alloc] init] getFrame] bytes] maxLength:[[[[KAATcpPingResponse alloc] init] getFrame] length]];
    
    [tcpChannel syncAll];
    [verifyCount(multiplexer, times(2)) compileRequestForTypes:[tcpChannel getSupportedTransportTypes]];
    
    KAATcpDisconnect *disconnect = [[KAATcpDisconnect alloc] initWithDisconnectReason:DisconnectReasonInternalError];
    [tcpChannel.outputStream write:[[disconnect getFrame] bytes] maxLength:[[disconnect getFrame] length]];
    
    [tcpChannel syncAll];
    [verifyCount(multiplexer, times(3)) compileRequestForTypes:[tcpChannel getSupportedTransportTypes]];
    [tcpChannel shutdown];
}

- (void)testConnectivity {
    KeyPair *clientKeys = [KeyUtils generateKeyPair];
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    [given([clientState privateKey]) willReturnStruct:[clientKeys getPrivateKeyRef] objCType:@encode(SecKeyRef)];
    [given([clientState publicKey]) willReturnStruct:[clientKeys getPublicKeyRef] objCType:@encode(SecKeyRef)];
    
    id<FailoverManager> failoverManager = mockProtocol(@protocol(FailoverManager));
    DefaultOperationTcpChannel *channel = [[DefaultOperationTcpChannel alloc] initWithClientState:clientState
                                                                                  failoverManager:failoverManager
                                                                                  failureDelegate:nil];
    
    id<TransportConnectionInfo> server = [self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:[TransportProtocolIdHolder TCPTransportID] host:@"www.test.fake" port:999 publicKey:[KeyUtils getPublicKey]];
    XCTAssertNotNil(server);
    
    ConnectivityChecker *checker = mock([ConnectivityChecker class]);
    [given([checker isConnected]) willReturnBool:NO];
    [channel setConnectivityChecker:checker];
}

#pragma mark - Supporting methods

- (id<TransportConnectionInfo>)createTestServerInfoWithServerType:(ServerType)serverType
                                              transportProtocolId:(TransportProtocolId *)TPid
                                                             host:(NSString *)host
                                                             port:(uint32_t)port
                                                        publicKey:(NSData *)publicKey {
    ProtocolMetaData *md = [TestsHelper buildMetaDataWithTransportProtocolId:TPid host:host port:port publicKey:publicKey];
    return  [[GenericTransportInfo alloc] initWithServerType:serverType meta:md];
}

- (NSData *)getNewKAATcpSyncResponseWithResponse:(SyncResponse *)syncResponse {
    AvroBytesConverter *responseCreator = [[AvroBytesConverter alloc] init];
    NSData *data = [responseCreator toBytes:syncResponse];
    KAATcpSyncResponse *response = [[KAATcpSyncResponse alloc] initWithAvro:data zipped:NO encypted:NO];
    return [response getFrame];
}

@end
