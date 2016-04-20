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

#import "DefaultEndpointRegistrationManager.h"
#import "EventDelegates.h"
#import "EndpointGen.h"
#import "UserVerifierConstants.h"
#include <stdlib.h>
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"DefaultEndpointRegistrationManager >>>"

@interface DefaultEndpointRegistrationManager ()

@property (nonatomic, strong) id<KaaClientState> state;
@property (nonatomic, strong) id<ExecutorContext> context;
@property (nonatomic, strong) volatile id<UserTransport> userTransport;
@property (nonatomic, strong) volatile id<ProfileTransport> profileTransport;

@property (nonatomic, weak) id<AttachEndpointToUserDelegate> attachEndpointToUserDelegate;
@property (nonatomic, weak) id<DetachEndpointFromUserDelegate> detachEndpointFromUserDelegate;

@property (nonatomic, strong) UserAttachRequest *userAttachRequest;
@property (nonatomic, weak) id<UserAttachDelegate> userAttachDelegate;

@property (nonatomic, strong) NSMutableDictionary *endpointAttachDelegates;
@property (nonatomic, strong) NSMutableDictionary *endpointDetachDelegates;

@property (nonatomic, strong) NSMutableDictionary *attachEndpointRequests;
@property (nonatomic, strong) NSMutableDictionary *detachEndpointRequests;

@property (nonatomic) int attachRequestId;
@property (nonatomic) int detachRequestId;

- (void)onEndpointAccessTokenChanged;
- (void)notifyAttachDelegateWithResult:(SyncResponseResultType)result
                              delegate:(id<OnAttachEndpointOperationDelegate>)delegate
                               keyHash:(EndpointKeyHash *)keyHash;
- (void)notifyDetachDelegateWithResult:(SyncResponseResultType)result
                              delegate:(id<OnDetachEndpointOperationDelegate>)delegate;
- (void)addDelegate:(id)delegate forRequestId:(NSNumber *)requestId;

@end

@implementation DefaultEndpointRegistrationManager

- (instancetype)initWithState:(id<KaaClientState>)state
              executorContext:(id<ExecutorContext>)context
                userTransport:(id<UserTransport>)userTransport
             profileTransport:(id<ProfileTransport>)profileTransport {
    self = [super init];
    if (self) {
        self.state = state;
        self.context = context;
        self.attachRequestId = 0;
        self.detachRequestId = 0;
        self.userTransport = userTransport;
        self.profileTransport = profileTransport;
        
        self.endpointAttachDelegates = [NSMutableDictionary dictionary];
        self.endpointDetachDelegates = [NSMutableDictionary dictionary];
        self.attachEndpointRequests = [NSMutableDictionary dictionary];
        self.detachEndpointRequests = [NSMutableDictionary dictionary];
        
        NSString *endpointAccessToken = [self.state endpointAccessToken];
        if (!endpointAccessToken || endpointAccessToken.length == 0) {
            [self.state refreshEndpointAccessToken];
        }
    }
    return self;
}

- (void)updateEndpointAccessToken:(NSString *)token {
    [self.state setEndpointAccessToken:token];
    [self onEndpointAccessTokenChanged];
}

- (NSString *)refreshEndpointAccessToken {
    NSString *newEndpointAccessToken = [self.state refreshEndpointAccessToken];
    DDLogInfo(@"%@ New endpoint access token is generated: %@", TAG, newEndpointAccessToken);
    [self onEndpointAccessTokenChanged];
    return newEndpointAccessToken;
}

- (NSDictionary *)getAttachedEndpointList {
    return [self.state attachedEndpoints];
}

- (void)attachEndpointWithAccessToken:(EndpointAccessToken *)accessToken delegate:(id<OnAttachEndpointOperationDelegate>)delegate {
    NSNumber *requestId = @(self.attachRequestId++);
    DDLogInfo(@"%@ Going to attach Endpoint by access token: %@", TAG, accessToken);
    @synchronized (self.attachEndpointRequests) {
        self.attachEndpointRequests[requestId] = accessToken;
    }
    [self addDelegate:delegate forRequestId:requestId];
}

