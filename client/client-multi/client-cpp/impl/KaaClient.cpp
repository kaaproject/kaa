/*
 * Copyright 2014 CyberVision, Inc.
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

#include <fstream>

#include "kaa/KaaClient.hpp"

#include "kaa/bootstrap/BootstrapManager.hpp"
#include "kaa/KaaDefaults.hpp"

#include "kaa/configuration/ConfigurationProcessor.hpp"
#include "kaa/configuration/manager/ConfigurationManager.hpp"
#include "kaa/configuration/storage/ConfigurationPersistenceManager.hpp"
#include "kaa/schema/SchemaProcessor.hpp"
#include "kaa/schema/storage/SchemaPersistenceManager.hpp"

#include "kaa/bootstrap/BootstrapTransport.hpp"
#include "kaa/channel/MetaDataTransport.hpp"
#include "kaa/configuration/ConfigurationTransport.hpp"
#include "kaa/notification/NotificationTransport.hpp"
#include "kaa/profile/ProfileTransport.hpp"
#include "kaa/event/EventTransport.hpp"
#include "kaa/event/registration/UserTransport.hpp"
#include "kaa/log/LoggingTransport.hpp"
#include "kaa/channel/RedirectionTransport.hpp"

#include "kaa/channel/KaaChannelManager.hpp"

#include "kaa/channel/connectivity/PingConnectivityChecker.hpp"

#include "kaa/logging/Log.hpp"

namespace kaa {

KaaClient::KaaClient()
    : status_(new ClientStatus(CLIENT_STATUS_FILE_LOCATION))
    , options_(0)
{

}

void KaaClient::init(int options)
{
    options_ = options;
    initClientKeys();

#ifdef KAA_USE_CONFIGURATION
    schemaProcessor_.reset(new SchemaProcessor);
    configurationProcessor_.reset(new ConfigurationProcessor);
    deltaManager_.reset(new DefaultDeltaManager);
    configurationManager_.reset(new ConfigurationManager);
#endif

    bootstrapManager_.reset(new BootstrapManager);
    channelManager_.reset(new KaaChannelManager(*bootstrapManager_, getServerInfoList()));
    registrationManager_.reset(new EndpointRegistrationManager(status_));

#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_.reset(new NotificationManager(status_));
#endif
    profileManager_.reset(new ProfileManager());

    eventManager_.reset(new EventManager(status_));
    eventFamilyFactory_.reset(new EventFamilyFactory(*eventManager_));
#ifdef KAA_USE_LOGGING
    logCollector_.reset(new LogCollector());
#endif

    initKaaConfiguration();
    initKaaTransport();
}

void KaaClient::start()
{
#ifdef KAA_USE_CONFIGURATION
    auto configHash = configurationPersistenceManager_->getConfigurationHash().getHash();
    if (!configHash.first || !configHash.second || !schemaProcessor_->getSchema()) {
        SequenceNumber sn = { 0, 0, 1 };
        status_->setAppSeqNumber(sn);
        setDefaultConfiguration();
    }
#endif
    bootstrapManager_->receiveOperationsServerList();
}

void KaaClient::stop()
{
}

void KaaClient::initKaaConfiguration()
{
#ifdef KAA_USE_CONFIGURATION
    ConfigurationPersistenceManager *cpm = new ConfigurationPersistenceManager;
    cpm->setConfigurationProcessor(configurationProcessor_.get());
    configurationPersistenceManager_.reset(cpm);

    SchemaPersistenceManager *spm = new SchemaPersistenceManager;
    spm->setSchemaProcessor(schemaProcessor_.get());
    schemaPersistenceManager_.reset(spm);

    schemaProcessor_->subscribeForSchemaUpdates(*configurationProcessor_);
    schemaProcessor_->subscribeForSchemaUpdates(*configurationPersistenceManager_);
    schemaProcessor_->subscribeForSchemaUpdates(*schemaPersistenceManager_);
    configurationProcessor_->addOnProcessedObserver(*configurationManager_);
    configurationProcessor_->subscribeForUpdates(*configurationManager_);
    configurationProcessor_->subscribeForUpdates(*deltaManager_);
    configurationManager_->subscribeForConfigurationChanges(*configurationPersistenceManager_);
#endif
}

void KaaClient::initKaaTransport()
{
    IBootstrapTransportPtr bootstrapTransport(new BootstrapTransport(*channelManager_, *bootstrapManager_));
    bootstrapProcessor_.reset(new BootstrapDataProcessor(bootstrapTransport));

    bootstrapManager_->setTransport(std::dynamic_pointer_cast<BootstrapTransport, IBootstrapTransport>(bootstrapTransport).get());
    bootstrapManager_->setChannelManager(channelManager_.get());

    EndpointObjectHash publicKeyHash(clientKeys_.first.begin(), clientKeys_.first.size());
    IMetaDataTransportPtr metaDataTransport(new MetaDataTransport(status_, publicKeyHash, 60000L));
    IProfileTransportPtr profileTransport(new ProfileTransport(*channelManager_, clientKeys_.first));
#ifdef KAA_USE_CONFIGURATION
    IConfigurationTransportPtr configurationTransport(new ConfigurationTransport(
            *channelManager_
            , configurationProcessor_.get()
            , schemaProcessor_.get()
            , configurationPersistenceManager_.get()
            , status_));
#endif
#ifdef KAA_USE_NOTIFICATIONS
    INotificationTransportPtr notificationTransport(new NotificationTransport(status_, *channelManager_));
#endif
    IUserTransportPtr userTransport(new UserTransport(*registrationManager_, *channelManager_));
    IEventTransportPtr eventTransport(new EventTransport(*eventManager_, *channelManager_));
#ifdef KAA_USE_LOGGING
    ILoggingTransportPtr loggingTransport(new LoggingTransport(*channelManager_, *logCollector_));
#endif
    IRedirectionTransportPtr redirectionTransport(new RedirectionTransport(*bootstrapManager_));

    profileTransport->setProfileManager(profileManager_.get());
    dynamic_cast<ProfileTransport*>(profileTransport.get())->setClientState(status_);
    profileManager_->setTransport(profileTransport);

    operationsProcessor_.reset(
            new OperationsDataProcessor(
              metaDataTransport
            , profileTransport
#ifdef KAA_USE_CONFIGURATION
            , configurationTransport
#else
            , nullptr
#endif
#ifdef KAA_USE_NOTIFICATIONS
            , notificationTransport
#else
            , nullptr
#endif
            , userTransport
            , eventTransport
#ifdef KAA_USE_LOGGING
            , loggingTransport
#else
            , nullptr
#endif
            , redirectionTransport
            , status_));

    eventManager_->setTransport(std::dynamic_pointer_cast<EventTransport, IEventTransport>(eventTransport).get());
    registrationManager_->setTransport(std::dynamic_pointer_cast<UserTransport, IUserTransport>(userTransport).get());
#ifdef KAA_USE_LOGGING
    logCollector_->setTransport(std::dynamic_pointer_cast<LoggingTransport, ILoggingTransport>(loggingTransport).get());
#endif
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->setTransport(std::dynamic_pointer_cast<NotificationTransport, INotificationTransport>(notificationTransport));
#endif
#ifdef KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL
    bootstrapChannel_.reset(new DefaultBootstrapChannel(channelManager_.get(), clientKeys_));
    bootstrapChannel_->setDemultiplexer(bootstrapProcessor_.get());
    bootstrapChannel_->setMultiplexer(bootstrapProcessor_.get());
    KAA_LOG_INFO(boost::format("Going to set default bootstrap channel: %1%") % bootstrapChannel_.get());
    channelManager_->addChannel(bootstrapChannel_.get());
#endif
#ifdef KAA_DEFAULT_TCP_CHANNEL
    opsTcpChannel_.reset(new DefaultOperationTcpChannel(channelManager_.get(), clientKeys_));
    opsTcpChannel_->setDemultiplexer(operationsProcessor_.get());
    opsTcpChannel_->setMultiplexer(operationsProcessor_.get());
    KAA_LOG_INFO(boost::format("Going to set default operations Kaa TCP channel: %1%") % opsTcpChannel_.get());
    channelManager_->addChannel(opsTcpChannel_.get());
#endif
#ifdef KAA_DEFAULT_CONNECTIVITY_CHECKER
    ConnectivityCheckerPtr connectivityChecker(new PingConnectivityChecker(
            *static_cast<KaaChannelManager*>(channelManager_.get())));
    channelManager_->setConnectivityChecker(connectivityChecker);
#endif
}

void KaaClient::initClientKeys()
{
    std::ifstream key(CLIENT_PUB_KEY_LOCATION);
    bool exists = key.good();
    key.close();
    if (exists) {
        clientKeys_ = KeyUtils::loadKeyPair(CLIENT_PUB_KEY_LOCATION, CLIENT_PRIV_KEY_LOCATION);
    } else {
        clientKeys_ = KeyUtils().generateKeyPair(2048);
        KeyUtils::saveKeyPair(clientKeys_, CLIENT_PUB_KEY_LOCATION, CLIENT_PRIV_KEY_LOCATION);
    }

    EndpointObjectHash publicKeyHash(clientKeys_.first.begin(), clientKeys_.first.size());
    publicKeyHash_ = Botan::base64_encode(publicKeyHash.getHash().first.get(), publicKeyHash.getHash().second);

    status_->setEndpointKeyHash(publicKeyHash_);
    status_->save();

}

void KaaClient::setDefaultConfiguration()
{
#ifdef KAA_USE_CONFIGURATION
    const std::string& schema = getDefaultConfigSchema();
    if (!schema.empty()) {
        schemaProcessor_->loadSchema(reinterpret_cast<const std::uint8_t*>(schema.data()), schema.length());
        const Botan::SecureVector<std::uint8_t>& config = getDefaultConfigData();
        if (!config.empty()) {
            configurationProcessor_->processConfigurationData(config.begin(), config.size(), true);
        }
    }
#endif
}

}
