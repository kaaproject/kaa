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

#import "KaaClientProperties.h"
#import "NSString+Commons.h"
#import "EndpointGen.h"
#import "TransportProtocolId.h"
#import "GenericTransportInfo.h"
#import "TransportCommon.h"
#import "SHAMessageDigest.h"
#import "KaaDefaults.h"

@interface KaaClientProperties ()

@property(nonatomic, strong) NSUserDefaults *properties;
@property(nonatomic, strong) id<KAABase64> base64;
@property(nonatomic, strong) NSData *cachedPropertiesHash;

- (NSDictionary *)loadProperties;
- (NSDictionary *)parseBootstrapServersFromString:(NSString *)serversStr;

@end

@implementation KaaClientProperties

- (instancetype)initWithDictionary:(NSDictionary *)defaults base64:(id<KAABase64>)base64 {
    self = [super init];
    if (self) {
        self.properties = [NSUserDefaults standardUserDefaults];
        for (NSString *key in defaults) {
            [self.properties setObject:defaults[key] forKey:key];
        }
        self.base64 = base64;
    }
    return self;
}

- (instancetype)initDefaultsWithBase64:(id<KAABase64>)base64 {
    return [self initWithDictionary:[self loadProperties] base64:base64];
}

- (NSData *)propertiesHash {
    if (!self.cachedPropertiesHash) {
        SHAMessageDigest *digest = [[SHAMessageDigest alloc] init];
        [digest updateWithString:[self.properties objectForKey:TRANSPORT_POLL_DELAY_KEY]];
        [digest updateWithString:[self.properties objectForKey:TRANSPORT_POLL_PERIOD_KEY]];
        [digest updateWithString:[self.properties objectForKey:TRANSPORT_POLL_UNIT_KEY]];
        [digest updateWithString:[self.properties objectForKey:BOOTSTRAP_SERVERS_KEY]];
        [digest updateWithString:[self.properties objectForKey:CONFIG_DATA_DEFAULT_KEY]];
        [digest updateWithString:[self.properties objectForKey:CONFIG_SCHEMA_DEFAULT_KEY]];
        [digest updateWithString:[self.properties objectForKey:SDK_TOKEN_KEY]];
        self.cachedPropertiesHash = [NSMutableData dataWithBytes:[digest final] length:[digest size]];
    }
    return self.cachedPropertiesHash;
}

- (NSData *)propertyAsData:(NSString *)property {
    return [[self.properties objectForKey:property] dataUsingEncoding:NSUTF8StringEncoding];
}

- (NSDictionary *)loadProperties {
    NSData *schemaBytes = [self.base64 decodeString:CONFIG_SCHEMA_DEFAULT];
    return @{
       BUILD_VERSION_KEY         : BUILD_VERSION,
       BUILD_COMMIT_HASH_KEY     : BUILD_COMMIT_HASH,
       TRANSPORT_POLL_DELAY_KEY  : TRANSPORT_POLL_DELAY,
       TRANSPORT_POLL_PERIOD_KEY : TRANSPORT_POLL_PERIOD,
       TRANSPORT_POLL_UNIT_KEY   : TRANSPORT_POLL_UNIT,
       BOOTSTRAP_SERVERS_KEY     : BOOTSTRAP_SERVERS,
       CONFIG_DATA_DEFAULT_KEY   : CONFIG_DATA_DEFAULT,
       CONFIG_SCHEMA_DEFAULT_KEY : [[NSString alloc] initWithData:schemaBytes encoding:NSUTF8StringEncoding],
       STATE_FILE_LOCATION_KEY   : STATE_FILE_LOCATION,
       SDK_TOKEN_KEY             : SDK_TOKEN
    };
}

- (NSDictionary *)parseBootstrapServersFromString:(NSString *)serversStr {
    NSMutableDictionary *servers = [NSMutableDictionary dictionary];
    NSArray *splittedServers = [serversStr componentsSeparatedByString:@";"];
    for (NSString *server in splittedServers) {
        if (server && server.length > 0) {
            NSArray *tokens = [server componentsSeparatedByString:@":"];
            ProtocolMetaData *metaData = [[ProtocolMetaData alloc] init];
            [metaData setAccessPointId:[tokens[0] intValue]];
            ProtocolVersionPair *versionInfo = [[ProtocolVersionPair alloc] init];
            versionInfo.id = [tokens[1] intValue];
            versionInfo.version = [tokens[2] intValue];
            [metaData setProtocolVersionInfo:versionInfo];
            [metaData setConnectionInfo:[self.base64 decodeString:tokens[3]]];
            TransportProtocolId *key = [[TransportProtocolId alloc] initWithId:versionInfo.id version:versionInfo.version];
            NSMutableArray *serverList = servers[key];
            if (!serverList) {
                serverList = [NSMutableArray array];
                servers[key] = serverList;
            }
            [serverList addObject:[[GenericTransportInfo alloc] initWithServerType:SERVER_BOOTSTRAP meta:metaData]];
        }
    }
    return servers;
}

- (NSDictionary *)bootstrapServers {
    return [self parseBootstrapServersFromString:[self.properties stringForKey:BOOTSTRAP_SERVERS_KEY]];
}

- (NSString *)buildVersion {
    return [self.properties stringForKey:BUILD_VERSION_KEY];
}

- (NSString *)commitHash {
    return [self.properties stringForKey:BUILD_COMMIT_HASH_KEY];
}

- (NSString *)sdkToken {
    return [self.properties stringForKey:SDK_TOKEN_KEY];
}

- (int32_t)pollDelay {
    return [[self.properties stringForKey:TRANSPORT_POLL_DELAY_KEY] intValue];
}

- (int32_t)pollPeriod {
    return [[self.properties stringForKey:TRANSPORT_POLL_PERIOD_KEY] intValue];
}

- (TimeUnit)pollUnit {
    return (TimeUnit)[[self.properties stringForKey:TRANSPORT_POLL_UNIT_KEY] intValue];
}

- (NSData *)defaultConfigData {
    NSString *schema = [self.properties stringForKey:CONFIG_DATA_DEFAULT_KEY];
    if (!schema) {
        return nil;
    }
    return [self.base64 decodeBase64:[schema dataUsingEncoding:NSUTF8StringEncoding]];
}

- (NSData *)defaultConfigSchema {
    NSString *schema = [self.properties stringForKey:CONFIG_SCHEMA_DEFAULT_KEY];
    if (!schema) {
        return nil;
    }
    return [schema dataUsingEncoding:NSUTF8StringEncoding]; 
}

- (NSString *)stringForKey:(NSString *)key {
    return [self.properties stringForKey:key];
}

- (void)setString:(NSString *)object forKey:(NSString *)key {
    [self.properties setObject:object forKey:key];
}

@end
