/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import <Foundation/Foundation.h>

#import "KaaClient.h"
#import "GenericKaaClient.h"
#import "KaaClientState.h"

#import "GenericLogCollector.h"
#import "LogCollector.h"

#import "ConfigurationCommon.h"
#import "NotificationCommon.h"
#import "ProfileCommon.h"

#import "EndpointRegistrationProcessor.h"
#import "NotificationProcessor.h"
#import "SchemaProcessor.h"

#import "LogStorage.h"
#import "SimpleConfigurationStorage.h"

#import "AvroBytesConverter.h"

#import "BaseEventFamily.h"
#import "KaaDataChannel.h"

#import "BootstrapManager.h"
#import "EndpointRegistrationManager.h"
#import "EventManger.h"
#import "FailoverManager.h"
#import "KaaChannelManager.h"
#import "NotificationManager.h"
#import "ProfileManager.h"

#import "BootstrapTransport.h"
#import "ConfigurationTransport.h"
#import "DefaultProfileTransport.h"
#import "EventTransport.h"
#import "KaaTransport.h"
#import "LogTransport.h"
#import "MetaDataTransport.h"
#import "NotificationTransport.h"
#import "ProfileTransport.h"
#import "RedirectionTransport.h"
#import "UserTransport.h"

#import "Constants.h"
#import "KaaExceptions.h"

#import "KaaClientStateDelegate.h"
#import "KaaClientPlatformContext.h"
#import "DefaultKaaPlatformContext.h"

#import "DefaultLogUploadStrategy.h"
#import "PeriodicLogUploadStrategy.h"
#import "RecordCountLogUploadStrategy.h"
#import "RecordCountWithTimeLimitLogUploadStrategy.h"
#import "StorageSizeLogUploadStrategy.h"
#import "StorageSizeWithTimeLimitLogUploadStrategy.h"

#import "ConfigurationGen.h"
#import "EndpointGen.h"
#import "EventGen.h"
#import "LogGen.h"
#import "NotificationGen.h"
#import "ProfileGen.h"

#import "AccessPointCommand.h"
#import "EventDelegates.h"
#import "ExecutorContext.h"
#import "KAABase64.h"
#import "LogFailoverCommand.h"

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
+ (id<KaaClient>)clientWithStateDelegate:(id<KaaClientStateDelegate>)delegate;

/**
 * Creates new Kaa client based on default platform context.
 */
+ (id<KaaClient>)client;

@end