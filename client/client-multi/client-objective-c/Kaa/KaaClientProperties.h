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

#import <Foundation/Foundation.h>
#import "KAABase64.h"
#import "TimeCommons.h"

#define BUILD_VERSION_KEY           @"build.version"
#define BUILD_COMMIT_HASH_KEY       @"build.commit_hash"
#define TRANSPORT_POLL_DELAY_KEY    @"transport.poll.initial_delay"
#define TRANSPORT_POLL_PERIOD_KEY   @"transport.poll.period"
#define TRANSPORT_POLL_UNIT_KEY     @"transport.poll.unit"
#define BOOTSTRAP_SERVERS_KEY       @"transport.bootstrap.servers"
#define CONFIG_DATA_DEFAULT_KEY     @"config.data.default"
#define CONFIG_SCHEMA_DEFAULT_KEY   @"config.schema.default"
#define STATE_FILE_LOCATION_KEY     @"state.file.location"
#define SDK_TOKEN_KEY               @"sdk_token"

@interface KaaClientProperties : NSObject

- (instancetype)initWithDictionary:(NSDictionary *)defaults base64:(id<KAABase64>)base64;

- (instancetype)initDefaults:(id<KAABase64>)base64;

- (NSData *)propertiesHash;

- (NSString *)buildVersion;

- (NSString *)commitHash;

- (NSString *)sdkToken;

- (NSInteger)pollDelay;

- (NSInteger)pollPeriod;

- (TimeUnit)pollUnit;

- (NSData *)defaultConfigData;

- (NSData *)defaultConfigSchema;

- (NSDictionary *)bootstrapServers; //<TransportProtocolId, NSArray<TransportConnectionInfo>> as key-value

- (NSString *)stringForKey:(NSString *)key;

- (void)setString:(NSString *)object forKey:(NSString *)key;

@end
