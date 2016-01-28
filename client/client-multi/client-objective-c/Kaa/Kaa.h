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

#import "KaaClient.h"

#import "KaaDataChannel.h"
#import "KaaTransport.h"
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
#import "SchemaProcessor.h"
#import "ConfigurationTransport.h"
#import "LogCollector.h"

#import "CommonEPConstants.h"

#import "KaaClientStateDelegate.h"
#import "KaaClientPlatformContext.h"
#import "DefaultKaaPlatformContext.h"

#import "KaaExceptions.h"
#import "KaaLogging.h"

#import "DefaultLogUploadStrategy.h"
#import "PeriodicLogUploadStrategy.h"
#import "RecordCountLogUploadStrategy.h"
#import "RecordCountWithTimeLimitLogUploadStrategy.h"
#import "StorageSizeLogUploadStrategy.h"
#import "StorageSizeWithTimeLimitLogUploadStrategy.h"

/**
 * Used to create new Kaa client instances.
 */
@interface Kaa : NSObject

/**
 * Creates new Kaa client with specified platform context and state delegate.
 */
+ (id<KaaClient>)clientWithContext:(id<KaaClientPlatformContext>)context stateDelegate:(id<KaaClientStateDelegate>)delegate;

/**
 * Creates new Kaa client with specified platform context.
 */
+ (id<KaaClient>)clientWithContext:(id<KaaClientPlatformContext>)context;

/**
 * Creates new Kaa client with default platform context and specified client state delegate.
 */
+ (id<KaaClient>)clientWithDelegate:(id<KaaClientStateDelegate>)delegate;

/**
 * Creates new Kaa client based on default platform context.
 */
+ (id<KaaClient>)client;

@end