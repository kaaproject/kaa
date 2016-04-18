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

/*
 * AUTO-GENERATED CODE
 */

#import <Foundation/Foundation.h>
#import "AvroUtils.h"
#import "AvroBased.h"
#import "KAAUnion.h"


@interface TopicState : AvroBased

@property(nonatomic) int64_t topicId;
@property(nonatomic) int32_t seqNumber;

- (instancetype)initWithTopicId:(int64_t)topicId seqNumber:(int32_t)seqNumber;

@end


typedef enum {
    SYNC_RESPONSE_STATUS_NO_DELTA,
    SYNC_RESPONSE_STATUS_DELTA,
    SYNC_RESPONSE_STATUS_RESYNC,
} SyncResponseStatus;


typedef enum {
    NOTIFICATION_TYPE_SYSTEM,
    NOTIFICATION_TYPE_CUSTOM,
} NotificationType;


typedef enum {
    SUBSCRIPTION_TYPE_MANDATORY_SUBSCRIPTION,
    SUBSCRIPTION_TYPE_OPTIONAL_SUBSCRIPTION,
} SubscriptionType;


typedef enum {
    SUBSCRIPTION_COMMAND_TYPE_ADD,
    SUBSCRIPTION_COMMAND_TYPE_REMOVE,
} SubscriptionCommandType;


typedef enum {
    SYNC_RESPONSE_RESULT_TYPE_SUCCESS,
    SYNC_RESPONSE_RESULT_TYPE_FAILURE,
    SYNC_RESPONSE_RESULT_TYPE_PROFILE_RESYNC,
    SYNC_RESPONSE_RESULT_TYPE_REDIRECT,
} SyncResponseResultType;


typedef enum {
    LOG_DELIVERY_ERROR_CODE_NO_APPENDERS_CONFIGURED,
    LOG_DELIVERY_ERROR_CODE_APPENDER_INTERNAL_ERROR,
    LOG_DELIVERY_ERROR_CODE_REMOTE_CONNECTION_ERROR,
    LOG_DELIVERY_ERROR_CODE_REMOTE_INTERNAL_ERROR,
} LogDeliveryErrorCode;


typedef enum {
    USER_ATTACH_ERROR_CODE_NO_VERIFIER_CONFIGURED,
    USER_ATTACH_ERROR_CODE_TOKEN_INVALID,
    USER_ATTACH_ERROR_CODE_TOKEN_EXPIRED,
    USER_ATTACH_ERROR_CODE_INTERNAL_ERROR,
    USER_ATTACH_ERROR_CODE_CONNECTION_ERROR,
    USER_ATTACH_ERROR_CODE_REMOTE_ERROR,
    USER_ATTACH_ERROR_CODE_OTHER,
} UserAttachErrorCode;


@interface SubscriptionCommand : AvroBased

@property(nonatomic) int64_t topicId;
@property(nonatomic) SubscriptionCommandType command;

- (instancetype)initWithTopicId:(int64_t)topicId command:(SubscriptionCommandType)command;

@end


@interface UserAttachRequest : AvroBased

@property(nonatomic, strong) NSString *userVerifierId;
@property(nonatomic, strong) NSString *userExternalId;
@property(nonatomic, strong) NSString *userAccessToken;

- (instancetype)initWithUserVerifierId:(NSString *)userVerifierId userExternalId:(NSString *)userExternalId userAccessToken:(NSString *)userAccessToken;

@end


@interface UserAttachResponse : AvroBased

@property(nonatomic) SyncResponseResultType result;
@property(nonatomic, strong) KAAUnion *errorCode;
@property(nonatomic, strong) KAAUnion *errorReason;

- (instancetype)initWithResult:(SyncResponseResultType)result errorCode:(KAAUnion *)errorCode errorReason:(KAAUnion *)errorReason;


# ifndef KAA_UNION_USER_ATTACH_ERROR_CODE_OR_NULL_H_
# define KAA_UNION_USER_ATTACH_ERROR_CODE_OR_NULL_H_

# define KAA_UNION_USER_ATTACH_ERROR_CODE_OR_NULL_BRANCH_0    0
# define KAA_UNION_USER_ATTACH_ERROR_CODE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_USER_ATTACH_ERROR_CODE_OR_NULL_H_


