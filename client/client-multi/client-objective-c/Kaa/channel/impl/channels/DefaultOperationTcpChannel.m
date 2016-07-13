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

#import "DefaultOperationTcpChannel.h"
#import "KaaLogging.h"
#import "IPTransportInfo.h"
#import "MessageEncoderDecoder.h"
#import "KAAMessageFactory.h"
#import "Constants.h"
#import "TransportProtocolIdHolder.h"

typedef enum {
    CHANNEL_STATE_SHUTDOWN,
    CHANNEL_STATE_PAUSE,
    CHANNEL_STATE_CLOSED,
    CHANNEL_STATE_OPENED
} ChannelState;

#define TAG                 @"DefaultOperationTcpChannel >>>"
#define CHANNEL_TIMEOUT     200
#define PING_TIMEOUT_SEC    (CHANNEL_TIMEOUT / 2)
#define MAX_THREADS_COUNT   2
#define CHANNEL_ID          @"default_operation_tcp_channel"

@interface OpenConnectionTask : NSOperation

@property (nonatomic, weak) DefaultOperationTcpChannel *channel;
@property (nonatomic) int64_t delay;

- (instancetype)initWithChannel:(DefaultOperationTcpChannel *)channel delay:(int64_t)delay; //delay in milliseconds

@end

@interface DefaultOperationTcpChannel () <ConnAckDelegate, PingResponseDelegate, SyncResponseDelegate, DisconnectDelegate, NSStreamDelegate>

@property (nonatomic, strong) NSDictionary *supportedTypes; //<TransportType,ChannelDirection> as key-value
@property (nonatomic, strong) IPTransportInfo *currentServer;
@property (nonatomic, strong) id<KaaClientState> state;
@property (nonatomic) volatile ChannelState channelState;
@property (nonatomic, strong) id<KaaDataDemultiplexer> demultiplexer;
@property (nonatomic, strong) id<KaaDataMultiplexer> multiplexer;
@property (nonatomic, strong) MessageEncoderDecoder *encoderDecoder;
@property (nonatomic, strong) id<FailoverManager> failoverManager;
@property (nonatomic, strong) volatile ConnectivityChecker *checker;
@property (nonatomic, strong) KAAMessageFactory *messageFactory;
@property (nonatomic) volatile BOOL isOpenConnectionScheduled;
@property (nonatomic) volatile BOOL isPingTaskCancelled;
@property (nonatomic, strong) NSOperationQueue *executor;
@property (nonatomic, strong) KAASocket *socket;//volatile
@property (nonatomic, weak) id<FailureDelegate> failureDelegate;

- (void)onServerFailed;
- (void)onServerFailedWithFailoverStatus:(FailoverStatus)status;
- (void)closeConnection;
- (void)sendFrame:(KAAMqttFrame *)frame;
- (void)sendPingRequest;
- (void)sendDisconnect;
- (void)sendKaaSyncRequestWithTypes:(NSDictionary *)types; //<TransportType, ChannelDirection> as key-value
- (void)sendConnect;
- (void)openConnection;
- (void)scheduleOpenConnectionTaskWithRetryPeriod:(int64_t)retryPeriod;
- (void)schedulePingTask;
- (void)destroyExecutor;

@end

@implementation DefaultOperationTcpChannel

- (instancetype)initWithClientState:(id<KaaClientState>)state
                    failoverManager:(id<FailoverManager>)failoverMgr
                    failureDelegate:(id<FailureDelegate>)delegate {
    self = [super init];
    if (self) {
        self.supportedTypes = @{
                                @(TRANSPORT_TYPE_PROFILE)       : @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                @(TRANSPORT_TYPE_CONFIGURATION) : @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                @(TRANSPORT_TYPE_NOTIFICATION)  : @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                @(TRANSPORT_TYPE_USER)          : @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                @(TRANSPORT_TYPE_EVENT)         : @(CHANNEL_DIRECTION_BIDIRECTIONAL),
                                @(TRANSPORT_TYPE_LOGGING)       : @(CHANNEL_DIRECTION_BIDIRECTIONAL)
                                };
        self.channelState = CHANNEL_STATE_CLOSED;
        self.messageFactory = [[KAAMessageFactory alloc] init];
        self.state = state;
        self.failoverManager = failoverMgr;
        self.failureDelegate = delegate;
        [self.messageFactory registerConnAckDelegate:self];
        [self.messageFactory registerSyncResponseDelegate:self];
        [self.messageFactory registerPingResponseDelegate:self];
        [self.messageFactory registerDisconnectDelegate:self];
    }
    return self;
}

