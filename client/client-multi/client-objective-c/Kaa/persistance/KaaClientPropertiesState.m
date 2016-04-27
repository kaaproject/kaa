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

#import "KaaClientPropertiesState.h"
#import "KeyPair.h"
#import "EndpointAccessToken.h"
#import "EndpointKeyHash.h"
#import "AvroBytesConverter.h"
#import "EndpointGen.h"
#import "NSData+Conversion.h"
#import "UUID.h"
#import "KeyUtils.h"
#import "KaaLogging.h"
#import "TopicListHashCalculator.h"

#define TAG @"KaaClientPropertiesState >>>"

#define APP_STATE_SEQ_NUMBER    @"APP_STATE_SEQ_NUMBER"
#define PROFILE_HASH            @"PROFILE_HASH"
#define ENDPOINT_ACCESS_TOKEN   @"ENDPOINT_ACCESS_TOKEN"

#define ATTACHED_ENDPOINTS      @"attached_eps"
#define NF_SUBSCRIPTIONS        @"nf_subscriptions"
#define IS_REGISTERED           @"is_registered"
#define IS_ATTACHED             @"is_attached"
#define EVENT_SEQ_NUM           @"event.seq.num"
#define PROPERTIES_HASH         @"properties.hash"
#define TOPIC_LIST              @"topic.list"
#define TOPIC_LIST_HASH         @"topic.list.hash"
#define NEED_PROFILE_RESYNC     @"need.profile.resync"

@interface KaaClientPropertiesState ()

@property (nonatomic, strong) id<KAABase64> base64;
@property (nonatomic, strong) NSMutableDictionary *state;
@property (nonatomic, strong) NSString *stateFileLocation;
@property (nonatomic, strong) NSMutableDictionary *topicDictionary;            //<int64_t, Topic> as key-value
@property (nonatomic, strong) NSMutableDictionary *notificationSubscriptions;  //<int64_t, int32_t> as key-value
@property (nonatomic, strong) KeyPair *keyPair;
@property (nonatomic) BOOL isConfigVersionUpdated;
@property (nonatomic) BOOL hasUpdate;

- (void)setPropertiesHash:(NSData *)hash;
- (BOOL)isSDKProperyListUpdated:(KaaClientProperties *)sdkProperties;
- (void)parseNotificationSubscriptions;
- (void)parseTopics;
- (KeyPair *)getOrGenerateKeyPair;
- (void)deleteFileAtPath:(NSString *)path;

- (void)setStateStringValue:(NSString *)value forPropertyKey:(NSString *)propertyKey;
- (void)setStateBooleanValue:(BOOL)value forPropertyKey:(NSString *)propertyKey;

@end

@implementation KaaClientPropertiesState

@synthesize isRegistred = _isRegistred;
@synthesize privateKey = _privateKey;
@synthesize publicKey = _publicKey;
@synthesize endpointKeyHash = _endpointKeyHash;
@synthesize appStateSequenceNumber = _appStateSequenceNumber;
@synthesize profileHash = _profileHash;
@synthesize attachedEndpoints = _attachedEndpoints;
@synthesize endpointAccessToken = _endpointAccessToken;
@synthesize eventSequenceNumber = _eventSequenceNumber;
@synthesize isAttachedToUser = _isAttachedToUser;
@synthesize topicListHash = _topicListHash;
@synthesize needProfileResync = _needProfileResync;

