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
#import "MessageEncoderDecoder.h"
#import "KeyUtils.h"

@interface MessageEncoderDecoderTests : XCTestCase

@property (nonatomic, strong) KeyPair *clientPair;

@property (nonatomic, strong) NSData *serverPrivateTag;
@property (nonatomic, strong) NSData *serverPublicTag;
@property (nonatomic, strong) KeyPair *serverPair;

@property (nonatomic, strong) NSData *thiefPrivateTag;
@property (nonatomic, strong) NSData *thiefPublicTag;
@property (nonatomic, strong) KeyPair *thiefPair;

@end

@implementation MessageEncoderDecoderTests

- (void)setUp {
    [super setUp];
    self.clientPair = [KeyUtils generateKeyPair];
    
    self.serverPrivateTag = [self generateTag];
    self.serverPublicTag = [self generateTag];
    self.serverPair = [KeyUtils generateKeyPairWithPrivateTag:self.serverPrivateTag publicTag:self.serverPublicTag];
    
    self.thiefPrivateTag = [self generateTag];
    self.thiefPublicTag = [self generateTag];
    self.thiefPair = [KeyUtils generateKeyPairWithPrivateTag:self.thiefPrivateTag publicTag:self.thiefPublicTag];
}

- (void)tearDown {
    [super tearDown];
    [KeyUtils deleteExistingKeyPair];
    [KeyUtils removeKeyByTag:self.serverPrivateTag];
    [KeyUtils removeKeyByTag:self.serverPublicTag];
    [KeyUtils removeKeyByTag:self.thiefPrivateTag];
    [KeyUtils removeKeyByTag:self.thiefPublicTag];
}

- (void)testBasic {
    NSString *message = [NSString stringWithFormat:@"secret%i", arc4random()];
    NSData *messageData = [message dataUsingEncoding:NSUTF8StringEncoding];
    MessageEncoderDecoder *client = [[MessageEncoderDecoder alloc] initWithKeyPair:self.clientPair
                                                             remotePublicKeyRef:[self.serverPair getPublicKeyRef]];
    MessageEncoderDecoder *server = [[MessageEncoderDecoder alloc] initWithKeyPair:self.serverPair
                                                             remotePublicKeyRef:[self.clientPair getPublicKeyRef]];
    MessageEncoderDecoder *theif = [[MessageEncoderDecoder alloc] initWithKeyPair:self.thiefPair
                                                            remotePublicKeyRef:[self.clientPair getPublicKeyRef]];
    NSData *secretData = [client encodeData:messageData];
    NSData *signature = [client signatureForMessage:secretData];
    NSData *encodedSessionKey = [client getEncodedSessionKey];
    
    XCTAssertTrue([server verifyMessage:secretData withSignature:signature]);
    NSData *decodedData = [server decodeData:secretData withEncodedKey:encodedSessionKey];
    NSString *decodedSecret = [[NSString alloc] initWithData:decodedData encoding:NSUTF8StringEncoding];
    
    XCTAssertTrue([message isEqualToString:decodedSecret]);
    
    NSData *theifData = [theif encodeData:messageData];
    NSData *theifSignature = [theif signatureForMessage:theifData];
    XCTAssertFalse([server verifyMessage:theifData withSignature:theifSignature]);
}

- (void)testBasicSubsequentTest {
    NSString *message = [NSString stringWithFormat:@"secret%i", arc4random()];
    NSData *messageData = [message dataUsingEncoding:NSUTF8StringEncoding];
    
    MessageEncoderDecoder *client = [[MessageEncoderDecoder alloc] initWithKeyPair:self.clientPair
                                                             remotePublicKeyRef:[self.serverPair getPublicKeyRef]];
    MessageEncoderDecoder *client2 = [[MessageEncoderDecoder alloc] initWithKeyPair:self.thiefPair
                                                             remotePublicKeyRef:[self.serverPair getPublicKeyRef]];
    MessageEncoderDecoder *server = [[MessageEncoderDecoder alloc] initWithKeyPair:self.serverPair
                                                             remotePublicKeyRef:[self.clientPair getPublicKeyRef]];
    
    NSData *secretData = [client encodeData:messageData];
    NSData *signature = [client signatureForMessage:secretData];
    NSData *encodedSessionKey = [client getEncodedSessionKey];
    XCTAssertTrue([server verifyMessage:secretData withSignature:signature]);
    
    NSData *decodedData = [server decodeData:secretData withEncodedKey:encodedSessionKey];
    NSString *decodedSecret = [[NSString alloc] initWithData:decodedData encoding:NSUTF8StringEncoding];
    XCTAssertTrue([message isEqualToString:decodedSecret]);
    
    [server setRemotePublicKeyRef:[self.thiefPair getPublicKeyRef]];

    NSData *secretData2 = [client2 encodeData:messageData];
    NSData *signature2 = [client2 signatureForMessage:secretData2];
    NSData *encodedSessionKey2 = [client2 getEncodedSessionKey];
    XCTAssertTrue([server verifyMessage:secretData2 withSignature:signature2]);
    
    NSData *decodedData2 = [server decodeData:secretData2 withEncodedKey:encodedSessionKey2];
    NSString *decodedSecret2 = [[NSString alloc] initWithData:decodedData2 encoding:NSUTF8StringEncoding];
    XCTAssertTrue([message isEqualToString:decodedSecret2]);
}

- (void)testBasicUpdate {
    MessageEncoderDecoder *client = [[MessageEncoderDecoder alloc] initWithKeyPair:self.clientPair
                                                             remotePublicKeyRef:[self.serverPair getPublicKeyRef]];
    
    BOOL test = [client getPrivateKey] != nil;
    XCTAssertTrue(test);
    test = [client getPublicKey] != nil;
    XCTAssertTrue(test);
    test = [client getRemotePublicKey] != nil;
    XCTAssertTrue(test);

}


- (NSData *)generateTag {
    int randomInt = arc4random();
    return [NSData dataWithBytes:&randomInt length:sizeof(randomInt)];
}

@end