- (void)onConnAckMessage:(KAATcpConnAck *)message {
    DDLogInfo(@"%@ ConnAck [%i] message received for channel [%@]", TAG, message.returnCode, [self getId]);
    if (message.returnCode != ReturnCodeAccepted) {
        DDLogError(@"%@ Connection for channel [%@] was rejected: %i", TAG, [self getId], message.returnCode);
        if (message.returnCode == ReturnCodeRefuseBadCredentials) {
            DDLogInfo(@"%@ Cleaning client state", TAG);
            [self.state clean];
        }
        [self onServerFailed];
    }
}

- (void)onPingResponseMessage:(KAATcpPingResponse *)message {
#pragma unused(message)
    DDLogInfo(@"%@ PingResponse message received for channel [%@]", TAG, [self getId]);
}

- (void)onSyncResponseMessage:(KAATcpSyncResponse *)message {
    DDLogInfo(@"%@ KaaSync message (zipped:%i, encrypted:%i) received for channel [%@]",
              TAG, message.zipped, message.encrypted, [self getId]);
    NSData *resultBody = nil;
    if (message.encrypted) {
        @synchronized(self) {
            @try {
                resultBody = [self.encoderDecoder decodeData:[message avroObject]];
            }
            @catch (NSException *ex) {
                DDLogError(@"%@ Failed to decrypt message body for channel [%@]", TAG, [self getId]);
                DDLogError(@"%@ Error: %@, reason: %@", TAG, ex.name, ex.reason);
            }
        }
    } else {
        resultBody = [message avroObject];
    }
    if (resultBody) {
        @try {
            [self.demultiplexer preProcess];
            [self.demultiplexer processResponse:resultBody];
            [self.demultiplexer postProcess];
        }
        @catch (NSException *ex) {
            DDLogError(@"%@ Failed to process response for channel [%@]: %@. Reason: %@", TAG, [self getId], ex.name, ex.reason);
        }
        
        @synchronized(self) {
            self.channelState = CHANNEL_STATE_OPENED;
        }
        
        [self.failoverManager onServerConnectedWithConnectionInfo:self.currentServer];
    } else {
        DDLogWarn(@"%@ Result body in nil", TAG);
    }
}

- (void)onDisconnectMessage:(KAATcpDisconnect *)message {
    DDLogInfo(@"%@ Disconnect message (reason:%i) received for channel [%@]", TAG, message.reason, [self getId]);
    switch (message.reason) {
        case DisconnectReasonNone:
            [self closeConnection];
            break;
        case DisconnectReasonCredentialsRevoked:
            [self onServerFailedWithFailoverStatus:FailoverStatusEndpointCredentialsRevoked];
            break;
        default:
            DDLogError(@"%@ Server error occurred: %i", TAG, message.reason);
            [self onServerFailed];
            break;
    }
}

- (void)sendFrame:(KAAMqttFrame *)frame {
    if (self.socket) {
        @synchronized(self.socket) {
            [self.socket.output write:[[frame getFrame] bytes] maxLength:[frame getFrame].length];
        }
    }
}

- (void)sendPingRequest {
    DDLogDebug(@"%@ Sending PinRequest from channel: %@", TAG, [self getId]);
    [self sendFrame:[[KAATcpPingRequest alloc] init]];
}

- (void)sendDisconnect {
    DDLogDebug(@"%@ Sending Disconnect from channel: %@", TAG, [self getId]);
    [self sendFrame:[[KAATcpDisconnect alloc] initWithDisconnectReason:DisconnectReasonNone]];
}

- (void)sendKaaSyncRequestWithTypes:(NSDictionary *)types {
    DDLogDebug(@"%@ Sending KaaSync from channel: %@", TAG, [self getId]);
    NSData *body = [self.multiplexer compileRequestForTypes:types];
    NSData *requestBodyEncoded = [self.encoderDecoder encodeData:body];
    [self sendFrame:[[KAATcpSyncRequest alloc] initWithAvro:requestBodyEncoded zipped:NO encypted:YES]];
}

