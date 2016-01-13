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

#define HC_SHORTHAND
#import <OCHamcrest/OCHamcrest.h>

#define MOCKITO_SHORTHAND
#import <OCMockito/OCMockito.h>

#import <XCTest/XCTest.h>
#import "DefaultOperationDataProcessor.h"
#import "AvroBytesConverter.h"
#import "EndpointGen.h"


@interface DefaultOperationDataProcessorTest : XCTestCase

@end

@implementation DefaultOperationDataProcessorTest

- (void)testUpRequestCreationWithNullTypes {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    XCTAssertNil([operationDataProcessor compileRequest:nil]);
}

- (void)testUpRequestCreationWithUnknownType {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    NSDictionary *types = [NSDictionary dictionaryWithObject:[NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL] forKey:[NSNumber numberWithInt:TRANSPORT_TYPE_BOOTSTRAP]];
    XCTAssertNil([operationDataProcessor compileRequest:types]);
}

- (void)testUpRequestCreationWithNullTransports {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    
    NSDictionary *transportTypes = [self getDictionaryWithTransportTypesWithBidirectional];
    
    XCTAssertNotNil([operationDataProcessor compileRequest:transportTypes]);
}

- (void)testUpRequestCreation {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    
    id <ProfileTransport> profileTransport = mockProtocol(@protocol(ProfileTransport));
    id <EventTransport> eventTransport = mockProtocol(@protocol(EventTransport));
    id <NotificationTransport> notificationTransport = mockProtocol(@protocol(NotificationTransport));
    id <ConfigurationTransport> configurationTransport = mockProtocol(@protocol(ConfigurationTransport));
    id <UserTransport> userTransport = mockProtocol(@protocol(UserTransport));
    id <MetaDataTransport> metaDataTransport = mockProtocol(@protocol(MetaDataTransport));
    id <LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    
    [operationDataProcessor setConfigurationTransport:configurationTransport];
    [operationDataProcessor setProfileTransport:profileTransport];
    [operationDataProcessor setEventTransport:eventTransport];
    [operationDataProcessor setNotificationTransport:notificationTransport];
    [operationDataProcessor setUserTransport:userTransport];
    [operationDataProcessor setMetaDataTransport:metaDataTransport];
    [operationDataProcessor setLogTransport:logTransport];
    
    NSDictionary *transportTypes = [self getDictionaryWithTransportTypesWithBidirectional];
    
    XCTAssertNotNil([operationDataProcessor compileRequest:transportTypes]);
    [verifyCount(configurationTransport, times(1)) createConfigurationRequest];
    [verifyCount(profileTransport, times(1)) createProfileRequest];
    [verifyCount(notificationTransport, times(1)) createNotificationRequest];
    [verifyCount(userTransport, times(1)) createUserRequest];
    [verifyCount(metaDataTransport, times(1)) createMetaDataRequest];
    [verifyCount(logTransport, times(1)) createLogRequest];
}

- (void)testDownRequestCreation {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    
    id <ProfileTransport> profileTransport = mockProtocol(@protocol(ProfileTransport));
    id <EventTransport> eventTransport = mockProtocol(@protocol(EventTransport));
    id <NotificationTransport> notificationTransport = mockProtocol(@protocol(NotificationTransport));
    id <ConfigurationTransport> configurationTransport = mockProtocol(@protocol(ConfigurationTransport));
    id <UserTransport> userTransport = mockProtocol(@protocol(UserTransport));
    id <MetaDataTransport> metaDataTransport = mockProtocol(@protocol(MetaDataTransport));
    id <LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    
    [operationDataProcessor setConfigurationTransport:configurationTransport];
    [operationDataProcessor setProfileTransport:profileTransport];
    [operationDataProcessor setEventTransport:eventTransport];
    [operationDataProcessor setNotificationTransport:notificationTransport];
    [operationDataProcessor setUserTransport:userTransport];
    [operationDataProcessor setMetaDataTransport:metaDataTransport];
    [operationDataProcessor setLogTransport:logTransport];
    
    NSDictionary *transportTypes = [self getDictionaryWithTransportTypesWithDownDirection];
    
    XCTAssertNotNil([operationDataProcessor compileRequest:transportTypes]);
    [verifyCount(configurationTransport, times(1)) createConfigurationRequest];
    [verifyCount(metaDataTransport, times(1)) createMetaDataRequest];
    [verifyCount(profileTransport, times(0)) createProfileRequest];
    [verifyCount(notificationTransport, times(0)) createNotificationRequest];
    [verifyCount(userTransport, times(0)) createUserRequest];
    [verifyCount(logTransport, times(0)) createLogRequest];
}

