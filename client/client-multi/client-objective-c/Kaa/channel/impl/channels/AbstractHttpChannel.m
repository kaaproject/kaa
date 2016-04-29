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

#import "AbstractHttpChannel.h"
#import "IPTransportInfo.h"
#import "TransportProtocolIdHolder.h"
#import "KaaLogging.h"

static NSString *const logTag = @"AbstractHttpChannel >>>";

typedef NS_ENUM(int, HttpStatus) {
    HttpStatusUnauthorised = 401,
    HttpStatusForbidden = 403
};

@interface AbstractHttpChannel ()

@property (nonatomic, strong) IPTransportInfo *currentServer;
@property (nonatomic, weak) AbstractKaaClient *kaaClient;
@property (nonatomic, strong) id<KaaClientState> kaaState;
@property (nonatomic, strong) id<FailoverManager> failoverMgr;

@property (nonatomic, strong) volatile NSOperationQueue *executor;

@property (nonatomic) volatile BOOL lastConnectionFailed;
@property (nonatomic) volatile BOOL isPaused;

@property (nonatomic, strong) AbstractHttpClient *kaaHttpClient;
@property (nonatomic, strong) id<KaaDataDemultiplexer> chDemultiplexer;
@property (nonatomic, strong) id<KaaDataMultiplexer> chMultiplexer;

@end

@implementation AbstractHttpChannel

- (instancetype)initWithClient:(AbstractKaaClient *)client
                         state:(id<KaaClientState>)state
               failoverManager:(id<FailoverManager>)manager {
    self = [super init];
    if (self) {
        self.kaaClient = client;
        self.kaaState = state;
        self.failoverMgr = manager;
    }
    return self;
}

- (TransportProtocolId *)getTransportProtocolId {
    return [TransportProtocolIdHolder HTTPTransportID];
}

- (void)syncForTransportType:(TransportType)type {
    @synchronized(self) {
        [self syncTransportTypes:[NSSet setWithObject:@(type)]];
    }
}

- (void)syncTransportTypes:(NSSet *)types {
    @synchronized(self) {
        if (self.isShutdown) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is down", logTag, [self getId]);
            return;
        }
        if (self.isPaused) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is paused", logTag, [self getId]);
            return;
        }
        if (!self.chMultiplexer) {
            DDLogInfo(@"%@ Can't sync. Channel %@ multiplexer is not set", logTag, [self getId]);
            return;
        }
        if (!self.chDemultiplexer) {
            DDLogWarn(@"%@ Can't sync. Channel %@ demultiplexer is not set", logTag, [self getId]);
            return;
        }
        if (!self.currentServer) {
            self.lastConnectionFailed = YES;
            DDLogWarn(@"%@ Can't sync. Server is nil", logTag);
        }
        
        NSMutableDictionary *typeMap = [NSMutableDictionary dictionary];
        for (NSNumber *type in types) {
            DDLogInfo(@"%@ Processing sync %i for channel %@", logTag, [type intValue], [self getId]);
            NSNumber *channelDirection = [self getSupportedTransportTypes][type];
            if (channelDirection) {
                typeMap[type] = channelDirection;
            } else {
                DDLogError(@"%@ Unsupported type %i for channel %@", logTag, [type intValue], [self getId]);
            }
        }
        if (self.executor) {
            __weak typeof(self) weakSelf = self;
            [self.executor addOperationWithBlock:^{
                [weakSelf processTypes:typeMap];
            }];
        } else {
            DDLogError(@"%@ No executor found for channel with id: %@", logTag, [self getId]);
        }
    }
}

- (void)syncAll {
    @synchronized(self) {
        if (self.isShutdown) {
            DDLogInfo(@"%@ Can't sync all. Channel %@ is down", logTag, [self getId]);
            return;
        }
        if (self.isPaused) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is paused", logTag, [self getId]);
            return;
        }
        
        if (!self.chMultiplexer || !self.chDemultiplexer) {
            DDLogWarn(@"%@ Can't sync, multiplexer/demultiplexer not set: %@/%@", logTag, self.chMultiplexer, self.chDemultiplexer);
            return;
        }
        if (self.currentServer) {
            __weak typeof(self) weakSelf = self;
            [self.executor addOperationWithBlock:^{
                [weakSelf processTypes:[weakSelf getSupportedTransportTypes]];
            }];
        } else {
            self.lastConnectionFailed = YES;
            DDLogWarn(@"%@ Can't sync. Server is nil", logTag);
        }
    }
}

- (void)syncAckForTransportType:(TransportType)type {
    [self syncAckForTransportTypes:[NSSet setWithObject:@(type)]];
}