- (void)sendConnect {
    DDLogDebug(@"%@ Sending Connect from channel: %@", TAG, [self getId]);
    NSData *body = [self.multiplexer compileRequestForTypes:[self getSupportedTransportTypes]];
    NSData *requestBodyEncoded = [self.encoderDecoder encodeData:body];
    NSData *sessionKey = [self.encoderDecoder getEncodedSessionKey];
    NSData *signature = [self.encoderDecoder signatureForMessage:sessionKey];
    [self sendFrame:[[KAATcpConnect alloc] initWithAlivePeriod:CHANNEL_TIMEOUT
                                                nextProtocolId:KAA_PLATFORM_PROTOCOL_AVRO_ID
                                                 aesSessionKey:sessionKey
                                                   syncRequest:requestBodyEncoded
                                                     signature:signature]];
}

- (void)closeConnection {
    @synchronized(self) {
        if (!self.isPingTaskCancelled) {
            self.isPingTaskCancelled = YES;
        }
        if (!self.socket) {
            return;
        }
        DDLogInfo(@"%@ Channel [%@]: closing current connection", TAG, [self getId]);
        @try {
            [self sendDisconnect];
        }
        @catch (NSException *ex) {
            DDLogError(@"%@ Failed to send Disconnect to server: %@. Reason: %@", TAG, ex.name, ex.reason);
        }
        @finally {
            @try {
                [self.socket.input removeFromRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
                [self.socket close];
            }
            @catch (NSException *exception) {
                DDLogError(@"%@ Failed to close socket: %@. Reason: %@", TAG, exception.name, exception.reason);
            }
            @finally {
                self.socket = nil;
                [self.messageFactory.framer flush];
                if (self.channelState != CHANNEL_STATE_SHUTDOWN) {
                    self.channelState = CHANNEL_STATE_CLOSED;
                }
            }
        }
    }
}

- (void)openConnection {
    @synchronized(self) {
        if (self.channelState == CHANNEL_STATE_PAUSE || self.channelState == CHANNEL_STATE_SHUTDOWN) {
            DDLogInfo(@"%@ Can't open connection, as channel is in the %i state", TAG, self.channelState);
            return;
        }
        
        DDLogInfo(@"%@ Channel [%@]: opening connection to server %@", TAG, [self getId], self.currentServer);
        self.isOpenConnectionScheduled = NO;
        self.socket = [self createSocket];
        
        [self.socket.input setDelegate:self];
        [self.socket.input scheduleInRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
        
        [self.socket open];
    }
}

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode {
    switch (eventCode) {
        case NSStreamEventOpenCompleted:
        {
            self.isPingTaskCancelled = NO;
            
            __weak typeof(self) weakSelf = self;
            [self.executor addOperationWithBlock:^{
                [weakSelf sendConnect];
                [weakSelf schedulePingTask];
            }];
        }
            break;
        case NSStreamEventErrorOccurred:
            [self onServerFailed];
            break;
            
        case NSStreamEventEndEncountered:
            DDLogInfo(@"%@ End of stream detected for channel [%@]", TAG, [self getId]);
            break;
        case NSStreamEventHasBytesAvailable:
        {
            __weak typeof(self) weakSelf = self;
            
            [self.executor addOperationWithBlock:^{
                
                if (aStream != weakSelf.socket.input) {
                    DDLogWarn(@"%@ Found outdated ref to socket stream", TAG);
                    return;
                }
                
                uint8_t buffer[1024];
                while ([weakSelf.socket.input hasBytesAvailable]) {
                    long read = [weakSelf.socket.input read:buffer maxLength:sizeof(buffer)];
                    if (read > 0) {
                        DDLogVerbose(@"%@ Read %li bytes from input stream", TAG, read);
                        [weakSelf.messageFactory.framer pushBytes:[NSMutableData dataWithBytes:buffer length:read]];
                    } else if (read == -1) {
                        DDLogInfo(@"%@ Channel [%@] received end of stream", TAG, [weakSelf getId]);
                    }
                }
            }];
        }
            break;
        default:
            break;
    }
}

- (KAASocket *)createSocket {
    return [KAASocket socketWithHost:[self.currentServer getHost] port:[self.currentServer getPort]];
}

- (void)onServerFailed {
    [self onServerFailedWithFailoverStatus:FailoverStatusNoConnectivity];
}

- (void)onServerFailedWithFailoverStatus:(FailoverStatus)status {
    DDLogInfo(@"%@ [%@] has failed", TAG, [self getId]);
    [self closeConnection];
    if (self.checker && ![self.checker isConnected]) {
        DDLogWarn(@"%@ Loss of connectivity detected", TAG);
        FailoverDecision *decision = [self.failoverManager decisionOnFailoverStatus:status];
        switch (decision.failoverAction) {
            case FailoverActionNoop:
                DDLogWarn(@"%@ No operation is performed according to failover strategy decision", TAG);
                break;
            case FailoverActionRetry:
            {
                int64_t retryPeriod = decision.retryPeriod;
                DDLogWarn(@"%@ Attempt to reconnect will be made in %lli ms according to failover strategy decision", TAG, retryPeriod);
                [self scheduleOpenConnectionTaskWithRetryPeriod:retryPeriod];
            }
                break;
            case FailoverActionFailure:
                DDLogWarn(@"%@ Calling failure delegate according to failover strategy decision!", TAG);
                [self.failureDelegate onFailure];
                break;
            case FailoverActionUseNextBootstrap:
            case FailoverActionUseNextOperations:
                DDLogWarn(@"%@ Failover actions NEXT_BOOTSTRAP & NEXT_OPERATIONS not supported yet!", TAG);
                break;
        }
    } else {
        [self.failoverManager onServerFailedWithConnectionInfo:self.currentServer failoverStatus:status];
    }
}

- (void)scheduleOpenConnectionTaskWithRetryPeriod:(int64_t)retryPeriod {
    @synchronized(self) {
        if (!self.isOpenConnectionScheduled) {
            if (self.executor) {
                DDLogInfo(@"%@ Scheduling open connection task", TAG);
                [self.executor addOperation:[[OpenConnectionTask alloc] initWithChannel:self delay:retryPeriod]];
                self.isOpenConnectionScheduled = YES;
            } else {
                DDLogWarn(@"%@ Executor is nil, can't schedule open connection task", TAG);
            }
        } else {
            DDLogInfo(@"%@ Reconnect is already scheduled, ignoring the call", TAG);
        }
    }
}

- (void)schedulePingTask {
    self.isPingTaskCancelled = NO;
    if (self.executor) {
        [self.executor addOperationWithBlock:^{
            dispatch_time_t time = dispatch_time(DISPATCH_TIME_NOW, (int64_t)PING_TIMEOUT_SEC * NSEC_PER_SEC);
            dispatch_after(time, dispatch_get_global_queue(QOS_CLASS_UTILITY, 0), ^{
                @try {
                    DDLogInfo(@"%@ Executing ping task for channel [%@]", TAG, [self getId]);
                    if (self.isPingTaskCancelled)   {
                        DDLogInfo(@"%@ Can't schedule new ping task for channel [%@]. Task was cancelled.", TAG, [self getId]);
                    } else {
                        [self sendPingRequest];
                        [self schedulePingTask];
                    }
                }
                @catch (NSException *ex) {
                    DDLogError(@"%@ Failed to send ping request for channel [%@]: %@. Reason: %@", TAG, [self getId], ex.name, ex.reason);
                    [self onServerFailed];
                }
            });
        }];
        DDLogDebug(@"%@ Submitting a ping task for channel [%@]", TAG, [self getId]);
    } else {
        DDLogWarn(@"%@ Executor is nil, can't schedule ping connection task", TAG);
    }
}

- (NSOperationQueue *)createExecutor {
    DDLogDebug(@"%@ Creating a new executor for channel [%@]", TAG, [self getId]);
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    queue.maxConcurrentOperationCount = MAX_THREADS_COUNT;
    return queue;
}

- (void)syncForTransportType:(TransportType)type {
    @synchronized(self) {
        [self syncTransportTypes:[NSSet setWithObject:@(type)]];
    }
}

- (void)syncTransportTypes:(NSSet *)types {
    @synchronized(self) {
        if (self.channelState == CHANNEL_STATE_SHUTDOWN) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is down", TAG, [self getId]);
            return;
        }
        if (self.channelState == CHANNEL_STATE_PAUSE) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is paused", TAG, [self getId]);
            return;
        }
        if (self.channelState != CHANNEL_STATE_OPENED) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is waiting for CONNACK message + KAASYNC message", TAG, [self getId]);
            return;
        }
        if (!self.multiplexer) {
            DDLogWarn(@"%@ Can't sync. Channel %@ multiplexer is not set", TAG, [self getId]);
            return;
        }
        if (!self.demultiplexer) {
            DDLogWarn(@"%@ Can't sync. Channel %@ demultiplexer is not set", TAG, [self getId]);
            return;
        }
        if (!self.currentServer || !self.socket) {
            DDLogWarn(@"%@ Can't sync. Server is %@, socket is %@", TAG, self.currentServer, self.socket);
            return;
        }
        
        NSMutableDictionary *typeMap = [NSMutableDictionary dictionaryWithCapacity:[[self getSupportedTransportTypes] count]];
        for (NSNumber *typeNum in types) {
            DDLogInfo(@"%@ Processing sync %i for channel [%@]", TAG, [typeNum intValue], [self getId]);
            NSNumber *directionNum = [self getSupportedTransportTypes][typeNum];
            if (directionNum) {
                typeMap[typeNum] = directionNum;
            } else {
                DDLogError(@"%@ Unsupported type %i for channel [%@]", TAG, [typeNum intValue], [self getId]);
            }
            for (NSNumber *transportType in [self getSupportedTransportTypes].allKeys) {
                if (![transportType isEqualToNumber:typeNum]) {
                    typeMap[transportType] = @(CHANNEL_DIRECTION_DOWN);
                }
            }
        }
        
        @try {
            [self sendKaaSyncRequestWithTypes:typeMap];
        }
        @catch (NSException *ex) {
            DDLogError(@"%@ Failed to sync channel %@: %@, reason: %@", TAG, [self getId], ex.name, ex.reason);
        }
    }
}