- (void)detachEndpointWithKeyHash:(EndpointKeyHash *)keyHash delegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    NSNumber *requestId = @(self.detachRequestId++);
    DDLogInfo(@"%@ Going to detach Endpoint by endpoint key hash: %@", TAG, keyHash);
    @synchronized (self.detachEndpointRequests) {
        self.detachEndpointRequests[requestId] = keyHash;
    }
    [self addDelegate:delegate forRequestId:requestId];
}

- (void)addDelegate:(id)delegate forRequestId:(NSNumber *)requestId  {
    if (delegate && [delegate conformsToProtocol:@protocol(OnAttachEndpointOperationDelegate)]) {
        @synchronized (self.endpointAttachDelegates) {
            self.endpointAttachDelegates[requestId] = delegate;
        }
    } else if (delegate && [delegate conformsToProtocol:@protocol(OnDetachEndpointOperationDelegate)]) {
        @synchronized (self.endpointDetachDelegates) {
            self.endpointDetachDelegates[requestId] = delegate;
        }
    }
    if (self.userTransport) {
        [self.userTransport sync];
    }
}

- (void)attachUserWithId:(NSString *)userExternalId userAccessToken:(NSString *)token delegate:(id<UserAttachDelegate>)delegate {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunreachable-code"
    if (DEFAULT_USER_VERIFIER_TOKEN != nil) {
        [self attachUserWithVerifierToken:DEFAULT_USER_VERIFIER_TOKEN userExternalId:userExternalId userAccessToken:token delegate:delegate];
    } else {
        [NSException raise:KaaIllegalStateException format:@"Default user verifier was not defined during SDK generation process!"];
    }
#pragma clang diagnostic pop
}

- (void)attachUserWithVerifierToken:(NSString *)userVerifierToken
                     userExternalId:(NSString *)externalId
                    userAccessToken:(NSString *)token
                           delegate:(id<UserAttachDelegate>)delegate {
    self.userAttachRequest = [[UserAttachRequest alloc] init];
    self.userAttachRequest.userVerifierId = userVerifierToken;
    self.userAttachRequest.userExternalId = externalId;
    self.userAttachRequest.userAccessToken = token;
    
    self.userAttachDelegate = delegate;
    if (self.userTransport) {
        [self.userTransport sync];
    }
}

