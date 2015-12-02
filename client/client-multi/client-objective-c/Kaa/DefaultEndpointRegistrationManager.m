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

#import "DefaultEndpointRegistrationManager.h"
#import "EventDelegates.h"
#import "EndpointGen.h"
#import "UserVerifierConstants.h"
#include <stdlib.h>
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"DefaultEndpointRegistrationManager >>>"

@interface DefaultEndpointRegistrationManager ()

@property (nonatomic,strong) id<KaaClientState> state;
@property (nonatomic,strong) id<ExecutorContext> context;
@property (nonatomic,strong) volatile id<UserTransport> userTransport;
@property (nonatomic,strong) volatile id<ProfileTransport> profileTransport;

@property (nonatomic,weak) id<AttachEndpointToUserDelegate> attachEndpointToUserDelegate;
@property (nonatomic,weak) id<DetachEndpointFromUserDelegate> detachEndpointFromUserDelegate;

@property (nonatomic,strong) UserAttachRequest *userAttachRequest;
@property (nonatomic,weak) id<UserAttachDelegate> userAttachDelegate;

@property (nonatomic,strong) NSMutableDictionary *endpointAttachDelegates;
@property (nonatomic,strong) NSMutableDictionary *endpointDetachDelegates;

@property (nonatomic,strong) NSMutableDictionary *attachEndpointRequests;
@property (nonatomic,strong) NSMutableDictionary *detachEndpointRequests;

- (void)onEndpointAccessTokenChanged;
- (void)notifyAttachedDelegate:(SyncResponseResultType)result
                      delegate:(id<OnAttachEndpointOperationDelegate>)delegate
                       keyHash:(EndpointKeyHash *)keyHash;
- (void)notifyDetachedDelegate:(SyncResponseResultType)result
                      delegate:(id<OnDetachEndpointOperationDelegate>)delegate;
- (int)getRandomInteger;

@end

@implementation DefaultEndpointRegistrationManager

