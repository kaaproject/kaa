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
#import "FailureDelegate.h"

#define TAG @"AbstractKaaClient >>>"
#define LONG_POLL_TIMEOUT 60000

@interface AbstractKaaClient () <FailureDelegate>

@property (nonatomic, strong) DefaultNotificationManager *notificationManager;
@property (nonatomic, strong) id<ProfileManager> profileManager;

@property (nonatomic, strong) KaaClientProperties *properties;
@property (nonatomic, strong) id<KaaClientState> clientState;
@property (nonatomic, strong) id<BootstrapManager> bootstrapManager;
@property (nonatomic, strong) id<EventManager> eventManager;
@property (nonatomic, strong) EventFamilyFactory *eventFamilyFactory;
@property (nonatomic, strong) DefaultEndpointRegistrationManager *endpointRegistrationManager;
@property (nonatomic, strong) id<KaaInternalChannelManager> channelManager;
@property (nonatomic, strong) id<FailoverManager> failoverManager;
@property (nonatomic, weak) id<FailureDelegate> failureDelegate;

- (NSOperationQueue *)getLifeCycleExecutor;
- (void)checkReadiness;

@end

@implementation AbstractKaaClient

- (instancetype)initWithPlatformContext:(id<KaaClientPlatformContext>)context delegate:(id<KaaClientStateDelegate>)delegate {
    self = [super init];
    if (self) {
        self.context = context;
        self.stateDelegate = delegate;
        self.lifecycleState = CLIENT_LIFECYCLE_STATE_CREATED;
        self.properties = [self.context getProperties];
        self.failureDelegate = self;
        if (![self.context getProperties]) {
            self.properties = [[KaaClientProperties alloc] initDefaultsWithBase64:[self.context getBase64]];
        }
        
        NSDictionary *bootstrapServers = [self.properties bootstrapServers];
        if ([bootstrapServers count] == 0) {
            [NSException raise:NSInternalInconsistencyException format:@"Unable to obtain list of bootstrap servers"];
        }
        
        for (NSMutableArray *serverList in bootstrapServers.allValues) {
            [serverList shuffle];
        }
        
        self.clientState = [[KaaClientPropertiesState alloc] initWithBase64:[context getBase64] clientProperties:self.properties];
        
        TransportContext *transportContext = [self buildTransportContextWithProperties:self.properties clientState:self.clientState];
        
        self.bootstrapManager = [self buildBootstrapManagerWithTransportContext:transportContext];
        self.channelManager = [self buildChannelManagerWithBootstrapManager:self.bootstrapManager servers:bootstrapServers];
        self.failoverManager = [self buildFailoverManagerWithChannelManager:self.channelManager];
        [self.channelManager setFailoverManager:self.failoverManager];
        
        [self initializeChannelsForManager:self.channelManager withTransportContext:transportContext];
        
        [self.bootstrapManager setChannelManager:self.channelManager];
        [self.bootstrapManager setFailoverManager:self.failoverManager];
        
        self.profileManager = [self buildProfileManagerWithTransportContext:transportContext];
        self.notificationManager = [self buildNotificationManagerWithClientState:self.clientState
                                                                transportContext:transportContext];
        self.eventManager = [self buildEventManagerWithClientState:self.clientState
                                                  transportContext:transportContext];
        self.endpointRegistrationManager = [self buildRegistrationManagerWithClientState:self.clientState
                                                                        transportContext:transportContext];
        self.logCollector = [self buildLogCollectorWithTransportContext:transportContext];
        self.configurationManager = [self buildConfigurationManagerWithProperties:self.properties
                                                                      clientState:self.clientState];
        
        [[transportContext getRedirectionTransport] setBootstrapManager:self.bootstrapManager];
        [[transportContext getBootstrapTransport] setBootstrapManager:self.bootstrapManager];
        [[transportContext getProfileTransport] setProfileManager:self.profileManager];
        [[transportContext getEventTransport] setEventManager:self.eventManager];
        [[transportContext getNotificationTransport] setNotificationProcessor:self.notificationManager];
        [[transportContext getConfigurationTransport] setConfigurationHashContainer:self.configurationManager];
        [[transportContext getConfigurationTransport] setConfigurationProcessor:self.configurationManager];
        [[transportContext getUserTransport] setEndpointRegistrationProcessor:self.endpointRegistrationManager];
        [[transportContext getLogTransport] setLogProcessor:self.logCollector];
        [transportContext initTransportsWithChannelManager:self.channelManager state:self.clientState];
        
        self.eventFamilyFactory = [[EventFamilyFactory alloc]
                                   initWithManager:self.eventManager
                                   executorContext:[self.context getExecutorContext]];
    }
    return self;
}