- (void)testResponse {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    
    id <ProfileTransport> profileTransport = mockProtocol(@protocol(ProfileTransport));
    id <EventTransport> eventTransport = mockProtocol(@protocol(EventTransport));
    id <NotificationTransport> notificationTransport = mockProtocol(@protocol(NotificationTransport));
    id <ConfigurationTransport> configurationTransport = mockProtocol(@protocol(ConfigurationTransport));
    id <UserTransport> userTransport = mockProtocol(@protocol(UserTransport));
    id <LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    id <RedirectionTransport> redirectionTransport = mockProtocol(@protocol(RedirectionTransport));
    
    [operationDataProcessor setConfigurationTransport:configurationTransport];
    [operationDataProcessor setProfileTransport:profileTransport];
    [operationDataProcessor setEventTransport:eventTransport];
    [operationDataProcessor setNotificationTransport:notificationTransport];
    [operationDataProcessor setUserTransport:userTransport];
    [operationDataProcessor setRedirectionTransport:redirectionTransport];
    [operationDataProcessor setLogTransport:logTransport];
    
    SyncResponse *response = [[SyncResponse alloc] init];
    [response setStatus:SYNC_RESPONSE_RESULT_TYPE_SUCCESS];
    [response setRequestId:1];
    [response setProfileSyncResponse:[self getProfileSyncResponseUnion]];
    [response setConfigurationSyncResponse:[self getConfigurationUnion]];
    [response setNotificationSyncResponse:[self getNotificationUnion]];
    [response setUserSyncResponse:[self getUserUnion]];
    [response setEventSyncResponse:[self getEventUnion]];
    [response setRedirectSyncResponse:[self getRedirectUnion]];
    [response setLogSyncResponse:[self getLogUnion]];
    
    AvroBytesConverter *converter = [[AvroBytesConverter alloc] init];
    NSData *data = [converter toBytes:response];
    [operationDataProcessor processResponse:data];
    
    [verifyCount(profileTransport, times(1)) onProfileResponse:anything()];
    [verifyCount(eventTransport, times(1)) onEventResponse:anything()];
    [verifyCount(notificationTransport, times(1)) onNotificationResponse:anything()];
    [verifyCount(configurationTransport, times(1)) onConfigurationResponse:anything()];
    [verifyCount(userTransport, times(1)) onUserResponse:anything()];
    [verifyCount(redirectionTransport, times(1)) onRedirectionResponse:anything()];
    [verifyCount(logTransport, times(1)) onLogResponse:anything()];
}

- (void)testResponseWithNullTransport {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    
    SyncResponse *response = [[SyncResponse alloc] init];
    [response setStatus:SYNC_RESPONSE_RESULT_TYPE_SUCCESS];
    [response setProfileSyncResponse:[self getProfileSyncResponseUnion]];
    [response setConfigurationSyncResponse:[self getConfigurationUnion]];
    [response setNotificationSyncResponse:[self getNotificationUnion]];
    [response setUserSyncResponse:[self getUserUnion]];
    [response setEventSyncResponse:[self getEventUnion]];
    [response setRedirectSyncResponse:[self getRedirectUnion]];
    [response setLogSyncResponse:[self getLogUnion]];
    
    AvroBytesConverter *converter = [[AvroBytesConverter alloc] init];
    NSData *data = [converter toBytes:response];
    [operationDataProcessor processResponse:data];
}

