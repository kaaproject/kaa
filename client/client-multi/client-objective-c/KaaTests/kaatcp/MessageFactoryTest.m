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
#import "KAAMessageFactory.h"
#import "KAATcpDelegates.h"
#import "KeyUtils.h"
#import "KeyPair.h"
#import "AvroBytesConverter.h"
#import "EndpointGen.h"
#import "EndpointObjectHash.h"
#import "MessageEncoderDecoder.h"
#import "TestsHelper.h"

@interface MessageFactoryTest : XCTestCase <ConnAckDelegate,ConnectDelegate,SyncResponseDelegate,SyncRequestDelegate,DisconnectDelegate>

@property (nonatomic, strong) NSData *signature;
@property (nonatomic, strong) NSData *sessionKey;
@property (nonatomic, strong) NSData *payload;

@property (nonatomic, strong) KeyPair *clientPair;
@property (nonatomic, strong) NSData *clientPrivateTag;
@property (nonatomic, strong) NSData *clientPublicTag;

@property (nonatomic, strong) KeyPair *serverPair;
@property (nonatomic, strong) NSData *serverPrivateTag;
@property (nonatomic, strong) NSData *serverPublicTag;

/**
 * Since there is only one delegate who several tests flag below is used to determine whether data is encypted or not.
 */
@property (nonatomic) BOOL testWithKey;


- (NSData *)generateTag;

@end

@implementation MessageFactoryTest

- (void)setUp {
    [super setUp];
    self.testWithKey = NO;
    
    self.serverPrivateTag = [self generateTag];
    self.serverPublicTag = [self generateTag];
    self.serverPair = [KeyUtils generateKeyPairWithPrivateTag:self.serverPrivateTag publicTag:self.serverPublicTag];
    
    self.clientPrivateTag = [self generateTag];
    self.clientPublicTag = [self generateTag];
    self.clientPair = [KeyUtils generateKeyPairWithPrivateTag:self.clientPrivateTag publicTag:self.clientPublicTag];
    
    self.signature = nil;
    self.sessionKey = nil;
    self.payload = nil;
}

- (void)tearDown {
    [super tearDown];
    [KeyUtils removeKeyByTag:self.serverPrivateTag];
    [KeyUtils removeKeyByTag:self.serverPublicTag];
    [KeyUtils removeKeyByTag:self.clientPrivateTag];
    [KeyUtils removeKeyByTag:self.clientPublicTag];
}

- (void)testConnackMessageDelegateMethods {
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] initWithFramer:[[KAAFramer alloc] init]];
    char reject[] = {0x20, 0x02, 0x00, 0x03};
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&reject length:sizeof(reject)]];
    id<ConnAckDelegate> idRejectDelegate = mockProtocol(@protocol(ConnAckDelegate));
    [factory registerConnAckDelegate:idRejectDelegate];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&reject length:sizeof(reject)]];
    [verifyCount(idRejectDelegate, times(1)) onConnAckMessage:anything()];
    
    char accept[] = {0x20, 0x02, 0x00, 0x01};
    id<ConnAckDelegate> acceptDelegate = mockProtocol(@protocol(ConnAckDelegate));
    [factory registerConnAckDelegate:acceptDelegate];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&accept length:sizeof(accept)]];
    
    char badprotocol[] = {0x20, 0x02, 0x00, 0x02};
    id<ConnAckDelegate> badprotocolDelegate = mockProtocol(@protocol(ConnAckDelegate));
    [factory registerConnAckDelegate:badprotocolDelegate];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&badprotocol length:sizeof(badprotocol)]];
    
    char serverunavaliable[] = {0x20, 0x02, 0x00, 0x04};
    id<ConnAckDelegate> servUnavaliableDelegate = mockProtocol(@protocol(ConnAckDelegate));
    [factory registerConnAckDelegate:servUnavaliableDelegate];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&serverunavaliable length:sizeof(serverunavaliable)]];
    
    char rawConnackBadCredentials[] = {0x20, 0x02, 0x00, 0x05};
    id<ConnAckDelegate> badCredentialsDelegate = mockProtocol(@protocol(ConnAckDelegate));
    [factory registerConnAckDelegate:badCredentialsDelegate];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&rawConnackBadCredentials length:sizeof(rawConnackBadCredentials)]];
    
    char rawConnackNoAuth[] = {0x20, 0x02, 0x00, 0x06};
    id<ConnAckDelegate> noAuthDelegate = mockProtocol(@protocol(ConnAckDelegate));
    [factory registerConnAckDelegate:noAuthDelegate];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&rawConnackNoAuth length:sizeof(rawConnackNoAuth)]];
    
    char rawConnackUndefined[] = {0x20, 0x02, 0x00, 0x07};
    id<ConnAckDelegate> undefinedDelegate = mockProtocol(@protocol(ConnAckDelegate));
    [factory registerConnAckDelegate:undefinedDelegate];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&rawConnackUndefined length:sizeof(rawConnackUndefined)]];
}