- (instancetype)initWith:(id<KaaClientState>)state
         executorContext:(id<ExecutorContext>)context
           userTransport:(id<UserTransport>)userTransport
        profileTransport:(id<ProfileTransport>)profileTransport {
    self = [super init];
    if (self) {
        self.state = state;
        self.context = context;
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

- (void)attachEndpoint:(EndpointAccessToken *)accessToken delegate:(id<OnAttachEndpointOperationDelegate>)delegate {
    NSNumber *requestId = [NSNumber numberWithInt:[self getRandomInteger]];
    DDLogInfo(@"%@ Going to attach Endpoint by access token: %@", TAG, accessToken);
    @synchronized (self.attachEndpointRequests) {
        [self.attachEndpointRequests setObject:accessToken forKey:requestId];
    }
    if (delegate) {
        @synchronized (self.endpointAttachDelegates) {
            [self.endpointAttachDelegates setObject:delegate forKey:requestId];
        }
    }
    if (self.userTransport) {
        [self.userTransport sync];
    }
}

- (void)detachEndpoint:(EndpointKeyHash *)keyHash delegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    NSNumber *requestId = [NSNumber numberWithInt:[self getRandomInteger]];
    DDLogInfo(@"%@ Going to detach Endpoint by endpoint key hash: %@", TAG, keyHash);
    @synchronized (self.detachEndpointRequests) {
        [self.detachEndpointRequests setObject:keyHash forKey:requestId];
    }
    if (delegate) {
        @synchronized (self.endpointDetachDelegates) {
            [self.endpointDetachDelegates setObject:delegate forKey:requestId];
        }
    }
    if (self.userTransport) {
        [self.userTransport sync];
    }
}

- (void)attachUser:(NSString *)userExternalId userAccessToken:(NSString *)token delegate:(id<UserAttachDelegate>)delegate {
    if (DEFAULT_USER_VERIFIER_TOKEN != nil) {
        [self attachUser:DEFAULT_USER_VERIFIER_TOKEN userExternalId:userExternalId userAccessToken:token delegate:delegate];
    } else {
        [NSException raise:KaaIllegalStateException format:@"Default user verifier was not defined during SDK generation process!"];
    }
}

- (void)attachUser:(NSString *)userVerifierToken
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

- (void)onUpdate:(NSArray *)attachResponses detachResponses:(NSArray *)detachResponses
    userResponse:(UserAttachResponse *)userResponse
userAttachNotification:(UserAttachNotification *)attachNotification
userDetachNotification:(UserDetachNotification *)detachNotification {
    
    if (userResponse) {
        if (self.userAttachDelegate) {
            __block id<UserAttachDelegate> delegate = self.userAttachDelegate;
            __weak typeof(self)weakSelf = self;
            [[self.context getCallbackExecutor] addOperationWithBlock:^{
                [delegate onAttachResult:userResponse];
            }];
            weakSelf.userAttachDelegate = nil;
        }
        if (userResponse.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
            [self.state setIsAttachedToUser:YES];
            if (self.attachEndpointToUserDelegate && self.userAttachRequest) {
                __block UserAttachRequest *request = self.userAttachRequest;
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
                id<OnAttachEndpointOperationDelegate> delegate = [self.endpointAttachDelegates objectForKey: [NSNumber numberWithInt:attached.requestId]];
                [self.endpointAttachDelegates removeObjectForKey:[NSNumber numberWithInt:attached.requestId]];
                EndpointKeyHash *keyHash = [[EndpointKeyHash alloc] initWithKeyHash:attached.endpointKeyHash.data];
                [self notifyAttachedDelegate:attached.result delegate:delegate keyHash:keyHash];
            }
            @synchronized (self.attachEndpointRequests) {
                [self.attachEndpointRequests removeObjectForKey:[NSNumber numberWithInt:attached.requestId]];
            }
        }
    }
    
    if (detachResponses && [detachResponses count] > 0) {
        for (EndpointDetachResponse *detached in detachResponses) {
            @synchronized (self.endpointDetachDelegates) {
                NSNumber *requestId = [NSNumber numberWithInt:detached.requestId];
                id<OnDetachEndpointOperationDelegate> delegate = [self.endpointDetachDelegates objectForKey: requestId];
                [self.endpointDetachDelegates removeObjectForKey:[NSNumber numberWithInt:detached.requestId]];
                [self notifyDetachedDelegate:detached.result delegate:delegate];
            }
            EndpointKeyHash *keyHash = nil;
            @synchronized (self.detachEndpointRequests) {
                keyHash = [self.detachEndpointRequests objectForKey:[NSNumber numberWithInt:detached.requestId]];
                [self.detachEndpointRequests removeObjectForKey:[NSNumber numberWithInt:detached.requestId]];
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
                [weakSelf.detachEndpointFromUserDelegate onDetachedFromUser:detachNotification.endpointAccessToken];
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

- (void)setAttachedDelegate:(id<AttachEndpointToUserDelegate>)delegate {
    self.attachEndpointToUserDelegate = delegate;
}

- (void)setDetachedDelegate:(id<DetachEndpointFromUserDelegate>)delegate {
    self.detachEndpointFromUserDelegate = delegate;
}

- (void)notifyAttachedDelegate:(SyncResponseResultType)result
                      delegate:(id<OnAttachEndpointOperationDelegate>)delegate
                       keyHash:(EndpointKeyHash *)keyHash {
    if (delegate) {
        [[self.context getCallbackExecutor] addOperationWithBlock:^{
            [delegate onAttach:result resultContext:keyHash];
        }];
    }
}

- (void)notifyDetachedDelegate:(SyncResponseResultType)result delegate:(id<OnDetachEndpointOperationDelegate>)delegate {
    if (delegate) {
        [[self.context getCallbackExecutor] addOperationWithBlock:^{
            [delegate onDetach:result];
        }];
    }
}

- (void)onEndpointAccessTokenChanged {
    if (self.profileTransport) {
        [self.profileTransport sync];
    }
}

- (int)getRandomInteger {
    @synchronized (self) {
        return arc4random();
    }
}

@end
