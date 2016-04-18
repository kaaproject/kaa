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

#import "TestsHelper.h"
#import <objc/objc-runtime.h>

#pragma clang diagnostic ignored "-Wdeprecated-declarations"

@implementation TestsHelper

+ (ProtocolMetaData *)buildMetaDataWithTransportProtocolId:(TransportProtocolId *)TPid
                                                      host:(NSString *)host
                                                      port:(int32_t)port
                                                 publicKey:(NSData *)publicKey {
    int32_t publicKeyLength = CFSwapInt32((uint32_t)[publicKey length]);
    int32_t hostLength = CFSwapInt32((uint32_t)[host lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);
    int32_t portToWrite = CFSwapInt32(port);
    NSMutableData *data = [NSMutableData data];
    
    [data appendBytes:&publicKeyLength length:sizeof(publicKeyLength)];
    [data appendData:publicKey];
    [data appendBytes:&hostLength length:sizeof(hostLength)];
    [data appendData:[host dataUsingEncoding:NSUTF8StringEncoding]];
    [data appendBytes:&portToWrite length:sizeof(portToWrite)];
    
    ProtocolVersionPair *pair = [[ProtocolVersionPair alloc]init];
    [pair setId:TPid.protocolId];
    [pair setVersion:TPid.protocolVersion];
    
    ProtocolMetaData *md = [[ProtocolMetaData alloc] init];
    [md setConnectionInfo:data];
    [md setAccessPointId:(int32_t)[[NSString stringWithFormat:@"%@:%i", host, port] hash]];
    [md setProtocolVersionInfo:pair];
    return md;
}

+ (KaaClientProperties *)getProperties {
    KaaClientProperties *properties = [[KaaClientProperties alloc] initDefaultsWithBase64:[CommonBase64 new]];
    [properties setString:@"0" forKey:TRANSPORT_POLL_DELAY_KEY];
    [properties setString:@"1" forKey:TRANSPORT_POLL_PERIOD_KEY];
    [properties setString:@"1" forKey:TRANSPORT_POLL_UNIT_KEY];
    [properties setString:@"123456" forKey:SDK_TOKEN_KEY];
    [properties setString:STATE_FILE_DEFAULT forKey:STATE_FILE_LOCATION_KEY];
    return properties;
}

+ (NSData *)getData {
    char five = 5;
    NSMutableData *data = [NSMutableData dataWithBytes:&five length:sizeof(five)];
    [data appendBytes:&five length:sizeof(five)];
    [data appendBytes:&five length:sizeof(five)];
    return data;
}

@end


@implementation HttpClientMock

- (void)executeHttpRequest:(NSString *)uri
                    entity:(NSDictionary *)entity
            verifyResponse:(BOOL)verifyResponse
                   success:(void (^)(NSData *))success
                   failure:(void (^)(NSInteger))failure {
#pragma unused(uri, entity, verifyResponse, failure)
    success([TestsHelper getData]);
}

- (void)close {
}

- (void)abort {
}

- (BOOL)canAbort {
    return YES;
}

@end


/** 
 * Implementation for XCTestLog category. 
 * Although it is deprecated, XCTest still uses it implicitly.
 */
@implementation XCTestLog (NoLog)

/** 
 * This method is called when application starts. 
 * In this case it replaces XCTestLog implementation of method testLogWithFormat:arguments: with our custom method.
 */
+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class class = [self class];
        SEL originalSelector = @selector(testLogWithFormat:arguments:);
        SEL replaceSelector = @selector(testNoLogWithFormat:arguments:);
        Method originalMethod = class_getInstanceMethod(class, originalSelector);
        Method replaceMethod = class_getInstanceMethod(class, replaceSelector);
        BOOL didAddMethod = class_addMethod(class, originalSelector, method_getImplementation(replaceMethod), method_getTypeEncoding(replaceMethod));
        if (didAddMethod) {
            class_replaceMethod(class, replaceSelector, method_getImplementation(originalMethod), method_getTypeEncoding(originalMethod));
        } else {
            method_exchangeImplementations(originalMethod, replaceMethod);
        }
    });
}

/**
 * Method to replace original testLogWithFormat:arguments: method of XCTestLog. It passes logging routine to CocoaLumberjack.
 */
- (void)testNoLogWithFormat:(NSString *)format arguments:(va_list)arguments NS_FORMAT_FUNCTION(1,0) {
    if (format) {
        DDLogDebug(format, arguments);
    }
}

@end