- (void)start {
    [self checkIfClientNotInLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client is already started"];
    [self checkIfClientNotInLifecycleState:CLIENT_LIFECYCLE_STATE_PAUSED withErrorMessage:@"Kaa client is paused, need to be resumed"];
    
    [self setLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED];
    
    [self checkReadiness];
    
    [[self.context getExecutorContext] initiate];
    __weak typeof(self)weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        DDLogDebug(@"%@ Client startup initiated", TAG);
        @try {
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
                [weakSelf.stateDelegate onStartFailureWithException:exception];
            }
        }
    }];
}

- (void)stop {
    [self checkIfClientNotInLifecycleState:CLIENT_LIFECYCLE_STATE_CREATED withErrorMessage:@"Kaa client is not started"];
    [self checkIfClientNotInLifecycleState:CLIENT_LIFECYCLE_STATE_STOPPED withErrorMessage:@"Kaa client is already stopped"];
    
    [self setLifecycleState:CLIENT_LIFECYCLE_STATE_STOPPED];
    
    __weak typeof(self) weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        @try {
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
                [weakSelf.stateDelegate onStopFailureWithException:exception];
            }
        }
        @finally {
            [[weakSelf.context getExecutorContext] stop];
        }
    }];
    
}

- (void)pause {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED
                    withErrorMessage:[NSString stringWithFormat:@"Kaa client is not started: %i is current state", self.lifecycleState]];
    
    [self setLifecycleState:CLIENT_LIFECYCLE_STATE_PAUSED];
    
    __weak typeof(self) weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        @try {
            [weakSelf.clientState persist];
            
            [weakSelf.channelManager pause];
            
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onPaused];
            }
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Pause failed: %@. Reason: %@", TAG, exception.name, exception.reason);
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onPauseFailureWithException:exception];
            }
        }
    }];
}

- (void)resume {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_PAUSED withErrorMessage:@"Kaa client isn't paused"];
    
    [self setLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED];
    
    __weak typeof(self) weakSelf = self;
    [[self getLifeCycleExecutor] addOperationWithBlock:^{
        @try {
            [weakSelf.channelManager resume];
            
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onResume];
            }
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Resume failed: %@. Reason: %@", TAG, exception.name, exception.reason);
            if (weakSelf.stateDelegate) {
                [weakSelf.stateDelegate onResumeFailureWithException:exception];
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
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
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
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
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

- (void)addNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId {
    [self.notificationManager addNotificationDelegate:delegate forTopicId:topicId];
}

- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate {
    [self.notificationManager removeNotificationDelegate:delegate];
}

- (void)removeNotificationDelegate:(id<NotificationDelegate>)delegate forTopicId:(int64_t)topicId {
    [self.notificationManager removeNotificationDelegate:delegate forTopicId:topicId];
}

- (void)subscribeToTopicWithId:(int64_t)topicId {
    [self subscribeToTopicWithId:topicId forceSync:FORSE_SYNC];
}

- (void)subscribeToTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.notificationManager subscribeToTopicWithId:topicId forceSync:forceSync];
}

- (void)subscribeToTopicsWithIDs:(NSArray *)topicIds {
    [self subscribeToTopicsWithIDs:topicIds forceSync:FORSE_SYNC];
}

- (void)subscribeToTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.notificationManager subscribeToTopicsWithIDs:topicIds forceSync:forceSync];
}

- (void)unsubscribeFromTopicWithId:(int64_t)topicId {
    [self unsubscribeFromTopicWithId:topicId forceSync:FORSE_SYNC];
}

- (void)unsubscribeFromTopicWithId:(int64_t)topicId forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.notificationManager unsubscribeFromTopicWithId:topicId forceSync:forceSync];
}

- (void)unsubscribeFromTopicsWithIDs:(NSArray *)topicIds {
    [self unsubscribeFromTopicsWithIDs:topicIds forceSync:FORSE_SYNC];
}

- (void)unsubscribeFromTopicsWithIDs:(NSArray *)topicIds forceSync:(BOOL)forceSync {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.notificationManager unsubscribeFromTopicsWithIDs:topicIds forceSync:forceSync];
}

- (void)syncTopicsList {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
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

- (void)findListenersForEventFQNs:(NSArray *)eventFQNs delegate:(id<FindEventListenersDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.eventManager requestListenersForEventFQNs:eventFQNs delegate:delegate];
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

- (void)attachEndpointWithAccessToken:(EndpointAccessToken *)endpointAccessToken delegate:(id<OnAttachEndpointOperationDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.endpointRegistrationManager attachEndpointWithAccessToken:endpointAccessToken delegate:delegate];
}

