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

#import "DefaultUserTransport.h"
#import "EndpointRegistrationProcessor.h"
#import "EndpointAccessToken.h"
#import "KaaLogging.h"

#define TAG @"DefaultUserTransport >>>"

@interface DefaultUserTransport ()

@property (nonatomic, strong) id<EndpointRegistrationProcessor> processor;
@property (nonatomic, strong) NSMutableDictionary *attachedEndpoints;

@end

@implementation DefaultUserTransport

- (instancetype)init {
    self = [super init];
    if (self) {
        self.attachedEndpoints = [NSMutableDictionary dictionary];
    }
    return self;
}

- (UserSyncRequest *)createUserRequest {
    if (!self.processor) {
        DDLogWarn(@"%@ Unable to create UserSyncRequest - no EndpointRegistrationProcessor specified!", TAG);
        return nil;
    }
    
    NSDictionary *attachEndpointRequests = [self.processor getAttachEndpointRequests];
    NSMutableArray *attachEPRequestList = [NSMutableArray array];
    for (NSNumber *key in attachEndpointRequests.allKeys) {
        EndpointAttachRequest *attachRequest = [[EndpointAttachRequest alloc] init];
        attachRequest.requestId = [key intValue];
        attachRequest.endpointAccessToken = [attachEndpointRequests[key] token];
        [attachEPRequestList addObject:attachRequest];
    }
    
    NSDictionary *detachEndpointRequests = [self.processor getDetachEndpointRequests];
    NSMutableArray *detachEPRequestList = [NSMutableArray array];
    for (NSNumber *key in detachEndpointRequests.allKeys) {
        EndpointDetachRequest *detachRequest = [[EndpointDetachRequest alloc] init];
        detachRequest.requestId = [key intValue];
        detachRequest.endpointKeyHash = [detachEndpointRequests[key] keyHash];
        [detachEPRequestList addObject:detachRequest];
    }
    
    UserSyncRequest *request = [[UserSyncRequest alloc] init];
    if ([self.processor getUserAttachRequest]) {
        request.userAttachRequest = [KAAUnion unionWithBranch:KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_0
                                                         data:[self.processor getUserAttachRequest]];
    }
    request.endpointAttachRequests = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_0
                                                          data:attachEPRequestList];
    request.endpointDetachRequests = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_0
                                                          data:detachEPRequestList];
    return request;
}

- (void)onUserResponse:(UserSyncResponse *)response {
    if (!self.processor) {
        return;
    }
    BOOL hasChanges = NO;
    if (response.endpointAttachResponses
        && response.endpointAttachResponses.branch == KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_BRANCH_0) {
        NSDictionary *attachEndpointRequests = [self.processor getAttachEndpointRequests];
        NSArray *attachResponses = response.endpointAttachResponses.data;
        for (EndpointAttachResponse *attached in attachResponses) {
            EndpointAccessToken *attachedToken = attachEndpointRequests[@(attached.requestId)];
            if (attached.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS && attachedToken) {
                DDLogInfo(@"%@ Token: %@", TAG, attachedToken);
                if (attached.endpointKeyHash.branch == KAA_UNION_STRING_OR_NULL_BRANCH_0) {
                    EndpointKeyHash *keyHash = [[EndpointKeyHash alloc] initWithKeyHash:attached.endpointKeyHash.data];
                    self.attachedEndpoints[attachedToken] = keyHash;
                    hasChanges = YES;
                } else {
                    DDLogError(@"%@ No endpointKeyHash for request id: %i", TAG, attached.requestId);
                }
            } else {
                DDLogError(@"%@ Failed to attach endpoint with token: %@. Request id: %i, result: %i",
                           TAG, attachedToken, attached.requestId, attached.result);
            }
        }
    }
    
    if (response.endpointDetachResponses
        && response.endpointDetachResponses.branch == KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_BRANCH_0) {
        NSDictionary *detachEndpointRequests = [self.processor getDetachEndpointRequests];
        NSArray *detachResponses = response.endpointDetachResponses.data;
        for (EndpointDetachResponse *detached in detachResponses) {
            EndpointKeyHash *detachedEndpointKeyHash = detachEndpointRequests[@(detached.requestId)];
            if (detached.result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS && detachedEndpointKeyHash) {
                for (EndpointAccessToken *key in self.attachedEndpoints.allKeys) {
                    EndpointKeyHash *value = self.attachedEndpoints[key];
                    if ([value isEqual:detachedEndpointKeyHash]) {
                        [self.attachedEndpoints removeObjectForKey:key];
                        if (!hasChanges) {
                            hasChanges = YES;
                        }
                    }
                }
            } else {
                DDLogError(@"%@ Failed to detach endpoint with key hash: %@. Request id: %i, result: %i",
                           TAG, detachedEndpointKeyHash, detached.requestId, detached.result);
            }
        }
    }
    
    if (hasChanges && self.clientState) {
        [self.clientState setAttachedEndpoints:self.attachedEndpoints];
    }
    
    [self.processor onUpdateWithAttachResponses:response.endpointAttachResponses.data
                                detachResponses:response.endpointDetachResponses.data
                                   userResponse:response.userAttachResponse.data
                         userAttachNotification:response.userAttachNotification.data
                         userDetachNotification:response.userDetachNotification.data];
    
    DDLogInfo(@"%@ Processed user response", TAG);
}

- (void)setEndpointRegistrationProcessor:(id<EndpointRegistrationProcessor>)processor {
    self.processor = processor;
}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_USER;
}

@end
