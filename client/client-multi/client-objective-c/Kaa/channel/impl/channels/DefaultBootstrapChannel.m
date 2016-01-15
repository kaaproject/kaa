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

#import "DefaultBootstrapChannel.h"
#import "HttpRequestCreator.h"
#import "KaaLogging.h"

#define TAG         @"DefaultBootstrapChannel >>>"
#define CHANNEL_ID  @"default_bootstrap_channel"
#define URL_SUFFIX  @"/BS/Sync"

@interface BootstrapRunner : NSOperation

@property (nonatomic,weak) DefaultBootstrapChannel *btChannel;

- (instancetype)initWithChannel:(DefaultBootstrapChannel *)channel;

@end

@interface DefaultBootstrapChannel ()

@property (nonatomic,strong) NSDictionary *SUPPORTED_TYPES; //<TransportType,ChannelDirection> as key-value

- (void)processTypes:(NSDictionary *)types;

@end

@implementation DefaultBootstrapChannel

- (instancetype)initWithClient:(AbstractKaaClient *)client state:(id<KaaClientState>)state
               failoverManager:(id<FailoverManager>)manager {
    self = [super initWithClient:client state:state failoverManager:manager];
    if (self) {
        self.SUPPORTED_TYPES = [[NSDictionary alloc] initWithObjectsAndKeys:
                                [NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL],
                                [NSNumber numberWithInt:TRANSPORT_TYPE_BOOTSTRAP], nil];
    }
    return self;
}

- (void)processTypes:(NSDictionary *)types {
    NSData *requestBodyRaw = [[self getMultiplexer] compileRequest:types];
    NSData *decodedResponse = nil;
    @synchronized(self) {
        MessageEncoderDecoder *encoderDecoder = [[self getHttpClient] getEncoderDecoder];
        NSDictionary *requestEntity = [HttpRequestCreator createBootstrapHttpRequest:requestBodyRaw
                                                                  withEncoderDecoder:encoderDecoder];
        NSData *responseDataRaw = [[self getHttpClient] executeHttpRequest:@""
                                                                    entity:requestEntity
                                                            verifyResponse:NO];
        decodedResponse = [encoderDecoder decodeData:responseDataRaw];
    }
    [[self getDemultiplexer] processResponse:decodedResponse];
}

- (NSString *)getId {
    return CHANNEL_ID;
}

- (ServerType)getServerType {
    return SERVER_BOOTSTRAP;
}

- (NSDictionary *)getSupportedTransportTypes {
    return self.SUPPORTED_TYPES;
}

- (NSString *)getURLSuffix {
    return URL_SUFFIX;
}

- (NSOperation *)createChannelRunner:(NSDictionary *)types {
    return [[BootstrapRunner alloc] initWithChannel:self];
}

@end

@implementation BootstrapRunner

- (instancetype)initWithChannel:(DefaultBootstrapChannel *)channel {
    self = [super init];
    if (self) {
        self.btChannel = channel;
    }
    return self;
}

- (void)main {
    if (self.isCancelled || self.isFinished) {
        return;
    }
    @try {
        [self.btChannel processTypes:[self.btChannel getSupportedTransportTypes]];
        [self.btChannel connectionStateChanged:NO];
    }
    @catch (NSException *ex) {
        if ([self.btChannel isShutdown]) {
            DDLogError(@"%@ Failed to receive operation servers list: %@, reason: %@", TAG, ex.name, ex.reason);
            [self.btChannel connectionStateChanged:YES];
        } else {
            DDLogDebug(@"%@ Failed to receive operation servers list: %@, reason: %@", TAG, ex.name, ex.reason);
        }
    }
}

@end