- (void)syncAll {
    @synchronized(self) {
        if (self.channelState == CHANNEL_STATE_SHUTDOWN) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is down", TAG, [self getId]);
            return;
        }
        if (self.channelState == CHANNEL_STATE_PAUSE) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is paused", TAG, [self getId]);
            return;
        }
        if (self.channelState != CHANNEL_STATE_OPENED) {
            DDLogInfo(@"%@ Can't sync. Channel %@ is waiting for CONNACK message + KAASYNC message", TAG, [self getId]);
            return;
        }
        if (!self.multiplexer || !self.demultiplexer) {
            DDLogWarn(@"%@ Can't sync. Multiplexer/Demultiplexer for channel [%@] not set", TAG, [self getId]);
            return;
        }
        if (!self.currentServer || !self.socket) {
            DDLogWarn(@"%@ Can't sync. Server is %@, socket is %@", TAG, self.currentServer, self.socket);
            return;
        }
        DDLogInfo(@"%@ Processing sync all for channel [%@]", TAG, [self getId]);
        @try {
            [self sendKaaSyncRequestWithTypes:[self getSupportedTransportTypes]];
        }
        @catch (NSException *ex) {
            DDLogError(@"%@ Failed to sync channel %@: %@, reason: %@", TAG, [self getId], ex.name, ex.reason);
            [self onServerFailed];
        }
    }
}

