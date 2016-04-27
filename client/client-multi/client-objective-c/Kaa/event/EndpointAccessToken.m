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

#import "EndpointAccessToken.h"

/**
 * Represents endpoint access token which has to be passed for endpoint attachment.
 */
@implementation EndpointAccessToken

- (instancetype)initWithToken:(NSString *)token {
    self = [super init];
    if (self) {
        self.token = token;
    }
    return self;
}

- (id)copyWithZone:(NSZone *)zone {
    return [[[self class] allocWithZone:zone] initWithToken:self.token];
}

- (NSUInteger)hash {
    const NSUInteger prime = 31;
    return prime + (self.token ? [self.token hash] : 0);
}

- (BOOL)isEqual:(id)object {
    if (self == object) {
        return YES;
    }
    if (object == nil) {
        return NO;
    }
    if ([object isKindOfClass:[EndpointAccessToken class]]) {
        EndpointAccessToken *other = (EndpointAccessToken*)object;
        if (self.token == nil) {
            if (other.token != nil) {
                return NO;
            } else {
                return YES;
            }
        }
        if ([self.token isEqualToString:other.token]) {
            return YES;
        }
    }
    
    return NO;
}

- (NSString *)description {
    return self.token;
}

@end