- (void)detachEndpointWithKeyHash:(EndpointKeyHash *)endpointKeyHash delegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.endpointRegistrationManager detachEndpointWithKeyHash:endpointKeyHash delegate:delegate];
}

- (void)attachUserWithId:(NSString *)userExternalId accessToken:(NSString *)userAccessToken delegate:(id<UserAttachDelegate>)delegate {
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.endpointRegistrationManager attachUserWithId:userExternalId userAccessToken:userAccessToken delegate:delegate];
}

- (void)attachUserWithVerifierToken:(NSString *)userVerifierToken
                             userId:(NSString *)userExternalId
                        accessToken:(NSString *)userAccessToken
                           delegate:(id<UserAttachDelegate>)delegate {
    
    [self checkLifecycleState:CLIENT_LIFECYCLE_STATE_STARTED withErrorMessage:@"Kaa client isn't started"];
    [self.endpointRegistrationManager attachUserWithVerifierToken:userVerifierToken
                                                   userExternalId:userExternalId
                                                  userAccessToken:userAccessToken
                                                         delegate:delegate];
}

- (BOOL)isAttachedToUser {
    return [self.clientState isAttachedToUser];
}

- (void)setAttachDelegate:(id<AttachEndpointToUserDelegate>)delegate {
    [self.endpointRegistrationManager setAttachDelegate:delegate];
}

- (void)setDetachDelegate:(id<DetachEndpointFromUserDelegate>)delegate {
    [self.endpointRegistrationManager setDetachDelegate:delegate];
}

- (void)setFailoverStrategy:(id<FailoverStrategy>)failoverStrategy {
    [self.failoverManager setFailoverStrategy:failoverStrategy];
}

- (void)setFailureDelegate:(id<FailureDelegate>)failureDelegate {
    if (failureDelegate == nil) {
        [NSException raise:NSInvalidArgumentException format:@"Failure delegate is nil"];
    }
    
    _failureDelegate = failureDelegate;
}

- (void)onFailure {
    [self stop];
}

- (NSOperationQueue *)getLifeCycleExecutor {
    return [[self.context getExecutorContext] getLifeCycleExecutor];
}

- (TransportContext *)buildTransportContextWithProperties:(KaaClientProperties *)properties clientState:(id<KaaClientState>)state {
    id<BootstrapTransport> bsTransport = [self buildBootstrapTransportWithProperties:properties];
    id<ProfileTransport> pfTransport = [self buildProfileTransportWithProperties:properties];
    id<EventTransport> evTransport = [self buildEventTransportWithClientState:state];
    id<NotificationTransport> nfTransport = [self buildNotificationTransport];
    id<ConfigurationTransport> cfTransport = [self buildConfigurationTransport];
    id<UserTransport> usrTransport = [self buildUserTransport];
    id<RedirectionTransport> redTransport = [self buildRedirectionTransport];
    id<LogTransport> logTransport = [self buildLogTransport];
    
    EndpointObjectHash *publicKeyHash = [EndpointObjectHash hashWithSHA1:[state publicKeyAsBytes]];
    
    id<MetaDataTransport> mdTransport = [[DefaultMetaDataTransport alloc] init];
    [mdTransport setClientProperties:properties];
    [mdTransport setClientState:state];
    [mdTransport setEndpointPublicKeyHash:publicKeyHash];
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

- (id<KaaInternalChannelManager>)buildChannelManagerWithBootstrapManager:(id<BootstrapManager>)bootstrapManager
                                                                 servers:(NSDictionary *)bootstrapServers {
    id<KaaInternalChannelManager> manager = [[DefaultChannelManager alloc] initWithBootstrapManager:bootstrapManager
                                                                                   bootstrapServers:bootstrapServers
                                                                                            context:[self.context getExecutorContext]
                                                                                    failureDelegate:self.failureDelegate];
    [manager setConnectivityChecker:[self.context createConnectivityChecker]];
    return manager;
}

- (void)initializeChannelsForManager:(id<KaaInternalChannelManager>)manager withTransportContext:(TransportContext *)context {
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
                                                                           failoverManager:self.failoverManager
                                                                           failureDelegate:self.failureDelegate];
    [opChannel setMultiplexer:opProcessor];
    [opChannel setDemultiplexer:opProcessor];
    [manager addChannel:opChannel];
}

- (id<FailoverManager>)buildFailoverManagerWithChannelManager:(id<KaaChannelManager>)manager {
    return [[DefaultFailoverManager alloc] initWithChannelManager:manager context:[self.context getExecutorContext]];
}

- (ResyncConfigurationManager *)buildConfigurationManagerWithProperties:(KaaClientProperties *)properties
                                                            clientState:(id<KaaClientState>)state {
    return [[ResyncConfigurationManager alloc] initWithClientProperties:properties state:state executorContext:[self.context getExecutorContext]];
}

