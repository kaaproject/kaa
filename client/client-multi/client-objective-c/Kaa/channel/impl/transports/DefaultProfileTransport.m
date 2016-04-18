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

#import "DefaultProfileTransport.h"
#import "KeyUtils.h"
#import "KaaLogging.h"

#define TAG @"DefaultProfileTransport >>>"

@interface DefaultProfileTransport ()

@property (nonatomic, strong) id<ProfileManager> profileMgr;
@property (nonatomic, strong) KaaClientProperties *properties;

- (BOOL)isProfileOutdated:(EndpointObjectHash *)currentProfileHash;

@end

@implementation DefaultProfileTransport

- (void)sync {
    [self syncAll:TRANSPORT_TYPE_PROFILE];
}

- (ProfileSyncRequest *)createProfileRequest {
    if (self.clientState && self.profileMgr && self.properties) {
        NSData *serializedProfile = [self.profileMgr getSerializedProfile];
        EndpointObjectHash *currentProfileHash = [EndpointObjectHash hashWithSHA1:serializedProfile];
        if ([self isProfileOutdated:currentProfileHash]
            || ![self.clientState isRegistred]
            || [self.clientState needProfileResync]) {
            [self.clientState setProfileHash:currentProfileHash];
            ProfileSyncRequest *request = [[ProfileSyncRequest alloc] init];
            request.endpointAccessToken = [KAAUnion unionWithBranch:KAA_UNION_STRING_OR_NULL_BRANCH_0
                                                               data:[self.clientState endpointAccessToken]];
            if (![self.clientState isRegistred]) {
                request.endpointPublicKey = [KAAUnion unionWithBranch:KAA_UNION_BYTES_OR_NULL_BRANCH_0
                                                                 data:[self.clientState publicKeyAsBytes]];
            }
            request.profileBody = serializedProfile;
            return request;
            
        } else {
            DDLogInfo(@"%@ Profile is up to date", TAG);
        }
    } else {
        DDLogError(@"%@ Failed to create ProfileSyncRequest clientState %@, manager %@, properties %@",
                   TAG, self.clientState, self.profileMgr, self.properties);
    }
    return nil;
}

- (void)onProfileResponse:(ProfileSyncResponse *)response {
    if (response.responseStatus == SYNC_RESPONSE_STATUS_RESYNC) {
        [self.clientState setNeedProfileResync:YES];
        [self syncAll:[self getTransportType]];
    } else if (self.clientState && ![self.clientState isRegistred]) {
        [self.clientState setIsRegistred:YES];
    }
    DDLogInfo(@"%@ Processed profile response", TAG);
}

- (void)setProfileManager:(id<ProfileManager>)manager {
    self.profileMgr = manager;
}

- (void)setClientProperties:(KaaClientProperties *)clientProperties {
    self.properties = clientProperties;
}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_PROFILE;
}

- (BOOL)isProfileOutdated:(EndpointObjectHash *)currentProfileHash {
    EndpointObjectHash *currentHash = [self.clientState profileHash];
    return !currentHash || ![currentProfileHash isEqual:currentHash];
}

@end
