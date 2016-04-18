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

#import "TransactionId.h"
#import "UUID.h"

@interface TransactionId ()

@property (nonatomic, strong) NSString *identifier;

@end

@implementation TransactionId

- (instancetype)init {
    return [self initWithStringId:[UUID randomUUID]];
}

- (instancetype)initWithStringId:(NSString *)stringId {
    self = [super init];
    if (self) {
        self.identifier = stringId;
    }
    return self;
}

- (instancetype)initWithTransactionId:(TransactionId *)transactionId {
    return [self initWithStringId:transactionId.identifier];
}

- (id)copyWithZone:(NSZone *)zone {
    return [[[self class] allocWithZone:zone] initWithStringId:self.identifier];
}

- (NSUInteger)hash {
    int prime = 31;
    return prime + [self.identifier hash];
}

- (BOOL)isEqual:(id)object {
    if ([object isKindOfClass:[TransactionId class]]) {
        TransactionId *other = (TransactionId *)object;
        if ([self.identifier isEqualToString:other.identifier]) {
            return YES;
        }
    }
    
    return NO;
}

- (NSString *)description {
    return self.identifier;
}

@end
