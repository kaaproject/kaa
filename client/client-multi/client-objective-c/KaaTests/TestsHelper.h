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
#import "TransportProtocolId.h"
#import <Kaa/Kaa.h>
#import <XCTest/XCTest.h>

#define KAATestEqual(a, b)\
if ((a) != (b)) {\
[NSException raise:@"Test failed!" format:@"%li != %li", (long)a, (long)b];\
}

#define KAATestObjectsEqual(a, b)\
if (![(a) isEqual:(b)]) {\
[NSException raise:@"Test failed!" format:@"%@ ISN'T EQUAL TO %@", a, b];\
}

@interface TestsHelper : NSObject

+ (ProtocolMetaData *)buildMetaDataWithTransportProtocolId:(TransportProtocolId *)TPid
                                                      host:(NSString *)host
                                                      port:(int32_t)port
                                                 publicKey:(NSData *)publicKey;

+ (KaaClientProperties *)getProperties;

+ (NSData *)getData;

@end


@interface HttpClientMock : AbstractHttpClient

@end


/** 
 * XCTestLog class category to swizzle logging. Needed to stop test crashes.
 */
@interface XCTestLog (NoLog)

@end
