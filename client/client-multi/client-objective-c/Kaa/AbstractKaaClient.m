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

#import "AbstractKaaClient.h"
#import "NSMutableArray+Shuffling.h"
#import "KaaClientPropertiesState.h"
#import "KeyUtils.h"
#import "DefaultMetaDataTransport.h"
#import "DefaultChannelManager.h"
#import "ProfileManager.h"
#import "DefaultProfileManager.h"
#import "DefaultBootstrapManager.h"
#import "DefaultBootstrapTransport.h"
#import "DefaultRedirectionTransport.h"
#import "DefaultLogTransport.h"
#import "DefaultConfigurationTransport.h"
#import "DefaultProfileTransport.h"
#import "DefaultEventTransport.h"
#import "DefaultNotificationTransport.h"
#import "DefaultFailoverManager.h"
#import "DefaultBootstrapDataProcessor.h"
#import "DefaultOperationDataProcessor.h"
#import "DefaultBootstrapChannel.h"
#import "DefaultOperationTcpChannel.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"AbstractKaaClient >>>"
#define LONG_POLL_TIMEOUT 60000

@interface AbstractKaaClient ()

@property (nonatomic,strong) DefaultNotificationManager *notificationManager;
@property (nonatomic,strong) id<ProfileManager> profileManager;

@property (nonatomic,strong) KaaClientProperties *properties;
@property (nonatomic,strong) id<KaaClientState> clientState;
@property (nonatomic,strong) id<BootstrapManager> bootstrapManager;
@property (nonatomic,strong) id<EventManager> eventManager;
@property (nonatomic,strong) EventFamilyFactory *eventFamilyFactory;
@property (nonatomic,strong) DefaultEndpointRegistrationManager *endpointRegistrationManager;
@property (nonatomic,strong) id<KaaInternalChannelManager> channelManager;
@property (nonatomic,strong) id<FailoverManager> failoverManager;

- (NSOperationQueue *)getLifeCycleExecutor;
- (void)checkReadiness;

@end

@implementation AbstractKaaClient

- (instancetype)initWithPlatformContext:(id<KaaClientPlatformContext>)context andDelegate:(id<KaaClientStateDelegate>)delegate {
    self = [super init];
    if (self) {
        self.context = context;
        self.stateDelegate = delegate;
        self.lifecycleState = CLIENT_LIFECYCLE_STATE_CREATED;
        self.properties = [self.context getProperties];
        if (![self.context getProperties]) {
            self.properties = [[KaaClientProperties alloc] initDefaults:[self.context getBase64]];
        }
        
        NSDictionary *bootstrapServers = [self.properties bootstrapServers];
        if ([bootstrapServers count] <= 0) {
            [NSException raise:NSInternalInconsistencyException format:@"Unable to obtain list of bootstrap servers"];
        }
        
        for (NSMutableArray *serverList in bootstrapServers.allValues) {
            [serverList shuffle];
        }
        
        self.clientState = [[KaaClientPropertiesState alloc] initWith:[context getBase64] andClientProperties:self.properties];
        
        TransportContext *transportContext = [self buildTransportContext:self.properties andClientState:self.clientState];
        
        self.bootstrapManager = [self buildBootstrapManager:self.properties
                                                clientState:self.clientState transport:transportContext];
        self.channelManager = [self buildChannelManagerWithBootstrap:self.bootstrapManager andServers:bootstrapServers];
        self.failoverManager = [self buildFailoverManager:self.channelManager];
        [self.channelManager setFailoverManager:self.failoverManager];
        
        [self initializeChannels:self.channelManager transport:transportContext];
        
        [self.bootstrapManager setChannelManager:self.channelManager];
        [self.bootstrapManager setFailoverManager:self.failoverManager];
        
        self.profileManager = [self buildProfileManager:self.properties
                                            clientState:self.clientState transport:transportContext];
        self.notificationManager = [self buildNotificationManager:self.properties
                                                      clientState:self.clientState transport:transportContext];
        self.eventManager = [self buildEventManager:self.properties
                                        clientState:self.clientState transport:transportContext];
        self.endpointRegistrationManager = [self buildRegistrationManager:self.properties
                                                              clientState:self.clientState transport:transportContext];
        self.logCollector = [self buildLogCollector:self.properties
                                        clientState:self.clientState transport:transportContext];
        self.configurationManager = [self buildConfigurationManager:self.properties
                                                        clientState:self.clientState transport:transportContext];
        
        [[transportContext getRedirectionTransport] setBootstrapManager:self.bootstrapManager];
        [[transportContext getBootstrapTransport] setBootstrapManager:self.bootstrapManager];
        [[transportContext getProfileTransport] setProfileManager:self.profileManager];
        [[transportContext getEventTransport] setEventManager:self.eventManager];
        [[transportContext getNotificationTransport] setNotificationProcessor:self.notificationManager];
        [[transportContext getConfigurationTransport] setConfigurationHashContainer:self.configurationManager];
        [[transportContext getConfigurationTransport] setConfigurationProcessor:self.configurationManager];
        [[transportContext getUserTransport] setEndpointRegistrationProcessor:self.endpointRegistrationManager];
        [[transportContext getLogTransport] setLogProcessor:self.logCollector];
        [transportContext initTransportsWithChannelManager:self.channelManager andState:self.clientState];
        
        self.eventFamilyFactory = [[EventFamilyFactory alloc]
                                   initWith:self.eventManager executorContext:[self.context getExecutorContext]];
    }
    return self;
}

