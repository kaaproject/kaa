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
#import "DefaultBootstrapDataProcessor.h"
#import "EndpointGen.h"
#import "AvroBytesConverter.h"

@interface DefaultBootstrapDataProcessorTest : XCTestCase

@end

@implementation DefaultBootstrapDataProcessorTest

- (void)testRequestCreation {
    DefaultBootstrapDataProcessor *processor = [[DefaultBootstrapDataProcessor alloc] init];
    id<BootstrapTransport> transport = mockProtocol(@protocol(BootstrapTransport));
    [given([transport createResolveRequest]) willReturn:[self getNewSyncRequest]];
    [processor setBootstrapTransport:transport];
    XCTAssertNotNil([processor compileRequestForTypes:nil]);
    [verifyCount(transport, times(1)) createResolveRequest];
}

- (void)testRequestCreationWithNullTransport {
    DefaultBootstrapDataProcessor *processor = [[DefaultBootstrapDataProcessor alloc] init];
    XCTAssertNil([processor compileRequestForTypes:nil]);
}

- (void)testResponse {
    DefaultBootstrapDataProcessor *processor = [[DefaultBootstrapDataProcessor alloc] init];
    id<BootstrapTransport> transport = mockProtocol(@protocol(BootstrapTransport));
    [processor setBootstrapTransport:transport];
    SyncResponse *response = [self getNewSyncResponse];
    NSArray *mdArray = [NSArray array];
    BootstrapSyncResponse *bootstrapSyncResponse = [[BootstrapSyncResponse alloc] init];
    bootstrapSyncResponse.requestId = 1;
    bootstrapSyncResponse.supportedProtocols = mdArray;
    [response setBootstrapSyncResponse:[KAAUnion unionWithBranch:KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_BRANCH_0 data:bootstrapSyncResponse]];
    AvroBytesConverter *converter = [[AvroBytesConverter alloc] init];
    NSData *data = [converter toBytes:response];
    [processor processResponse:data];
    [verifyCount(transport, times(1)) onResolveResponse:anything()];
}

- (void)testNullResponse {
    DefaultBootstrapDataProcessor *processor = [[DefaultBootstrapDataProcessor alloc] init];
    id<BootstrapTransport> transport = mockProtocol(@protocol(BootstrapTransport));
    [processor setBootstrapTransport:transport];
    [processor processResponse:nil];
    [verifyCount(transport, times(0)) onResolveResponse:anything()];
}

- (void)testNullResponseWithNullTransport {
    DefaultBootstrapDataProcessor *processor = [[DefaultBootstrapDataProcessor alloc] init];
    [processor processResponse:nil];
}

#pragma mark - Supporting methods

- (SyncRequest *)getNewSyncRequest {
    SyncRequest *request = [[SyncRequest alloc] init];
    request.requestId = 1;
    return request;
}

- (SyncResponse *)getNewSyncResponse {
    SyncResponse *response = [[SyncResponse alloc] init];
    response.requestId = 1;
    response.status = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    return response;
}

@end
