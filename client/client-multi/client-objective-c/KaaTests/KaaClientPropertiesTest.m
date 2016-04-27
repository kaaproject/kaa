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
#import "KaaClientProperties.h"
#import "TransportProtocolIdHolder.h"
#import "TransportConnectionInfo.h"
#import "TimeCommons.h"

@interface KaaClientPropertiesTest : XCTestCase

@property (nonatomic, strong) KaaClientProperties *properties;

@end

@implementation KaaClientPropertiesTest

- (void)setUp {
    [super setUp];
    self.properties = [[KaaClientProperties alloc] initDefaultsWithBase64:[CommonBase64 new]];
}

- (void)testGetBootstrapServers {
    NSDictionary *bootstraps = [self.properties bootstrapServers];
    XCTAssertEqual(1, [bootstraps count]);
    
    NSArray *serverInfoList = bootstraps[[TransportProtocolIdHolder TCPTransportID]];
    XCTAssertNotNil(serverInfoList);
    XCTAssertEqual(1, [serverInfoList count]);
    
    id<TransportConnectionInfo> serverInfo = serverInfoList.firstObject;
    XCTAssertEqual(SERVER_BOOTSTRAP, [serverInfo serverType]);
    XCTAssertEqual(1, [serverInfo accessPointId]);
    XCTAssertTrue([[TransportProtocolIdHolder TCPTransportID] isEqual:[serverInfo transportId]]);
}

- (void)testGetSdkToken {
    XCTAssertTrue([@"O7D+oECY1jhs6qIK8LA0zdaykmQ=" isEqualToString:[self.properties sdkToken]]);
}

- (void)testGetPollDelay {
    XCTAssertEqual(0, [self.properties pollDelay]);
}

- (void)testGetPollPeriod {
    XCTAssertEqual(10, [self.properties pollPeriod]);
}

- (void)testGetPollUnit {
    XCTAssertEqual(TIME_UNIT_SECONDS, [self.properties pollUnit]);
}

- (void)testGetDefaultConfigData {
    XCTAssertNotNil([self.properties defaultConfigData]);
}

- (void)testGetDefaultConfigSchema {
    XCTAssertNotNil([self.properties defaultConfigSchema]);
}

@end
