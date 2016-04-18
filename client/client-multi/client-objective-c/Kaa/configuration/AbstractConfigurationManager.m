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

#import "AbstractConfigurationManager.h"
#import "KaaLogging.h"

#pragma clang diagnostic ignored "-Wprotocol"

#define TAG @"AbstractConfigurationManager >>>"

@interface AbstractConfigurationManager ()

@property(nonatomic, strong) KaaClientProperties *properties;
@property(nonatomic, strong) id<KaaClientState> state;
@property(nonatomic, strong) id<ConfigurationStorage> storage;
@property(nonatomic, strong) NSData *configurationData;
@property(nonatomic, strong) id<ExecutorContext> executorContext;

@property(strong) NSMutableSet *delegates;
@property(strong) NSLock *delegatesLock;

- (NSData *)loadConfigurationData;

@end

@implementation AbstractConfigurationManager

- (instancetype)initWithClientProperties:(KaaClientProperties *)properties
                                   state:(id<KaaClientState>)state
                         executorContext:(id<ExecutorContext>)context {
    self = [super init];
    if (self) {
        self.delegates = [NSMutableSet set];
        self.properties = properties;
        self.state = state;
        _deserializer = [[ConfigurationDeserializer alloc] initWithExecutorContext:context];
        self.executorContext = context;
    }
    return self;
}

- (void)initiate {
    [self getConfigurationData];
    DDLogDebug(@"%@ Configuration manager init completed!", TAG);
}

- (void)addDelegate:(id<ConfigurationDelegate>)delegate {
    if (delegate) {
        DDLogVerbose(@"%@ Adding delegate %@", TAG, delegate);
        [self.delegatesLock lock];
        [self.delegates addObject:delegate];
        [self.delegatesLock unlock];
    } else {
        [NSException raise:NSInvalidArgumentException format:@"Can't add nil delegate"];
    }
}

- (void)removeDelegate:(id<ConfigurationDelegate>)delegate {
    if (delegate) {
        DDLogVerbose(@"%@ Removing delegate", TAG);
        [self.delegatesLock lock];
        [self.delegates removeObject:delegate];
        [self.delegatesLock unlock];
    } else {
        [NSException raise:NSInvalidArgumentException format:@"Can't remove nil delegate"];

    }
}

- (void)processConfigurationData:(NSData *)data fullResync:(BOOL)fullResync {
    if (fullResync) {
        self.configurationData = data;
        DDLogVerbose(@"%@ Received configuration data: %@", TAG, self.configurationData);
        if (self.storage) {
            DDLogDebug(@"%@ Persisting configuration data from storage: %@", TAG, self.storage);
            [self.storage saveConfiguration:self.configurationData];
            DDLogDebug(@"%@ Persisted configuration data from storage: %@", TAG, self.storage);
        }
        [self.delegatesLock lock];
        [_deserializer notifyDelegates:self.delegates withData:self.configurationData];
        [self.delegatesLock unlock];
    } else {
        DDLogWarn(@"%@ Only full resync delta is supported!", TAG);
    }
}

- (EndpointObjectHash *)getConfigurationHash {
    return [EndpointObjectHash hashWithSHA1:[self getConfigurationData]];
}

- (void)setConfigurationStorage:(id<ConfigurationStorage>)storage {
    self.storage = storage;
}

- (NSData *)loadConfigurationData {
    if (self.storage) {
        if ([self.state isConfigurationVersionUpdated]) {
            DDLogDebug(@"%@ Clearing old configuration data from storage: %@", TAG, self.storage);
            @try {
                [self.storage clearConfiguration];
            }
            @catch (NSException *exception) {
                DDLogError(@"%@ Failed to clear configuration from storage: %@", TAG, exception);
            }
        } else {
            DDLogDebug(@"%@ Loading configuration data from storage: %@", TAG, self.storage);
            @try {
                self.configurationData = [self.storage loadConfiguration];
            }
            @catch (NSException *exception) {
                DDLogError(@"%@ Failed to load configuration from storage: %@", TAG, exception);
            }
        }
    }
    if (!self.configurationData) {
        DDLogDebug(@"%@ Loading configuration data from defaults: %@", TAG, self.storage);
        self.configurationData = [self getDefaultConfigurationData];
    }
    DDLogVerbose(@"%@ Loaded configuration data: %@", TAG, self.configurationData);
    return self.configurationData;
}

- (NSData *)getConfigurationData {
    if (!self.configurationData) {
        self.configurationData = [self loadConfigurationData];
    }
    return self.configurationData;
}

- (NSData *)getDefaultConfigurationData {
    return [self.properties defaultConfigData];
}

@end
