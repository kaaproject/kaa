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

#import "GenericTransportInfo.h"

@implementation GenericTransportInfo

@synthesize serverType = _serverType;
@synthesize transportId = _transportId;

- (instancetype)initWithServerType:(ServerType)serverType meta:(ProtocolMetaData *)meta {
    self = [super init];
    if (self) {
        _serverType = serverType;
        self.meta = meta;
        _transportId = [[TransportProtocolId alloc] initWithId:meta.protocolVersionInfo.id
                                                        version:meta.protocolVersionInfo.version];
    }
    return self;
}

- (int32_t)accessPointId {
    return [self.meta accessPointId];
}

- (NSData *)connectionInfo {
    return [self.meta connectionInfo];
}

- (BOOL)isEqual:(id)object {
    if ([object isKindOfClass:[GenericTransportInfo class]]) {
        GenericTransportInfo *other = (GenericTransportInfo*)object;
        if (other.serverType == _serverType && [other.transportId isEqual:_transportId] && [[other connectionInfo] isEqualToData:[self connectionInfo]]) {
            return YES;
        }
    }
    
    return NO;
}

- (NSUInteger)hash {
    NSUInteger prime = 31;
    NSUInteger result = 1;
    result = prime * result + _serverType;
    result = prime * result + [_transportId hash];
    result = prime * result + [_meta hash];
    return result;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"GenericTransportInfo [serverType = %i] [transportId = %@] [meta = %@]",
            _serverType, _transportId, _meta];
}

@end
