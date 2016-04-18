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

#import "IPTransportInfo.h"

@interface IPTransportInfo ()

@property (nonatomic, strong) NSData *publicKey;
@property (nonatomic, strong) NSString *host;
@property (nonatomic) int32_t port;

@end

@implementation IPTransportInfo

- (instancetype)initWithTransportInfo:(id<TransportConnectionInfo>)parent {
    ProtocolMetaData *meta = [[ProtocolMetaData alloc] init];
    meta.accessPointId = [parent accessPointId];
    meta.connectionInfo = [parent connectionInfo];
    ProtocolVersionPair *info = [[ProtocolVersionPair alloc] init];
    info.id = [parent transportId].protocolId;
    info.version = [parent transportId].protocolVersion;
    meta.protocolVersionInfo = info;
    self = [super initWithServerType:[parent serverType] meta:meta];
    if (self) {
        NSInputStream *input = [NSInputStream inputStreamWithData:super.meta.connectionInfo];
        [input open];
        
        uint8_t keySizeBytes[sizeof(uint32_t)];
        [input read:keySizeBytes maxLength:sizeof(keySizeBytes)];
        uint32_t keySize = htonl(*(uint32_t *)keySizeBytes);
        
        uint8_t key[keySize];
        [input read:key maxLength:sizeof(key)];
        self.publicKey = [NSData dataWithBytes:key length:sizeof(key)];
        
        uint8_t hostSizeBytes[sizeof(uint32_t)];
        [input read:hostSizeBytes maxLength:sizeof(hostSizeBytes)];
        uint32_t hostSize = htonl(*(uint32_t *)hostSizeBytes);
        
        uint8_t host[hostSize];
        [input read:host maxLength:sizeof(host)];
        self.host = [[NSString alloc] initWithBytes:host length:sizeof(host) encoding:NSUTF8StringEncoding];
        
        uint8_t portBytes[sizeof(uint32_t)];
        [input read:portBytes maxLength:sizeof(portBytes)];
        self.port = htonl(*(uint32_t *)portBytes);
        
        [input close];
    }
    return self;
}

- (NSString *)getHost {
    return _host;
}

- (int32_t)getPort {
    return _port;
}

- (NSData *)getPublicKey {
    return _publicKey;
}

- (NSString *)getUrl {
    return [NSString stringWithFormat:@"http://%@:%i", self.host, self.port];
}

- (NSString *)description {
    return [NSString stringWithFormat:@"IPTransportInfo: %@", [self getUrl]];
}

@end