- (void)testResponseWithNullTransportAndResponses {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    
    SyncResponse *response = [[SyncResponse alloc] init];
    [response setStatus:SYNC_RESPONSE_RESULT_TYPE_SUCCESS];
    
    AvroBytesConverter *converter = [[AvroBytesConverter alloc] init];
    NSData *data = [converter toBytes:response];
    [operationDataProcessor processResponse:data];
}

- (void)testResponseWithNullResponses {
    DefaultOperationDataProcessor *operationDataProcessor = [[DefaultOperationDataProcessor alloc] init];
    
    id <ProfileTransport> profileTransport = mockProtocol(@protocol(ProfileTransport));
    id <EventTransport> eventTransport = mockProtocol(@protocol(EventTransport));
    id <NotificationTransport> notificationTransport = mockProtocol(@protocol(NotificationTransport));
    id <ConfigurationTransport> configurationTransport = mockProtocol(@protocol(ConfigurationTransport));
    id <UserTransport> userTransport = mockProtocol(@protocol(UserTransport));
    id <LogTransport> logTransport = mockProtocol(@protocol(LogTransport));
    id <RedirectionTransport> redirectionTransport = mockProtocol(@protocol(RedirectionTransport));
    
    [operationDataProcessor setConfigurationTransport:configurationTransport];
    [operationDataProcessor setProfileTransport:profileTransport];
    [operationDataProcessor setEventTransport:eventTransport];
    [operationDataProcessor setNotificationTransport:notificationTransport];
    [operationDataProcessor setUserTransport:userTransport];
    [operationDataProcessor setRedirectionTransport:redirectionTransport];
    [operationDataProcessor setLogTransport:logTransport];
    
    SyncResponse *response = [[SyncResponse alloc] init];
    [response setStatus:SYNC_RESPONSE_RESULT_TYPE_SUCCESS];
    
    AvroBytesConverter *converter = [[AvroBytesConverter alloc] init];
    NSData *data = [converter toBytes:response];
    [operationDataProcessor processResponse:data];
    
    [verifyCount(profileTransport, times(0)) onProfileResponse:anything()];
    [verifyCount(eventTransport, times(0)) onEventResponse:anything()];
    [verifyCount(notificationTransport, times(0)) onNotificationResponse:anything()];
    [verifyCount(configurationTransport, times(0)) onConfigurationResponse:anything()];
    [verifyCount(userTransport, times(0)) onUserResponse:anything()];
    [verifyCount(redirectionTransport, times(0)) onRedirectionResponse:anything()];
    [verifyCount(logTransport, times(0)) onLogResponse:anything()];
}

#pragma mark - Supporting methods 

- (NSDictionary *) getDictionaryWithTransportTypesWithBidirectional {
    NSArray *keys = @[[NSNumber numberWithInt:TRANSPORT_TYPE_PROFILE],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_CONFIGURATION],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_NOTIFICATION],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_USER],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_EVENT],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_LOGGING]];
    NSArray *objects = @[[NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_BIDIRECTIONAL],];
    NSDictionary *transportTypes = [NSDictionary dictionaryWithObjects:objects
                                                               forKeys:keys];
    return transportTypes;
}

- (NSDictionary *) getDictionaryWithTransportTypesWithDownDirection {
    NSArray *keys = @[[NSNumber numberWithInt:TRANSPORT_TYPE_PROFILE],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_CONFIGURATION],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_NOTIFICATION],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_USER],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_EVENT],
                      [NSNumber numberWithInt:TRANSPORT_TYPE_LOGGING]];
    NSArray *objects = @[[NSNumber numberWithInt:CHANNEL_DIRECTION_DOWN],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_DOWN],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_DOWN],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_DOWN],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_DOWN],
                         [NSNumber numberWithInt:CHANNEL_DIRECTION_DOWN],];
    NSDictionary *transportTypes = [NSDictionary dictionaryWithObjects:objects
                                                               forKeys:keys];
    return transportTypes;
}

