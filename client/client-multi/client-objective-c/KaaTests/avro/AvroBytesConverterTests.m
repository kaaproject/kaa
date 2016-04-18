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

@import UIKit;
#import <XCTest/XCTest.h>
#import "AvroBytesConverter.h"
#import "EndpointGen.h"

@interface AvroBytesConverterTests : XCTestCase

@property (nonatomic, strong) AvroBytesConverter *converter;

@end

@implementation AvroBytesConverterTests

- (void)setUp {
    [super setUp];
    self.converter = [[AvroBytesConverter alloc] init];
}

- (void)testBytesConverter {
    TopicState *state = [[TopicState alloc] init];
    state.topicId = 10;
    state.seqNumber = 100;
    
    NSData *serialized = [self.converter toBytes:state];
    
    TopicState *deserializedState = (TopicState *)[self.converter fromBytes:serialized object:[TopicState new]];
    
    XCTAssertEqual(state.topicId, deserializedState.topicId);
    XCTAssertEqual(state.seqNumber, deserializedState.seqNumber);
    
    
    SubscriptionCommand *command = [[SubscriptionCommand alloc] init];
    command.topicId = 1;
    command.command = SUBSCRIPTION_COMMAND_TYPE_REMOVE;
    
    serialized = [self.converter toBytes:command];
    
    SubscriptionCommand *deserializedCommand = (SubscriptionCommand *)[self.converter fromBytes:serialized
                                                                                         object:[SubscriptionCommand new]];
    
    XCTAssertEqual(command.topicId, deserializedCommand.topicId);
    XCTAssertEqual(command.command, deserializedCommand.command);
    
    
    UserAttachNotification *notification = [[UserAttachNotification alloc] init];
    notification.userExternalId = @"TestUserExternalId";
    notification.endpointAccessToken = @"TestEndpointAccessToken";
    
    serialized = [self.converter toBytes:notification];
    
    UserAttachNotification *deserializedNotification = (UserAttachNotification *)[self.converter fromBytes:serialized
                                                                                                    object:[UserAttachNotification new]];
    
    XCTAssertTrue([notification.userExternalId isEqualToString:deserializedNotification.userExternalId]);
    XCTAssertTrue([notification.endpointAccessToken isEqualToString:deserializedNotification.endpointAccessToken]);
}

- (void)testKAAUnionSerialization {
    
    UserAttachResponse *response = [[UserAttachResponse alloc] init];
    response.result = SYNC_RESPONSE_RESULT_TYPE_REDIRECT;
    NSNumber *errorCode = @(USER_ATTACH_ERROR_CODE_REMOTE_ERROR);
    NSString *errorReason = @"test errorReason";
    response.errorCode = [KAAUnion unionWithBranch:KAA_UNION_USER_ATTACH_ERROR_CODE_OR_NULL_BRANCH_0 data:errorCode];
    response.errorReason = [KAAUnion unionWithBranch:KAA_UNION_STRING_OR_NULL_BRANCH_0 data:errorReason];
    
    NSData *serialized = [self.converter toBytes:response];
    
    UserAttachResponse *deserializedResponse = (UserAttachResponse *)[self.converter fromBytes:serialized
                                                                                        object:[UserAttachResponse new]];
    
    XCTAssertEqual(response.result, deserializedResponse.result);
    XCTAssertEqual(response.errorCode.branch, deserializedResponse.errorCode.branch);
    XCTAssertTrue([errorCode isEqualToNumber:deserializedResponse.errorCode.data]);
    
    XCTAssertEqual(response.errorReason.branch, deserializedResponse.errorReason.branch);
    NSString *deserializedReason = deserializedResponse.errorReason.data;
    XCTAssertTrue([errorReason isEqualToString:deserializedReason]);
}

- (void)testArrayOfObjects {
    LogEntry *entry1 = [[LogEntry alloc] init];
    entry1.data = [@"entry1data" dataUsingEncoding:NSUTF8StringEncoding];
    LogEntry *entry2 = [[LogEntry alloc] init];
    entry2.data = [@"entry2data" dataUsingEncoding:NSUTF8StringEncoding];
    LogEntry *entry3 = [[LogEntry alloc] init];
    entry3.data = [@"entry3data" dataUsingEncoding:NSUTF8StringEncoding];
    
    NSMutableArray *entries = [NSMutableArray arrayWithObjects:entry1, entry2, entry3, nil];
    
    [entries addObject:entry1];
    [entries addObject:entry2];
    [entries addObject:entry3];

    LogSyncRequest *request = [[LogSyncRequest alloc] init];
    request.requestId = 10;
    request.logEntries = [KAAUnion unionWithBranch:KAA_UNION_ARRAY_LOG_ENTRY_OR_NULL_BRANCH_0 data:entries];
    
    NSData *serialized = [self.converter toBytes:request];
    
    LogSyncRequest *deserializedRequest = (LogSyncRequest *)[self.converter fromBytes:serialized object:[LogSyncRequest new]];
    XCTAssertEqual(request.requestId, deserializedRequest.requestId);
    
}

@end
