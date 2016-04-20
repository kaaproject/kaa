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

#define HC_SHORTHAND
#import <OCHamcrest/OCHamcrest.h>

#define MOCKITO_SHORTHAND
#import <OCMockito/OCMockito.h>

#import <XCTest/XCTest.h>
#import "DefaultMetaDataTransport.h"
#import "KaaClientProperties.h"
#import "EndpointObjectHash.h"

@interface DefaultMetaDataTransportTest : XCTestCase

@end

@implementation DefaultMetaDataTransportTest

- (void)testCreateMetaDataRequest {
    KaaClientProperties *properties = mock([KaaClientProperties class]);
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    [given([clientState profileHash]) willReturn:[EndpointObjectHash hashWithSHA1:[self getNewDataWith123]]];
    EndpointObjectHash *publicHash = [EndpointObjectHash hashWithSHA1:[self getNewDataWith567]];
    id<MetaDataTransport> transport = [[DefaultMetaDataTransport alloc] init];
    [transport createMetaDataRequest];
    [transport setClientProperties:properties];
    [transport createMetaDataRequest];
    [transport setClientState:clientState];
    [transport createMetaDataRequest];
    [transport setEndpointPublicKeyHash:publicHash];
    [transport setTimeout:5];
    
    SyncRequestMetaData *request = [transport createMetaDataRequest];
    
    [verifyCount(clientState, times(1)) profileHash];
    [verifyCount(properties, times(1)) sdkToken];
    
    XCTAssertEqualObjects(@(5), request.timeout.data);
}

#pragma mark - Supporting methods

- (NSData *)getNewDataWith123 {
    int32_t integer = 123;
    NSData *data = [NSData dataWithBytes:&integer length:sizeof(integer)];
    return data;
}

- (NSData *)getNewDataWith567 {
    int32_t integer = 567;
    NSData *data = [NSData dataWithBytes:&integer length:sizeof(integer)];
    return data;
}

@end