- (void)syncAckForTransportType:(TransportType)type {
    DDLogInfo(@"%@ Adding sync acknowledgement for type %i as a regular sync for channel [%@]", TAG, type, [self getId]);
    [self syncAckForTransportTypes:[NSSet setWithObject:@(type)]];
}

- (void)syncAckForTransportTypes:(NSSet *)types {
    @synchronized(self) {
        if (self.channelState != CHANNEL_STATE_OPENED) {
            DDLogInfo(@"%@ First KaaSync message received and processed for channel [%@]", TAG, [self getId]);
            self.channelState = CHANNEL_STATE_OPENED;
            [self.failoverManager onServerConnectedWithConnectionInfo:self.currentServer];
            DDLogDebug(@"%@ There are pending requests for channel [%@] -> starting sync", TAG, [self getId]);
            [self syncAll];
        } else {
            DDLogDebug(@"%@ Acknowledgment is pending for channel [%@] -> starting sync", TAG, [self getId]);
            if ([types count] == 1) {
                [self syncForTransportType:[types.anyObject intValue]];
            } else {
                [self syncAll];
            }
        }
    }
}

- (void)setDemultiplexer:(id<KaaDataDemultiplexer>)demultiplexer {
    @synchronized(self) {
        if (demultiplexer) {
            _demultiplexer = demultiplexer;
        }
    }
}

- (void)setMultiplexer:(id<KaaDataMultiplexer>)multiplexer {
    @synchronized(self) {
        if (multiplexer) {
            _multiplexer = multiplexer;
        }
    }
}