- (instancetype)initWithBase64:(id<KAABase64>)base64 clientProperties:(KaaClientProperties *)properties {
    self = [super init];
    if (self) {
        self.base64 = base64;
        self.notificationSubscriptions = [NSMutableDictionary dictionary];
        self.topicDictionary = [NSMutableDictionary dictionary];
        self.attachedEndpoints = [NSMutableDictionary dictionary];
        self.isConfigVersionUpdated = NO;
        
        NSString *storage = [NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES) firstObject];
        NSString *stateFileName = [properties stringForKey:STATE_FILE_LOCATION_KEY];
        if (!stateFileName) {
            stateFileName = STATE_FILE_DEFAULT;
        }
        self.stateFileLocation = [storage stringByAppendingPathComponent:stateFileName];
        DDLogInfo(@"%@ Version: [%@], commit hash: [%@]", TAG, [properties buildVersion], [properties commitHash]);
        
        self.state = [NSMutableDictionary dictionary];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        if ([fileManager fileExistsAtPath:self.stateFileLocation]) {
            //TODO: check if stateFileLocation is PLIST and it has correct path for both iOS and OS X
            @try {
                self.state = [[NSMutableDictionary alloc] initWithContentsOfFile:self.stateFileLocation];
                if ([self isSDKProperyListUpdated:properties]) {
                    DDLogInfo(@"%@ SDK properties were updated", TAG);
                    [self setIsRegistred:NO];
                    [self setPropertiesHash:[properties propertiesHash]];
                } else {
                    DDLogInfo(@"%@ SDK properties are up to date", TAG);
                }
                
                [self parseTopics];
                [self parseNotificationSubscriptions];
                
                NSString *attachedEndpointsString = self.state[ATTACHED_ENDPOINTS];
                if (attachedEndpointsString) {
                    NSArray *endpointsList = [attachedEndpointsString componentsSeparatedByString:@","];
                    for (NSString *attachedEndpoint in endpointsList) {
                        if (attachedEndpoint.length == 0) {
                            continue;
                        }
                        NSArray *splittedValues = [attachedEndpoint componentsSeparatedByString:@":"];
                        EndpointKeyHash *keyHash = [[EndpointKeyHash alloc] initWithKeyHash:splittedValues[1]];
                        EndpointAccessToken *token = [[EndpointAccessToken alloc] initWithToken:splittedValues.firstObject];
                        self.attachedEndpoints[token] = keyHash;
                    }
                }
                
                NSString *eventSeqNumStr = self.state[EVENT_SEQ_NUM];
                if (eventSeqNumStr) {
                    int eventSeqNum = [eventSeqNumStr intValue];
                    if (eventSeqNum == 0) {
                        DDLogError(@"%@ Error occurred parsing event sequence number. Can not parse %@ to int",
                                   TAG, eventSeqNumStr);
                    }
                    self.eventSequenceNumber = eventSeqNum;
                }
                
                NSString *topicListHashString = self.state[TOPIC_LIST_HASH];
                if (topicListHashString) {
                    _topicListHash = [topicListHashString intValue];
                    
                }
            }
            @catch (NSException *exception) {
                DDLogError(@"%@ Can't load state file. Error name: %@. Error reason: %@",
                           TAG, exception.name, exception.reason);
            }
        } else {
            DDLogInfo(@"%@ First SDK start!", TAG);
            
            if (![fileManager fileExistsAtPath:storage]) {
                NSError *error;
                BOOL dirCreated = [fileManager createDirectoryAtPath:storage withIntermediateDirectories:YES attributes:nil error:&error];
                if (!dirCreated) {
                    DDLogError(@"%@ Creating directory for client state: %@", TAG, error);
                }
            }
            
            [self setPropertiesHash:properties.propertiesHash];
        }
    }
    return self;
}

- (BOOL)isConfigurationVersionUpdated {
    return self.isConfigVersionUpdated;
}

- (void)setTopicListHash:(int32_t)topicListHash {
    if (_topicListHash != topicListHash) {
        _topicListHash = topicListHash;
        self.hasUpdate = YES;
    }
}

- (BOOL)isRegistred {
    NSString *value = self.state[IS_REGISTERED];
    return value ? [value boolValue] : NO;
}

- (void)setIsRegistred:(BOOL)isRegistred {
    [self setStateBooleanValue:isRegistred forPropertyKey:IS_REGISTERED];
}

- (BOOL)needProfileResync {
    NSString *value = self.state[NEED_PROFILE_RESYNC];
    return value ? [value boolValue] : NO;
}

- (void)setNeedProfileResync:(BOOL)needProfileResync {
    [self setStateBooleanValue:needProfileResync forPropertyKey:NEED_PROFILE_RESYNC];
}

