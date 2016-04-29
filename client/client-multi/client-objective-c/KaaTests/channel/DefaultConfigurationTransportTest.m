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

#import <Foundation/Foundation.h>
#import <XCTest/XCTest.h>
#import "KaaClientState.h"
#import "ConfigurationTransport.h"
#import "DefaultConfigurationTransport.h"
#import "SchemaProcessor.h"

@interface DefaultConfigurationTransportTest : XCTestCase

@end

@implementation DefaultConfigurationTransportTest

- (void)testSyncNegative {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<ConfigurationTransport> transport = [[DefaultConfigurationTransport alloc] init];
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
    
    id<ConfigurationTransport> transport = [[DefaultConfigurationTransport alloc] init];
    [transport setChannelManager:channelManager];
    [transport setClientState:clientState];
    [transport sync];
    
    [verifyCount(channelManager, times(1)) syncForTransportType:TRANSPORT_TYPE_CONFIGURATION];
}

- (void)testCreateRequest {
    id<ConfigurationHashContainer> hashContainer = mockProtocol(@protocol(ConfigurationHashContainer));
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    
    id<ConfigurationTransport> transport = [[DefaultConfigurationTransport alloc] init];
    [transport createConfigurationRequest];
    [transport setConfigurationHashContainer:hashContainer];
    [transport createConfigurationRequest];
    [transport setClientState:clientState];
    
    ConfigurationSyncRequest *request = [transport createConfigurationRequest];
    
    XCTAssertNotNil(request);
    
    [verifyCount(hashContainer, times(1)) getConfigurationHash];
}

- (void)testOnConfigurationResponse {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<SchemaProcessor> schemaProcessor = mockProtocol(@protocol(SchemaProcessor));
    id<ConfigurationProcessor> configProcessor = mockProtocol(@protocol(ConfigurationProcessor));
    
    ConfigurationSyncResponse *response = [[ConfigurationSyncResponse alloc] init];
    [response setResponseStatus:SYNC_RESPONSE_STATUS_DELTA];
    
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    
    id<ConfigurationTransport> transport = [[DefaultConfigurationTransport alloc] init];
    [transport setChannelManager:channelManager];
    [transport onConfigurationResponse:response];
    [transport setClientState:clientState];
    [transport onConfigurationResponse:response];
    [transport setConfigurationProcessor:configProcessor];
    [transport onConfigurationResponse:response];
    [transport setSchemaProcessor:schemaProcessor];
    [transport onConfigurationResponse:response];
    

    NSData *data = [self getDataWith123];
    [response setConfDeltaBody:[KAAUnion unionWithBranch:KAA_UNION_BYTES_OR_NULL_BRANCH_0 data:data]];
    [transport onConfigurationResponse:response];
    [response setConfSchemaBody:[KAAUnion unionWithBranch:KAA_UNION_BYTES_OR_NULL_BRANCH_0 data:data]];
    [transport onConfigurationResponse:response];
    
    [verifyCount(schemaProcessor, times(1)) loadSchema:data];
    [verifyCount(configProcessor, times(2)) processConfigurationData:data fullResync:NO];
}

#pragma mark - Supporting methods

- (NSData *)getDataWith123 {
    char one = 1;
    char two = 2;
    char three = 3;
    NSMutableData *data = [NSMutableData dataWithBytes:&one length:sizeof(one)];
    [data appendBytes:&two length:sizeof(two)];
    [data appendBytes:&three length:sizeof(three)];
    return data;
}

@end