- (void)setServer:(id<TransportConnectionInfo>)server {
    [self setServer:server withKeyPair:nil];
}

- (void)setServer:(id<TransportConnectionInfo>)server withKeyPair:(KeyPair *)sentKeyPair {
    @synchronized(self) {
        if (!server) {
            DDLogWarn(@"%@ Server is nil for channel [%@]", TAG, [self getId]);
            return;
        }
        if (self.channelState == CHANNEL_STATE_SHUTDOWN) {
            DDLogWarn(@"%@ Can't set server. Channel [%@] is down", TAG, [self getId]);
            return;
        }
        DDLogInfo(@"%@ Setting server [%@] for channel [%@]", TAG, server, [self getId]);
        IPTransportInfo *oldServer = self.currentServer;
        self.currentServer = [[IPTransportInfo alloc] initWithTransportInfo:server];
        KeyPair *keyPair;
        if (sentKeyPair) {
            keyPair = sentKeyPair;
        } else {
            keyPair = [[KeyPair alloc] initWithPrivateKeyRef:[self.state privateKey] publicKeyRef:[self.state publicKey]];
        }
        self.encoderDecoder = [[MessageEncoderDecoder alloc] initWithKeyPair:keyPair remotePublicKey:[self.currentServer getPublicKey]];
        if (self.channelState != CHANNEL_STATE_PAUSE) {
            if (!self.executor) {
                self.executor = [self createExecutor];
            }
            if (!oldServer
                || !self.socket
                || ![[oldServer getHost] isEqualToString:[self.currentServer getHost]]
                || [oldServer getPort] != [self.currentServer getPort]) {
                DDLogInfo(@"%@ New server's: %@ host or ip is different from the old %@, reconnecting",
                          TAG, self.currentServer, oldServer);
                [self closeConnection];
                [self scheduleOpenConnectionTaskWithRetryPeriod:0];
            }
        } else {
            DDLogInfo(@"%@ Can't start new session. Channel [%@] is paused", TAG, [self getId]);
        }
    }
}

- (id<TransportConnectionInfo>)getServer {
    return _currentServer;
}

- (void)setConnectivityChecker:(ConnectivityChecker *)checker {
    _checker = checker;
}

- (void)shutdown {
    @synchronized(self) {
        DDLogInfo(@"%@ Shutting down...", TAG);
        self.channelState = CHANNEL_STATE_SHUTDOWN;
        [self closeConnection];
        [self destroyExecutor];
    }
}

- (void)pause {
    @synchronized(self) {
        if (self.channelState != CHANNEL_STATE_PAUSE) {
            DDLogInfo(@"%@ Pausing...", TAG);
            self.channelState = CHANNEL_STATE_PAUSE;
            [self closeConnection];
            [self destroyExecutor];
        }
    }
}

- (void)resume {
    @synchronized(self) {
        if (self.channelState == CHANNEL_STATE_PAUSE) {
            DDLogInfo(@"%@ Resuming...", TAG);
            self.channelState = CHANNEL_STATE_CLOSED;
            if (!self.executor) {
                self.executor = [self createExecutor];
            }
            [self scheduleOpenConnectionTaskWithRetryPeriod:0];
        }
    }
}

- (NSString *)getId {
    return CHANNEL_ID;
}

- (TransportProtocolId *)getTransportProtocolId {
    return [TransportProtocolIdHolder TCPTransportID];
}

- (ServerType)getServerType {
    return SERVER_OPERATIONS;
}

- (NSDictionary *)getSupportedTransportTypes {
    return self.supportedTypes;
}

- (void)destroyExecutor {
    @synchronized(self) {
        if (self.executor) {
            [self.executor cancelAllOperations];
            self.isOpenConnectionScheduled = NO;
            self.executor = nil;
        }
    }
}

@end

@implementation OpenConnectionTask

- (instancetype)initWithChannel:(DefaultOperationTcpChannel *)channel delay:(int64_t)delay {
    self = [super init];
    if (self) {
        _channel = channel;
        _delay = delay;
    }
    return self;
}

- (void)main {
    if (self.isFinished || self.isCancelled) {
        DDLogWarn(@"%@ Can't run OpenConnectionTask: task was cancelled/finished", TAG);
        return;
    }
    [NSThread sleepForTimeInterval:self.delay / 1000];
    [self.channel openConnection];
}

@end
