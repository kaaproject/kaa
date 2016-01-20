/*
 * Copyright 2014-2015 CyberVision, Inc.
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
#import "KaaClientPropertiesState.h"
#import "NSData+Conversion.h"
#import "TestsHelper.h"

@interface KaaClientPropertiesStateTest : XCTestCase

@property (nonatomic,strong) id<KaaClientState> state;

@end

@implementation KaaClientPropertiesStateTest

- (void)setUp {
    [super setUp];
    self.state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    NSLog(@"New state created!");
}

- (void)testKeys {
    SecKeyRef privateKey = [self.state privateKey];
    SecKeyRef publicKey = [self.state publicKey];
    XCTAssertTrue(privateKey != NULL);
    XCTAssertTrue(publicKey != NULL);
}

- (void)testProfileHash {
    NSData *hashEntry = [@"testProfileHash" dataUsingEncoding:NSUTF8StringEncoding];
    EndpointObjectHash *hash = [EndpointObjectHash fromSHA1:hashEntry];
    [self.state setProfileHash:hash];
    XCTAssertTrue([hash isEqual:[self.state profileHash]]);
}

- (void)testNfSubscription {
    Topic *topic1 = [[Topic alloc] init];
    topic1.id = @"1234";
    topic1.name = @"testName";
    topic1.subscriptionType = SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION;
    
    Topic *topic2 = [[Topic alloc] init];
    topic2.id = @"4321";
    topic2.name = @"testName";
    topic2.subscriptionType = SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION;
    
    [self.state addTopic:topic1];
    [self.state addTopic:topic2];
    
    [self.state updateTopicSubscriptionInfo:topic2.id sequence:1];
    
    [self.state updateTopicSubscriptionInfo:topic1.id sequence:0];
    [self.state updateTopicSubscriptionInfo:topic1.id sequence:5];
    [self.state updateTopicSubscriptionInfo:topic1.id sequence:1];
    
    NSMutableDictionary *expected = [NSMutableDictionary dictionary];
    expected[topic1.id] = [NSNumber numberWithInt:5];
    expected[topic2.id] = [NSNumber numberWithInt:1];
    
    XCTAssertTrue([expected isEqualToDictionary:[self.state getNotificationSubscriptions]]);
    
    [self.state persist];
    self.state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    
    XCTAssertTrue([expected isEqualToDictionary:[self.state getNotificationSubscriptions]]);

    [self.state removeTopic:topic1.id];
    [self.state persist];
    
    self.state = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:[TestsHelper getProperties]];
    
    [expected removeObjectForKey:topic1.id];
    
    XCTAssertTrue([expected isEqualToDictionary:[self.state getNotificationSubscriptions]]);
}

- (void)testSDKPropertiesUpdate {
    XCTAssertFalse([self.state isRegistred]);
    
    [self.state setIsRegistred:YES];
    [self.state persist];
    
    XCTAssertTrue([self.state isRegistred]);
    
    KaaClientProperties *properties = [TestsHelper getProperties];
    [properties setString:@"SDK_TOKEN_100500" forKey:SDK_TOKEN_KEY];
    
    KaaClientPropertiesState *newState = [[KaaClientPropertiesState alloc] initWithBase64:[CommonBase64 new] clientProperties:properties];
    
    XCTAssertFalse([newState isRegistred]);
}

- (void)testClean {
    [self.state persist];
    [self.state setIsRegistred:YES];
    [self.state persist];
    
    NSString *storage = [NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES) firstObject];
    NSString *backupFileName = [NSString stringWithFormat:@"%@_bckp", STATE_FILE_DEFAULT];

    NSString *stateFile = [[[NSURL fileURLWithPath:storage] URLByAppendingPathComponent:STATE_FILE_DEFAULT] path];
    NSString *backupFile = [[[NSURL fileURLWithPath:storage] URLByAppendingPathComponent:backupFileName] path];

    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    XCTAssertTrue([fileManager fileExistsAtPath:stateFile]);
    XCTAssertTrue([fileManager fileExistsAtPath:backupFile]);
    
    [self.state clean];
    XCTAssertFalse([fileManager fileExistsAtPath:stateFile]);
    XCTAssertFalse([fileManager fileExistsAtPath:backupFile]);
}

@end
