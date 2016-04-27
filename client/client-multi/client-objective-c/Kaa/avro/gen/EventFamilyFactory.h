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
#import "EventManger.h"
#import "ExecutorContext.h"
#import "TransactionId.h"
#import "EventGen.h"

/**
 * Factory for accessing supported event families.
 *
 * AUTO-GENERATED: IN ORDER TO CHANGE IT, PLEASE CHANGE CORRESPONDING TEMPLATE
 */

@interface EventFamilyFactory : NSObject

- (instancetype)initWithManager:(id<EventManager>)manager executorContext:(id<ExecutorContext>)context;

- (TransactionId *)startEventsBlock;
- (void)submitEventsBlockWithTransactionId:(TransactionId *)trxId;
- (void)removeEventsBlockWithTransactionId:(TransactionId *)trxId;

@end
