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

#import "DefaultLogTransport.h"
#import "KaaLogging.h"

#define TAG @"DefaultLogTransport >>>"

@interface DefaultLogTransport ()

@property (nonatomic, strong) id<LogProcessor> processor;

@end

@implementation DefaultLogTransport

- (void)setLogProcessor:(id<LogProcessor>)processor {
    self.processor = processor;
}

- (LogSyncRequest *)createLogRequest {
    if (self.processor) {
        LogSyncRequest *request = [[LogSyncRequest alloc] init];
        [self.processor fillSyncRequest:request];
        return request;
    } else {
        DDLogError(@"%@ Can't create request. LogProcessor is nil", TAG);
    }
    return nil;
}

- (void)onLogResponse:(LogSyncResponse *)response {
    if (self.processor) {
        @try {
            [self.processor onLogResponse:response];
        }
        @catch (NSException *exception) {
            DDLogError(@"%@ Failed to process Log response: %@, reason: %@.", TAG, exception.name, exception.reason);
        }
    } else {
        DDLogError(@"%@ Can't process response. LogProcessor is nil", TAG);
    }
}

- (TransportType)getTransportType {
    return TRANSPORT_TYPE_LOGGING;
}

@end