- (void)start {
    [self checkLifecycleStateNot:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client is already started"];
    [self checkLifecycleStateNot:CLIENT_LIFECYCLE_STATE_PAUSED withError:@"Kaa client is paused, need to be resumed"];
    
    [self checkReadiness];
    
    [[self.context getExecutorContext] initiate];
    __weak typeof(self)weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        DDLogDebug(@"%@ Client startup initiated", TAG);
        @try {
            [weakSelf setLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED];
            
            //load configuration
            [weakSelf.configurationManager initiate];
            [weakSelf.bootstrapManager receiveOperationsServerList];
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onStarted];
            }
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Start failed: %@. Reason: %@", TAG, exception.name, exception.reason);
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onStartFailure:exception];
            }
        }
    }];
}

- (void)stop {
    [self checkLifecycleStateNot:CLIENT_LIFECYCLE_STATE_CREATED withError:@"Kaa client is not started"];
    [self checkLifecycleStateNot:CLIENT_LIFECYCLE_STATE_STOPPED withError:@"Kaa client is already stopped"];
    
    __weak typeof(self) weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        @try {
            [weakSelf setLifecycleState:CLIENT_LIFECYCLE_STATE_STOPPED];
            [weakSelf.logCollector stop];
            [weakSelf.clientState persist];
            [weakSelf.channelManager shutdown];
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onStopped];
            }
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Stop failed: %@. Reason: %@", TAG, exception.name, exception.reason);
            if(weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onStopFailure:exception];
            }
        }
        @finally {
            [[weakSelf.context getExecutorContext] stop];
        }
    }];
    
}

- (void)pause {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED
                    withError:[NSString stringWithFormat:@"Kaa client is not started: %i is current state", self.lifecycleState]];
    
    __weak typeof(self) weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        @try {
            [weakSelf.clientState persist];
            [weakSelf setLifecycleState:CLIENT_LIFECYCLE_STATE_PAUSED];
            [weakSelf.channelManager pause];
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onPaused];
            }
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Pause failed: %@. Reason: %@", TAG, exception.name, exception.reason);
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onPauseFailure:exception];
            }
        }
    }];
}

- (void)resume {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_PAUSED withError:@"Kaa client isn't paused"];
    
    __weak typeof(self) weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        @try {
            [weakSelf.channelManager resume];
            [weakSelf setLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED];
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onResume];
            }
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Resume failed: %@. Reason: %@", TAG, exception.name, exception.reason);
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onResumeFailure:exception];
            }
        }
    }];
}

