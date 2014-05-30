/*
 * Copyright 2014 CyberVision, Inc.
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

typedef shared.ObjectId id

typedef double probability
typedef string uri

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
  10: id unicastNotificationId
  11: id topicId
  12: binary keyHash
  13: Operation op
}

struct RedirectionRule {
  1: uri dnsName
  2: shared.Long ruleId
  3: probability redirectionProbability
  4: shared.Long ruleTTL
}

service OperationsThriftService extends cli.CliThriftService{

/**
*   Application Sequence Numbers
*/
  void onNotification(1: Notification notification);
  

/**
*  Set redirection rule for Operations server
*/
  void setRedirectionRule(1: RedirectionRule redirectionRule);
  
}