- (void)testConnackMessageReturnTypes {
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] initWithFramer:[[KAAFramer alloc] init]];
    char reject[] = {0x20, 0x02, 0x00, 0x03};
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&reject length:sizeof(reject)]];
    [factory registerConnAckDelegate:self];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&reject length:sizeof(reject)]];
    
    char accept[] = {0x20, 0x02, 0x00, 0x01};
    [factory registerConnAckDelegate:self];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&accept length:sizeof(accept)]];
    
    char badprotocol[] = {0x20, 0x02, 0x00, 0x02};
    [factory registerConnAckDelegate:self];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&badprotocol length:sizeof(badprotocol)]];
    
    char serverunavaliable[] = {0x20, 0x02, 0x00, 0x04};
    [factory registerConnAckDelegate: self];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&serverunavaliable length:sizeof(serverunavaliable)]];
    
    char rawConnackBadCredentials[] = {0x20, 0x02, 0x00, 0x05};
    [factory registerConnAckDelegate:self];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&rawConnackBadCredentials length:sizeof(rawConnackBadCredentials)]];
    
    char rawConnackNoAuth[] = {0x20, 0x02, 0x00, 0x06};
    [factory registerConnAckDelegate:self];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&rawConnackNoAuth length:sizeof(rawConnackNoAuth)]];
    
    char rawConnackUndefined[] = {0x20, 0x02, 0x00, 0x07};
    [factory registerConnAckDelegate:self];
    [factory.framer pushBytes:[NSMutableData dataWithBytes:&rawConnackUndefined length:sizeof(rawConnackUndefined)]];
}

- (void)testConnectMessage {
    
    self.testWithKey = YES;
    
    MessageEncoderDecoder *crypt = [[MessageEncoderDecoder alloc] initWithKeyPair:self.clientPair
                                                            remotePublicKeyRef:[self.serverPair getPublicKeyRef]];
    self.payload = [crypt encodeData:[self getRawData]];
    self.sessionKey = [crypt getEncodedSessionKey];
    self.signature = [crypt signatureForMessage:self.sessionKey];
    
    char connectHeader[] = {0x10, 0xC2, 0x04, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0xf2, 0x91,  0xf2, 0xd4, 0x11, 0x01, 0x00, 0xC8};
    
    NSMutableData *connectBuffer = [NSMutableData dataWithBytes:&connectHeader length:sizeof(connectHeader)];
    [connectBuffer appendData:self.sessionKey];
    [connectBuffer appendData:self.signature];
    [connectBuffer appendData:self.payload];
    
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    [factory.framer pushBytes:connectBuffer];
    
    [factory registerConnectDelegate:self];
    [factory.framer pushBytes:connectBuffer];
    
    id<ConnectDelegate> mockDelegate = mockProtocol(@protocol(ConnectDelegate));
    [factory registerConnectDelegate:mockDelegate];
    [factory.framer pushBytes:connectBuffer];
    [verifyCount(mockDelegate, times(1)) onConnectMessage:anything()];
}