- (void)setLogDeliveryDelegate:(id<LogDeliveryDelegate>)delegate {
    [self.logCollector setLogDeliveryDelegate:delegate];
}

- (void)setProfileContainer:(id<ProfileContainer>)container {
    [self.profileManager setProfileContainer:container];
}

- (void)updateProfile {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.profileManager updateProfile];
}

- (void)setConfigurationStorage:(id<ConfigurationStorage>)storage {
    [self.configurationManager setConfigurationStorage:storage];
}

- (void)addConfigurationDelegate:(id<ConfigurationDelegate>)delegate {
    [self.configurationManager addDelegate:delegate];
}

- (void)removeConfigurationDelegate:(id<ConfigurationDelegate>)delegate {
    [self.configurationManager removeDelegate:delegate];
}

- (NSArray *)getTopics {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    return [self.notificationManager getTopics];
}

- (void)addTopicListDelegate:(id<NotificationTopicListDelegate>)delegate {
    [self.notificationManager addTopicListDelegate:delegate];
}

- (void)removeTopicListDelegate:(id<NotificationTopicListDelegate>)delegate {
    [self.notificationManager removeTopicListDelegate:delegate];
}

- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate {
    [self.notificationManager addNotificationDelegate:delegate];
}

- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate for:(NSString *)topicId {
    [self.notificationManager addNotificationDelegate:delegate forTopic:topicId];
}

- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate {
    [self.notificationManager removeNotificationDelegate:delegate];
}

- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate for:(NSString *)topicId {
    [self.notificationManager removeNotificationDelegate:delegate forTopic:topicId];
}

- (void)subscribeToTopic:(NSString *)topicId {
    [self subscribeToTopic:topicId forceSync:FORSE_SYNC];
}

- (void)subscribeToTopic:(NSString *)topicId forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.notificationManager subscribeToTopic:topicId forceSync:forceSync];
}

- (void)subscribeToTopics:(NSArray *)topicIds {
    [self subscribeToTopics:topicIds forceSync:FORSE_SYNC];
}

- (void)subscribeToTopics:(NSArray *)topicIds forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.notificationManager subscribeToTopics:topicIds forceSync:forceSync];
}

- (void)unsubscribeFromTopic:(NSString *)topicId {
    [self unsubscribeFromTopic:topicId forceSync:FORSE_SYNC];
}

- (void)unsubscribeFromTopic:(NSString *)topicId forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.notificationManager unsubscribeFromTopic:topicId forceSync:forceSync];
}

- (void)unsubscribeFromTopics:(NSArray *)topicIds {
    [self unsubscribeFromTopics:topicIds forceSync:FORSE_SYNC];
}

- (void)unsubscribeFromTopics:(NSArray *)topicIds forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.notificationManager unsubscribeFromTopics:topicIds forceSync:forceSync];
}

- (void)syncTopicsList {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.notificationManager sync];
}

- (void)setLogStorage:(id<LogStorage>)storage {
    [self.logCollector setStorage:storage];
}

- (void)setLogUploadStrategy:(id<LogUploadStrategy>)strategy {
    [self.logCollector setStrategy:strategy];
}

- (EventFamilyFactory *)getEventFamilyFactory {
    //TODO: on which stage do we need to check client's state, here or in a specific event factory?
    return self.eventFamilyFactory;
}

- (void)findEventListeners:(NSArray *)eventFQNs delegate:(id<FindEventListenersDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.eventManager findEventListeners:eventFQNs delegate:delegate];
}

- (id<KaaChannelManager>)getChannelManager {
    return self.channelManager;
}

- (SecKeyRef)getClientPrivateKey {
    return [self.clientState privateKey];
}

- (SecKeyRef)getClientPublicKey {
    return [self.clientState publicKey];
}

- (NSString *)getEndpointKeyHash {
    return [self.clientState endpointKeyHash].keyHash;
}