- (void)persist {
    if (!self.hasUpdate) {
        DDLogVerbose(@"%@ No updates: ignoring persist call", TAG);
        return;
    }
    
    NSMutableData *encodedData = [NSMutableData data];
    @try {
        for (Topic *topic in _topicDictionary.allValues) {
            
            size_t topicSize = [topic getSize];
            char *buffer = (char *)malloc((topicSize) * sizeof(char));
            avro_writer_t writer = avro_writer_memory(buffer, topicSize);
            if (!writer) {
                DDLogError(@"%@ Unable to allocate '%li'bytes for avro writer during persisting", TAG, topicSize);
                continue;
            }
            
            [topic serialize:writer];
            [encodedData appendBytes:writer->buf length:(NSUInteger)writer->written];
            avro_writer_free(writer);
        }
        NSData *base64Encoded = [self.base64 encodeBase64:encodedData];
        NSString *base64Str = [[NSString alloc] initWithData:base64Encoded encoding:NSUTF8StringEncoding];
        
        self.state[TOPIC_LIST] = base64Str;
        DDLogInfo(@"%@ Persisted %lli topics", TAG, (int64_t)[_topicDictionary.allValues count]);
    }
    @catch (NSException *ex) {
        DDLogError(@"%@ Can't persist topic list info. Error: %@, reason: %@", TAG, ex.name, ex.reason);
    }
    
    NSData *subscriptionsData = [NSKeyedArchiver archivedDataWithRootObject:_notificationSubscriptions];
    self.state[NF_SUBSCRIPTIONS] = [self.base64 encodedString:subscriptionsData];
    
    NSMutableString *attachedEndpointsString = [NSMutableString string];
    NSArray *keys = self.attachedEndpoints.allKeys;
    for (EndpointAccessToken *key in keys) {
        EndpointKeyHash *value = self.attachedEndpoints[key];
        [attachedEndpointsString appendString:[NSString stringWithFormat:@"%@:%@,", key.token, value.keyHash]];
    }
    self.state[ATTACHED_ENDPOINTS] = attachedEndpointsString;
    self.state[EVENT_SEQ_NUM] = [NSString stringWithFormat:@"%i", self.eventSequenceNumber];
    self.state[TOPIC_LIST_HASH] = @(_topicListHash);
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:self.stateFileLocation]) {
        NSString *backup = [NSString stringWithFormat:@"%@_bckp", self.stateFileLocation];
        BOOL backupResult = [fileManager copyItemAtPath:self.stateFileLocation toPath:backup error:nil];
        DDLogDebug(@"%@ Backup created: %d", TAG, backupResult);
    }
    
    BOOL peristResult = [self.state writeToFile:self.stateFileLocation atomically:YES];
    DDLogDebug(@"%@ Persist finished. Result: %d", TAG, peristResult);
}

- (NSString *)refreshEndpointAccessToken {
    NSString *newAccessToken = [UUID randomUUID];
    [self setEndpointAccessToken:newAccessToken];
    return newAccessToken;
}

- (NSData *)publicKeyAsBytes {
    [self getOrGenerateKeyPair];
    return [KeyUtils getPublicKey];
}

- (SecKeyRef)publicKey {
    return [[self getOrGenerateKeyPair] getPublicKeyRef];
}

- (SecKeyRef)privateKey {
    return [[self getOrGenerateKeyPair] getPrivateKeyRef];
}

- (EndpointKeyHash *)endpointKeyHash {
    if (!_endpointKeyHash) {
        
        //to ensure key pair generated
        [self getOrGenerateKeyPair];
        
        EndpointObjectHash *publicKeyHash = [EndpointObjectHash hashWithSHA1:[KeyUtils getPublicKey]];
        NSString *base64Str = [[NSString alloc] initWithData:[self.base64 encodeBase64:publicKeyHash.data] encoding:NSUTF8StringEncoding];
        _endpointKeyHash = [[EndpointKeyHash alloc] initWithKeyHash:base64Str];
    }
    return _endpointKeyHash;
}

- (int32_t)appStateSequenceNumber {
    NSString *value = self.state[APP_STATE_SEQ_NUMBER];
    return value ? [value intValue] : 1;
}

- (EndpointObjectHash *)profileHash {
    NSString *hash = self.state[PROFILE_HASH];
    if (!hash) {
        hash = [[NSString alloc] initWithData:[self.base64 encodeBase64:[NSData data]] encoding:NSUTF8StringEncoding];
    }
    return [EndpointObjectHash hashWithBytes:[self.base64 decodeBase64:[hash dataUsingEncoding:NSUTF8StringEncoding]]];
}

- (void)setAppStateSequenceNumber:(int32_t)appStateSequenceNumber {
    [self setStateStringValue:[@(appStateSequenceNumber) stringValue] forPropertyKey:APP_STATE_SEQ_NUMBER];
}

