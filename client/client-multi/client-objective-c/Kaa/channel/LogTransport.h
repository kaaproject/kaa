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

#ifndef Kaa_LogTransport_h
#define Kaa_LogTransport_h

#import <Foundation/Foundation.h>
#import "KaaTransport.h"
#import "EndpointGen.h"

/**
 * Processes the Logging requests and responses.
 */
@protocol LogProcessor

/**
 * Fills the given request with the latest Logging state.
 */
- (void)fillSyncRequest:(LogSyncRequest *)request;

/**
* Updates the state using response from the server.
*/
- (void)onLogResponse:(LogSyncResponse *)response;

@end

/**
 * <KaaTransport> for the Logging service.
 * Used for sending logs to the remote server.
 */
@protocol LogTransport <KaaTransport>

/**
 * Creates the Log request that consists of current log records.
 */
- (LogSyncRequest *)createLogRequest;

/**
 * Updates the state of the Log collector according to the given response.
 */
- (void)onLogResponse:(LogSyncResponse *)response;

/**
 * Sets the given Log processor.
 */
- (void)setLogProcessor:(id<LogProcessor>)processor;

@end

#endif