- (void)setEndpointAccessToken:(NSString *)token {
    [self.endpointRegistrationManager updateEndpointAccessToken:token];
}

- (NSString *)refreshEndpointAccessToken {
    return [self.endpointRegistrationManager refreshEndpointAccessToken];
}

- (NSString *)getEndpointAccessToken {
    return [self.clientState endpointAccessToken];
}

- (void)attachEndpoint:(EndpointAccessToken *)endpointAccessToken delegate:(id<OnAttachEndpointOperationDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.endpointRegistrationManager attachEndpoint:endpointAccessToken delegate:delegate];
}

- (void)detachEndpoint:(EndpointKeyHash *)endpointKeyHash delegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.endpointRegistrationManager detachEndpoint:endpointKeyHash delegate:delegate];
}

- (void)attachUser:(NSString *)userExternalId token:(NSString *)userAccessToken delegate:(id<UserAttachDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.endpointRegistrationManager attachUser:userExternalId userAccessToken:userAccessToken delegate:delegate];
}

- (void)attachUser:(NSString *)userVerifierToken
                id:(NSString *)userExternalId
             token:(NSString *)userAccessToken
          delegate:(id<UserAttachDelegate>)delegate {
    
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withError:@"Kaa client isn't started"];
    [self.endpointRegistrationManager attachUser:userVerifierToken
                                  userExternalId:userExternalId
                                 userAccessToken:userAccessToken
                                        delegate:delegate];
}

- (BOOL)isAttachedToUser {
    return [self.clientState isAttachedToUser];
}

- (void)setAttachedDelegate:(id<AttachEndpointToUserDelegate>)delegate {
    [self.endpointRegistrationManager setAttachedDelegate:delegate];
}

- (void)setDetachedDelegate:(id<DetachEndpointFromUserDelegate>)delegate {
    [self.endpointRegistrationManager setDetachedDelegate:delegate];
}

- (NSOperationQueue *)getLifeCycleExecutor {
    return [[self.context getExecutorContext] getLifeCycleExecutor];
}

- (TransportContext *)buildTransportContext:(KaaClientProperties *)properties andClientState:(id<KaaClientState>)state {
    id<BootstrapTransport> bsTransport = [self buildBootstrapTransport:properties clientState:state];
    id<ProfileTransport> pfTransport = [self buildProfileTransport:properties clientState:state];
    id<EventTransport> evTransport = [self buildEventTransport:properties clientState:state];
    id<NotificationTransport> nfTransport = [self buildNotificationTransport:properties clientState:state];
    id<ConfigurationTransport> cfTransport = [self buildConfigurationTransport:properties clientState:state];
    id<UserTransport> usrTransport = [self buildUserTransport:properties clientState:state];
    id<RedirectionTransport> redTransport = [self buildRedirectionTransport:properties clientState:state];
    id<LogTransport> logTransport = [self buildLogTransport:properties clientState:state];
    
    EndpointObjectHash *publicKeyHash = [EndpointObjectHash fromSHA1:[KeyUtils getPublicKey]];
    
    id<MetaDataTransport> mdTransport = [[DefaultMetaDataTransport alloc] init];
    [mdTransport setClientProperties:properties];
    [mdTransport setClientState:state];
    [mdTransport setEndpointPublicKeyhash:publicKeyHash];
    [mdTransport setTimeout:LONG_POLL_TIMEOUT];
    
    return [[TransportContext alloc] initWithMetaDataTransport:mdTransport
                                            bootstrapTransport:bsTransport
                                              profileTransport:pfTransport
                                                eventTransport:evTransport
                                         notificationTransport:nfTransport
                                        configurationTransport:cfTransport
                                                 userTransport:usrTransport
                                          redirectionTransport:redTransport
                                                  logTransport:logTransport];
}

