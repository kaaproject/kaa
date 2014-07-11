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

namespace java org.kaaproject.kaa.server.common.thrift.gen.control
namespace cpp kaa

typedef shared.ObjectId id
typedef shared.DataStruct data
typedef shared.Integer int

enum SdkPlatform {
  JAVA = 1,
  ANDROID = 2,
  CPP = 3
}

struct Sdk {
  1: string fileName
  2: binary data
}

exception ControlThriftException {
   1: string message
   2: string causeExceptionClass
   3: string causeStackStrace
}

service ControlThriftService extends cli.CliThriftService{

/**
*   Tenants
*/

  list<data> getTenants() throws(1: ControlThriftException ControlException)
  data getTenant(1: id tenantId) throws(1: ControlThriftException ControlException)
  data editTenant(1: data tenant) throws(1: ControlThriftException ControlException)
  void deleteTenant(1: id tenantId) throws(1: ControlThriftException ControlException)
  
/**
*   Users
*/
 
  list<data> getUsers() throws(1: ControlThriftException ControlException)
  list<data> getTenantUsers(1: id tenantId) throws(1: ControlThriftException ControlException)
  data getUser(1: id userId) throws(1: ControlThriftException ControlException)
  data getUserByExternalUid(1: string uid) throws(1: ControlThriftException ControlException)
  data editUser(1: data user) throws(1: ControlThriftException ControlException)
  void deleteUser(1: id userId) throws(1: ControlThriftException ControlException)

/**
*   TenantAdmins
*/

  list<data> getTenantAdmins() throws(1: ControlThriftException ControlException)
  data getTenantAdmin(1: id tenantId) throws(1: ControlThriftException ControlException)
  data editTenantAdmin(1: data tenantAdmin) throws(1: ControlThriftException ControlException)
  void deleteTenantAdmin(1: id tenantId) throws(1: ControlThriftException ControlException)
    
/**
*   Applications
*/
  
  list<data> getApplicationsByTenantId(1: id tenantId) throws(1: ControlThriftException ControlException)
  data getApplication(1: id applicationId) throws(1: ControlThriftException ControlException)
  data editApplication(1: data application) throws(1: ControlThriftException ControlException)
  void deleteApplication(1: id applicationId) throws(1: ControlThriftException ControlException)
  
/**
*   ConfigurationSchemas
*/

  list<data> getConfigurationSchemaVersionsByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  list<data> getConfigurationSchemasByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  data getConfigurationSchema(1: id configurationSchemaId) throws(1: ControlThriftException ControlException)
  data editConfigurationSchema(1: data configurationSchema) throws(1: ControlThriftException ControlException)
  
/**
*   ProfileSchemas
*/

  list<data> getProfileSchemaVersionsByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  list<data> getProfileSchemasByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  data getProfileSchema(1: id profileSchemaId) throws(1: ControlThriftException ControlException)
  data editProfileSchema(1: data profileSchema) throws(1: ControlThriftException ControlException)

/**  
*   LogSchemas
*/
  list<data> getLogSchemaVersionsByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  list<data> getLogSchemasByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  data getLogSchema(1: id logSchemaId) throws(1: ControlThriftException ControlException)
  data editLogSchema(1: data logSchema) throws(1: ControlThriftException ControlException)
  
/**
*   EndpointGroups
*/

  list<data> getEndpointGroupsByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  data getEndpointGroup(1: id endpointGroupId) throws(1: ControlThriftException ControlException)
  data editEndpointGroup(1: data endpointGroup) throws(1: ControlThriftException ControlException)
  void deleteEndpointGroup(1: id endpointGroupId) throws(1: ControlThriftException ControlException)

  data removeTopicsFromEndpointGroup(1: id endpointGroupId, 2: string topicId) throws(1: ControlThriftException ControlException)
  data addTopicsToEndpointGroup(1: id endpointGroupId, 2: string topicId) throws(1: ControlThriftException ControlException)
  
/**
*   ProfileFilters
*/

  list<data> getProfileFilterRecordsByEndpointGroupId(1: id endpointGroupId, 2: bool includeDeprecated) throws(1: ControlThriftException ControlException)
  data getProfileFilterRecord(1: id schemaId, 2: id endpointGroupId) throws(1: ControlThriftException ControlException)
  list<data> getVacantProfileSchemasByEndpointGroupId(1: id endpointGroupId) throws(1: ControlThriftException ControlException)
  
  data getProfileFilter(1: id profileFilterId) throws(1: ControlThriftException ControlException)

  data editProfileFilter(1: data profileFilter) throws(1: ControlThriftException ControlException)
  data activateProfileFilter(1: id profileFilterId, 2: string activatedUsername) throws(1: ControlThriftException ControlException)
  data deactivateProfileFilter(1: id profileFilterId, 2: string deactivatedUsername) throws(1: ControlThriftException ControlException)
  void deleteProfileFilterRecord(1: id schemaId, 2: id endpointGroupId, 3: string deactivatedUsername) throws(1: ControlThriftException ControlException)
  
/**
*   Configurations
*/  

  list<data> getConfigurationRecordsByEndpointGroupId(1: id endpointGroupId, 2: bool includeDeprecated) throws(1: ControlThriftException ControlException)
  data getConfigurationRecord(1: id schemaId, 2: id endpointGroupId) throws(1: ControlThriftException ControlException)
  list<data> getVacantConfigurationSchemasByEndpointGroupId(1: id endpointGroupId) throws(1: ControlThriftException ControlException)
  
  data getConfiguration(1: id configurationId) throws(1: ControlThriftException ControlException)

  data editConfiguration(1: data configuration) throws(1: ControlThriftException ControlException)
  data activateConfiguration(1: id configurationId, 2: string activatedUsername) throws(1: ControlThriftException ControlException)
  data deactivateConfiguration(1: id configurationId, 2: string deactivatedUsername) throws(1: ControlThriftException ControlException)
  void deleteConfigurationRecord(1: id schemaId, 2: id endpointGroupId, 3: string deactivatedUsername) throws(1: ControlThriftException ControlException)


/**
*   NotificationSchemas
*/

  list<data> getNotificationSchemaVersionsByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  data editNotificationSchema(1: data notificationSchema) throws(1: ControlThriftException ControlException)
  data getNotificationSchema(1: id notificationSchemaId) throws(1: ControlThriftException ControlException)
  list<data> getNotificationSchemasByAppId(1: id applicationId) throws(1: ControlThriftException ControlException)
  list<data> getUserNotificationSchemasByAppId(1: id applicationId) throws(1: ControlThriftException ControlException)
  list<data> findNotificationSchemasByAppIdAndType(1: id applicationId, 2: data type) throws(1: ControlThriftException ControlException)

/**
*   Notifications
*/
  data editNotification(1: data notification) throws(1: ControlThriftException ControlException)
  data getNotification(1: id notificationId) throws(1: ControlThriftException ControlException)
  list<data> getNotificationsByTopicId(1: id topicId) throws(1: ControlThriftException ControlException)


/**
*   Topics
*/
  data editTopic(1: data topic) throws(1: ControlThriftException ControlException)
  data getTopic(1: id topicId) throws(1: ControlThriftException ControlException)
  list<data> getTopicByAppId(1: id appId) throws(1: ControlThriftException ControlException)
  list<data> getTopicByEndpointGroupId(1: id endpointGroupId) throws(1: ControlThriftException ControlException)
  list<data> getVacantTopicByEndpointGroupId(1: id endpointGroupId) throws(1: ControlThriftException ControlException)
  void deleteTopicById(1: id topicId) throws(1: ControlThriftException ControlException)

/**
*   Unicast Notifications
*/
  data getUnicastNotification(1: id notificationId) throws(1: ControlThriftException ControlException)
  data editUnicastNotification(1: data notification) throws(1: ControlThriftException ControlException)
  list<data> getUnicastNotificationsByKeyHash(1: binary keyhash) throws(1: ControlThriftException ControlException)
     
/**
*   Client SDK
*/ 
  
  Sdk generateSdk(1: SdkPlatform sdkPlatform, 2: id applicationId, 3: shared.Integer profileSchemaVersion, 4: shared.Integer configurationSchemaVersion, 5: shared.Integer notificationSchemaVersion, 6: list<id> aefMapIds, 7: shared.Integer logSchemaVersion) throws(1: ControlThriftException ControlException)

/**
*   Events
*/ 

  data editEventClassFamily(1: data eventClassFamily) throws(1: ControlThriftException ControlException)
  list<data> getEventClassFamiliesByTenantId(1: id tenantId) throws(1: ControlThriftException ControlException)
  data getEventClassFamily(1: id eventClassFamilyId) throws(1: ControlThriftException ControlException)
  void addEventClassFamilySchema(1: id eventClassFamilyId, 2: string eventClassFamilySchema, 3: string createdUsername) throws(1: ControlThriftException ControlException)

  list<data> getEventClassesByFamilyIdVersionAndType(1: id ecfId, 2: int version, 3: data type) throws(1: ControlThriftException ControlException)
  
  data editApplicationEventFamilyMap(1: data applicationEventFamilyMap) throws(1: ControlThriftException ControlException)
  data getApplicationEventFamilyMap(1: id applicationEventFamilyMapId) throws(1: ControlThriftException ControlException)
  list<data> getApplicationEventFamilyMapsByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  list<data> getVacantEventClassFamiliesByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)
  list<data> getEventClassFamiliesByApplicationId(1: id applicationId) throws(1: ControlThriftException ControlException)

/**  
*   Endpoint User
*/
  list<data> getEndpointUsers() throws(1: ControlThriftException ControlException)
  data getEndpointUser(1: id endpointUserId) throws(1: ControlThriftException ControlException)
  data editEndpointUser(1: data endpointUser) throws(1: ControlThriftException ControlException)
  void deleteEndpointUser(1: id endpointUserId) throws(1: ControlThriftException ControlException)
  string generateEndpointUserAccessToken(1: string externalUid, 2: string tenantId) throws(1: ControlThriftException ControlException)

/**
*   MongoDBCollectionsCreation               
*/
  void createSecureCollection(1: id applicationId, 2: string password) throws(1: ControlThriftException ControlException)  

}