- (ConfigurationSyncResponse *) getConfigurationResponse {
    ConfigurationSyncResponse *response = [[ConfigurationSyncResponse alloc] init];
    response.appStateSeqNumber = 1;
    response.responseStatus = SYNC_RESPONSE_STATUS_DELTA;

    return response;
}

- (NotificationSyncResponse *) getNotificationSyncReponse {
    NotificationSyncResponse *response = [[NotificationSyncResponse alloc]init];
    response.appStateSeqNumber = 1;
    response.responseStatus = SYNC_RESPONSE_STATUS_DELTA;

    return response;
}

- (ProfileSyncResponse *) getProfileSyncResponse {
    ProfileSyncResponse *response = [[ProfileSyncResponse alloc] init];
    response.responseStatus = SYNC_RESPONSE_STATUS_DELTA;
    return response;
}

- (RedirectSyncResponse *) getRedirectSyncReponse {
    RedirectSyncResponse *response = [[RedirectSyncResponse alloc] init];
    response.accessPointId = 1;
    return response;
}

- (LogSyncResponse *) getLogSyncResponse {
    LogDeliveryStatus *status = [[LogDeliveryStatus alloc] init];
    status.requestId = 42;
    status.result = SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    
    LogSyncResponse *response = [[LogSyncResponse alloc] init];
    NSArray *array = [NSArray arrayWithObject:status];
    response.deliveryStatuses = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_DELIVERY_STATUS_OR_NULL_BRANCH_0 andData:array];
    return response;
}

- (KAAUnion *) getProfileSyncResponseUnion {
    KAAUnion *profileUnion = [KAAUnion unionWithBranch:KAA_UNION_PROFILE_SYNC_RESPONSE_OR_NULL_BRANCH_0 andData:[self getProfileSyncResponse]];
    return profileUnion;
}

- (KAAUnion *) getConfigurationUnion {
    KAAUnion *confUnion = [KAAUnion unionWithBranch:KAA_UNION_CONFIGURATION_SYNC_RESPONSE_OR_NULL_BRANCH_0 andData:[self getConfigurationResponse]];
    return confUnion;
}

- (KAAUnion *) getNotificationUnion {
    KAAUnion *notifUnion = [KAAUnion unionWithBranch:KAA_UNION_NOTIFICATION_SYNC_RESPONSE_OR_NULL_BRANCH_0 andData:[self getNotificationSyncReponse]];
    return notifUnion;
}

- (KAAUnion *) getUserUnion {
    KAAUnion *userUnion = [KAAUnion unionWithBranch:KAA_UNION_USER_SYNC_RESPONSE_OR_NULL_BRANCH_0
                                            andData:[[UserSyncResponse alloc] init]];
    return userUnion;
}

- (KAAUnion *) getEventUnion {
    KAAUnion *eventUnion = [KAAUnion unionWithBranch:KAA_UNION_EVENT_SYNC_RESPONSE_OR_NULL_BRANCH_0
                                             andData:[[EventSyncResponse alloc] init]];
    return eventUnion;
}

- (KAAUnion *) getRedirectUnion {
    KAAUnion *redirectUnion = [KAAUnion unionWithBranch:KAA_UNION_REDIRECT_SYNC_RESPONSE_OR_NULL_BRANCH_0 andData:[self getRedirectSyncReponse]];
    return redirectUnion;
}

- (KAAUnion *) getLogUnion {
    KAAUnion *logUnion = [KAAUnion unionWithBranch:KAA_UNION_LOG_SYNC_RESPONSE_OR_NULL_BRANCH_0 andData:[self getLogSyncResponse]];
    return logUnion;
}

@end