- (id<KaaInternalChannelManager>)buildChannelManagerWithBootstrap:(id<BootstrapManager>)bootstrapManager
                                                       andServers:(NSDictionary *)bootstrapServers {
    id<KaaInternalChannelManager> manager = [[DefaultChannelManager alloc] initWith:bootstrapManager
                                                                   bootstrapServers:bootstrapServers
                                                                            context:[self.context getExecutorContext]];
    [manager setConnectivityChecker:[self.context createConnectivityChecker]];
    return manager;
}

- (void)initializeChannels:(id<KaaInternalChannelManager>)manager transport:(TransportContext *)context {
    DefaultBootstrapDataProcessor *btProcessor = [[DefaultBootstrapDataProcessor alloc] init];
    [btProcessor setBootstrapTransport:[context getBootstrapTransport]];
    
    DefaultOperationDataProcessor *opProcessor = [[DefaultOperationDataProcessor alloc] initWithClientState:self.clientState];
    [opProcessor setConfigurationTransport:[context getConfigurationTransport]];
    [opProcessor setEventTransport:[context getEventTransport]];
    [opProcessor setMetaDataTransport:[context getMetaDataTransport]];
    [opProcessor setNotificationTransport:[context getNotificationTransport]];
    [opProcessor setProfileTransport:[context getProfileTransport]];
    [opProcessor setRedirectionTransport:[context getRedirectionTransport]];
    [opProcessor setUserTransport:[context getUserTransport]];
    [opProcessor setLogTransport:[context getLogTransport]];
    
    id<KaaDataChannel> btChannel = [[DefaultBootstrapChannel alloc] initWithClient:self
                                                                             state:self.clientState
                                                                   failoverManager:self.failoverManager];
    [btChannel setMultiplexer:btProcessor];
    [btChannel setDemultiplexer:btProcessor];
    [manager addChannel:btChannel];
    
    id<KaaDataChannel> opChannel = [[DefaultOperationTcpChannel alloc] initWithClientState:self.clientState
                                                                            andFailoverMgr:self.failoverManager];
    [opChannel setMultiplexer:opProcessor];
    [opChannel setDemultiplexer:opProcessor];
    [manager addChannel:opChannel];
}

- (id<FailoverManager>)buildFailoverManager:(id<KaaChannelManager>)manager {
    return [[DefaultFailoverManager alloc] initWithChannelManager:manager context:[self.context getExecutorContext]];
}

- (ResyncConfigurationManager *)buildConfigurationManager:(KaaClientProperties *)properties
                                              clientState:(id<KaaClientState>)state transport:(TransportContext *)context {
    return [[ResyncConfigurationManager alloc] initWithClientProperties:properties state:state andExecutorContext:[self.context getExecutorContext]];
}

- (DefaultLogCollector *)buildLogCollector:(KaaClientProperties *)properties
                               clientState:(id<KaaClientState>)state transport:(TransportContext *)context {
    return [[DefaultLogCollector alloc] initWith:[context getLogTransport]
                                 executorContext:[self.context getExecutorContext]
                                  channelManager:self.channelManager
                                 failoverManager:self.failoverManager];
}

- (DefaultEndpointRegistrationManager *)buildRegistrationManager:(KaaClientProperties *)properties
                                                     clientState:(id<KaaClientState>)state
                                                       transport:(TransportContext *)context {
    return [[DefaultEndpointRegistrationManager alloc] initWith:state executorContext:[self.context getExecutorContext]
                                                  userTransport:[context getUserTransport]
                                               profileTransport:[context getProfileTransport]];
}

- (DefaultEventManager *)buildEventManager:(KaaClientProperties *)properties
                               clientState:(id<KaaClientState>)state transport:(TransportContext *)context {
    return [[DefaultEventManager alloc] initWith:state executorContext:[self.context getExecutorContext]
                                  eventTransport:[context getEventTransport]];
}

- (DefaultNotificationManager *)buildNotificationManager:(KaaClientProperties *)properties
                                             clientState:(id<KaaClientState>)state transport:(TransportContext *)context {
    return [[DefaultNotificationManager alloc] initWith:state executorContext:[self.context getExecutorContext]
                                  notificationTransport:[context getNotificationTransport]];
}

