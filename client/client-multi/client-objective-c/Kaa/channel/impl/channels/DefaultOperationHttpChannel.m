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

#import "DefaultOperationHttpChannel.h"
#import "HttpRequestCreator.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG         @"DefaultOperationHttpChannel >>>"
#define CHANNEL_ID  @"default_operations_http_channel"
#define URL_SUFFIX  @"/EP/Sync"

@interface DefaultOperationHttpChannel ()

/**
 * <TransportType, ChannelDirection> as key-value
 */
@property (nonatomic, strong) NSDictionary *supportedTypes;

@end


@implementation DefaultOperationHttpChannel

- (instancetype)initWithClient:(AbstractKaaClient *)client
                         state:(id<KaaClientState>)state
               failoverManager:(id<FailoverManager>)manager {
    self = [super initWithClient:client state:state failoverManager:manager];
    if (self) {
        self.supportedTypes = @{@(TRANSPORT_TYPE_EVENT) : @(CHANNEL_DIRECTION_UP),
                                @(TRANSPORT_TYPE_LOGGING) : @(CHANNEL_DIRECTION_UP)};
    }
    return self;
}

- (void)processTypes:(NSDictionary *)types {
    NSData *requestBodyRaw = [[self getMultiplexer] compileRequestForTypes:types];
    @synchronized(self) {
        MessageEncoderDecoder *encoderDecoder = [[self getHttpClient] getEncoderDecoder];
        NSDictionary *requestEntity = [HttpRequestCreator createOperationHttpRequest:requestBodyRaw
                                                                  withEncoderDecoder:encoderDecoder];
        __weak typeof(self) weakSelf = self;
        [[self getHttpClient] executeHttpRequest:@""
                                          entity:requestEntity
                                  verifyResponse:NO
                                         success:^(NSData *response){
                                             NSData *decodedResponse = [encoderDecoder decodeData:response];
                                             [[weakSelf getDemultiplexer] processResponse:decodedResponse];
                                             [weakSelf connectionEstablished];
                                         }
                                         failure:^(NSInteger responseCode) {
                                             DDLogError(@"%@ Failed to receive response from the operation server: %i", TAG, (int)responseCode);
                                             [weakSelf connectionFailedWithStatus:(int)responseCode];
                                         }];
    }
}

- (NSString *)getId {
    return CHANNEL_ID;
}

- (ServerType)getServerType {
    return SERVER_OPERATIONS;
}

- (NSDictionary *)getSupportedTransportTypes {
    return self.supportedTypes;
}

- (NSString *)getURLSuffix {
    return URL_SUFFIX;
}

@end