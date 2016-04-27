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

/**
 * Immutable class to represent transport ID. Holds references to transport
 * protocol id and transport protocol version
 */
@interface TransportProtocolId : NSObject <NSCopying>

@property(nonatomic, readonly) int32_t protocolId;
@property(nonatomic, readonly) int32_t protocolVersion;

- (instancetype)initWithId:(int32_t)id version:(int32_t)version;

@end