- (DefaultLogCollector *)buildLogCollectorWithTransportContext:(TransportContext *)context {
    return [[DefaultLogCollector alloc] initWithTransport:[context getLogTransport]
                                          executorContext:[self.context getExecutorContext]
                                           channelManager:self.channelManager
                                          failoverManager:self.failoverManager];
}

- (DefaultEndpointRegistrationManager *)buildRegistrationManagerWithClientState:(id<KaaClientState>)state
                                                              transportContext:(TransportContext *)context {
    return [[DefaultEndpointRegistrationManager alloc] initWithState:state executorContext:[self.context getExecutorContext]
                                                       userTransport:[context getUserTransport]
                                                    profileTransport:[context getProfileTransport]];
}

- (DefaultEventManager *)buildEventManagerWithClientState:(id<KaaClientState>)state
                                        transportContext:(TransportContext *)context {
    return [[DefaultEventManager alloc] initWithState:state
                                      executorContext:[self.context getExecutorContext]
                                       eventTransport:[context getEventTransport]];
}

- (DefaultNotificationManager *)buildNotificationManagerWithClientState:(id<KaaClientState>)state
                                                      transportContext:(TransportContext *)context {
    return [[DefaultNotificationManager alloc] initWithState:state
                                             executorContext:[self.context getExecutorContext]
                                       notificationTransport:[context getNotificationTransport]];
}

- (id<ProfileManager>)buildProfileManagerWithTransportContext:(TransportContext *)context {
    return [[DefaultProfileManager alloc] initWithTransport:[context getProfileTransport]];
}

- (id<BootstrapManager>)buildBootstrapManagerWithTransportContext:(TransportContext *)context {
    return [[DefaultBootstrapManager alloc] initWithTransport:[context getBootstrapTransport]
                                              executorContext:[self.context getExecutorContext]
                                              failureDelegate:self.failureDelegate];
}

- (AbstractHttpClient *)createHttpClientWithURLString:(NSString *)url
                                        privateKeyRef:(SecKeyRef)privateK
                                         publicKeyRef:(SecKeyRef)publicK
                                            remoteKey:(NSData *)remoteK {
    return [self.context createHttpClientWithURLString:url privateKeyRef:privateK publicKeyRef:publicK remoteKey:remoteK];
}

- (id<BootstrapTransport>)buildBootstrapTransportWithProperties:(KaaClientProperties *)properties {
    return [[DefaultBootstrapTransport alloc] initWithToken:properties.sdkToken];
}

- (id<ProfileTransport>)buildProfileTransportWithProperties:(KaaClientProperties *)properties {
    id<ProfileTransport> transport = [[DefaultProfileTransport alloc] init];
    [transport setClientProperties:properties];
    return transport;
}

- (id<ConfigurationTransport>)buildConfigurationTransport {
    id<ConfigurationTransport> transport = [[DefaultConfigurationTransport alloc] init];
    
    //TODO: this should be part of properties and provided by user during SDK generation
    [transport setResyncOnly:YES];
    
    return transport;
}

- (id<NotificationTransport>)buildNotificationTransport {
    return [[DefaultNotificationTransport alloc] init];
}

- (DefaultUserTransport *)buildUserTransport {
    return [[DefaultUserTransport alloc] init];
}

- (id<EventTransport>)buildEventTransportWithClientState:(id<KaaClientState>)state {
    return [[DefaultEventTransport alloc] initWithState:state];
}

- (id<LogTransport>)buildLogTransport {
    return [[DefaultLogTransport alloc] init];
}

- (id<RedirectionTransport>)buildRedirectionTransport {
    return [[DefaultRedirectionTransport alloc] init];
}

- (void)checkLifecycleState:(ClientLifecycleState)expected withErrorMessage:(NSString *)message {
    if (self.lifecycleState != expected) {
        [NSException raise:KaaRuntimeException format:@"%@", message];
    }
}

- (void)checkIfClientNotInLifecycleState:(ClientLifecycleState)expected withErrorMessage:(NSString *)message {
    if (self.lifecycleState == expected) {
        [NSException raise:KaaRuntimeException format:@"%@", message];
    }
}

- (void)checkReadiness {
    if (!self.profileManager || ![self.profileManager isInitialized]) {
        NSString *errorMessage = @"Profile manager isn't initialized: maybe profile container isn't set";
        NSException *exception = [NSException exceptionWithName:KaaRuntimeException reason:errorMessage userInfo:nil];
        DDLogError(@"%@ %@", TAG, errorMessage);
        if (self.stateDelegate) {
            [self.stateDelegate onStartFailureWithException:exception];
        } else {
            [exception raise];
        }
    }
}

@end
