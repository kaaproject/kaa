/*
 * Copyright 2014-2015 CyberVision, Inc.
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
#import "GenericKaaClient.h"
#import "KaaClientPlatformContext.h"
#import "KaaClientStateDelegate.h"
#import "TransportContext.h"
#import "KaaInternalChannelManager.h"
#import "FailoverManager.h"
#import "ResyncConfigurationManager.h"
#import "KaaClientProperties.h"
#import "DefaultEndpointRegistrationManager.h"
#import "DefaultEventManager.h"
#import "DefaultNotificationManager.h"
#import "ProfileManager.h"
#import "BootstrapManager.h"
#import "AbstractHttpClient.h"
#import "DefaultUserTransport.h"
#import "DefaultLogCollector.h"

#define FORSE_SYNC YES

typedef enum {
    CLIENT_LIFECYCLE_STATE_CREATED,
    CLIENT_LIFECYCLE_STATE_STARTED,
    CLIENT_LIFECYCLE_STATE_PAUSED,
    CLIENT_LIFECYCLE_STATE_STOPPED
} ClientLifecycleState;

/**
 * Abstract class that holds general elements of Kaa library.
 *
 * This class creates and binds Kaa library modules. Public access to each
 * module is performed using <KaaClient> interface.
 *
 * Class contains abstract methods
 * AbstractKaaClient#createHttpClient(NSString, SecKeyRef, SecKeyRef, NSData)
 * and few more which are used to reference the platform-specific implementation
 * of http client and Kaa's state persistent storage.
 *
 * Http client <AbstractHttpClient> is used to provide basic
 * communication with Bootstrap and Operation servers using HTTP protocol.
 */
@interface AbstractKaaClient : NSObject <GenericKaaClient>

@property (nonatomic,strong) id<ConfigurationManager> configurationManager;
@property (nonatomic,strong) AbstractLogCollector *logCollector;

@property (nonatomic,strong) id<KaaClientPlatformContext> context;
@property (nonatomic,weak) id<KaaClientStateDelegate> stateDelegate;

@property (nonatomic) ClientLifecycleState lifecycleState;

- (instancetype)initWithPlatformContext:(id<KaaClientPlatformContext>)context
                            andDelegate:(id<KaaClientStateDelegate>)delegate;

- (TransportContext *)buildTransportContext:(KaaClientProperties *)properties
                             andClientState:(id<KaaClientState>)state;

/**
 * @param bootstrap servers <TransportProtocolId, List<TransportConnectionInfo>> as key-value
 */
- (id<KaaInternalChannelManager>)buildChannelManagerWithBootstrap:(id<BootstrapManager>)bootstrapManager
                                                       andServers:(NSDictionary *)bootstrapServers;

- (void)initializeChannels:(id<KaaInternalChannelManager>)manager transport:(TransportContext *)context;

- (id<FailoverManager>)buildFailoverManager:(id<KaaChannelManager>)manager;

- (ResyncConfigurationManager *)buildConfigurationManager:(KaaClientProperties *)properties
                                              clientState:(id<KaaClientState>)state
                                                transport:(TransportContext *)context;

- (DefaultLogCollector *)buildLogCollector:(KaaClientProperties *)properties
                               clientState:(id<KaaClientState>)state
                                 transport:(TransportContext *)context;

- (DefaultEndpointRegistrationManager *)buildRegistrationManager:(KaaClientProperties *)properties
                                                     clientState:(id<KaaClientState>)state
                                                       transport:(TransportContext *)context;

- (DefaultEventManager *)buildEventManager:(KaaClientProperties *)properties
                               clientState:(id<KaaClientState>)state
                                 transport:(TransportContext *)context;

- (DefaultNotificationManager *)buildNotificationManager:(KaaClientProperties *)properties
                                             clientState:(id<KaaClientState>)state
                                               transport:(TransportContext *)context;

- (id<ProfileManager>)buildProfileManager:(KaaClientProperties *)properties
                              clientState:(id<KaaClientState>)state
                                transport:(TransportContext *)context;

- (id<BootstrapManager>)buildBootstrapManager:(KaaClientProperties *)properties
                                  clientState:(id<KaaClientState>)state
                                    transport:(TransportContext *)context;

- (AbstractHttpClient *)createHttpClientWithURL:(NSString *)url
                                     privateKey:(SecKeyRef)privateK
                                      publicKey:(SecKeyRef)publicK
                                      remoteKey:(NSData *)remoteK;

- (id<BootstrapTransport>)buildBootstrapTransport:(KaaClientProperties *)properties
                                      clientState:(id<KaaClientState>)state;

- (id<ProfileTransport>)buildProfileTransport:(KaaClientProperties *)properties
                                  clientState:(id<KaaClientState>)state;

- (id<ConfigurationTransport>)buildConfigurationTransport:(KaaClientProperties *)properties
                                              clientState:(id<KaaClientState>)state;

- (id<NotificationTransport>)buildNotificationTransport:(KaaClientProperties *)properties
                                            clientState:(id<KaaClientState>)state;

- (DefaultUserTransport *)buildUserTransport:(KaaClientProperties *)properties
                                 clientState:(id<KaaClientState>)state;

- (id<EventTransport>)buildEventTransport:(KaaClientProperties *)properties
                              clientState:(id<KaaClientState>)state;

- (id<LogTransport>)buildLogTransport:(KaaClientProperties *)properties
                          clientState:(id<KaaClientState>)state;

- (id<RedirectionTransport>)buildRedirectionTransport:(KaaClientProperties *)properties
                                          clientState:(id<KaaClientState>)state;

- (void)checkLifecycleState:(ClientLifecycleState)expected withError:(NSString *)message;

- (void)checkLifecycleStateNot:(ClientLifecycleState)expected withError:(NSString *)message;

@end
