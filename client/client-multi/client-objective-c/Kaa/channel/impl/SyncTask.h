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

#import <Foundation/Foundation.h>
#import "TransportCommon.h"

/**
 * Abstraction designed to represent transport types need to sync.
 */
@interface SyncTask : NSObject

- (instancetype)initWithTransportType:(TransportType)type ackOnly:(BOOL)ackOnly all:(BOOL)all;
/**
 <p></p>
 @param types set of TransportType wrapped with NSNumber
 */
- (instancetype)initWithTransports:(NSSet *)types ackOnly:(BOOL)ackOnly all:(BOOL)all;

- (NSSet *)getTransportTypes;
- (BOOL)isAckOnly;
- (BOOL)isAll;

+ (SyncTask *)mergeTask:(SyncTask *)task withAdditionalTasks:(NSArray *)tasks;

@end