- (void)testConnectMessageWithoutKey {
    
    self.testWithKey = NO;
    
    NSData *rawData = [self getRawData];
    //we assume that size of rawdata is less then 128 here
    
    char charConnectHeaderPart[] = {0x10, [rawData length] + 18, 0x00, 0x06, 'K','a','a','t','c','p', 0x01, 0x02, 0xf2, 0x91, 0xf2, 0xd4, 0x00, 0x00, 0x00, 0xC8};
    NSMutableData *connectBuffer = [NSMutableData dataWithCapacity:rawData.length + 20];
    [connectBuffer appendBytes:&charConnectHeaderPart length:sizeof(charConnectHeaderPart)];
    [connectBuffer appendData:rawData];
    
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    [factory.framer pushBytes:connectBuffer];
    
    [factory registerConnectDelegate:self];
    [factory.framer pushBytes:connectBuffer];
    
    id<ConnectDelegate> mockDelegate = mockProtocol(@protocol(ConnectDelegate));
    [factory registerConnectDelegate:mockDelegate];
    [factory.framer pushBytes:connectBuffer];
    [verifyCount(mockDelegate, times(1)) onConnectMessage:anything()];
}

- (void)testSyncResponse {
    char charConnectHeaderPart2[] = {0xF0, 0x0D, 0x00, 0x06, 'K','a','a','t','c','p', 0x01, 0x00, 0x05, 0x14, 0xFF};
    NSMutableData *syncRequest = [NSMutableData dataWithBytes:charConnectHeaderPart2 length:sizeof(charConnectHeaderPart2)];
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    
    [factory registerSyncResponseDelegate:self];
    [factory.framer pushBytes:syncRequest];
    
    id<SyncResponseDelegate> syncRespDelegate = mockProtocol(@protocol(SyncResponseDelegate));
    [factory registerSyncResponseDelegate:syncRespDelegate];
    [factory.framer pushBytes:syncRequest];
    [verifyCount(syncRespDelegate, times(1)) onSyncResponseMessage:anything()];
}

- (void)testSyncRequest {
    char charConnectHeaderPart2[] = {0xF0, 0x0D, 0x00, 0x06, 'K','a','a','t','c','p', 0x01, 0x00, 0x05, 0x15, 0xFF};
    NSMutableData *syncRequest = [NSMutableData dataWithBytes:charConnectHeaderPart2 length:sizeof(charConnectHeaderPart2)];
    
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    [factory registerSyncRequestDelegate:self];
    [factory.framer pushBytes:syncRequest];
    
    id<SyncRequestDelegate> syncReqDelegate = mockProtocol(@protocol(SyncRequestDelegate));
    [factory registerSyncRequestDelegate:syncReqDelegate];
    [factory.framer pushBytes:syncRequest];
    [verifyCount(syncReqDelegate, times(1)) onSyncRequestMessage:anything()];
}

- (void)testPingRequest {
    char pingRequestChar[] = {0xC0, 0x00};
    NSMutableData *pingRequest = [NSMutableData dataWithBytes:&pingRequestChar length:sizeof(pingRequestChar)];
    
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    [factory.framer pushBytes:pingRequest];
    id<PingRequestDelegate> delegate = mockProtocol(@protocol(PingRequestDelegate));
    [factory registerPingRequestDelegate:delegate];
    [factory.framer pushBytes:pingRequest];
    [verifyCount(delegate, times(1)) onPingRequestMessage:anything()];
}

- (void)testPingResponse {
    char pingResponseChar[] = {0xD0, 0x00};
    NSMutableData *pingResponse = [NSMutableData dataWithBytes:&pingResponseChar length:sizeof(pingResponseChar)];
    
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    [factory.framer pushBytes:pingResponse];
    id<PingResponseDelegate> delegate = mockProtocol(@protocol(PingResponseDelegate));
    [factory registerPingResponseDelegate:delegate];
    [factory.framer pushBytes:pingResponse];
    [verifyCount(delegate, times(1)) onPingResponseMessage:anything()];
}

