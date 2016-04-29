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
#import "DefaultProfileManager.h"
#import "ProfileTransport.h"
#import "KAADummyProfile.h"
#import "ProfileCommon.h"

#define HC_SHORTHAND
#import <OCHamcrest/OCHamcrest.h>

#define MOCKITO_SHORTHAND
#import <OCMockito/OCMockito.h>

@interface TestProfileContainer : NSObject <ProfileContainer>

@end

@implementation TestProfileContainer

- (KAADummyProfile *)getProfile {
    return [[KAADummyProfile alloc] init];
}

@end

@interface DefaultProfileManagerTest : XCTestCase

@end

@implementation DefaultProfileManagerTest

- (void)testProfileManagerIsInitialized {
    id<ProfileTransport> transport = mockProtocol(@protocol(ProfileTransport));
    DefaultProfileManager *manager = [[DefaultProfileManager alloc] initWithTransport:transport];
    
    ProfileSerializer *serializer = [[ProfileSerializer alloc] init];
    
    if (serializer.isDefault) {
        XCTAssertTrue([manager isInitialized]);
    } else {
        XCTAssertFalse([manager isInitialized]);
        [manager setProfileContainer:[[TestProfileContainer alloc] init]];
        XCTAssertTrue([manager isInitialized]);
    }
}

- (void)testProfileManager {
    
    id<ProfileTransport> transport = mockProtocol(@protocol(ProfileTransport));
    TestProfileContainer *container = [[TestProfileContainer alloc] init];
    
    DefaultProfileManager *profileManager =  [[DefaultProfileManager alloc] initWithTransport:transport];
    [profileManager setProfileContainer:container];
    
    XCTAssertNotNil([profileManager getSerializedProfile]);
    
    [profileManager updateProfile];
    [verifyCount(transport, times(1)) sync];
}

@end
