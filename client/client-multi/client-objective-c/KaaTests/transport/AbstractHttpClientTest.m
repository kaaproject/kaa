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
#import <Foundation/Foundation.h>
#import "TestsHelper.h"

@interface AbstractHttpClientTest : XCTestCase

@property (nonatomic, strong) KeyPair *clientPair;

@property (nonatomic, strong) NSData *serverPrivateTag;
@property (nonatomic, strong) NSData *serverPublicTag;
@property (nonatomic, strong) KeyPair *serverPair;

@end

@implementation AbstractHttpClientTest

- (void)setUp {
    [super setUp];
    self.clientPair = [KeyUtils generateKeyPair];
    
    self.serverPrivateTag = [self generateTag];
    self.serverPublicTag = [self generateTag];
    self.serverPair = [KeyUtils generateKeyPairWithPrivateTag:self.serverPrivateTag publicTag:self.serverPublicTag];
}

- (void)tearDown {
    [super tearDown];
    [KeyUtils deleteExistingKeyPair];
    [KeyUtils removeKeyByTag:self.serverPrivateTag];
    [KeyUtils removeKeyByTag:self.serverPublicTag];
}

- (void)testDisableVerification {
    HttpClientMock *client = [[HttpClientMock alloc] initWithURLString:@"test_url" privateKeyRef:nil publicKeyRef:nil remoteKeyRef:nil];
    [client disableVerification];
    int a = 1; int b = 2; int c = 3;
    NSMutableData *body = [NSMutableData data];
    [body appendBytes:&a length:sizeof(a)];
    [body appendBytes:&b length:sizeof(b)];
    [body appendBytes:&c length:sizeof(c)];
    
    NSData *signature = [NSData dataWithData:body];
    XCTAssertEqualObjects(body, [client verifyResponse:body signature:signature]);
}

- (void)testSignature {
    HttpClientMock *client = [[HttpClientMock alloc] initWithURLString:@"test_url" privateKeyRef:[self.clientPair getPrivateKeyRef] publicKeyRef:[self.clientPair getPublicKeyRef] remoteKeyRef:[self.serverPair getPublicKeyRef]];
    
    MessageEncoderDecoder *serverEncoder = [[MessageEncoderDecoder alloc] initWithKeyPair:self.serverPair remotePublicKeyRef:[self.clientPair getPublicKeyRef]];
    
    int a = 1; int b = 2; int c = 3;
    NSMutableData *message = [NSMutableData data];
    [message appendBytes:&a length:sizeof(a)];
    [message appendBytes:&b length:sizeof(b)];
    [message appendBytes:&c length:sizeof(c)];
    
    NSData *signature = [serverEncoder signatureForMessage:message];
    XCTAssertEqualObjects(message, [client verifyResponse:message signature:signature]);
}

- (void)testVerifyResponseFailure {
    
    @try {
        HttpClientMock *client = [[HttpClientMock alloc] initWithURLString:@"test_url" privateKeyRef:[self.clientPair getPrivateKeyRef] publicKeyRef:[self.clientPair getPublicKeyRef] remoteKeyRef:[self.serverPair getPublicKeyRef]];
        
        int a = 1; int b = 2; int c = 3;
        NSMutableData *body = [NSMutableData data];
        [body appendBytes:&a length:sizeof(a)];
        [body appendBytes:&b length:sizeof(b)];
        [body appendBytes:&c length:sizeof(c)];
        
        NSData *signature = [NSData dataWithData:body];
        [client verifyResponse:body signature:signature];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testVerifyResponseFailure passed!");
    }
}

- (NSData *)generateTag {
    int randomInt = arc4random();
    return [NSData dataWithBytes:&randomInt length:sizeof(randomInt)];
}
@end