# ifndef KAA_UNION_STRING_OR_NULL_H_
# define KAA_UNION_STRING_OR_NULL_H_

# define KAA_UNION_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_STRING_OR_NULL_H_

@end


@interface UserAttachNotification : AvroBased

@property(nonatomic, strong) NSString *userExternalId;
@property(nonatomic, strong) NSString *endpointAccessToken;

- (instancetype)initWithUserExternalId:(NSString *)userExternalId endpointAccessToken:(NSString *)endpointAccessToken;

@end


@interface UserDetachNotification : AvroBased

@property(nonatomic, strong) NSString *endpointAccessToken;

- (instancetype)initWithEndpointAccessToken:(NSString *)endpointAccessToken;

@end


@interface EndpointAttachRequest : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) NSString *endpointAccessToken;

- (instancetype)initWithRequestId:(int32_t)requestId endpointAccessToken:(NSString *)endpointAccessToken;

@end


@interface EndpointAttachResponse : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) KAAUnion *endpointKeyHash;
@property(nonatomic) SyncResponseResultType result;

- (instancetype)initWithRequestId:(int32_t)requestId endpointKeyHash:(KAAUnion *)endpointKeyHash result:(SyncResponseResultType)result;


# ifndef KAA_UNION_STRING_OR_NULL_H_
# define KAA_UNION_STRING_OR_NULL_H_

# define KAA_UNION_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_STRING_OR_NULL_H_

@end


@interface EndpointDetachRequest : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) NSString *endpointKeyHash;

- (instancetype)initWithRequestId:(int32_t)requestId endpointKeyHash:(NSString *)endpointKeyHash;

@end


@interface EndpointDetachResponse : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic) SyncResponseResultType result;

- (instancetype)initWithRequestId:(int32_t)requestId result:(SyncResponseResultType)result;

@end


@interface Event : AvroBased

@property(nonatomic) int32_t seqNum;
@property(nonatomic, strong) NSString *eventClassFQN;
@property(nonatomic, strong) NSData *eventData;
@property(nonatomic, strong) KAAUnion *source;
@property(nonatomic, strong) KAAUnion *target;

- (instancetype)initWithSeqNum:(int32_t)seqNum eventClassFQN:(NSString *)eventClassFQN eventData:(NSData *)eventData source:(KAAUnion *)source target:(KAAUnion *)target;


# ifndef KAA_UNION_STRING_OR_NULL_H_
# define KAA_UNION_STRING_OR_NULL_H_

# define KAA_UNION_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_STRING_OR_NULL_H_


# ifndef KAA_UNION_STRING_OR_NULL_H_
# define KAA_UNION_STRING_OR_NULL_H_

# define KAA_UNION_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_STRING_OR_NULL_H_

@end


@interface EventListenersRequest : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) NSArray *eventClassFQNs;

- (instancetype)initWithRequestId:(int32_t)requestId eventClassFQNs:(NSArray *)eventClassFQNs;

@end


@interface EventListenersResponse : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) KAAUnion *listeners;
@property(nonatomic) SyncResponseResultType result;

- (instancetype)initWithRequestId:(int32_t)requestId listeners:(KAAUnion *)listeners result:(SyncResponseResultType)result;


# ifndef KAA_UNION_ARRAY_STRING_OR_NULL_H_
# define KAA_UNION_ARRAY_STRING_OR_NULL_H_

# define KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_STRING_OR_NULL_H_

@end


@interface EventSequenceNumberRequest : AvroBased



@end


@interface EventSequenceNumberResponse : AvroBased

@property(nonatomic) int32_t seqNum;

- (instancetype)initWithSeqNum:(int32_t)seqNum;

@end


@interface Notification : AvroBased

@property(nonatomic) int64_t topicId;
@property(nonatomic) NotificationType type;
@property(nonatomic, strong) KAAUnion *uid;
@property(nonatomic, strong) KAAUnion *seqNumber;
@property(nonatomic, strong) NSData *body;

- (instancetype)initWithTopicId:(int64_t)topicId type:(NotificationType)type uid:(KAAUnion *)uid seqNumber:(KAAUnion *)seqNumber body:(NSData *)body;


# ifndef KAA_UNION_STRING_OR_NULL_H_
# define KAA_UNION_STRING_OR_NULL_H_