- (void)syncAckForTransportTypes:(NSSet *)types {
#pragma unused(types)
    DDLogInfo(@"%@ Sync ack message is ignored for Channel with id: %@", logTag, [self getId]);
}

- (void)setMultiplexer:(id<KaaDataMultiplexer>)multiplexer {
    @synchronized(self) {
        if (multiplexer) {
            self.chMultiplexer = multiplexer;
        }
    }
}

- (void)setDemultiplexer:(id<KaaDataDemultiplexer>)demultiplexer {
    @synchronized(self) {
        if (demultiplexer) {
            self.chDemultiplexer = demultiplexer;
        }
    }
}

- (void)setServer:(id<TransportConnectionInfo>)server {
    @synchronized(self) {
        if (self.isShutdown) {
            DDLogInfo(@"%@ Can't set server. Channel %@ is down", logTag, [self getId]);
            return;
        }
        if (!self.executor && !self.isPaused) {
            self.executor = [self createExecutor];
        }
        if (server) {
            self.currentServer = [[IPTransportInfo alloc] initWithTransportInfo:server];
            NSString *url = [NSString stringWithFormat:@"%@%@", [self.currentServer getUrl], [self getURLSuffix]];
            self.kaaHttpClient = [self.kaaClient createHttpClientWithURLString:url
                                                                 privateKeyRef:[self.kaaState privateKey]
                                                                  publicKeyRef:[self.kaaState publicKey]
                                                                     remoteKey:[self.currentServer getPublicKey]];
            if (self.lastConnectionFailed && !self.isPaused) {
                self.lastConnectionFailed = NO;
                [self syncAll];
            }
        }
    }
}

- (id<TransportConnectionInfo>)getServer {
    return self.currentServer;
}

- (void)setConnectivityChecker:(ConnectivityChecker *)checker {
#pragma unused (checker)
    DDLogInfo(@"%@ Ignore set connectivity checker", logTag);
}

- (void)shutdown {
    if (!self.isShutdown) {
        _isShutdown = YES;
        if (self.executor) {
            [self.executor cancelAllOperations];
        }
    }
}

- (void)pause {
    if (self.isShutdown) {
        DDLogInfo(@"%@ Can't pause. Channel %@ is down", logTag, [self getId]);
        return;
    }
    if (!self.isPaused) {
        self.isPaused = YES;
        if (self.executor) {
            [self.executor cancelAllOperations];
            self.executor = nil;
        }
    }
}

- (void)resume {
    if (self.isShutdown) {
        DDLogInfo(@"%@ Can't resume. Channel %@ is down", logTag, [self getId]);
        return;
    }
    
    if (self.isPaused) {
        self.isPaused = NO;
        if (!self.executor) {
            self.executor = [self createExecutor];
        }
        if (self.lastConnectionFailed) {
            self.lastConnectionFailed = NO;
            [self syncAll];
        }
    }
}

- (ServerType)getServerType {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return -1;
}

- (NSDictionary *)getSupportedTransportTypes {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return nil;
}

- (NSString *)getId {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return nil;
}

- (void)connectionEstablished {
    self.lastConnectionFailed = NO;
    [self.failoverMgr onServerConnectedWithConnectionInfo:self.currentServer];
}

- (void)connectionFailedWithStatus:(int)status {
    FailoverStatus failoverStatus = FailoverStatusOperationsServersNotAvailable;
    switch (status) {
        case HttpStatusUnauthorised:
            [self.kaaState clean];
            failoverStatus = FailoverStatusEndpointVerificationFailed;
            break;
        case HttpStatusForbidden:
            failoverStatus = FailoverStatusEndpointCredentialsRevoked;
            break;
        default:
            break;
    }
    self.lastConnectionFailed = YES;
    [self.failoverMgr onServerFailedWithConnectionInfo:self.currentServer failoverStatus:failoverStatus];
}

- (id<KaaDataMultiplexer>)getMultiplexer {
    return self.chMultiplexer;
}

- (id<KaaDataDemultiplexer>)getDemultiplexer {
    return self.chDemultiplexer;
}

- (AbstractHttpClient *)getHttpClient {
    return self.kaaHttpClient;
}

- (void)processTypes:(NSDictionary *)types {
#pragma unused(types)
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
}

- (NSString *)getURLSuffix {
    [NSException raise:NSInternalInconsistencyException format:@"Not implemented in abstract class"];
    return nil;
}

- (NSOperationQueue *)createExecutor {
    DDLogInfo(@"%@ Creating a new executor for channel: %@", logTag, [self getId]);
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    queue.maxConcurrentOperationCount = 1;
    return queue;
}

@end
