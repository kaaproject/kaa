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

#import "KAAUnion.h"

@implementation KAAUnion

- (instancetype)initWithBranch:(int)branch data:(id)data {
    self = [super init];
    if (self) {
        self.branch = branch;
        self.data = data;
    }
    return self;
}

- (instancetype)initWithBranch:(int)branch {
    self = [super init];
    if (self) {
        self.branch = branch;
        self.data = nil;
    }
    return self;
}

+ (instancetype)unionWithBranch:(int)branch data:(id)data {
    return [[self alloc] initWithBranch:branch data:data];
}

+ (instancetype)unionWithBranch:(int)branch {
    return [[self alloc] initWithBranch:branch];
}

- (NSString *)description {
    if (self.data) {
        return [NSString stringWithFormat:@"Branch:%i Data(%@):%@", self.branch, [self.data class], [self.data description]];
    } else {
        return [NSString stringWithFormat:@"Empty union with branch: %i", self.branch];
    }
}

@end
