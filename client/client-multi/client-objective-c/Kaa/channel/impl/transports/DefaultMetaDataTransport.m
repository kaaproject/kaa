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

#import "DefaultMetaDataTransport.h"
#import "KaaLogging.h"

#define TAG @"DefaultMetaDataTransport >>>"

@interface DefaultMetaDataTransport ()

@property (nonatomic, strong) KaaClientProperties *properties;
@property (nonatomic, strong) id<KaaClientState> state;
@property (nonatomic, strong) EndpointObjectHash *publicKeyHash;
@property (nonatomic) int64_t timeout;

@end

@implementation DefaultMetaDataTransport

- (SyncRequestMetaData *)createMetaDataRequest {
    if (!self.state || !self.properties || !self.publicKeyHash) {
        DDLogError(@"%@ Unable to create MetaDataRequest - params not completed", TAG);
        return nil;
    }
    
    SyncRequestMetaData *request = [[SyncRequestMetaData alloc] init];
    request.sdkToken = [self.properties sdkToken];
    if (self.publicKeyHash.data) {
        request.endpointPublicKeyHash = [KAAUnion unionWithBranch:KAA_UNION_BYTES_OR_NULL_BRANCH_0
                                                             data:self.publicKeyHash.data];
    }
    NSData *profileHashData = [self.state profileHash].data;
    if (profileHashData) {
        request.profileHash = [KAAUnion unionWithBranch:KAA_UNION_BYTES_OR_NULL_BRANCH_0
                                                   data:profileHashData];
    }
    
    request.timeout = [KAAUnion unionWithBranch:KAA_UNION_LONG_OR_NULL_BRANCH_0
                                           data:@(self.timeout)];
    return request;
}

- (void)setClientProperties:(KaaClientProperties *)properties {
    _properties = properties;
}

- (void)setClientState:(id<KaaClientState>)state {
    _state = state;
}

- (void)setEndpointPublicKeyHash:(EndpointObjectHash *)hash {
    _publicKeyHash = hash;
}

- (void)setTimeout:(int64_t)timeout {
    _timeout = timeout;
}

@end
