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

#import "DefaultBootstrapChannel.h"
#import "HttpRequestCreator.h"
#import "KaaLogging.h"

#define TAG         @"DefaultBootstrapChannel >>>"
#define CHANNEL_ID  @"default_bootstrap_channel"
#define URL_SUFFIX  @"/BS/Sync"

@interface DefaultBootstrapChannel ()

/**
 * <TransportType,ChannelDirection> as key-value
 */
@property (nonatomic, strong) NSDictionary *supportedTypes;

@end

@implementation DefaultBootstrapChannel

- (instancetype)initWithClient:(AbstractKaaClient *)client
                         state:(id<KaaClientState>)state
               failoverManager:(id<FailoverManager>)manager {
    self = [super initWithClient:client state:state failoverManager:manager];
    if (self) {
        self.supportedTypes = @{@(TRANSPORT_TYPE_BOOTSTRAP) : @(CHANNEL_DIRECTION_BIDIRECTIONAL)};
    }
    return self;
}

- (void)processTypes:(NSDictionary *)types {
    NSData *requestBodyRaw = [[self getMultiplexer] compileRequestForTypes:types];
    @synchronized(self) {
        MessageEncoderDecoder *encoderDecoder = [[self getHttpClient] getEncoderDecoder];
        NSDictionary *requestEntity = [HttpRequestCreator createBootstrapHttpRequest:requestBodyRaw
                                                                  withEncoderDecoder:encoderDecoder];
        __weak typeof(self) weakSelf = self;
        [[self getHttpClient] executeHttpRequest:@""
                                          entity:requestEntity
                                  verifyResponse:NO
                                         success:^(NSData *response) {
                                             NSData *decodedResponse = [encoderDecoder decodeData:response];
                                             [[weakSelf getDemultiplexer] processResponse:decodedResponse];
                                             [weakSelf connectionEstablished];
                                         } failure:^(NSInteger responseCode) {
                                             if (![weakSelf isShutdown]) {
                                                 DDLogError(@"%@ Failed to receive operation servers list, response code: %i", TAG, (int)responseCode);
                                                 [weakSelf connectionFailedWithStatus:(int)responseCode];
                                             }
                                         }];
    }
    
}

- (NSString *)getId {
    return CHANNEL_ID;
}

- (ServerType)getServerType {
    return SERVER_BOOTSTRAP;
}

- (NSDictionary *)getSupportedTransportTypes {
    return self.supportedTypes;
}

- (NSString *)getURLSuffix {
    return URL_SUFFIX;
}

@end
