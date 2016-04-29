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

#import "EndpointObjectHash.h"
#import <CommonCrypto/CommonCrypto.h>
#import "NSData+Conversion.h"


@interface EndpointObjectHash ()

- (instancetype)initWithData:(NSData *)data;

@end

@implementation EndpointObjectHash

- (instancetype)initWithData:(NSData *)data {
    self = [super init];
    if (self) {
        _data = [NSData dataWithData:data];
    }
    return self;
}

+ (instancetype)hashWithString:(NSString *)data {
    if (!data) {
        return nil;
    }
    NSData *utf8Data = [data dataUsingEncoding:NSUTF8StringEncoding];
    NSData *encodedData = [utf8Data base64EncodedDataWithOptions:0];
    return [[self alloc] initWithData: encodedData];
}

+ (instancetype)hashWithBytes:(NSData *)data {
    if (!data) {
        return nil;
    }
    return [[self alloc] initWithData:data];
}

+ (instancetype)hashWithSHA1:(NSData *)data {
    if (!data) {
        return nil;
    }
    unsigned char hashedChars[20];
    CC_SHA1([data bytes], (CC_LONG)[data length], hashedChars);
    NSData *hashedData = [NSData dataWithBytes:hashedChars length:20];
    return [[self alloc] initWithData:hashedData];
}

- (NSUInteger)hash {
    const NSUInteger prime = 31;
    return prime + [self.data hash];
}

- (BOOL)isEqual:(id)object {
    if ([object isKindOfClass:[EndpointObjectHash class]]) {
        EndpointObjectHash *other = (EndpointObjectHash*)object;
        if ([self.data isEqualToData:other.data]) {
            return YES;
        }
    }
    
    return NO;
}

- (NSString *)description {
    return [self.data hexadecimalString];
}

@end
