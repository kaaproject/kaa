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

#import <XCTest/XCTest.h>
#import "SyncTask.h"

#define TAG @"SyncTaskTest >>>"

@interface SyncTaskTest : XCTestCase

@end

@implementation SyncTaskTest

- (void)testMerge {
    SyncTask *task1 = [[SyncTask alloc] initWithTransportType:TRANSPORT_TYPE_CONFIGURATION ackOnly:YES all:NO];
    SyncTask *task2 = [[SyncTask alloc] initWithTransportType:TRANSPORT_TYPE_NOTIFICATION ackOnly:NO all:NO];
    
    SyncTask *merged = [SyncTask mergeTask:task1 withAdditionalTasks:[NSArray arrayWithObject:task2]];
    
    XCTAssertEqual(2, [[merged getTransportTypes] count]);
    XCTAssertFalse([merged isAckOnly]);
    XCTAssertFalse([merged isAll]);
}

- (void)testMergeAcks {
    SyncTask *task1 = [[SyncTask alloc] initWithTransportType:TRANSPORT_TYPE_CONFIGURATION ackOnly:YES all:NO];
    SyncTask *task2 = [[SyncTask alloc] initWithTransportType:TRANSPORT_TYPE_NOTIFICATION ackOnly:YES all:NO];
    
    SyncTask *merged = [SyncTask mergeTask:task1 withAdditionalTasks:[NSArray arrayWithObject:task2]];
    
    XCTAssertEqual(2, [[merged getTransportTypes] count]);
    XCTAssertTrue([merged isAckOnly]);
    XCTAssertFalse([merged isAll]);
}

- (void)testMergeAll {
    SyncTask *task1 = [[SyncTask alloc] initWithTransportType:TRANSPORT_TYPE_CONFIGURATION ackOnly:YES all:NO];
    SyncTask *task2 = [[SyncTask alloc] initWithTransportType:TRANSPORT_TYPE_NOTIFICATION ackOnly:NO all:YES];
    
    SyncTask *merged = [SyncTask mergeTask:task1 withAdditionalTasks:[NSArray arrayWithObject:task2]];
    
    XCTAssertEqual(2, [[merged getTransportTypes] count]);
    XCTAssertFalse([merged isAckOnly]);
    XCTAssertTrue([merged isAll]);
}

@end