# define KAA_UNION_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_STRING_OR_NULL_H_


# ifndef KAA_UNION_INT_OR_NULL_H_
# define KAA_UNION_INT_OR_NULL_H_

# define KAA_UNION_INT_OR_NULL_BRANCH_0    0
# define KAA_UNION_INT_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_INT_OR_NULL_H_

@end


@interface Topic : AvroBased

@property(nonatomic) int64_t id;
@property(nonatomic, strong) NSString *name;
@property(nonatomic) SubscriptionType subscriptionType;

- (instancetype)initWithId:(int64_t)id name:(NSString *)name subscriptionType:(SubscriptionType)subscriptionType;

@end


@interface LogEntry : AvroBased

@property(nonatomic, strong) NSData *data;

- (instancetype)initWithData:(NSData *)data;

@end


@interface SyncRequestMetaData : AvroBased

@property(nonatomic, strong) NSString *sdkToken;
@property(nonatomic, strong) KAAUnion *endpointPublicKeyHash;
@property(nonatomic, strong) KAAUnion *profileHash;
@property(nonatomic, strong) KAAUnion *timeout;

- (instancetype)initWithSdkToken:(NSString *)sdkToken endpointPublicKeyHash:(KAAUnion *)endpointPublicKeyHash profileHash:(KAAUnion *)profileHash timeout:(KAAUnion *)timeout;


# ifndef KAA_UNION_BYTES_OR_NULL_H_
# define KAA_UNION_BYTES_OR_NULL_H_

# define KAA_UNION_BYTES_OR_NULL_BRANCH_0    0
# define KAA_UNION_BYTES_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BYTES_OR_NULL_H_


# ifndef KAA_UNION_BYTES_OR_NULL_H_
# define KAA_UNION_BYTES_OR_NULL_H_

# define KAA_UNION_BYTES_OR_NULL_BRANCH_0    0
# define KAA_UNION_BYTES_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BYTES_OR_NULL_H_


# ifndef KAA_UNION_LONG_OR_NULL_H_
# define KAA_UNION_LONG_OR_NULL_H_

# define KAA_UNION_LONG_OR_NULL_BRANCH_0    0
# define KAA_UNION_LONG_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_LONG_OR_NULL_H_

@end


@interface ProfileSyncRequest : AvroBased

@property(nonatomic, strong) KAAUnion *endpointPublicKey;
@property(nonatomic, strong) NSData *profileBody;
@property(nonatomic, strong) KAAUnion *endpointAccessToken;

- (instancetype)initWithEndpointPublicKey:(KAAUnion *)endpointPublicKey profileBody:(NSData *)profileBody endpointAccessToken:(KAAUnion *)endpointAccessToken;


# ifndef KAA_UNION_BYTES_OR_NULL_H_
# define KAA_UNION_BYTES_OR_NULL_H_

# define KAA_UNION_BYTES_OR_NULL_BRANCH_0    0
# define KAA_UNION_BYTES_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BYTES_OR_NULL_H_


# ifndef KAA_UNION_STRING_OR_NULL_H_
# define KAA_UNION_STRING_OR_NULL_H_

# define KAA_UNION_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_STRING_OR_NULL_H_

@end


@interface ProtocolVersionPair : AvroBased

@property(nonatomic) int32_t id;
@property(nonatomic) int32_t version;

- (instancetype)initWithId:(int32_t)id version:(int32_t)version;

@end


@interface BootstrapSyncRequest : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) NSArray *supportedProtocols;

- (instancetype)initWithRequestId:(int32_t)requestId supportedProtocols:(NSArray *)supportedProtocols;

@end


@interface ConfigurationSyncRequest : AvroBased

@property(nonatomic, strong) NSData *configurationHash;
@property(nonatomic, strong) KAAUnion *resyncOnly;

- (instancetype)initWithConfigurationHash:(NSData *)configurationHash resyncOnly:(KAAUnion *)resyncOnly;


# ifndef KAA_UNION_BOOLEAN_OR_NULL_H_
# define KAA_UNION_BOOLEAN_OR_NULL_H_

# define KAA_UNION_BOOLEAN_OR_NULL_BRANCH_0    0
# define KAA_UNION_BOOLEAN_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BOOLEAN_OR_NULL_H_

@end


@interface NotificationSyncRequest : AvroBased

