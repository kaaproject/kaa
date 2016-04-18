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

#import "TransportContext.h"

@interface TransportContext ()

@property (nonatomic, strong) id<MetaDataTransport> metadataTransport;
@property (nonatomic, strong) id<BootstrapTransport> bootstrapTransport;
@property (nonatomic, strong) id<ProfileTransport> profileTransport;
@property (nonatomic, strong) id<EventTransport> eventTransport;
@property (nonatomic, strong) id<NotificationTransport> notificationTransport;
@property (nonatomic, strong) id<ConfigurationTransport> configurationTransport;
@property (nonatomic, strong) id<UserTransport> userTransport;
@property (nonatomic, strong) id<RedirectionTransport> redirectionTransport;
@property (nonatomic, strong) id<LogTransport> logTransport;

@end

@implementation TransportContext

- (instancetype)initWithMetaDataTransport:(id<MetaDataTransport>)metaData
                       bootstrapTransport:(id<BootstrapTransport>)bootstrap
                         profileTransport:(id<ProfileTransport>)profile
                           eventTransport:(id<EventTransport>)event
                    notificationTransport:(id<NotificationTransport>)notification
                   configurationTransport:(id<ConfigurationTransport>)configuration
                            userTransport:(id<UserTransport>)user
                     redirectionTransport:(id<RedirectionTransport>)redirection
                             logTransport:(id<LogTransport>)log {
    self = [super init];
    if (self) {
        self.metadataTransport = metaData;
        self.bootstrapTransport = bootstrap;
        self.profileTransport = profile;
        self.eventTransport = event;
        self.notificationTransport = notification;
        self.configurationTransport = configuration;
        self.userTransport = user;
        self.redirectionTransport = redirection;
        self.logTransport = log;
    }
    return self;
}

- (id<MetaDataTransport>)getMetaDataTransport {
    return self.metadataTransport;
}

- (id<BootstrapTransport>)getBootstrapTransport {
    return self.bootstrapTransport;
}

- (id<ProfileTransport>)getProfileTransport {
    return self.profileTransport;
}

- (id<EventTransport>)getEventTransport {
    return self.eventTransport;
}

- (id<NotificationTransport>)getNotificationTransport {
    return self.notificationTransport;
}

- (id<ConfigurationTransport>)getConfigurationTransport {
    return self.configurationTransport;
}

- (id<UserTransport>)getUserTransport {
    return self.userTransport;
}

- (id<RedirectionTransport>)getRedirectionTransport {
    return self.redirectionTransport;
}

- (id<LogTransport>)getLogTransport {
    return self.logTransport;
}

- (void)initTransportsWithChannelManager:(id<KaaChannelManager>)manager state:(id<KaaClientState>)state {
    NSArray *kaaTransports = [NSArray arrayWithObjects:
                      self.bootstrapTransport, self.profileTransport,
                      self.eventTransport, self.notificationTransport,
                      self.configurationTransport, self.userTransport, self.logTransport, nil];
    for (id<KaaTransport> transport in kaaTransports) {
        [transport setChannelManager:manager];
        [transport setClientState:state];
    }
}

@end
