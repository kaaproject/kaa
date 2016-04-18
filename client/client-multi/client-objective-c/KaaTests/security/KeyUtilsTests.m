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
#import "KeyUtils.h"

@interface KeyUtilsTests : XCTestCase

@property (nonatomic, strong) NSData *remoteKeyTag;

@end

@implementation KeyUtilsTests

- (void)setUp {
    [super setUp];
    int randomInt = arc4random();
    self.remoteKeyTag = [NSData dataWithBytes:&randomInt length:sizeof(randomInt)];
}

- (void)testGenerateKeyPair {
    KeyPair *defaultKeyPair = [KeyUtils generateKeyPair];
    
    XCTAssertNotNil(defaultKeyPair);
    XCTAssertTrue([defaultKeyPair getPrivateKeyRef] != NULL);
    XCTAssertTrue([defaultKeyPair getPublicKeyRef] != NULL);
    
    XCTAssertTrue([KeyUtils getPrivateKeyRef] != NULL);
    XCTAssertTrue([KeyUtils getPublicKeyRef] != NULL);
    
    XCTAssertNotNil([KeyUtils getPublicKey]);
    XCTAssertTrue([[KeyUtils getPublicKey] length] > 0);
    
    [KeyUtils deleteExistingKeyPair];
    
    XCTAssertTrue([KeyUtils getPrivateKeyRef] == NULL);
    XCTAssertTrue([KeyUtils getPublicKeyRef] == NULL);
}

- (void)testStoreAndRemoveRemoteKey {
    [KeyUtils generateKeyPair];
    
    NSData *remoteKey = [KeyUtils getPublicKey];
    XCTAssertNotNil(remoteKey);
    XCTAssertTrue([remoteKey length] > 0);
    
    //store
    SecKeyRef remoteKeyRef = [KeyUtils storePublicKey:remoteKey withTag:self.remoteKeyTag];
    XCTAssertTrue(remoteKeyRef != NULL);
    
    //retrieve
    NSData *restoredRemoteKey = [KeyUtils getPublicKeyByTag:self.remoteKeyTag];
    XCTAssertNotNil(restoredRemoteKey);
    XCTAssertTrue([remoteKey isEqualToData:restoredRemoteKey]);
    
    //remove
    [KeyUtils removeKeyByTag:self.remoteKeyTag];
    XCTAssertNil([KeyUtils getPublicKeyByTag:self.remoteKeyTag]);
    
    [KeyUtils deleteExistingKeyPair];
}

@end
