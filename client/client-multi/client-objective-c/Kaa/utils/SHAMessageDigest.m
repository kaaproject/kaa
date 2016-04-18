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

#import "SHAMessageDigest.h"
#import "NSString+Commons.h"

@interface SHAMessageDigest ()

- (void)reset;

@end

@implementation SHAMessageDigest {
    CC_SHA1_CTX context;
    unsigned char digest[CC_SHA1_DIGEST_LENGTH];
}

- (void)reset {
    CC_SHA1_Init(&context);
    memset(digest, 0, sizeof(digest));
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self reset];
    }
    return self;
}

- (void)update:(NSData *)data {
    if (data && [data length] > 0) {
        CC_SHA1_Update(&context, [data bytes], (CC_LONG)[data length]);
    }
}

- (void)updateWithString:(NSString *)string {
    if (string && ![string isEmpty]) {
        const char *cString = [string cStringUsingEncoding:NSUTF8StringEncoding];
        CC_LONG strLength = (CC_LONG)strlen(cString);
        CC_SHA1_Update(&context, cString, strLength);
    }
}

- (size_t)size {
    return sizeof(digest);
}

- (unsigned char *)final {
    CC_SHA1_Final(digest, &context);
    return digest;
}

@end
