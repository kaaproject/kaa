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

#import <Foundation/Foundation.h>
#import "EndpointRegistrationManager.h"
#import "EndpointRegistrationProcessor.h"
#import "KaaClientState.h"
#import "ExecutorContext.h"
#import "ProfileTransport.h"
#import "UserTransport.h"

@interface DefaultEndpointRegistrationManager : NSObject <EndpointRegistrationManager,EndpointRegistrationProcessor>

- (instancetype)initWithState:(id<KaaClientState>)state
              executorContext:(id<ExecutorContext>)context
                userTransport:(id<UserTransport>)userTransport
             profileTransport:(id<ProfileTransport>)profileTransport;

- (void)updateEndpointAccessToken:(NSString *)token;

- (NSString *)refreshEndpointAccessToken;

/**
 * @return dictionary of attached endpoints <EndpointAccessToken, EndpointKeyHash> as key-value;
 */
- (NSDictionary *)getAttachedEndpointList;

@end
