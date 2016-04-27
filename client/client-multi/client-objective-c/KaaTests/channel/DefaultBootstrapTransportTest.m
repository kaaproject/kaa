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
#import "KaaClientState.h"
#import "BootstrapTransport.h"
#import "DefaultBootstrapTransport.h"

@interface DefaultBootstrapTransportTest : XCTestCase

@end

@implementation DefaultBootstrapTransportTest

- (void)testSyncNegative {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<BootstrapTransport> transport = [[DefaultBootstrapTransport alloc] initWithToken:@"some token"];
    [transport setClientState:clientState];
    @try {
        [transport sync];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSyncNegative succeed. Caught ChannelRuntimeException");
    }
}

- (void)testSync {
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<BootstrapTransport> transport = [[DefaultBootstrapTransport alloc] initWithToken:@"some token"];
    [transport setChannelManager:channelManager];
    [transport setClientState:clientState];
    [transport sync];
    
    [verifyCount(channelManager, times(1)) syncForTransportType:TRANSPORT_TYPE_BOOTSTRAP];
}

- (void)testCreateRequest {
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<BootstrapTransport> transport = [[DefaultBootstrapTransport alloc] initWithToken:@"some token"];
    [transport setChannelManager:channelManager];
    [transport createResolveRequest];
    [transport setClientState:clientState];
    [transport createResolveRequest];
}

- (void)testOnBootstrapResponse {
    id<BootstrapTransport> transport = [[DefaultBootstrapTransport alloc] initWithToken:@"some token"];
    id<BootstrapManager> manager = mockProtocol(@protocol(BootstrapManager));
    
    SyncResponse *response = [self getNewSyncResponse];
    NSArray *mdArray = [NSArray array];
    BootstrapSyncResponse *bootstrapSyncResponse = [[BootstrapSyncResponse alloc] init];
    bootstrapSyncResponse.requestId = 1;
    bootstrapSyncResponse.supportedProtocols = mdArray;
    [response setBootstrapSyncResponse:[KAAUnion unionWithBranch:KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_BRANCH_0 data:bootstrapSyncResponse]];
    
    [transport onResolveResponse:response];
    [transport setBootstrapManager:manager];
    [transport onResolveResponse:response];
    
    [verifyCount(manager, times(1)) onProtocolListUpdated:mdArray];
}

#pragma mark - Supported methods

- (SyncResponse *)getNewSyncResponse {
    SyncResponse *response = [[SyncResponse alloc] init];
    response.requestId = 1;
    response.status = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    
    return response;
}

@end