@property(nonatomic) int32_t topicListHash;
@property(nonatomic, strong) KAAUnion *topicStates;
@property(nonatomic, strong) KAAUnion *acceptedUnicastNotifications;
@property(nonatomic, strong) KAAUnion *subscriptionCommands;

- (instancetype)initWithTopicListHash:(int32_t)topicListHash topicStates:(KAAUnion *)topicStates acceptedUnicastNotifications:(KAAUnion *)acceptedUnicastNotifications subscriptionCommands:(KAAUnion *)subscriptionCommands;


# ifndef KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_H_
# define KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_H_

# define KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_TOPIC_STATE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_STRING_OR_NULL_H_
# define KAA_UNION_ARRAY_STRING_OR_NULL_H_

# define KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_STRING_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_STRING_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_H_
# define KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_H_

# define KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_SUBSCRIPTION_COMMAND_OR_NULL_H_

@end


@interface UserSyncRequest : AvroBased

@property(nonatomic, strong) KAAUnion *userAttachRequest;
@property(nonatomic, strong) KAAUnion *endpointAttachRequests;
@property(nonatomic, strong) KAAUnion *endpointDetachRequests;

- (instancetype)initWithUserAttachRequest:(KAAUnion *)userAttachRequest endpointAttachRequests:(KAAUnion *)endpointAttachRequests endpointDetachRequests:(KAAUnion *)endpointDetachRequests;


# ifndef KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_H_
# define KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_H_

# define KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_USER_ATTACH_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_ENDPOINT_ATTACH_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_ENDPOINT_DETACH_REQUEST_OR_NULL_H_

@end


@interface EventSyncRequest : AvroBased

@property(nonatomic, strong) KAAUnion *eventSequenceNumberRequest;
@property(nonatomic, strong) KAAUnion *eventListenersRequests;
@property(nonatomic, strong) KAAUnion *events;

- (instancetype)initWithEventSequenceNumberRequest:(KAAUnion *)eventSequenceNumberRequest eventListenersRequests:(KAAUnion *)eventListenersRequests events:(KAAUnion *)events;


# ifndef KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_H_
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_H_

# define KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_EVENT_SEQUENCE_NUMBER_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EVENT_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_EVENT_OR_NULL_H_

@end


@interface LogSyncRequest : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) KAAUnion *logEntries;

- (instancetype)initWithRequestId:(int32_t)requestId logEntries:(KAAUnion *)logEntries;


# ifndef KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_H_
# define KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_H_

# define KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_H_

@end


@interface ProtocolMetaData : AvroBased

@property(nonatomic) int32_t accessPointId;
@property(nonatomic, strong) ProtocolVersionPair *protocolVersionInfo;
@property(nonatomic, strong) NSData *connectionInfo;

- (instancetype)initWithAccessPointId:(int32_t)accessPointId protocolVersionInfo:(ProtocolVersionPair *)protocolVersionInfo connectionInfo:(NSData *)connectionInfo;

@end


@interface BootstrapSyncResponse : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) NSArray *supportedProtocols;

- (instancetype)initWithRequestId:(int32_t)requestId supportedProtocols:(NSArray *)supportedProtocols;

@end


@interface ProfileSyncResponse : AvroBased

@property(nonatomic) SyncResponseStatus responseStatus;

- (instancetype)initWithResponseStatus:(SyncResponseStatus)responseStatus;

@end


@interface ConfigurationSyncResponse : AvroBased

@property(nonatomic) SyncResponseStatus responseStatus;
@property(nonatomic, strong) KAAUnion *confSchemaBody;
@property(nonatomic, strong) KAAUnion *confDeltaBody;

- (instancetype)initWithResponseStatus:(SyncResponseStatus)responseStatus confSchemaBody:(KAAUnion *)confSchemaBody confDeltaBody:(KAAUnion *)confDeltaBody;


# ifndef KAA_UNION_BYTES_OR_NULL_H_
# define KAA_UNION_BYTES_OR_NULL_H_

# define KAA_UNION_BYTES_OR_NULL_BRANCH_0    0
# define KAA_UNION_BYTES_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BYTES_OR_NULL_H_


# ifndef KAA_UNION_BYTES_OR_NULL_H_
# define KAA_UNION_BYTES_OR_NULL_H_