- (void)setProfileHash:(EndpointObjectHash *)profileHash {
    NSData *base64Data = [self.base64 encodeBase64:profileHash.data];
    NSString *base64Str = [[NSString alloc] initWithData:base64Data encoding:NSUTF8StringEncoding];
    [self setStateStringValue:base64Str forPropertyKey:PROFILE_HASH];
}

- (void)addTopic:(Topic *)topic {
    NSNumber *topicId = @(topic.id);
    if (!_topicDictionary[topicId]) {
        _topicDictionary[topicId] = topic;
        if (topic.subscriptionType == SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION) {
            _notificationSubscriptions[topicId] = @(0);
            DDLogInfo(@"%@ Adding new seqNumber 0 for subscription with topic id: %lli", TAG, topic.id);
        }
        _hasUpdate = YES;
        DDLogInfo(@"%@ Adding new topic with id: %lli ", TAG, topic.id);
    }
}

- (void)removeTopicId:(int64_t)topicId {
    NSNumber *key = @(topicId);
    if ([_topicDictionary objectForKey:key]) {
        [_topicDictionary removeObjectForKey:key];
        [_notificationSubscriptions removeObjectForKey:key];
        DDLogInfo(@"%@ Removed topic and subscription info for topic id: %lli", TAG, topicId);
        _hasUpdate = YES;
    }
    
}

- (void)addSubscriptionForTopicWithId:(int64_t)topicId {
    NSNumber *sequenceNumber = _notificationSubscriptions[@(topicId)];
    if (!sequenceNumber) {
        _notificationSubscriptions[@(topicId)] = @(0);
        DDLogInfo(@"%@ Added new sequence number 0 for subscription with topic id: %lli", TAG, topicId);
        _hasUpdate = YES;
    }
}

- (void)removeSubscriptionForTopicWithId:(int64_t)topicId {
    NSNumber *sequenceNumber = _notificationSubscriptions[@(topicId)];
    if (sequenceNumber) {
        [_notificationSubscriptions removeObjectForKey:@(topicId)];
        DDLogInfo(@"%@ Removed subscription info for topic with id: %lli", TAG, topicId);
        _hasUpdate = YES;
    }
}

- (BOOL)updateSubscriptionInfoForTopicId:(int64_t)topicId sequence:(int32_t)sequenceNumber {
    NSNumber *seqNum = self.notificationSubscriptions[@(topicId)];
    BOOL updated = NO;
    if (seqNum && sequenceNumber > [seqNum intValue]) {
        updated = YES;
        self.notificationSubscriptions[@(topicId)] = @(sequenceNumber);
        self.hasUpdate = YES;
        DDLogDebug(@"%@ Updated seqNumber to %i for %lld subscription", TAG, sequenceNumber, topicId);
    }
    return updated;
}

- (NSDictionary *)getNotificationSubscriptions {
    return _notificationSubscriptions;
}

- (NSArray *)getTopics {
    return _topicDictionary.allValues;
}

- (void)setAttachedEndpoints:(NSMutableDictionary *)attachedEndpoints {
    [self.attachedEndpoints removeAllObjects];
    [self.attachedEndpoints addEntriesFromDictionary:attachedEndpoints];
    self.hasUpdate = YES;
}

- (void)setEndpointAccessToken:(NSString *)endpointAccessToken {
    [self setStateStringValue:endpointAccessToken forPropertyKey:ENDPOINT_ACCESS_TOKEN];
}

- (NSString *)endpointAccessToken {
    NSString *token = self.state[ENDPOINT_ACCESS_TOKEN];
    return token ?: @"";
}

- (int32_t)getAndIncrementEventSequenceNumber {
    self.hasUpdate = YES;
    return self.eventSequenceNumber++;
}

- (void)setEventSequenceNumber:(int32_t)eventSequenceNumber {
    if (eventSequenceNumber != _eventSequenceNumber) {
        _eventSequenceNumber = eventSequenceNumber;
        self.hasUpdate = YES;
    }
}

- (BOOL)isAttachedToUser {
    NSString *value = self.state[IS_ATTACHED];
    return value ? [value boolValue] : NO;
}

- (void)setIsAttachedToUser:(BOOL)isAttachedToUser {
    [self setStateBooleanValue:isAttachedToUser forPropertyKey:IS_ATTACHED];
}

