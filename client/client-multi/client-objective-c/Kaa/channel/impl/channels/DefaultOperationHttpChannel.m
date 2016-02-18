/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import "DefaultOperationHttpChannel.h"
#import "HttpRequestCreator.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG         @"DefaultOperationHttpChannel >>>"
#define CHANNEL_ID  @"default_operations_http_channel"
#define URL_SUFFIX  @"/EP/Sync"

@interface OperationRunner : NSOperation

@property (nonatomic, weak) DefaultOperationHttpChannel *operationChannel;
@property (nonatomic, strong) NSDictionary *operationTypes;

- (instancetype)initWithChannel:(DefaultOperationHttpChannel *)channel types:(NSDictionary *)types;

@end

@interface DefaultOperationHttpChannel ()

@property (nonatomic, strong) NSDictionary *supportedTypes; //<TransportType,ChannelDirection> as key-value

- (void)processTypes:(NSDictionary *)types;

@end

@implementation DefaultOperationHttpChannel

- (instancetype)initWithClient:(AbstractKaaClient *)client
                         state:(id<KaaClientState>)state
               failoverManager:(id<FailoverManager>)manager {
    self = [super initWithClient:client state:state failoverManager:manager];
    if (self) {
        self.supportedTypes = [[NSDictionary alloc] initWithObjectsAndKeys:
                                @(CHANNEL_DIRECTION_UP),
                                @(TRANSPORT_TYPE_EVENT),
                                @(CHANNEL_DIRECTION_UP),
                                @(TRANSPORT_TYPE_LOGGING), nil];
    }
    return self;
}

- (void)processTypes:(NSDictionary *)types {
    NSData *requestBodyRaw = [[self getMultiplexer] compileRequestForTypes:types];
    NSData *decodedResponse = nil;
    @synchronized(self) {
        MessageEncoderDecoder *encoderDecoder = [[self getHttpClient] getEncoderDecoder];
        NSDictionary *requestEntity = [HttpRequestCreator createOperationHttpRequest:requestBodyRaw
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
    return SERVER_OPERATIONS;
}

- (NSDictionary *)getSupportedTransportTypes {
    return self.supportedTypes;
}

- (NSOperation *)createChannelRunnerWithTypes:(NSDictionary *)types {
    return [[OperationRunner alloc] initWithChannel:self types:types];
}

- (NSString *)getURLSuffix {
    return URL_SUFFIX;
}

@end

@implementation OperationRunner

- (instancetype)initWithChannel:(DefaultOperationHttpChannel *)channel types:(NSDictionary *)types {
    self = [super init];
    if (self) {
        self.operationChannel = channel;
        self.operationTypes = types;
    }
    return self;
}

- (void)main {
    if (self.isCancelled || self.isFinished) {
        return;
    }
    
    @try {
        [self.operationChannel processTypes:self.operationTypes];
        [self.operationChannel connectionEstablished];
    }
    @catch (NSException *ex) {
        DDLogError(@"%@ Failed to receive response from the operation: %@, reason: %@", TAG, ex.name, ex.reason);
        if ([ex.name isEqualToString:KaaTransportException]) {
            [self.operationChannel connectionFailedWithStatus:[ex.reason intValue]];
        } else {
            [self.operationChannel connectionFailedWithStatus:UNKNOWN_HTTP_STATUS];
        }
    }
}

@end