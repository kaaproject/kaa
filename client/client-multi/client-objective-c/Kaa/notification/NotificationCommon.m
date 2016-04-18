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

#import "NotificationCommon.h"
#import "AvroBytesConverter.h"

@interface NotificationDeserializer ()

@property (nonatomic, strong) AvroBytesConverter *converter;
@property (nonatomic, strong) id<ExecutorContext> executorContext;

@end

@implementation NotificationDeserializer

- (instancetype)initWithExecutorContext:(id<ExecutorContext>)context {
    self = [super init];
    if (self) {
        self.converter = [[AvroBytesConverter alloc] init];
        self.executorContext = context;
    }
    return self;
}

- (void)notifyDelegates:(NSArray *)delegates withTopic:(Topic *)topic data:(NSData *)notificationData {
    KAADummyNotification *notification = [self.converter fromBytes:notificationData object:[KAADummyNotification new]];
    for (id<NotificationDelegate> delegate in delegates) {
        [[self.executorContext getCallbackExecutor] addOperationWithBlock:^{
            [delegate onNotification:notification withTopicId:topic.id];
        }];
    }
}

@end