- (void)clean {
    [self setIsRegistred:NO];
    [self setNeedProfileResync:NO];
    [self deleteFileAtPath:self.stateFileLocation];
    [self deleteFileAtPath:[NSString stringWithFormat:@"%@_bckp", self.stateFileLocation]];
    self.hasUpdate = YES;
}

- (void)setPropertiesHash:(NSData *)hash {
    NSData *encodedHash = [self.base64 encodeBase64:hash];
    NSString *encodedStr = [[NSString alloc] initWithData:encodedHash encoding:NSUTF8StringEncoding];
    [self setStateStringValue:encodedStr forPropertyKey:PROPERTIES_HASH];
}

- (BOOL)isSDKProperyListUpdated:(KaaClientProperties *)sdkProperties {
    NSData *hashFromSDK = [sdkProperties propertiesHash];
    NSString *stateHash = self.state[PROPERTIES_HASH];
    if (!stateHash) {
        NSData *emptyData = [self.base64 encodeBase64:[NSData data]];
        stateHash = [[NSString alloc] initWithData:emptyData encoding:NSUTF8StringEncoding];
    }
    NSData *hashFromStateFile = [self.base64 decodeBase64:[stateHash dataUsingEncoding:NSUTF8StringEncoding]];
    return ![hashFromSDK isEqualToData:hashFromStateFile];
}

- (void)parseNotificationSubscriptions {
    NSString *encodedSubscriptions = self.state[NF_SUBSCRIPTIONS];
    if (encodedSubscriptions) {
        NSData *decodedSubscriptions = [self.base64 decodeString:encodedSubscriptions];
        [_notificationSubscriptions addEntriesFromDictionary:[NSKeyedUnarchiver unarchiveObjectWithData:decodedSubscriptions]];
    } else {
        DDLogInfo(@"%@ No subscription info found in state", TAG);
    }
}

- (void)parseTopics {
    NSString *topicListStr = self.state[TOPIC_LIST];
    if (topicListStr) {
        NSData *data = [self.base64 decodeString:topicListStr];
        Topic *topic = nil;
        avro_reader_t reader = avro_reader_memory([data bytes], [data length]);
        @try {
            while (reader->read < reader->len) {
                topic = [[Topic alloc] init];
                [topic deserialize:reader];
                DDLogDebug(@"%@ Loaded topic: %@", TAG, topic);
                _topicDictionary[@(topic.id)] = topic;
            }
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Error occurred while reading information from decoder: %@", TAG, [data hexadecimalString]);
        }
        avro_reader_free(reader);
    } else {
        DDLogInfo(@"%@ No topic list found!", TAG);
    }
}

- (KeyPair *)getOrGenerateKeyPair {
    if (self.keyPair) {
        return self.keyPair;
    }
    
    SecKeyRef private = [KeyUtils getPrivateKeyRef];
    SecKeyRef public = [KeyUtils getPublicKeyRef];
    if (private != NULL && public != NULL) {
        DDLogDebug(@"%@ Found existing key pair", TAG);
        self.keyPair = [[KeyPair alloc] initWithPrivateKeyRef:private publicKeyRef:public];
        return self.keyPair;
    }
    
    DDLogDebug(@"%@ Generating new key pair", TAG);
    self.keyPair = [KeyUtils generateKeyPair];
    return self.keyPair;
}

- (void)deleteFileAtPath:(NSString *)path {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager removeItemAtPath:path error:NULL] == NO) {
        DDLogWarn(@"%@ Unable to remove file at path: %@", TAG, path);
    }
}

- (void)setStateStringValue:(NSString *)value forPropertyKey:(NSString *)propertyKey {
    NSString *previous = self.state[propertyKey];
    self.state[propertyKey] = value;
    self.hasUpdate |= ![value isEqualToString:previous];
}

- (void)setStateBooleanValue:(BOOL)value forPropertyKey:(NSString *)propertyKey {
    NSString *previousRawValue = self.state[propertyKey];
    BOOL previousValue = NO;
    if (previousRawValue && previousRawValue.length > 0) {
        previousValue = [previousRawValue boolValue];
    }
    self.state[propertyKey] = value ? @"Y" : @"N";
    self.hasUpdate |= value != previousValue;
}

@end
