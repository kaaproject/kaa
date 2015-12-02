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

#import "TestsHelper.h"
#import "KaaClientPropertiesState.h"

@implementation TestsHelper

+ (ProtocolMetaData *)buildMetaDataWithTPid:(TransportProtocolId *)TPid
                                       host:(NSString *)host
                                       port:(int32_t)port
                               andPublicKey:(NSData *)publicKey {
    int32_t publicKeyLength = CFSwapInt32([publicKey length]);
    int32_t hostLength = CFSwapInt32([host lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);
    int32_t portToWrite = CFSwapInt32(port);
    NSMutableData *data = [NSMutableData data];
    
    [data appendBytes:&publicKeyLength length:sizeof(publicKeyLength)];
    [data appendData:publicKey];
    [data appendBytes:&hostLength length:sizeof(hostLength)];
    [data appendData:[host dataUsingEncoding:NSUTF8StringEncoding]];
    [data appendBytes:&portToWrite length:sizeof(portToWrite)];
    
    ProtocolVersionPair *pair = [[ProtocolVersionPair alloc]init];
    [pair setId:TPid.protocolId];
    [pair setVersion:TPid.protocolVersion];
    
    ProtocolMetaData *md = [[ProtocolMetaData alloc] init];
    [md setConnectionInfo:data];
    [md setAccessPointId:[[NSString stringWithFormat:@"%@:%i", host, port] hash]];
    [md setProtocolVersionInfo:pair];
    return md;
}

+ (KaaClientProperties *)getProperties {
    KaaClientProperties *properties = [[KaaClientProperties alloc] initDefaults:[CommonBase64 new]];
    [properties setString:@"0" forKey:TRANSPORT_POLL_DELAY_KEY];
    [properties setString:@"1" forKey:TRANSPORT_POLL_PERIOD_KEY];
    [properties setString:@"1" forKey:TRANSPORT_POLL_UNIT_KEY];
    [properties setString:@"123456" forKey:SDK_TOKEN_KEY];
    [properties setString:STATE_FILE_DEFAULT forKey:STATE_FILE_LOCATION_KEY];
    return properties;
}

@end
