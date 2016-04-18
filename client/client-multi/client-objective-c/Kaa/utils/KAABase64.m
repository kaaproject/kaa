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

#import "KAABase64.h"

@implementation CommonBase64

- (NSData *)decodeBase64:(NSData *)base64Data {
    return [[NSData alloc] initWithBase64EncodedData:base64Data options:0];
}

- (NSData *)decodeString:(NSString *)base64String {
    return [[NSData alloc] initWithBase64EncodedString:base64String options:0];
}

- (NSData *)encodeBase64:(NSData *)binaryData {
    return [binaryData base64EncodedDataWithOptions:0];
}

- (NSString *)encodedString:(NSData *)binaryData {
    return [binaryData base64EncodedStringWithOptions:0];
}

@end
