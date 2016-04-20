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
#import "IPTransportInfo.h"
#import "KeyUtils.h"
#import "TransportProtocolIdHolder.h"
#import "TestsHelper.h"

@interface IPTransportInfoTest : XCTestCase

@end

@implementation IPTransportInfoTest

- (void)testInit {
    [KeyUtils generateKeyPair];
    NSData *publicKey = [KeyUtils getPublicKey];
    TransportProtocolId *TPid = [TransportProtocolIdHolder TCPTransportID];
    uint32_t port = 80;
    
    IPTransportInfo *info = [[IPTransportInfo alloc] initWithTransportInfo:[self createTestServerInfoWithServerType:SERVER_OPERATIONS transportProtocolId:TPid host:@"localhost" port:port publicKey:publicKey]];
    
    XCTAssertEqual(SERVER_OPERATIONS, [info serverType]);
    XCTAssertEqualObjects(TPid, [info transportId]);
    XCTAssertEqualObjects(@"localhost", [info getHost]);
    XCTAssertEqual(port, [info getPort]);
}

- (id<TransportConnectionInfo>)createTestServerInfoWithServerType:(ServerType)serverType
                                              transportProtocolId:(TransportProtocolId *)TPid
                                                             host:(NSString *)host
                                                             port:(uint32_t)port
                                                        publicKey:(NSData *)publicKey {
    ProtocolMetaData *md = [TestsHelper buildMetaDataWithTransportProtocolId:TPid host:host port:port publicKey:publicKey];
    return  [[GenericTransportInfo alloc] initWithServerType:serverType meta:md];
}

@end