# define KAA_UNION_BYTES_OR_NULL_BRANCH_0    0
# define KAA_UNION_BYTES_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BYTES_OR_NULL_H_

@end


@interface NotificationSyncResponse : AvroBased

@property(nonatomic) SyncResponseStatus responseStatus;
@property(nonatomic, strong) KAAUnion *notifications;
@property(nonatomic, strong) KAAUnion *availableTopics;

- (instancetype)initWithResponseStatus:(SyncResponseStatus)responseStatus notifications:(KAAUnion *)notifications availableTopics:(KAAUnion *)availableTopics;


# ifndef KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_H_
# define KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_H_

# define KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_NOTIFICATION_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_TOPIC_OR_NULL_H_
# define KAA_UNION_ARRAY_TOPIC_OR_NULL_H_

# define KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_TOPIC_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_TOPIC_OR_NULL_H_

@end


@interface UserSyncResponse : AvroBased

@property(nonatomic, strong) KAAUnion *userAttachResponse;
@property(nonatomic, strong) KAAUnion *userAttachNotification;
@property(nonatomic, strong) KAAUnion *userDetachNotification;
@property(nonatomic, strong) KAAUnion *endpointAttachResponses;
@property(nonatomic, strong) KAAUnion *endpointDetachResponses;

- (instancetype)initWithUserAttachResponse:(KAAUnion *)userAttachResponse userAttachNotification:(KAAUnion *)userAttachNotification userDetachNotification:(KAAUnion *)userDetachNotification endpointAttachResponses:(KAAUnion *)endpointAttachResponses endpointDetachResponses:(KAAUnion *)endpointDetachResponses;


# ifndef KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_H_
# define KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_H_

# define KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_USER_ATTACH_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_H_
# define KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_H_

# define KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_BRANCH_0    0
# define KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_USER_ATTACH_NOTIFICATION_OR_NULL_H_


# ifndef KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_H_
# define KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_H_

# define KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_BRANCH_0    0
# define KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_USER_DETACH_NOTIFICATION_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_ENDPOINT_ATTACH_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_H_
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_H_

# define KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_ENDPOINT_DETACH_RESPONSE_OR_NULL_H_

@end


@interface EventSyncResponse : AvroBased

@property(nonatomic, strong) KAAUnion *eventSequenceNumberResponse;
@property(nonatomic, strong) KAAUnion *eventListenersResponses;
@property(nonatomic, strong) KAAUnion *events;

- (instancetype)initWithEventSequenceNumberResponse:(KAAUnion *)eventSequenceNumberResponse eventListenersResponses:(KAAUnion *)eventListenersResponses events:(KAAUnion *)events;


# ifndef KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_H_
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_H_

# define KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_EVENT_LISTENERS_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EVENT_OR_NULL_H_
# define KAA_UNION_ARRAY_EVENT_OR_NULL_H_

# define KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_EVENT_OR_NULL_H_

@end


@interface LogDeliveryStatus : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic) SyncResponseResultType result;
@property(nonatomic, strong) KAAUnion *errorCode;

- (instancetype)initWithRequestId:(int32_t)requestId result:(SyncResponseResultType)result errorCode:(KAAUnion *)errorCode;


# ifndef KAA_UNION_LOG_DELIVERY_ERROR_CODE_OR_NULL_H_
# define KAA_UNION_LOG_DELIVERY_ERROR_CODE_OR_NULL_H_

# define KAA_UNION_LOG_DELIVERY_ERROR_CODE_OR_NULL_BRANCH_0    0
# define KAA_UNION_LOG_DELIVERY_ERROR_CODE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_LOG_DELIVERY_ERROR_CODE_OR_NULL_H_

@end


@interface LogSyncResponse : AvroBased

@property(nonatomic, strong) KAAUnion *deliveryStatuses;

- (instancetype)initWithDeliveryStatuses:(KAAUnion *)deliveryStatuses;


# ifndef KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_H_
# define KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_H_

# define KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_H_

@end


@interface RedirectSyncResponse : AvroBased

@property(nonatomic) int32_t accessPointId;

- (instancetype)initWithAccessPointId:(int32_t)accessPointId;

@end


@interface ExtensionSync : AvroBased

@property(nonatomic) int32_t extensionId;
@property(nonatomic, strong) NSData *payload;

