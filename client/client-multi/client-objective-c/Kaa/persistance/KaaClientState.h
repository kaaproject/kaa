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

#ifndef Kaa_KaaClientState_h
#define Kaa_KaaClientState_h

#import <Security/Security.h>
#import "EndpointKeyHash.h"
#import "EndpointObjectHash.h"
#import "EndpointGen.h"

/**
 * Protocol provides basic methods used store and retrieve Kaa client state.
 */
@protocol KaaClientState

/**
 * Returns existing or generates new private key.
 */
@property (nonatomic, readonly) SecKeyRef privateKey;

/**
 * Returns existing or generates new public key.
 */
@property (nonatomic, readonly) SecKeyRef publicKey;

/**
 * Defines whether server knows about current endpoint profile.
 */
@property (nonatomic) BOOL isRegistred;

/**
 * Unique endpoint identifier.
 */
@property (nonatomic, strong, readonly) EndpointKeyHash *endpointKeyHash;

/**
 * Holds application state sequence number.
 */
@property (nonatomic) int32_t appStateSequenceNumber;

/**
 * Holds endpoint profile hash.
 */
@property (nonatomic, strong) EndpointObjectHash *profileHash;

/**
 * @return <EndpointAccessToken, EndpointKeyHash> as key-value.
 */
@property (nonatomic, strong) NSMutableDictionary *attachedEndpoints;

/**
 * Holds current endpoint access token.
 */
@property (nonatomic, strong) NSString *endpointAccessToken;

/**
 * Holds current event sequence number.
 */
@property (atomic) int32_t eventSequenceNumber;

/**
 * Defines whether endpoint is attached to its user.
 */
@property (nonatomic) BOOL isAttachedToUser;

/**
 * Holds hash calculated from list of available topics. See TopicListHashCalculator for more info.
 */
@property (nonatomic) int32_t topicListHash;

/**
 * Defines whether server need profile resync.
 */
@property (nonatomic) BOOL needProfileResync;

/**
 * Returns existing or generates new public key as bytes.
 */
- (NSData *)publicKeyAsBytes;

/**
 * Adds topic with all its information.
 */
- (void)addTopic:(Topic *)topic;

/**
 * Remove topic with all its information by topic id.
 */
- (void)removeTopicId:(int64_t)topicId;

/**
 * Add subscription for topic by ID.
 */
- (void)addSubscriptionForTopicWithId:(int64_t)topicId;

/**
 * Remove subscription for topic by ID.
 */
- (void)removeSubscriptionForTopicWithId:(int64_t)topicId;

/**
 * Used to update subscription info.
 */
- (BOOL)updateSubscriptionInfoForTopicId:(int64_t)topicId sequence:(int32_t)sequenceNumber;

/**
 * Return dictionary with notification subscriptions information with Topic id as key 
 * and sequence number of TopicSubscriptionInfo as value.
 *
 * @return Dictionary of <@(int64_t), @(int32_t)> as key-value.
 */
- (NSDictionary *)getNotificationSubscriptions;

/**
 * @see Topic
 * @return Array of topics
 */
- (NSArray *)getTopics;

/**
 * @return Next event sequence number that could be used by the system.
 */
- (int32_t)getAndIncrementEventSequenceNumber;

/**
 * <p></p>
 * @return YES - if configuration version was updated, NO - if it wasn't.
 */
- (BOOL)isConfigurationVersionUpdated;

/**
 * Persists current client state.
 */
- (void)persist;

/**
 * Generates and stores new endpoint access token.
 *
 * @return New access token
 */
- (NSString *)refreshEndpointAccessToken;

/**
 * Cleans up persisted client state.
 */
- (void)clean;

@end

#endif