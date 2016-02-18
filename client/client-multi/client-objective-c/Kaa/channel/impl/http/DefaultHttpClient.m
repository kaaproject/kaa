/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import "DefaultHttpClient.h"
#import <AFNetworking/AFNetworking.h>
#import "Constants.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"DefaultHttpClient >>>"

#define SUCCESS_CODE        (200)
#define REDIRECTION_CODE    (300)

@interface DefaultHttpClient ()

@property (nonatomic, strong) AFHTTPClient *client;
@property (nonatomic, strong) AFHTTPRequestOperation *operation;
@property (nonatomic) volatile BOOL isShutDown;

- (NSData *)getResponseBody:(AFHTTPRequestOperation *)response verify:(BOOL)verify;

@end

@implementation DefaultHttpClient

- (instancetype)initWithURLString:(NSString *)url
                    privateKeyRef:(SecKeyRef)privateK
                     publicKeyRef:(SecKeyRef)publicK
                        remoteKey:(NSData *)remoteK {
    self = [super initWithURLString:url privateKeyRef:privateK publicKeyRef:publicK remoteKey:remoteK];
    if (self) {
        self.client = [[AFHTTPClient alloc] initWithBaseURL:[NSURL URLWithString:url]];
        self.isShutDown = NO;
    }
    return self;
}

- (NSData *)executeHttpRequest:(NSString *)uri entity:(NSDictionary *)entity verifyResponse:(BOOL)verifyResponse {
    if (self.isShutDown) {
        DDLogError(@"%@ Can't proceed with request because service is down: %@", TAG, uri);
        [NSException raise:KaaInterruptedException format:@"Client is already down"];
        return nil;
    }
    
    NSMutableURLRequest *request =
    [self.client multipartFormRequestWithMethod:@"POST"
                                           path:uri
                                     parameters:nil
                      constructingBodyWithBlock:^(id <AFMultipartFormData>formData) {
                          for (NSString *key in entity.allKeys) {
                              [formData appendPartWithFormData:entity[key] name:key];
                          }
                      }];
    self.operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    [self.operation setCompletionBlockWithSuccess:^(AFHTTPRequestOperation *operation, id responseObject) {
#pragma unused(operation, responseObject)
        DDLogVerbose(@"%@ Successfully processed request", TAG);
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
#pragma unused(operation)
        DDLogError(@"%@ Error processing request: %@", TAG, error);
    }];
    [self.client enqueueHTTPRequestOperation:self.operation];
    [self.operation waitUntilFinished];
    NSData *result = nil;
    NSInteger statusCode = [[self.operation response] statusCode];
    if (statusCode >= SUCCESS_CODE && statusCode < REDIRECTION_CODE) {
        result = [self getResponseBody:self.operation verify:verifyResponse];
    } else {
        DDLogError(@"%@ TransportException response status: %li", TAG, (long)statusCode);
        [NSException raise:KaaTransportException format:@"%li", (long)statusCode];
    }
    self.operation = nil;
    return result;
}

- (void)close {
    if (!self.isShutDown) {
        [self.client.operationQueue cancelAllOperations];
        self.isShutDown = YES;
    }
}

- (void)abort {
    if ([self canAbort]) {
        [self.operation cancel];
        self.operation = nil;
    }
}

- (BOOL)canAbort {
    return self.operation && ![self.operation isCancelled] && ![self.operation isFinished];
}

- (NSData *)getResponseBody:(AFHTTPRequestOperation *)response verify:(BOOL)verify {
    if (!response || !response.responseData) {
        [NSException raise:KaaIOException format:@"Can't read message!"];
    }
    
    NSData *result = nil;
    if (verify) {
        NSDictionary *headers = response.response.allHeaderFields;
        DDLogVerbose(@"%@ %@", TAG, headers);
        NSString *signatureHeader = headers[SIGNATURE_HEADER_NAME];
        if (!signatureHeader) {
            [NSException raise:KaaIOException format:@"Can't verify message"];
        }
        
        NSData *signature = [signatureHeader dataUsingEncoding:NSUTF8StringEncoding];
        
        result = [self verifyResponse:response.responseData signature:signature];
    } else {
        result = response.responseData;
    }
    
    return result;
}

@end
