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

#import "EndpointKeyHash.h"

/**
 * Represents endpoint key hash returned from OPS after it was successfully attached.
 */
@implementation EndpointKeyHash

- (instancetype)initWithKeyHash:(NSString *)keyHash {
    self = [super init];
    if (self) {
        self.keyHash = keyHash;
    }
    return self;
}

- (NSUInteger)hash {
    const NSUInteger prime = 31;
    return prime + [self.keyHash hash];
}

- (BOOL)isEqual:(id)object {
    if ([object isKindOfClass:[EndpointKeyHash class]]) {
        EndpointKeyHash *other = (EndpointKeyHash *)object;
        if ([self.keyHash isEqualToString:other.keyHash]) {
            return YES;
        }
    }
    
    return NO;
}

- (NSString *)description {
    return self.keyHash;
}
@end
