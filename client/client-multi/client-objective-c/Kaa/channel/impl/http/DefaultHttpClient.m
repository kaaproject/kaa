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

#import "DefaultHttpClient.h"
#import <AFNetworking/AFNetworking.h>
#import "Constants.h"
#import "KaaLogging.h"
#import "KaaExceptions.h"

#define TAG @"DefaultHttpClient >>>"

#define SUCCESS_CODE            (200)
#define REDIRECTION_CODE        (300)
#define REQUEST_TIMEOUT_SEC     (5.0)

@interface DefaultHttpClient ()

@property (nonatomic, strong) AFHTTPRequestSerializer *requestSerializer;
@property (nonatomic, strong) NSURLSession *session;
@property (nonatomic, strong) NSURLSessionDataTask *task;
@property (nonatomic) volatile BOOL isShutDown;
@property (nonatomic, strong) NSString *baseURLString;

- (void)processResponseBody:(NSURLResponse *)response
                    payload:(NSData *)payload
                     verify:(BOOL)verify
                    success:(void (^)(NSData *))success
                    failure:(void (^)(NSInteger))failure;

@end

@implementation DefaultHttpClient

- (instancetype)initWithURLString:(NSString *)url
                    privateKeyRef:(SecKeyRef)privateK
                     publicKeyRef:(SecKeyRef)publicK
                        remoteKey:(NSData *)remoteK {
    self = [super initWithURLString:url privateKeyRef:privateK publicKeyRef:publicK remoteKey:remoteK];
    if (self) {
        NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
        configuration.timeoutIntervalForRequest = REQUEST_TIMEOUT_SEC;
        self.session = [NSURLSession sessionWithConfiguration:configuration];
        self.requestSerializer = [AFHTTPRequestSerializer serializer];
        self.baseURLString = url;
    }
    return self;
}

- (void)executeHttpRequest:(NSString *)uri
                    entity:(NSDictionary *)entity
            verifyResponse:(BOOL)verifyResponse
                   success:(void (^)(NSData *))success
                   failure:(void (^)(NSInteger))failure {
    
    if (self.isShutDown) {
        DDLogError(@"%@ Can't proceed with request because client is down: %@", TAG, uri);
        failure(HTTP_ERROR_CODE_CLIEN_IS_DOWN);
        return;
    }
    
    NSError *serializationError = nil;
    NSString *urlString = [self.baseURLString stringByAppendingPathComponent:uri];
    NSMutableURLRequest *request = [self.requestSerializer multipartFormRequestWithMethod:@"POST"
                                                                                URLString:urlString
                                                                               parameters:nil
                                                                constructingBodyWithBlock:^(id<AFMultipartFormData>formData) {
                                                                    for (NSString *key in entity.allKeys) {
                                                                        [formData appendPartWithFormData:entity[key] name:key];
                                                                    }
                                                                }
                                                                                    error:&serializationError];
    if (serializationError) {
        DDLogError(@"%@ Unable to serialize request to [%@], reason: %@", TAG, urlString, serializationError);
        failure(HTTP_ERROR_CODE_CANT_SERIALIZE_REQUEST);
        return;
    }
    
    __weak typeof(self) weakSelf = self;
    self.task = [self.session dataTaskWithRequest:request
                                completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
                                    
                                    if (error) {
                                        DDLogWarn(@"%@ Error executing request: %@", TAG, error);
                                    }
                                    weakSelf.task = nil;
                                    
                                    [weakSelf processResponseBody:response
                                                          payload:data
                                                           verify:verifyResponse
                                                          success:success
                                                          failure:failure];
                                }];
    
    [self.task resume];
}

- (void)close {
    if (!self.isShutDown) {
        [self.session getTasksWithCompletionHandler:^(NSArray *dataTasks, NSArray *uploadTasks, NSArray *downloadTasks) {
#pragma unused(uploadTasks, downloadTasks)
            for (NSURLSessionTask *task in dataTasks) {
                [task cancel];
            }
            
            self.isShutDown = YES;
        }];
    }
}

- (void)abort {
    if ([self canAbort]) {
        [self.task cancel];
        self.task = nil;
    }
}

- (BOOL)canAbort {
    return self.task && [self.task state] == NSURLSessionTaskStateRunning;
}

- (void)processResponseBody:(NSURLResponse *)response
                    payload:(NSData *)payload
                     verify:(BOOL)verify
                    success:(void (^)(NSData *))success
                    failure:(void (^)(NSInteger))failure {
    if (!response || !payload) {
        DDLogError(@"%@ Can't process response: %@, %@", TAG, response, payload);
        failure(HTTP_ERROR_CODE_CANT_READ_RESPONSE);
        return;
    }
    
    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
    
    NSInteger statusCode = httpResponse.statusCode;
    
    if (statusCode >= SUCCESS_CODE && statusCode < REDIRECTION_CODE) {
        
        NSData *result;
        if (verify) {
            DDLogVerbose(@"%@ %@", TAG, httpResponse.allHeaderFields);
            
            NSString *signatureHeader = httpResponse.allHeaderFields[SIGNATURE_HEADER_NAME];
            if (!signatureHeader) {
                DDLogError(@"%@ Can't verify response", TAG);
                failure(HTTP_ERROR_CODE_CANT_VERIFY_RESPONSE);
                return;
            }
            
            NSData *signature = [signatureHeader dataUsingEncoding:NSUTF8StringEncoding];
            
            result = [self verifyResponse:payload signature:signature];
        } else {
            result = payload;
        }
        
        success(result);
        
    } else {
        failure(statusCode);
    }
    
}

@end
