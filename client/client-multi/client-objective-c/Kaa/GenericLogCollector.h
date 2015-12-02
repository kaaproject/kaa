/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef Kaa_GenericLogCollector_h
#define Kaa_GenericLogCollector_h

#import <Foundation/Foundation.h>
#import "LogStorage.h"
#import "LogUploadStrategy.h"

/**
 * Root interface for a log collector.
 *
 * Adds new log record to a local storage.
 *
 * May be configured by setting user defined log record storage and log upload
 * strategy. Each of them may be set independently of others.
 *
 * Reference implementation of each module is provided.
 *
 * @see LogStorage
 * @see LogStorageStatus
 * @see LogUploadStrategy
 * @see LogUploadConfiguration
 */
@protocol GenericLogCollector

/**
 * Set user implementation of a log storage.
 *
 * storage - user-defined log storage object
 */
- (void)setStorage:(id<LogStorage>)storage;

/**
 * Set user implementation of a log upload strategy.
 *
 * strategy - user-defined log upload strategy object.
 */
- (void)setStrategy:(id<LogUploadStrategy>)strategy;

/**
 * Stops and/or cleanup resources.
 */
- (void)stop;

@end
#endif
