/**
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


include "shared.thrift"
include "cli.thrift"

namespace java org.kaaproject.kaa.server.common.thrift.gen.operations
namespace cpp kaa

typedef shared.Integer int
typedef shared.ObjectId id

typedef double probability
typedef string uri

typedef binary endpoint_id
typedef string user_id
typedef string tenant_id
typedef string endpoint_class_family_id
typedef string application_token

/**
* This is enum of changes type. The same as org.kaaproject.kaa.common.dto.ChangeType
**/
enum Operation {
      UPDATE = 1,
      DELETE = 2,
      INSERT = 3,
      ADD_TOPIC = 4,
      REMOVE_TOPIC = 5,
      UPDATE_WEIGHT = 6,
      ADD_CONF = 7,
      REMOVE_CONF = 8,
      ADD_PROF = 9,
      REMOVE_PROF = 10,
      REMOVE_GROUP = 11,
      REMOVE_CONF_VERSION = 12,
      REMOVE_PROF_VERSION = 13,
      REMOVE_NOTIFICATION_VERSION = 14
      ADD_LOG_APPENDER = 15
      REMOVE_LOG_APPENDER = 16
      UPDATE_LOG_APPENDER = 17
      ADD_USER_VERIFIER = 18
      REMOVE_USER_VERIFIER = 19
      UPDATE_USER_VERIFIER = 20
      APP_UPDATE = 21
}

/**
* Enum defines route update operation type
*/
enum EventRouteUpdateType {
      ADD = 1;
      DELETE = 2;
      UPDATE = 3
}

/**
* Defines types of messages, all pass through one interface and demultiplex by this enum
*/
enum EventMessageType {
      ROUTE_UPDATE = 1;
      USER_ROUTE_INFO = 2;
      EVENT = 3;
      ENDPOINT_ROUTE_UPDATE = 4;
      ENDPOINT_STATE_UPDATE = 5;
}

struct Notification {
  1: id appId
  2: shared.Integer appSeqNumber
  3: id groupId
  4: shared.Integer groupSeqNumber
  5: id profileFilterId
  6: shared.Integer profileFilterSeqNumber
  7: id configurationId
  8: shared.Integer configurationSeqNumber
  9: id notificationId
  10: id topicId
  11: Operation op
  12: id appenderId
  13: string userVerifierToken
}

struct RedirectionRule {
  1: shared.Integer accessPointId
  2: shared.Long ruleId
  3: probability initRedirectProbability
  4: probability sessionRedirectProbability
  5: shared.Long ruleTTL
}

struct EventClassFamilyVersion {
  1: endpoint_class_family_id endpointClassFamilyId
  2: shared.Integer  endpointClassFamilyVersion
}

struct RouteInfo {
  1: EventRouteUpdateType updateType
  2: list<EventClassFamilyVersion> eventClassFamilyVersion
  3: application_token applicationToken
  4: endpoint_id endpointId
}

struct EventRoute {
  1: user_id userId
  2: tenant_id tenantId
  3: list<RouteInfo> routeInfo
  4: string operationsServerId
}

struct EndpointEvent {
  1: string uuid
  2: endpoint_id sender
  3: binary eventData
  4: shared.Long createTime
  5: shared.Integer version
}

struct RouteAddress {
  1: endpoint_id endpointKey
  2: application_token applicationToken
  3: string operationsServerId
}

struct Event {
  1: user_id userId
  2: tenant_id tenantId
  3: EndpointEvent endpointEvent
  4: RouteAddress routeAddress
}

struct UserRouteInfo {
  1: user_id userId
  2: tenant_id tenantId
  3: string operationsServerId
  4: EventRouteUpdateType updateType
}

struct EndpointRouteUpdate {
  1: tenant_id tenantId
  2: user_id userId
  3: RouteAddress routeAddress
  4: EventRouteUpdateType updateType
  5: int cfSchemaVersion
  6: binary ucfHash
}

struct EndpointStateUpdate {
  1: tenant_id tenantId
  2: user_id userId
  3: application_token applicationToken
  4: endpoint_id endpointKey
  5: binary ucfHash
}

struct Message {
  1: EventMessageType type
  2: shared.Long eventId
  3: Event event
  4: EventRoute route
  5: UserRouteInfo userRoute
  6: EndpointRouteUpdate endpointRouteUpdate
  7: EndpointStateUpdate endpointStateUpdate
}

struct UserConfigurationUpdate {
  1: tenant_id tenantId
  2: user_id userId
  3: application_token applicationToken
  4: int cfSchemaVersion
  5: binary ucfHash
}

/**
* Enum defines route operation type
*/
enum ThriftRouteOperation {
      ADD = 1;
      DELETE = 2;
      UPDATE = 3
}

/**
* Enum defines cluster entity type
*/
enum ThriftClusterEntityType {
      ENDPOINT = 1;
}

struct ThriftEntityAddress {
  1: tenant_id tenantId
  2: application_token applicationToken
  3: ThriftClusterEntityType entityType
  4: binary entityId
}

enum ThriftActorClassifier {
    LOCAL = 1;
    GLOBAL = 2;
    APPLICATION = 3;
}

struct ThriftEndpointDeregistrationMessage {
  1: ThriftEntityAddress address
  2: ThriftActorClassifier actorClassifier
}

struct ThriftEntityClusterAddress {
  1: string nodeId
  2: ThriftEntityAddress address
}

struct ThriftEntityRouteMessage {
  1: ThriftEntityClusterAddress address
  2: ThriftRouteOperation operation
}

struct ThriftUnicastNotificationMessage {
  1: ThriftEntityAddress address
  2: ThriftActorClassifier actorClassifier
  3: string notificationId;
}

struct ThriftServerProfileUpdateMessage {
  1: ThriftEntityAddress address
  2: ThriftActorClassifier actorClassifier
}

service OperationsThriftService {

/**
*   Application Sequence Numbers
*/
  void onNotification(1: Notification notification);

/**
*  Set redirection rule for Operations server
*/
  void setRedirectionRule(1: RedirectionRule redirectionRule);

/**
*  Interface to send unified event messages
*/
  void sendMessages(1: list<Message> messages);

/**
*   Report user configuration update from control to operation servers
*/
  void sendUserConfigurationUpdates(1: list<UserConfigurationUpdate> updates);

/**
*  Interface to send unified entity route messages
*/
  void onEntityRouteMessages(1: list<ThriftEntityRouteMessage> messages);
  
/**
*  Interface to send unicast notification message
*/
  void onUnicastNotification(1: ThriftUnicastNotificationMessage message);
  
/**
*  Interface to send server profile update message
*/
  void onServerProfileUpdate(1: ThriftServerProfileUpdateMessage message);  

/**
 *  Interface to send server profile update message
 */
  void onEndpointDeregistration(1: ThriftEndpointDeregistrationMessage message);
}
