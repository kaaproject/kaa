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
#import "LogGen.h"
#import "KAADummyLog.h"

/**
 * This class is auto-generated. Do not change it.
 *
 * Wrapper class to encapsulate Avro-generated log record.
 * Use it for log collecting.
 */
@interface LogRecord : NSObject

/**
 * Avro-encoded log record.
 */
@property(nonatomic, strong, readonly) NSData *data;

- (instancetype)initWithRecord:(KAADummyLog *)record;
- (instancetype)initWithData:(NSData *)data;
- (int64_t)getSize;

@end
