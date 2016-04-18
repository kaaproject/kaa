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

#import "SyncTask.h"

@interface SyncTask ()

@property (nonatomic, strong) NSSet *transportTypes;
@property (nonatomic) BOOL ackOnly;
@property (nonatomic) BOOL all;

@end

@implementation SyncTask

- (instancetype)initWithTransportType:(TransportType)type ackOnly:(BOOL)ackOnly all:(BOOL)all {
    return [self initWithTransports:[NSSet setWithObject:@(type)] ackOnly:ackOnly all:all];
}

- (instancetype)initWithTransports:(NSSet *)types ackOnly:(BOOL)ackOnly all:(BOOL)all {
    self = [super init];
    if (self) {
        self.transportTypes = types;
        self.ackOnly = ackOnly;
        self.all = all;
    }
    return self;
}

- (NSSet *)getTransportTypes {
    return self.transportTypes;
}

- (BOOL)isAckOnly {
    return self.ackOnly;
}

- (BOOL)isAll {
    return self.all;
}

+ (SyncTask *)mergeTask:(SyncTask *)task withAdditionalTasks:(NSArray *)tasks {
    NSMutableSet *types = [NSMutableSet setWithSet:[task getTransportTypes]];
    BOOL ack = [task isAckOnly];
    BOOL all = [task isAll];
    for (SyncTask *task in tasks) {
        [types addObjectsFromArray:[task getTransportTypes].allObjects];
        ack = ack && [task isAckOnly];
        all = all || [task isAll];
    }
    return [[SyncTask alloc] initWithTransports:types ackOnly:ack all:all];
}

- (NSString *)description {
    return [NSString stringWithFormat:@"SyncTask [types: %@,ackOnly: %d,all: %d]", self.transportTypes, self.ackOnly, self.all];
}

@end