- (void)testDisconnect {
    char disconnectChar[] = {0xE0, 0x02, 0x00, 0x02};
    NSMutableData *disconnect = [NSMutableData dataWithBytes:&disconnectChar length:sizeof(disconnectChar)];
    
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    [factory.framer pushBytes:disconnect];
    
    [factory registerDisconnectDelegate:self];
    [factory.framer pushBytes:disconnect];
    
    id<DisconnectDelegate> mockDisconnect = mockProtocol(@protocol(DisconnectDelegate));
    [factory registerDisconnectDelegate:mockDisconnect];
    [factory.framer pushBytes:disconnect];
    [verifyCount(mockDisconnect, times(1)) onDisconnectMessage:anything()];
}

- (void)testBytesPartialPush {
    char syncRequestChar[] = {0xF0, 0x0D, 0x00, 0x06, 'K','a','a','t','c','p', 0x01, 0x00, 0x05, 0x15, 0xFF};
    
    NSMutableData *syncRequest1 = [NSMutableData dataWithBytes:&syncRequestChar length:sizeof(syncRequestChar)];
    NSMutableData *syncRequest2 = [NSMutableData dataWithBytes:&syncRequestChar length:sizeof(syncRequestChar)];
    NSMutableData *syncRequest3 = [NSMutableData dataWithBytes:&syncRequestChar length:sizeof(syncRequestChar)];
    NSInteger totalLength = syncRequest1.length + syncRequest2.length + syncRequest3.length;
    
    NSMutableData *totalBuffer = [NSMutableData dataWithCapacity:totalLength];
    [totalBuffer appendData:syncRequest1];
    [totalBuffer appendData:syncRequest2];
    [totalBuffer appendData:syncRequest3];
    
    uint8_t firstBuffer[syncRequest1.length - 2];
    uint8_t secondBuffer[syncRequest2.length + 4];
    uint8_t thirdBuffer[syncRequest3.length - 2];
    
    NSInputStream *stream = [NSInputStream inputStreamWithData:totalBuffer];
    [stream open];
    [stream read:firstBuffer maxLength:(syncRequest1.length - 2)];
    [stream read:secondBuffer maxLength:syncRequest2.length + 4];
    [stream read:thirdBuffer maxLength:(syncRequest3.length - 2)];
    [stream close];
    
    KAAMessageFactory *factory = [[KAAMessageFactory alloc] init];
    
    [factory registerSyncRequestDelegate:self];
    
    NSMutableData *firstBufferData = [NSMutableData dataWithBytes:&firstBuffer length:sizeof(firstBuffer)];
    NSMutableData *secondBufferData = [NSMutableData dataWithBytes:&secondBuffer length:sizeof(secondBuffer)];
    NSMutableData *thirdBufferData = [NSMutableData dataWithBytes:&thirdBuffer length:sizeof(thirdBuffer)];

    int i = [factory.framer pushBytes:firstBufferData];
    KAATestEqual(firstBufferData.length, i);
    i = [factory.framer pushBytes:secondBufferData];
    KAATestEqual(secondBufferData.length, i);
    i = [factory.framer pushBytes:thirdBufferData];
    KAATestEqual(thirdBufferData.length, i);
    
    id<SyncRequestDelegate> syncRequestDelegate = mockProtocol(@protocol(SyncRequestDelegate));
    [factory registerSyncRequestDelegate:syncRequestDelegate];
    
    i = [factory.framer pushBytes:firstBufferData];
    KAATestEqual(firstBufferData.length, i);
    i = [factory.framer pushBytes:secondBufferData];
    KAATestEqual(secondBufferData.length, i);
    i = [factory.framer pushBytes:thirdBufferData];
    KAATestEqual(thirdBufferData.length, i);

    [verifyCount(syncRequestDelegate, times(3)) onSyncRequestMessage:anything()];

}