- (void)onUpdateWithAttachResponses:(NSArray *)attachResponses
                    detachResponses:(NSArray *)detachResponses
                       userResponse:(UserAttachResponse *)userResponse
             userAttachNotification:(UserAttachNotification *)attachNotification
             userDetachNotification:(UserDetachNotification *)detachNotification {
    
    if (userResponse) {
        if (self.userAttachDelegate) {
            id<UserAttachDelegate> delegate = self.userAttachDelegate;
            [[self.context getCallbackExecutor] addOperationWithBlock:^{
                [delegate onAttachResult:userResponse];
            }];
            self.userAttachDelegate = nil;
        }
        if (userResponse.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
            [self.state setIsAttachedToUser:YES];
            if (self.attachEndpointToUserDelegate && self.userAttachRequest) {
                UserAttachRequest *request = self.userAttachRequest;
                __weak typeof(self)weakSelf = self;
                [[self.context getCallbackExecutor] addOperationWithBlock:^{
                    [weakSelf.attachEndpointToUserDelegate onAttachedToUser:request.userExternalId token:[weakSelf.state endpointAccessToken]];
                }];
            }
        }
        self.userAttachRequest = nil;
    }
    
    if (attachResponses && [attachResponses count] > 0) {
        for (EndpointAttachResponse *attached in attachResponses) {
            @synchronized (self.endpointAttachDelegates) {
                id<OnAttachEndpointOperationDelegate> delegate = self.endpointAttachDelegates[@(attached.requestId)];
                [self.endpointAttachDelegates removeObjectForKey:@(attached.requestId)];
                EndpointKeyHash *keyHash = [[EndpointKeyHash alloc] initWithKeyHash:attached.endpointKeyHash.data];
                [self notifyAttachDelegateWithResult:attached.result delegate:delegate keyHash:keyHash];
            }
            @synchronized (self.attachEndpointRequests) {
                [self.attachEndpointRequests removeObjectForKey:@(attached.requestId)];
            }
        }
    }
    
    if (detachResponses && [detachResponses count] > 0) {
        for (EndpointDetachResponse *detached in detachResponses) {
            @synchronized (self.endpointDetachDelegates) {
                NSNumber *requestId = @(detached.requestId);
                id<OnDetachEndpointOperationDelegate> delegate = self.endpointDetachDelegates[requestId];
                [self.endpointDetachDelegates removeObjectForKey:@(detached.requestId)];
                [self notifyDetachDelegateWithResult:detached.result delegate:delegate];
            }
            EndpointKeyHash *keyHash = nil;
            @synchronized (self.detachEndpointRequests) {
                keyHash = self.detachEndpointRequests[@(detached.requestId)];
                [self.detachEndpointRequests removeObjectForKey:@(detached.requestId)];
            }
            if (keyHash && detached.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
                if ([keyHash isEqual:[self.state endpointKeyHash]]) {
                    [self.state setIsAttachedToUser:NO];
                }
            }
        }
    }
    if (attachNotification) {
        [self.state setIsAttachedToUser:YES];
        if (self.attachEndpointToUserDelegate) {
            __weak typeof(self)weakSelf = self;
            [[self.context getCallbackExecutor] addOperationWithBlock:^{
                [weakSelf.attachEndpointToUserDelegate onAttachedToUser:attachNotification.userExternalId
                                                                  token:attachNotification.endpointAccessToken];
            }];
        }
    }
    if (detachNotification) {
        [self.state setIsAttachedToUser:NO];
        if (self.detachEndpointFromUserDelegate) {
            __weak typeof(self)weakSelf = self;
            [[self.context getCallbackExecutor] addOperationWithBlock:^{
                [weakSelf.detachEndpointFromUserDelegate onDetachedEndpointWithAccessToken:detachNotification.endpointAccessToken];
            }];
        }
    }
}

- (NSDictionary *)getAttachEndpointRequests {
    @synchronized (self.attachEndpointRequests) {
        return [NSMutableDictionary dictionaryWithDictionary:self.attachEndpointRequests];
    }
}

- (NSDictionary *)getDetachEndpointRequests {
    @synchronized (self.detachEndpointRequests) {
        return [NSMutableDictionary dictionaryWithDictionary:self.detachEndpointRequests];
    }
}

- (UserAttachRequest *)getUserAttachRequest {
    return _userAttachRequest;
}

- (BOOL)isAttachedToUser {
    return [self.state isAttachedToUser];
}

- (void)setAttachDelegate:(id<AttachEndpointToUserDelegate>)delegate {
    self.attachEndpointToUserDelegate = delegate;
}

- (void)setDetachDelegate:(id<DetachEndpointFromUserDelegate>)delegate {
    self.detachEndpointFromUserDelegate = delegate;
}

- (void)notifyAttachDelegateWithResult:(SyncResponseResultType)result
                                delegate:(id<OnAttachEndpointOperationDelegate>)delegate
                                 keyHash:(EndpointKeyHash *)keyHash {
    if (delegate) {
        [[self.context getCallbackExecutor] addOperationWithBlock:^{
            [delegate onAttachResult:result withEndpointKeyHash:keyHash];
        }];
    }
}

- (void)notifyDetachDelegateWithResult:(SyncResponseResultType)result delegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    if (delegate) {
        [[self.context getCallbackExecutor] addOperationWithBlock:^{
            [delegate onDetachResult:result];
        }];
    }
}

- (void)onEndpointAccessTokenChanged {
    if (self.profileTransport) {
        [self.profileTransport sync];
    }
}

@end
