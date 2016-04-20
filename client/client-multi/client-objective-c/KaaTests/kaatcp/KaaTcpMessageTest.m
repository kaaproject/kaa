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

#import "KAATcpConnAck.h"
#import "KAATcpConnect.h"
#import "KAATcpDisconnect.h"
#import "KAATcpPingRequest.h"
#import "KAATcpPingResponse.h"
#import "KAATcpSyncResponse.h"
#import "KAATcpSyncRequest.h"
#import <XCTest/XCTest.h>

@interface KaaTcpMessageTest : XCTestCase

@end

@implementation KaaTcpMessageTest

- (void)testSyncResponseMessage {
    unsigned char bytes = {0xFF};
    char kaatcp[] = {0xF0, 0x0D, 0x00,0x06,'K','a','a','t','c','p', 0x01, 0x00, 0x05, 0x14, 0xFF};
    NSData *kaaSync = [NSData dataWithBytes:&kaatcp length:sizeof(kaatcp)];
    KAATcpSyncResponse *syncResponse = [[KAATcpSyncResponse alloc] init];
    XCTAssertNotNil(syncResponse);
    
    KAATcpSyncResponse *message = [[KAATcpSyncResponse alloc] initWithAvro:[NSData dataWithBytes:&bytes length:1] zipped:NO encypted:YES];
    [message setMessageId:5];
    NSData *actual = [message getFrame];
    XCTAssertEqualObjects(kaaSync, actual);
}

- (void)testSyncRequestMessage {
    char kaatcp[] = {0xF0, 0x0D, 0x00, 0x06,'K','a','a','t','c','p', 0x01, 0x00, 0x05, 0x15, 0xFF};
    NSData *kaaSync = [NSData dataWithBytes:&kaatcp length:sizeof(kaatcp)];
    
    KAATcpSyncRequest *message = [[KAATcpSyncRequest alloc] initWithAvro:[NSData dataWithBytes:(char[]){0xFF} length:1] zipped:NO encypted:YES];
    [message setMessageId:5];
    NSData *actual = [message getFrame];
    XCTAssertEqualObjects(kaaSync, actual);
}

- (void)testConnectMessage {
    char charpayload[] = {0xFF, 0x01, 0x02, 0x03};
    NSData *payload = [NSData dataWithBytes:&charpayload length:sizeof(charpayload)];
    char charConnectHeader[20] = {0x10, 0x16, 0x00, 0x06, 'K','a','a','t','c','p', 0x01, 0x02, 0xf2, 0x91, 0xf2, 0xd4, 0x00, 0x00, 0x00, 0xC8};
    NSData *connectedHeader = [NSData dataWithBytes:&charConnectHeader length:sizeof(charConnectHeader)];
    KAATcpConnect *message = [[KAATcpConnect alloc] initWithAlivePeriod:200 nextProtocolId:0xf291f2d4 aesSessionKey:nil syncRequest:payload signature:nil];
    NSData *frame = [message getFrame];
    uint8_t headerCheck[20];
    uint8_t payloadCheck[4];
    NSInputStream *stream = [NSInputStream inputStreamWithData:frame];
    [stream open];
    [stream read:headerCheck maxLength:sizeof(charConnectHeader)];
    [stream read:payloadCheck maxLength:sizeof(charpayload)];
    [stream close];
    NSData *headerCheckData = [NSData dataWithBytes:&headerCheck length:sizeof(headerCheck)];
    NSData *payloadCheckData = [NSData dataWithBytes:&payloadCheck length:sizeof(payloadCheck)];
    XCTAssertEqualObjects(headerCheckData, connectedHeader);
    XCTAssertEqualObjects(payload, payloadCheckData);
}

- (void)testDisconnect {
    KAATcpDisconnect *message = [[KAATcpDisconnect alloc] initWithDisconnectReason:DisconnectReasonInternalError];
    NSData *actual = [message getFrame];
    char disconnect[] = {0xE0, 0x02, 0x00, 0x02};
    XCTAssertEqualObjects(actual, [NSData dataWithBytes:&disconnect length:sizeof(disconnect)]);
}

- (void)testConnack {
    KAATcpConnAck *message = [[KAATcpConnAck alloc] initWithReturnCode:ReturnCodeRefuseIdReject];
    NSData *actual = [message getFrame];
    char reject[] = {0x20, 0x02, 0x00, 0x03};
    XCTAssertEqualObjects(actual, [NSData dataWithBytes:&reject length:sizeof(reject)]);
}

- (void)testPingRequest {
    KAATcpPingRequest *message = [[KAATcpPingRequest alloc] init];
    NSData *actual = [message getFrame];
    char request[] = {0xC0, 0x00};
    XCTAssertEqualObjects(actual, [NSData dataWithBytes:&request length:sizeof(request)]);
}

- (void)testPingResponse {
    KAATcpPingResponse *message = [[KAATcpPingResponse alloc] init];
    NSData *actual = [message getFrame];
    char response[] = {0xD0, 0x00};
    XCTAssertEqualObjects(actual, [NSData dataWithBytes:&response length:sizeof(response)]);
}

@end
