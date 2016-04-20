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
#import "KaaDataDemultiplexer.h"
#import "KaaDataMultiplexer.h"
#import "RedirectionTransport.h"
#import "MetaDataTransport.h"
#import "ConfigurationTransport.h"
#import "EventTransport.h"
#import "NotificationTransport.h"
#import "ProfileTransport.h"
#import "UserTransport.h"
#import "LogTransport.h"
#import "KaaClientState.h"

@interface DefaultOperationDataProcessor : NSObject <KaaDataDemultiplexer, KaaDataMultiplexer>

- (instancetype)initWithClientState:(id<KaaClientState>)state;

- (void)setRedirectionTransport:(id<RedirectionTransport>)transport;
- (void)setMetaDataTransport:(id<MetaDataTransport>)transport;
- (void)setConfigurationTransport:(id<ConfigurationTransport>)transport;
- (void)setEventTransport:(id<EventTransport>)transport;
- (void)setNotificationTransport:(id<NotificationTransport>)transport;
- (void)setProfileTransport:(id<ProfileTransport>)transport;
- (void)setUserTransport:(id<UserTransport>)transport;
- (void)setLogTransport:(id<LogTransport>)transport;

@end
