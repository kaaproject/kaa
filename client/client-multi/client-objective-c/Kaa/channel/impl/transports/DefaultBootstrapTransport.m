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

#import "DefaultBootstrapTransport.h"
#import "KaaLogging.h"

#define TAG @"DefaultBootstrapTransport >>>"

@interface DefaultBootstrapTransport ()

@property (nonatomic, strong) id<BootstrapManager> manager;
@property (nonatomic, strong) NSString *sdkToken;
@property (atomic) int increment;

@end

@implementation DefaultBootstrapTransport

- (instancetype)initWithToken:(NSString *)sdkToken {
    self = [super init];
    if (self) {
        self.sdkToken = sdkToken;
        self.increment = 0;
    }
    return self;
}

- (SyncRequest *)createResolveRequest {
    if (!self.clientState) {
        return nil;
    }
    SyncRequest *request = [[SyncRequest alloc] init];
    request.requestId = ++self.increment;
    
    BootstrapSyncRequest *resolveRequest = [[BootstrapSyncRequest alloc] init];
    NSArray *channels = [self.channelManager getChannels];
    NSMutableArray *pairs = [NSMutableArray arrayWithCapacity:[channels count]];
    for (id<KaaDataChannel> channel in channels) {
        TransportProtocolId *channelTransportId = [channel getTransportProtocolId];
        ProtocolVersionPair *pair = [[ProtocolVersionPair alloc] init];
        pair.id = channelTransportId.protocolId;
        pair.version = channelTransportId.protocolVersion;
        [pairs addObject:pair];
        DDLogDebug(@"%@ Adding '%@' to resolve request", TAG, pair);
    }
    resolveRequest.supportedProtocols = pairs;
    resolveRequest.requestId = self.increment;
    
    request.bootstrapSyncRequest =
    [KAAUnion unionWithBranch:KAA_UNION_BOOTSTRAP_SYNC_REQUEST_OR_NULL_BRANCH_0 data:resolveRequest];
    
    SyncRequestMetaData *meta = [[SyncRequestMetaData alloc] init];
    meta.sdkToken = self.sdkToken;
    
    request.syncRequestMetaData =
    [KAAUnion unionWithBranch:KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0 data:meta];
    return request;
}

- (void)onResolveResponse:(SyncResponse *)servers {
    if (self.manager && servers
        && servers.bootstrapSyncResponse.branch == KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_BRANCH_0) {
        BootstrapSyncResponse *responce = (BootstrapSyncResponse *)servers.bootstrapSyncResponse.data;
        [self.manager onProtocolListUpdated:responce.supportedProtocols];
    }
}

- (void)setBootstrapManager:(id<BootstrapManager>)manager {
    self.manager = manager;
}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_BOOTSTRAP;
}

@end
