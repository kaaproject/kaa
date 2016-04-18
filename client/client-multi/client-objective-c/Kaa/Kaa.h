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

#import <UIKit/UIKit.h>

//! Project version number for Kaa.
FOUNDATION_EXPORT double KaaVersionNumber;

//! Project version string for Kaa.
FOUNDATION_EXPORT const unsigned char KaaVersionString[];

#import <Kaa/KaaClient.h>
#import <Kaa/GenericKaaClient.h>
#import <Kaa/KaaClientState.h>

#import <Kaa/GenericLogCollector.h>
#import <Kaa/LogCollector.h>

#import <Kaa/ConfigurationCommon.h>
#import <Kaa/NotificationCommon.h>
#import <Kaa/ProfileCommon.h>

#import <Kaa/EndpointRegistrationProcessor.h>
#import <Kaa/NotificationProcessor.h>
#import <Kaa/SchemaProcessor.h>

#import <Kaa/LogStorage.h>
#import <Kaa/SimpleConfigurationStorage.h>

#import <Kaa/AvroBytesConverter.h>

#import <Kaa/BaseEventFamily.h>
#import <Kaa/KaaDataChannel.h>

#import <Kaa/BootstrapManager.h>
#import <Kaa/EndpointRegistrationManager.h>
#import <Kaa/EventManger.h>
#import <Kaa/FailoverManager.h>
#import <Kaa/KaaChannelManager.h>
#import <Kaa/NotificationManager.h>
#import <Kaa/ProfileManager.h>

#import <Kaa/BootstrapTransport.h>
#import <Kaa/ConfigurationTransport.h>
#import <Kaa/DefaultProfileTransport.h>
#import <Kaa/EventTransport.h>
#import <Kaa/KaaTransport.h>
#import <Kaa/LogTransport.h>
#import <Kaa/MetaDataTransport.h>
#import <Kaa/NotificationTransport.h>
#import <Kaa/ProfileTransport.h>
#import <Kaa/RedirectionTransport.h>
#import <Kaa/UserTransport.h>

#import <Kaa/Constants.h>
#import <Kaa/KaaExceptions.h>

#import <Kaa/KaaClientStateDelegate.h>
#import <Kaa/KaaClientPlatformContext.h>
#import <Kaa/DefaultKaaPlatformContext.h>

#import <Kaa/DefaultLogUploadStrategy.h>
#import <Kaa/PeriodicLogUploadStrategy.h>
#import <Kaa/RecordCountLogUploadStrategy.h>
#import <Kaa/RecordCountWithTimeLimitLogUploadStrategy.h>
#import <Kaa/StorageSizeLogUploadStrategy.h>
#import <Kaa/StorageSizeWithTimeLimitLogUploadStrategy.h>

#import <Kaa/ConfigurationGen.h>
#import <Kaa/EndpointGen.h>
#import <Kaa/EventGen.h>
#import <Kaa/LogGen.h>
#import <Kaa/NotificationGen.h>
#import <Kaa/ProfileGen.h>

#import <Kaa/AccessPointCommand.h>
#import <Kaa/EventDelegates.h>
#import <Kaa/ExecutorContext.h>
#import <Kaa/KAABase64.h>
#import <Kaa/LogFailoverCommand.h>

#import <Kaa/AbstractConfigurationManager.h>
#import <Kaa/AbstractExecutorContext.h>
#import <Kaa/AbstractHttpChannel.h>
#import <Kaa/AbstractKaaClient.h>
#import <Kaa/BaseKaaClient.h>
#import <Kaa/BlockingQueue.h>
#import <Kaa/DefaultBootstrapChannel.h>
#import <Kaa/DefaultBootstrapDataProcessor.h>
#import <Kaa/DefaultBootstrapTransport.h>
#import <Kaa/DefaultBootstrapManager.h>
#import <Kaa/DefaultChannelManager.h>
#import <Kaa/DefaultConfigurationTransport.h>
#import <Kaa/DefaultEventTransport.h>
#import <Kaa/DefaultFailoverManager.h>
#import <Kaa/DefaultFailoverStrategy.h>
#import <Kaa/DefaultHttpClient.h>
#import <Kaa/DefaultLogTransport.h>
#import <Kaa/DefaultMetaDataTransport.h>
#import <Kaa/DefaultNotificationTransport.h>
#import <Kaa/DefaultOperationDataProcessor.h>
#import <Kaa/DefaultOperationTcpChannel.h>
#import <Kaa/DefaultOperationHttpChannel.h>
#import <Kaa/DefaultProfileManager.h>
#import <Kaa/DefaultRedirectionTransport.h>
#import <Kaa/EventListenersRequestBinding.h>
#import <Kaa/GenericTransportInfo.h>
#import <Kaa/HttpRequestCreator.h>
#import <Kaa/IPTransportInfo.h>
#import <Kaa/KAAFramer.h>
#import <Kaa/KAAMessageFactory.h>
#import <Kaa/KeyUtils.h>
#import <Kaa/SimpleExecutorContext.h>
#import <Kaa/TransportProtocolId.h>
#import <Kaa/TransportProtocolIdHolder.h>
#import <Kaa/MemLogStorage.h>
#import <Kaa/KaaClientPropertiesState.h>
#import <Kaa/TopicListHashCalculator.h>
#import <Kaa/KaaLogging.h>
#import <Kaa/SQLiteLogStorage.h>
#import <Kaa/NSData+Conversion.h>
#import <Kaa/MemBucket.h>
#import <Kaa/SyncTask.h>
#import <Kaa/KaaClientFactory.h>
#import <Kaa/KaaDefaults.h>
#import <Kaa/NSData+CommonCrypto.h>
#import <Kaa/NSMutableArray+Shuffling.h>
#import <Kaa/NSString+Commons.h>
#import <Kaa/PersistentLogStorageConstants.h>
#import <Kaa/SHAMessageDigest.h>
#import <Kaa/SingleThreadExecutorContext.h>
#import <Kaa/UserVerifierConstants.h>
#import <Kaa/UUID.h>
#import <Kaa/KAADummyLog.h>
#import <Kaa/KAADummyProfile.h>
#import <Kaa/KAADummyNotification.h>
#import <Kaa/KAADummyConfiguration.h>