- (instancetype)initWithExtensionId:(int32_t)extensionId payload:(NSData *)payload;

@end


@interface SyncRequest : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic, strong) KAAUnion *syncRequestMetaData;
@property(nonatomic, strong) KAAUnion *bootstrapSyncRequest;
@property(nonatomic, strong) KAAUnion *profileSyncRequest;
@property(nonatomic, strong) KAAUnion *configurationSyncRequest;
@property(nonatomic, strong) KAAUnion *notificationSyncRequest;
@property(nonatomic, strong) KAAUnion *userSyncRequest;
@property(nonatomic, strong) KAAUnion *eventSyncRequest;
@property(nonatomic, strong) KAAUnion *logSyncRequest;
@property(nonatomic, strong) KAAUnion *extensionSyncRequests;

- (instancetype)initWithRequestId:(int32_t)requestId syncRequestMetaData:(KAAUnion *)syncRequestMetaData bootstrapSyncRequest:(KAAUnion *)bootstrapSyncRequest profileSyncRequest:(KAAUnion *)profileSyncRequest configurationSyncRequest:(KAAUnion *)configurationSyncRequest notificationSyncRequest:(KAAUnion *)notificationSyncRequest userSyncRequest:(KAAUnion *)userSyncRequest eventSyncRequest:(KAAUnion *)eventSyncRequest logSyncRequest:(KAAUnion *)logSyncRequest extensionSyncRequests:(KAAUnion *)extensionSyncRequests;


# ifndef KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_H_
# define KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_H_

# define KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_0    0
# define KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_SYNC_REQUEST_META_DATA_OR_NULL_H_


# ifndef KAA_UNION_BOOTSTRAP_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_BOOTSTRAP_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_BOOTSTRAP_SYNC_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_BOOTSTRAP_SYNC_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BOOTSTRAP_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_PROFILE_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_CONFIGURATION_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_NOTIFICATION_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_USER_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_USER_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_USER_SYNC_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_USER_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_EVENT_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_H_
# define KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_H_

# define KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_0    0
# define KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_H_
# define KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_H_

# define KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_H_

@end


@interface SyncResponse : AvroBased

@property(nonatomic) int32_t requestId;
@property(nonatomic) SyncResponseResultType status;
@property(nonatomic, strong) KAAUnion *bootstrapSyncResponse;
@property(nonatomic, strong) KAAUnion *profileSyncResponse;
@property(nonatomic, strong) KAAUnion *configurationSyncResponse;
@property(nonatomic, strong) KAAUnion *notificationSyncResponse;
@property(nonatomic, strong) KAAUnion *userSyncResponse;
@property(nonatomic, strong) KAAUnion *eventSyncResponse;
@property(nonatomic, strong) KAAUnion *redirectSyncResponse;
@property(nonatomic, strong) KAAUnion *logSyncResponse;
@property(nonatomic, strong) KAAUnion *extensionSyncResponses;

- (instancetype)initWithRequestId:(int32_t)requestId status:(SyncResponseResultType)status bootstrapSyncResponse:(KAAUnion *)bootstrapSyncResponse profileSyncResponse:(KAAUnion *)profileSyncResponse configurationSyncResponse:(KAAUnion *)configurationSyncResponse notificationSyncResponse:(KAAUnion *)notificationSyncResponse userSyncResponse:(KAAUnion *)userSyncResponse eventSyncResponse:(KAAUnion *)eventSyncResponse redirectSyncResponse:(KAAUnion *)redirectSyncResponse logSyncResponse:(KAAUnion *)logSyncResponse extensionSyncResponses:(KAAUnion *)extensionSyncResponses;


# ifndef KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_BOOTSTRAP_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_H_
# define KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_H_

# define KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_BRANCH_0    0
# define KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_H_


# ifndef KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_H_
# define KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_H_

# define KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_BRANCH_0    0
# define KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_BRANCH_1    1

# endif // KAA_UNION_ARRAY_EXTENSION_SYNC_OR_NULL_H_

@end


@interface TopicSubscriptionInfo : AvroBased

@property(nonatomic, strong) Topic *topicInfo;
@property(nonatomic) int32_t seqNumber;

- (instancetype)initWithTopicInfo:(Topic *)topicInfo seqNumber:(int32_t)seqNumber;

@end
