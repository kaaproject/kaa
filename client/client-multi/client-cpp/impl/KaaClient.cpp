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
#include "kaa/configuration/storage/ConfigurationPersistanceManager.hpp"
#include "kaa/schema/SchemaProcessor.hpp"
#include "kaa/schema/storage/SchemaPersistanceManager.hpp"

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
    schemaProcessor_.reset(new SchemaProcessor);
    configurationProcessor_.reset(new ConfigurationProcessor);
    deltaManager_.reset(new DefaultDeltaManager);
    configurationManager_.reset(new ConfigurationManager);

    bootstrapManager_.reset(new BootstrapManager);
    channelManager_.reset(new KaaChannelManager(*bootstrapManager_));
    registrationManager_.reset(new EndpointRegistrationManager(status_));

    notificationManager_.reset(new NotificationManager(status_));
    profileManager_.reset(new ProfileManager());

    eventManager_.reset(new EventManager(status_));
    eventFamilyFactory_.reset(new EventFamilyFactory(*eventManager_));
    logCollector_.reset(new LogCollector());

    initKaaConfiguration();
    initClientKeys();
    initKaaTransport();
}

void KaaClient::start()
{
    auto configHash = configurationPersistanceManager_->getConfigurationHash().getHash();
    if (!configHash.first || !configHash.second || !schemaProcessor_->getSchema()) {
        SequenceNumber sn = { 0, 0, 1 };
        status_->setAppSeqNumber(sn);
        setDefaultConfiguration();
    }
    bootstrapManager_->receiveOperationsServerList();
}

void KaaClient::stop()
{
}

void KaaClient::initKaaConfiguration()
{
    ConfigurationPersistanceManager *cpm = new ConfigurationPersistanceManager;
    cpm->setConfigurationProcessor(configurationProcessor_.get());
    configurationPersistanceManager_.reset(cpm);

    SchemaPersistanceManager *spm = new SchemaPersistanceManager;
    spm->setSchemaProcessor(schemaProcessor_.get());
    schemaPresistanceManager_.reset(spm);

    schemaProcessor_->subscribeForSchemaUpdates(*configurationProcessor_);
    schemaProcessor_->subscribeForSchemaUpdates(*configurationPersistanceManager_);
    schemaProcessor_->subscribeForSchemaUpdates(*schemaPresistanceManager_);
    configurationProcessor_->addOnProcessedObserver(*configurationManager_);
    configurationProcessor_->subscribeForUpdates(*configurationManager_);
    configurationProcessor_->subscribeForUpdates(*deltaManager_);
    configurationManager_->subscribeForConfigurationChanges(*configurationPersistanceManager_);
}

void KaaClient::initKaaTransport()
{
    IBootstrapTransportPtr bootstrapTransport(new BootstrapTransport(*channelManager_, *bootstrapManager_));
    bootstrapProcessor_.reset(new BootstrapDataProcessor(bootstrapTransport));

    bootstrapManager_->setTransport(boost::dynamic_pointer_cast<BootstrapTransport, IBootstrapTransport>(bootstrapTransport).get());
    bootstrapManager_->setChannelManager(channelManager_.get());

    EndpointObjectHash publicKeyHash(clientKeys_.first.begin(), clientKeys_.first.size());
    IMetaDataTransportPtr metaDataTransport(new MetaDataTransport(status_, publicKeyHash, 60000L));
    IProfileTransportPtr profileTransport(new ProfileTransport(*channelManager_, clientKeys_.first));
    IConfigurationTransportPtr configurationTransport(new ConfigurationTransport(
            *channelManager_
            , configurationProcessor_.get()
            , schemaProcessor_.get()
            , configurationPersistanceManager_.get()
            , status_));
    INotificationTransportPtr notificationTransport(new NotificationTransport(status_, *channelManager_));
    IUserTransportPtr userTransport(new UserTransport(*registrationManager_, *channelManager_));
    IEventTransportPtr eventTransport(new EventTransport(*eventManager_, *channelManager_));
    ILoggingTransportPtr loggingTransport(new LoggingTransport(*channelManager_, *logCollector_));
    IRedirectionTransportPtr redirectionTransport(new RedirectionTransport(*bootstrapManager_));

    profileTransport->setProfileManager(profileManager_.get());
    dynamic_cast<ProfileTransport*>(profileTransport.get())->setClientState(status_);
    profileManager_->setTransport(profileTransport);

    operationsProcessor_.reset(
            new OperationsDataProcessor(
              metaDataTransport
            , profileTransport
            , configurationTransport
            , notificationTransport
            , userTransport
            , eventTransport
            , loggingTransport
            , redirectionTransport
            , status_));

    eventManager_->setTransport(boost::dynamic_pointer_cast<EventTransport, IEventTransport>(eventTransport).get());
    registrationManager_->setTransport(boost::dynamic_pointer_cast<UserTransport, IUserTransport>(userTransport).get());
    logCollector_->setTransport(boost::dynamic_pointer_cast<LoggingTransport, ILoggingTransport>(loggingTransport).get());


    notificationManager_->setTransport(boost::dynamic_pointer_cast<NotificationTransport, INotificationTransport>(notificationTransport));

    bootstrapChannel_.reset(new DefaultBootstrapChannel(channelManager_.get(), clientKeys_));
    opsTcpChannel_.reset(new DefaultOperationTcpChannel(channelManager_.get(), clientKeys_));

    bootstrapChannel_->setDemultiplexer(bootstrapProcessor_.get());
    bootstrapChannel_->setMultiplexer(bootstrapProcessor_.get());
    opsTcpChannel_->setDemultiplexer(operationsProcessor_.get());
    opsTcpChannel_->setMultiplexer(operationsProcessor_.get());

    KAA_LOG_INFO(boost::format("Going to set default bootstrap channel: %1%") % bootstrapChannel_.get());
    channelManager_->addChannel(bootstrapChannel_.get());
    KAA_LOG_INFO(boost::format("Going to set default operations Kaa TCP channel: %1%") % opsTcpChannel_.get());
    channelManager_->addChannel(opsTcpChannel_.get());
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
}

void KaaClient::setDefaultConfiguration()
{
    const std::string& schema = getDefaultConfigSchema();
    if (!schema.empty()) {
        schemaProcessor_->loadSchema(reinterpret_cast<const boost::uint8_t*>(schema.data()), schema.length());
        const Botan::SecureVector<boost::uint8_t>& config = getDefaultConfigData();
        if (!config.empty()) {
            configurationProcessor_->processConfigurationData(config.begin(), config.size(), true);
        }
    }
}

}