- (id<ProfileManager>)buildProfileManager:(KaaClientProperties *)properties
                              clientState:(id<KaaClientState>)state transport:(TransportContext *)context {
    return [[DefaultProfileManager alloc] initWith:[context getProfileTransport]];
}

- (id<BootstrapManager>)buildBootstrapManager:(KaaClientProperties *)properties
                                  clientState:(id<KaaClientState>)state transport:(TransportContext *)context {
    return [[DefaultBootstrapManager alloc] initWith:[context getBootstrapTransport]
                                     executorContext:[self.context getExecutorContext]];
}

- (AbstractHttpClient *)createHttpClientWithURL:(NSString *)url
                                     privateKey:(SecKeyRef)privateK
                                      publicKey:(SecKeyRef)publicK
                                      remoteKey:(NSData *)remoteK {
    return [self.context createHttpClient:url privateKey:privateK publicKey:publicK remoteKey:remoteK];
}

- (id<BootstrapTransport>)buildBootstrapTransport:(KaaClientProperties *)properties clientState:(id<KaaClientState>)state {
    return [[DefaultBootstrapTransport alloc] initWithToken:properties.sdkToken];
}

- (id<ProfileTransport>)buildProfileTransport:(KaaClientProperties *)properties clientState:(id<KaaClientState>)state {
    id<ProfileTransport> transport = [[DefaultProfileTransport alloc] init];
    [transport setClientProperties:properties];
    return transport;
}

- (id<ConfigurationTransport>)buildConfigurationTransport:(KaaClientProperties *)properties
                                              clientState:(id<KaaClientState>)state {
    id<ConfigurationTransport> transport = [[DefaultConfigurationTransport alloc] init];
    
    //TODO this should be part of properties and provided by user during SDK generation
    [transport setResyncOnly:YES];
    
    return transport;
}

- (id<NotificationTransport>)buildNotificationTransport:(KaaClientProperties *)properties
                                            clientState:(id<KaaClientState>)state {
    return [[DefaultNotificationTransport alloc] init];
}

- (DefaultUserTransport *)buildUserTransport:(KaaClientProperties *)properties clientState:(id<KaaClientState>)state {
    return [[DefaultUserTransport alloc] init];
}

- (id<EventTransport>)buildEventTransport:(KaaClientProperties *)properties clientState:(id<KaaClientState>)state {
    return [[DefaultEventTransport alloc] initWithState:state];
}

- (id<LogTransport>)buildLogTransport:(KaaClientProperties *)properties clientState:(id<KaaClientState>)state {
    return [[DefaultLogTransport alloc] init];
}

- (id<RedirectionTransport>)buildRedirectionTransport:(KaaClientProperties *)properties
                                          clientState:(id<KaaClientState>)state {
    return [[DefaultRedirectionTransport alloc] init];
}

- (void)checkLifecycleState:(ClientLifecycleState)expected withError:(NSString *)message {
    if (self.lifecycleState != expected) {
        [NSException raise:KaaRuntimeException format:@"%@", message];
    }
}

- (void)checkLifecycleStateNot:(ClientLifecycleState)expected withError:(NSString *)message {
    if (self.lifecycleState == expected) {
        [NSException raise:KaaRuntimeException format:@"%@", message];
    }
}

- (void)checkReadiness {
    if (!self.profileManager || ![self.profileManager isInitialized]) {
        DDLogError(@"%@ Profile manager isn't initialized: maybe profile container isn't set", TAG);
        if (self.stateDelegate) {
            NSException *exception = [NSException exceptionWithName:KaaException
                                                             reason:@"Profile manager isn't initialized: maybe profile container isn't set"
                                                           userInfo:nil];
            [self.stateDelegate onStartFailure:exception];
        } else {
            [NSException raise:KaaRuntimeException format:@"Profile manager isn't initialized: maybe profile container isn't set"];
        }
    }
}

@end
