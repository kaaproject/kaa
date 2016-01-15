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
#import "KaaDataChannel.h"
#import "KaaTransport.h"
#import "KaaClient.h"
#import "KaaClientPlatformContext.h"
#import "KaaClientStateDelegate.h"

#import "GenericKaaClient.h"
#import "KaaChannelManager.h"
#import "ConfigurationCommon.h"
#import "EndpointRegistrationProcessor.h"
#import "EventManger.h"
#import "BaseEventFamily.h"
#import "ProfileManager.h"
#import "NotificationManager.h"
#import "UserTransport.h"
#import "BootstrapTransport.h"
#import "BootstrapManager.h"
#import "RedirectionTransport.h"
#import "MetaDataTransport.h"
#import "KaaClientStateDelegate.h"
#import "SchemaProcessor.h"
#import "ConfigurationTransport.h"
#import "LogCollector.h"
#import "Constants.h"
#import "CommonEPConstants.h"
#import "DefaultKaaPlatformContext.h"
#import "KaaExceptions.h"

/**
 * Creates new Kaa client based on platform context and optional state delegate.
 */
@interface Kaa : NSObject

+ (id<KaaClient>)clientWithContext:(id<KaaClientPlatformContext>)context andStateDelegate:(id<KaaClientStateDelegate>)delegate;

+ (id<KaaClient>)clientWithContext:(id<KaaClientPlatformContext>)context;

@end