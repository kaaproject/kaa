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

#import "DefaultProfileManager.h"
#import "ProfileCommon.h"

@interface DefaultProfileManager ()

@property (nonatomic, strong) ProfileSerializer *serializer;

@property (nonatomic, strong) id<ProfileTransport> transport;
@property (nonatomic, strong) id<ProfileContainer> container;

@end

@implementation DefaultProfileManager

- (instancetype)initWithTransport:(id<ProfileTransport>)transport {
    self = [super init];
    if (self) {
        self.transport = transport;
        self.serializer = [[ProfileSerializer alloc] init];
    }
    return self;
}

- (void)setProfileContainer:(id<ProfileContainer>)container {
    self.container = container;
}

- (NSData *)getSerializedProfile {
    return [self.serializer serializeContainer:self.container];
}

- (void)updateProfile {
    [self.transport sync];
}

- (BOOL)isInitialized {
    return self.container != nil || [self.serializer isDefault];
}

@end
