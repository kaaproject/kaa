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

#import "TransportProtocolId.h"

@implementation TransportProtocolId

- (instancetype)initWithId:(int32_t)id version:(int32_t)version {
    self = [super init];
    if (self) {
        _protocolId = id;
        _protocolVersion = version;
    }
    return self;
}

- (id)copyWithZone:(NSZone *)zone {
    return [[[self class] allocWithZone:zone] initWithId:self.protocolId version:self.protocolVersion];
}

- (NSUInteger)hash {
    NSUInteger prime = 31;
    NSUInteger result = 1;
    result = prime * result + self.protocolId;
    result = prime * result + self.protocolVersion;
    return result;
}

- (BOOL)isEqual:(id)object {
    
    if ([object isKindOfClass:[TransportProtocolId class]]) {
        TransportProtocolId *other = (TransportProtocolId*)object;
        if (other.protocolId == self.protocolId && other.protocolVersion == self.protocolVersion) {
            return YES;
        }
    }
    
    return NO;
}

- (NSString*)description {
    return [NSString stringWithFormat:@"TransportProtocolId [id:%i, version:%i]", self.protocolId, self.protocolVersion];
}

@end
