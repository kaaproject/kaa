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
#import "MetaDataTransport.h"
#import "BootstrapTransport.h"
#import "ProfileTransport.h"
#import "EventTransport.h"
#import "NotificationTransport.h"
#import "ConfigurationTransport.h"
#import "UserTransport.h"
#import "RedirectionTransport.h"
#import "LogTransport.h"
#import "KaaChannelManager.h"
#import "KaaClientState.h"

@interface TransportContext : NSObject

- (instancetype)initWithMetaDataTransport:(id<MetaDataTransport>)metaData
                       bootstrapTransport:(id<BootstrapTransport>)bootstrap
                         profileTransport:(id<ProfileTransport>)profile
                           eventTransport:(id<EventTransport>)event
                    notificationTransport:(id<NotificationTransport>)notification
                   configurationTransport:(id<ConfigurationTransport>)configuration
                            userTransport:(id<UserTransport>)user
                     redirectionTransport:(id<RedirectionTransport>)redirection
                             logTransport:(id<LogTransport>)log;

- (id<MetaDataTransport>)getMetaDataTransport;

- (id<BootstrapTransport>)getBootstrapTransport;

- (id<ProfileTransport>)getProfileTransport;

- (id<EventTransport>)getEventTransport;

- (id<NotificationTransport>)getNotificationTransport;

- (id<ConfigurationTransport>)getConfigurationTransport;

- (id<UserTransport>)getUserTransport;

- (id<RedirectionTransport>)getRedirectionTransport;

- (id<LogTransport>)getLogTransport;

- (void)initTransportsWithChannelManager:(id<KaaChannelManager>)manager state:(id<KaaClientState>)state;

@end
