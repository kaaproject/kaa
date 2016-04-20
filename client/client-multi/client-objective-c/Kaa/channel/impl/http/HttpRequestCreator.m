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

#import "HttpRequestCreator.h"
#import "NSData+Conversion.h"
#import "Constants.h"
#import "KaaLogging.h"

#define TAG @"HTTPRequestCreator >>>"

@implementation HttpRequestCreator

+ (NSDictionary *)createBootstrapHttpRequest:(NSData *)body withEncoderDecoder:(MessageEncoderDecoder *)messageEncDec {
    return [self createHttpRequest:body withEncoderDecoder:messageEncDec sign:NO];
}

+ (NSDictionary *)createOperationHttpRequest:(NSData *)body withEncoderDecoder:(MessageEncoderDecoder *)messageEncDec {
    return [self createHttpRequest:body withEncoderDecoder:messageEncDec sign:YES];
}

+ (NSDictionary *)createHttpRequest:(NSData *)body withEncoderDecoder:(MessageEncoderDecoder *)messageEncDec sign:(BOOL)sign {
    if (!body || !messageEncDec) {
        DDLogError(@"%@ Unable to create http request, invalid params: %@ %@", TAG, body, messageEncDec);
        return nil;
    }
    
    NSData *requestKeyEncoded = [messageEncDec getEncodedSessionKey];
    NSData *requestBodyEncoded = [messageEncDec encodeData:body];
    NSData *signature = nil;
    
    if (sign) {
        signature = [messageEncDec signatureForMessage:requestKeyEncoded];
        DDLogVerbose(@"%@ Signature size: %li", TAG, (long)(signature.length));
        DDLogVerbose(@"%@ Signature: %@", TAG, [signature hexadecimalString]);
    }
    
    DDLogVerbose(@"%@ RequestKeyEncoded size: %li", TAG, (long)(requestKeyEncoded.length));
    DDLogVerbose(@"%@ RequestKeyEncoded: %@", TAG, [requestKeyEncoded hexadecimalString]);
    DDLogVerbose(@"%@ RequestBodyEncoded size: %li", TAG, (long)(requestBodyEncoded.length));
    DDLogVerbose(@"%@ RequestBodyEncoded: %@", TAG, [requestBodyEncoded hexadecimalString]);
    
    NSMutableDictionary *requestEntity = [NSMutableDictionary dictionary];
    if (sign) {
        requestEntity[REQUEST_SIGNATURE_ATTR_NAME] = signature;
    }
    requestEntity[REQUEST_KEY_ATTR_NAME] = requestKeyEncoded;
    requestEntity[REQUEST_DATA_ATTR_NAME] = requestBodyEncoded;
    
    return requestEntity;
}

@end
