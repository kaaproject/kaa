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
#import "LogTransport.h"
#import "DefaultLogTransport.h"

@interface DefaultLogTransportTest : XCTestCase

@end

@implementation DefaultLogTransportTest

- (void)testSyncNegative {
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    id<LogTransport> transport = [[DefaultLogTransport alloc] init];
    [transport setClientState:clientState];
    
    @try {
        [transport sync];
        XCTFail();
    }
    @catch (NSException *exception) {
        NSLog(@"testSyncNegativeSucceed. Caught ChannelRuntimeException");
    }
}

- (void)testSync {
    id<KaaChannelManager> channelManager = mockProtocol(@protocol(KaaChannelManager));
    id<KaaClientState> clientState = mockProtocol(@protocol(KaaClientState));
    
    id<LogTransport> transport = [[DefaultLogTransport alloc] init];
    [transport setChannelManager:channelManager];
    [transport setClientState:clientState];
    [transport sync];
    
    [verifyCount(channelManager, times(1)) syncForTransportType:TRANSPORT_TYPE_LOGGING];
}

- (void)testCreateRequest {
    id<LogProcessor> processor = mockProtocol(@protocol(LogProcessor));
    
    id<LogTransport> transport = [[DefaultLogTransport alloc] init];
    [transport createLogRequest];
    [transport setLogProcessor:processor];
    [transport createLogRequest];
    
    [verifyCount(processor, times(1)) fillSyncRequest:anything()];
}

- (void)testOnEventResponse {
    id<LogProcessor> processor = mockProtocol(@protocol(LogProcessor));
    id<LogTransport> transport = [[DefaultLogTransport alloc] init];
    LogSyncResponse *response = [[LogSyncResponse alloc] init];
    
    [transport onLogResponse:response];
    [transport setLogProcessor:processor];
    [transport onLogResponse:response];
    
    [verifyCount(processor, times(1)) onLogResponse:response];
}

@end
