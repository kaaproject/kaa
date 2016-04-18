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

#import "BlockingQueue.h"

@interface BlockingQueue()

@property (nonatomic, strong) NSMutableArray *queue;
@property (nonatomic, strong) NSCondition *condition;

@end

@implementation BlockingQueue

- (instancetype)init {
    self = [super init];
    if (self) {
        self.queue = [NSMutableArray array];
        self.condition = [[NSCondition alloc] init];
    }
    return self;
}

- (void)offer:(id)object {
    [self.condition lock];
    [self.queue addObject:object];
    [self.condition signal];
    [self.condition unlock];
}

- (id)take {
    [self.condition lock];
    while (self.queue.count == 0) {
        [self.condition wait];
    }
    id object = self.queue.firstObject;
    [self.queue removeObjectAtIndex:0];
    [self.condition unlock];
    
    return object;
}

- (void)drainTo:(NSMutableArray *)array {
    if ([self.queue count] == 0) {
        return;
    }
    
    [self.condition lock];
    [array addObjectsFromArray:self.queue];
    [self.queue removeAllObjects];
    [self.condition unlock];
}

- (NSUInteger)size {
    return [self.queue count];
}

@end
