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

#import "KaaClientFactory.h"
#import "BaseKaaClient.h"
#import "KaaLogging.h"

#define TAG @"KaaClientFactory >>>"

@implementation KaaClientFactory

+ (id<KaaClient>)clientWithContext:(id<KaaClientPlatformContext>)context stateDelegate:(id<KaaClientStateDelegate>)delegate {
    
    [DDLog addLogger:[DDTTYLogger sharedInstance]]; // TTY = Xcode console
    [DDLog addLogger:[DDASLLogger sharedInstance]]; // ASL = Apple System Logs
    
    @try {
        return [[BaseKaaClient alloc] initWithPlatformContext:context delegate:delegate];
    }
    @catch (NSException *exception) {
        DDLogError(@"%@ Failed to create Kaa client: %@. Reason: %@", TAG, exception.name, exception.reason);
        [NSException raise:KaaInvalidChannelException format:@"%@:%@", exception.name, exception.reason];
    }
}

+ (id<KaaClient>)clientWithContext:(id<KaaClientPlatformContext>)context {
    return [self clientWithContext:context stateDelegate:nil];
}

+ (id<KaaClient>)clientWithStateDelegate:(id<KaaClientStateDelegate>)delegate {
    return [self clientWithContext:[[DefaultKaaPlatformContext alloc] init] stateDelegate:delegate];
}

+ (id<KaaClient>)client {
    return [self clientWithContext:[[DefaultKaaPlatformContext alloc] init]];
}

@end