#pragma mark - Supporting methods

- (void)onConnAckMessage:(KAATcpConnAck *)message {
    switch (message.returnCode) {
        case ReturnCodeAccepted:
            KAATestEqual(ReturnCodeAccepted, message.returnCode);
            break;
            
        case ReturnCodeRefuseBadCredentials:
            KAATestEqual(ReturnCodeRefuseBadCredentials, message.returnCode);
            break;
            
        case ReturnCodeRefuseIdReject:
            KAATestEqual(ReturnCodeRefuseIdReject, message.returnCode);
            break;
            
        case ReturnCodeRefuseBadProtocol:
            KAATestEqual(ReturnCodeRefuseBadProtocol, message.returnCode);
            break;
            
        case ReturnCodeRefuseNoAuth:
            KAATestEqual(ReturnCodeRefuseNoAuth, message.returnCode);
            break;
            
        case ReturnCodeRefuseServerUnavailable:
            KAATestEqual(ReturnCodeRefuseServerUnavailable, message.returnCode);
            break;
            
        case ReturnCodeUndefined:
            KAATestEqual(ReturnCodeUndefined, message.returnCode);
            break;
            
        default:
            break;
    }
}

- (void)onConnectMessage:(KAATcpConnect *)message {
    uint16_t keepAlive = 200;
    uint32_t nextProtocolId = 0xf291f2d4;
    KAATestEqual(keepAlive, message.keepAlive);
    KAATestEqual(nextProtocolId, message.nextProtocolId);
    if (message.signature) {
        KAATestObjectsEqual(self.signature, message.signature);
    }
    if (message.aesSessionKey) {
        KAATestObjectsEqual(self.sessionKey, message.aesSessionKey);
    }
    if (self.testWithKey) {
        KAATestObjectsEqual(self.payload, message.syncRequest);
    } else {
         KAATestObjectsEqual([self getRawData], message.syncRequest);
    }
}

- (NSData *)getRawData {
    AvroBytesConverter *requestConverter = [[AvroBytesConverter alloc] init];
    SyncRequest *request = [[SyncRequest alloc] init];
    
    EndpointObjectHash *publicKeyHash = [EndpointObjectHash hashWithSHA1:[KeyUtils getPublicKeyByTag:self.clientPublicTag]];
    
    request.requestId = 42;
    SyncRequestMetaData *md = [[SyncRequestMetaData alloc] init];
    md.sdkToken = @"sdkToken";
    md.endpointPublicKeyHash =
    [KAAUnion unionWithBranch:KAA_UNION_BYTES_OR_NULL_BRANCH_0
                      data:publicKeyHash.data];
    request.syncRequestMetaData =
    [KAAUnion unionWithBranch:KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0 data:md];
    
    return [requestConverter toBytes:request];
}

- (void)onSyncResponseMessage:(KAATcpSyncResponse *)message {
    KAATestEqual(1, message.avroObject.length);
    KAATestEqual(5, message.messageId);
    KAATestEqual(NO, message.zipped);
    KAATestEqual(YES, message.encrypted);
    KAATestEqual(NO, message.request);
}

- (void)onSyncRequestMessage:(KAATcpSyncRequest *)message {
    KAATestEqual(1, message.avroObject.length);
    KAATestEqual(5, message.messageId);
    KAATestEqual(NO, message.zipped);
    KAATestEqual(YES, message.encrypted);
    KAATestEqual(YES, message.request);
}

- (void)onDisconnectMessage:(KAATcpDisconnect *)message {
    KAATestEqual(DisconnectReasonInternalError, message.reason);
}

- (NSData *)generateTag {
    int randomInt = arc4random();
    return [NSData dataWithBytes:&randomInt length:sizeof(randomInt)];
}

@end
