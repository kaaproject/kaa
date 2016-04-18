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
#import "DefaultRedirectionTransport.h"

@interface DefaultRedirectionTransportTest : XCTestCase

@end

@implementation DefaultRedirectionTransportTest

- (void)testOnRedirectionReponse {
    id<BootstrapManager> manager = mockProtocol(@protocol(BootstrapManager));
    id<RedirectionTransport> transport = [[DefaultRedirectionTransport alloc] init];
    RedirectSyncResponse *response = [[RedirectSyncResponse alloc] init];
    [transport onRedirectionResponse:response];
    [transport setBootstrapManager:manager];
    [transport onRedirectionResponse:response];
    [response setAccessPointId:1];
    [transport onRedirectionResponse:response];
    [response setAccessPointId:2];
    [transport onRedirectionResponse:response];
    
    [verifyCount(manager, times(1)) useNextOperationsServerByAccessPointId:1];
}

@end
