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

#include <fstream>

#include "kaa/KaaClient.hpp"

#include "kaa/channel/connectivity/IPConnectivityChecker.hpp"
#include "kaa/bootstrap/BootstrapManager.hpp"
#include "kaa/KaaDefaults.hpp"

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

#include "kaa/failover/DefaultFailoverStrategy.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"

namespace kaa {

KaaClient::KaaClient()
    : status_(new ClientStatus(CLIENT_STATUS_FILE_LOCATION))
    , options_(0)
{

}

void KaaClient::init(int options /*= KAA_DEFAULT_OPTIONS*/)
{
    options_ = options;
    KAA_LOG_INFO(boost::format("Starting Kaa C++ sdk version %1%, commit hash %2%. Options: %3%")
        % BUILD_VERSION % BUILD_COMMIT_HASH % options);

    initClientKeys();

    executorContext_.reset(new SimpleExecutorContext);
    executorContext_->init();

#ifdef KAA_USE_CONFIGURATION
    configurationProcessor_.reset(new ConfigurationProcessor);
    configurationManager_.reset(new ConfigurationManager(*executorContext_));
#endif

    bootstrapManager_.reset(new BootstrapManager);
    channelManager_.reset(new KaaChannelManager(*bootstrapManager_, getBootstrapServers()));
    failoverStrategy_.reset(new DefaultFailoverStrategy);
    channelManager_->setFailoverStrategy(failoverStrategy_);
#ifdef KAA_USE_EVENTS
    registrationManager_.reset(new EndpointRegistrationManager(status_, *executorContext_));
    eventManager_.reset(new EventManager(status_, *executorContext_));
    eventFamilyFactory_.reset(new EventFamilyFactory(*eventManager_, *eventManager_));
#endif

#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_.reset(new NotificationManager(status_, *executorContext_));
#endif
    profileManager_.reset(new ProfileManager());

#ifdef KAA_USE_LOGGING
    logCollector_.reset(new LogCollector(channelManager_.get(), *executorContext_));
#endif

    initKaaConfiguration();
    initKaaTransport();
}

void KaaClient::start()
{
    executorContext_->getLifeCycleExecutor().add([this]
    {
#ifdef KAA_USE_CONFIGURATION
        auto configHash = configurationPersistenceManager_->getConfigurationHash().getHashDigest();
        if (configHash.empty()) {
            SequenceNumber sn = { 0, 0, 1 };
            status_->setAppSeqNumber(sn);
            setDefaultConfiguration();
        }
#endif
        bootstrapManager_->receiveOperationsServerList();
    });
}

void KaaClient::stop()
{
    executorContext_->getLifeCycleExecutor().add([this]
                                                {
                                                    channelManager_->shutdown();
                                                });
    executorContext_->stop();
}

void KaaClient::pause()
{
    executorContext_->getLifeCycleExecutor().add([this]
                                                {
                                                    status_->save();
                                                    channelManager_->pause();
                                                });
}

void KaaClient::resume()
{
    executorContext_->getLifeCycleExecutor().add([this]
                                                {
                                                    channelManager_->resume();
                                                });
}

void KaaClient::initKaaConfiguration()
{
#ifdef KAA_USE_CONFIGURATION
    ConfigurationPersistenceManager *cpm = new ConfigurationPersistenceManager(status_);
    cpm->setConfigurationProcessor(configurationProcessor_.get());
    configurationPersistenceManager_.reset(cpm);

    configurationProcessor_->addOnProcessedObserver(*configurationManager_);
    configurationProcessor_->subscribeForUpdates(*configurationManager_);
    configurationManager_->subscribeForConfigurationChanges(*configurationPersistenceManager_);
#endif
}

void KaaClient::initKaaTransport()
{
    IBootstrapTransportPtr bootstrapTransport(new BootstrapTransport(*channelManager_, *bootstrapManager_));

    bootstrapManager_->setTransport(bootstrapTransport.get());
    bootstrapManager_->setChannelManager(channelManager_.get());

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().begin(), clientKeys_->getPublicKey().size());
    IMetaDataTransportPtr metaDataTransport(new MetaDataTransport(status_, publicKeyHash, 60000L));
    IProfileTransportPtr profileTransport(new ProfileTransport(*channelManager_, clientKeys_->getPublicKey()));
#ifdef KAA_USE_CONFIGURATION
    IConfigurationTransportPtr configurationTransport(new ConfigurationTransport(
            *channelManager_
            , configurationProcessor_.get()
            , configurationPersistenceManager_.get()
            , status_));
#endif
#ifdef KAA_USE_NOTIFICATIONS
    INotificationTransportPtr notificationTransport(new NotificationTransport(status_, *channelManager_));
#endif
#ifdef KAA_USE_EVENTS
    IUserTransportPtr userTransport(new UserTransport(*registrationManager_, *channelManager_));
    IEventTransportPtr eventTransport(new EventTransport(*eventManager_, *channelManager_, status_));
    dynamic_cast<EventTransport*>(eventTransport.get())->setClientState(status_);
#endif
#ifdef KAA_USE_LOGGING
    ILoggingTransportPtr loggingTransport(new LoggingTransport(*channelManager_, *logCollector_));
#endif
    IRedirectionTransportPtr redirectionTransport(new RedirectionTransport(*bootstrapManager_));

    profileTransport->setProfileManager(profileManager_.get());
    dynamic_cast<ProfileTransport*>(profileTransport.get())->setClientState(status_);
    profileManager_->setTransport(profileTransport);

    syncProcessor_.reset(
            new SyncDataProcessor(
              metaDataTransport
            , bootstrapTransport
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
#ifdef KAA_USE_EVENTS
            , userTransport
            , eventTransport
#else
            , nullptr
            , nullptr
#endif
#ifdef KAA_USE_LOGGING
            , loggingTransport
#else
            , nullptr
#endif
            , redirectionTransport
            , status_));

#ifdef KAA_USE_EVENTS
    eventManager_->setTransport(std::dynamic_pointer_cast<EventTransport, IEventTransport>(eventTransport).get());
    registrationManager_->setTransport(std::dynamic_pointer_cast<UserTransport, IUserTransport>(userTransport).get());
#endif
#ifdef KAA_USE_LOGGING
    logCollector_->setTransport(std::dynamic_pointer_cast<LoggingTransport, ILoggingTransport>(loggingTransport).get());
#endif
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->setTransport(std::dynamic_pointer_cast<NotificationTransport, INotificationTransport>(notificationTransport));
#endif
#ifdef KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) {
        bootstrapChannel_.reset(new DefaultBootstrapChannel(channelManager_.get(), *clientKeys_));
        bootstrapChannel_->setDemultiplexer(syncProcessor_.get());
        bootstrapChannel_->setMultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default bootstrap channel: %1%") % bootstrapChannel_.get());
        channelManager_->addChannel(bootstrapChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_OPERATION_HTTP_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_OPERATION_HTTP_CHANNEL) {
        opsHttpChannel_.reset(new DefaultOperationHttpChannel(channelManager_.get(), *clientKeys_));
        opsHttpChannel_->setMultiplexer(syncProcessor_.get());
        opsHttpChannel_->setDemultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default operations Kaa HTTP channel: %1%") % opsHttpChannel_.get());
        channelManager_->addChannel(opsHttpChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_LONG_POLL_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_OPERATION_LONG_POLL_CHANNEL) {
        opsLongPollChannel_.reset(new DefaultOperationLongPollChannel(channelManager_.get(), *clientKeys_));
        opsLongPollChannel_->setMultiplexer(syncProcessor_.get());
        opsLongPollChannel_->setDemultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default operations Kaa HTTP Long Poll channel: %1%") % opsLongPollChannel_.get());
        channelManager_->addChannel(opsLongPollChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_TCP_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_OPERATION_KAATCP_CHANNEL) {
        opsTcpChannel_.reset(new DefaultOperationTcpChannel(channelManager_.get(), *clientKeys_));
        opsTcpChannel_->setDemultiplexer(syncProcessor_.get());
        opsTcpChannel_->setMultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default operations Kaa TCP channel: %1%") % opsTcpChannel_.get());
        channelManager_->addChannel(opsTcpChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_CONNECTIVITY_CHECKER
    if (options_ & KaaOption::USE_DEFAULT_CONNECTIVITY_CHECKER) {
        ConnectivityCheckerPtr connectivityChecker(new IPConnectivityChecker(
                *static_cast<KaaChannelManager*>(channelManager_.get())));
        channelManager_->setConnectivityChecker(connectivityChecker);
    }
#endif
}

void KaaClient::initClientKeys()
{
    std::ifstream key(CLIENT_PUB_KEY_LOCATION);
    bool exists = key.good();
    key.close();
    if (exists) {
        clientKeys_.reset(new KeyPair(KeyUtils::loadKeyPair(CLIENT_PUB_KEY_LOCATION, CLIENT_PRIV_KEY_LOCATION)));
    } else {
        clientKeys_.reset(new KeyPair(KeyUtils().generateKeyPair(2048)));
        KeyUtils::saveKeyPair(*clientKeys_, CLIENT_PUB_KEY_LOCATION, CLIENT_PRIV_KEY_LOCATION);
    }

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().begin(), clientKeys_->getPublicKey().size());
    auto digest = publicKeyHash.getHashDigest();
    publicKeyHash_ = Botan::base64_encode(digest.data(), digest.size());

    status_->setEndpointKeyHash(publicKeyHash_);
    status_->save();

}

void KaaClient::setDefaultConfiguration()
{
#ifdef KAA_USE_CONFIGURATION
    const Botan::SecureVector<std::uint8_t>& config = getDefaultConfigData();
    if (!config.empty()) {
        configurationProcessor_->processConfigurationData(config.begin(), config.size(), true);
    }
#endif
}


void KaaClient::setProfileContainer(IProfileContainerPtr container) {
    profileManager_->setProfileContainer(container);
}

void KaaClient::setConfigurationStorage(IConfigurationStoragePtr storage) {
#ifdef KAA_USE_CONFIGURATION
    configurationPersistenceManager_->setConfigurationStorage(storage);
#else
    throw KaaException("Failed to set configuration storage. Configuration subsystem is disabled");
#endif
}

void KaaClient::addConfigurationListener(IConfigurationReceiver &receiver) {
#ifdef KAA_USE_CONFIGURATION
    configurationManager_->subscribeForConfigurationChanges(receiver);
#else
    throw KaaException("Failed to subscribe to configuration changes. Configuration subsystem is disabled");
#endif
}

void KaaClient::removeConfigurationListener(IConfigurationReceiver &receiver) {
#ifdef KAA_USE_CONFIGURATION
    configurationManager_->unsubscribeFromConfigurationChanges(receiver);
#else
    throw KaaException("Failed to unsubscribe from configuration changes. Configuration subsystem is disabled");
#endif
}

const KaaRootConfiguration& KaaClient::getConfiguration() {
#ifdef KAA_USE_CONFIGURATION
    return configurationManager_->getConfiguration();
#else
    throw KaaException("Failed to subscribe to get configuration. Configuration subsystem is disabled");
#endif
}
void KaaClient::addTopicListListener(INotificationTopicListListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->addTopicListListener(listener);
#else
    throw KaaException("Failed to add topic list listeners. Notification subsystem is disabled");
#endif
}

void KaaClient::removeTopicListListener(INotificationTopicListListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->removeTopicListListener(listener);
#else
    throw KaaException("Failed to remove topic list listeners. Notification subsystem is disabled");
#endif
}

Topics KaaClient::getTopics() {
#ifdef KAA_USE_NOTIFICATIONS
    return notificationManager_->getTopics();
#else
    throw KaaException("Failed to get topics. Notification subsystem is disabled");
#endif
}
void KaaClient::addNotificationListener(INotificationListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->addNotificationListener(listener);
#else
    throw KaaException("Failed to add notification listener. Notification subsystem is disabled");
#endif
}
void KaaClient::addNotificationListener(const std::string& topidId, INotificationListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->addNotificationListener(topidId, listener);
#else
    throw KaaException("Failed to add notification listener. Notification subsystem is disabled");
#endif
}

void KaaClient::removeNotificationListener(INotificationListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->removeNotificationListener(listener);
#else
    throw KaaException("Failed to remove notification listener. Notification subsystem is disabled");
#endif
}

void KaaClient::removeNotificationListener(const std::string& topidId, INotificationListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->removeNotificationListener(topidId, listener);
#else
    throw KaaException("Failed to remove notification listener. Notification subsystem is disabled");
#endif
}

void KaaClient::subscribeToTopic(const std::string& id, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->subscribeToTopic(id, forceSync);
#else
    throw KaaException("Failed to subscribe to topics. Notification subsystem is disabled");
#endif
}

void KaaClient::subscribeToTopics(const std::list<std::string>& idList, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->subscribeToTopics(idList, forceSync);
#else
    throw KaaException("Failed to subscribe to topics. Notification subsystem is disabled");
#endif
}
void KaaClient::unsubscribeFromTopic(const std::string& id, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->unsubscribeFromTopic(id, forceSync);
#else
    throw KaaException("Failed to unsubscribe to topics. Notification subsystem is disabled");
#endif
}

void KaaClient::unsubscribeFromTopics(const std::list<std::string>& idList, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->unsubscribeFromTopics(idList, forceSync);
#else
    throw KaaException("Failed to unsubscribe to topics. Notification subsystem is disabled");
#endif
}
void KaaClient::syncTopicSubscriptions() {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->sync();
#else
    throw KaaException("Failed to get synchronized . Notification subsystem is disabled");
#endif
}

void KaaClient::attachEndpoint(const std::string&  endpointAccessToken
                              , IAttachEndpointCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    return registrationManager_->attachEndpoint(endpointAccessToken, listener);
#else
    throw KaaException("Failed to attach endpoint. Event subsystem is disabled");
#endif
}

void KaaClient::detachEndpoint(const std::string&  endpointKeyHash
                              , IDetachEndpointCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    return registrationManager_->detachEndpoint(endpointKeyHash, listener);
#else
    throw KaaException("Failed to detach endpoint. Event subsystem is disabled");
#endif
}

void KaaClient::attachUser(const std::string& userExternalId, const std::string& userAccessToken
                          , IUserAttachCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    return registrationManager_->attachUser(userExternalId, userAccessToken, listener);
#else
    throw KaaException("Failed to attach user. Event subsystem is disabled");
#endif
}

void KaaClient::attachUser(const std::string& userExternalId, const std::string& userAccessToken
                          , const std::string& userVerifierToken, IUserAttachCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    return registrationManager_->attachUser(userExternalId, userAccessToken, userVerifierToken, listener);
#else
    throw KaaException("Failed to attach user. Event subsystem is disabled");
#endif
}

void KaaClient::setAttachStatusListener(IAttachStatusListenerPtr listener) {
#ifdef KAA_USE_EVENTS
    return registrationManager_->setAttachStatusListener(listener);
#else
    throw KaaException("Failed to set listerner's status. Event subsystem is disabled");
#endif
}

bool KaaClient::isAttachedToUser() {
#ifdef KAA_USE_EVENTS
        return registrationManager_->isAttachedToUser();
#else
        throw KaaException("Failed to set listerner's status. Event subsystem is disabled");
#endif
}

EventFamilyFactory& KaaClient::getEventFamilyFactory()
{
#ifdef KAA_USE_EVENTS
    return *eventFamilyFactory_;
#else
    throw KaaException("Failed to retrieve EventFamilyFactory. Event subsystem is disabled");
#endif
}

std::int32_t KaaClient::findEventListeners(const std::list<std::string>& eventFQNs, IFetchEventListenersPtr listener) {
#ifdef KAA_USE_EVENTS
    return eventManager_->findEventListeners(eventFQNs, listener);
#else
    throw KaaException("Failed to find event listeners. Event subsystem is disabled");
#endif
}
IKaaChannelManager& KaaClient::getChannelManager()
{
    return *channelManager_;
}

const KeyPair& KaaClient::getClientKeyPair()
{
    return *clientKeys_;
}

void KaaClient::setEndpointAccessToken(const std::string& token)
{
    status_->setEndpointAccessToken(token);
}

std::string KaaClient::refreshEndpointAccessToken()
{
    return status_->refreshEndpointAccessToken();
}

std::string KaaClient::getEndpointAccessToken()
{
    return status_->getEndpointAccessToken();
}

void KaaClient::addLogRecord(const KaaUserLogRecord& record) {
#ifdef KAA_USE_LOGGING
    return logCollector_->addLogRecord(record);
#else
    throw KaaException("Failed to add log record. Logging subsystem is disabled");
#endif
}
void KaaClient::setLogStorage(ILogStoragePtr storage) {
#ifdef KAA_USE_LOGGING
    return logCollector_->setStorage(storage);
#else
    throw KaaException("Failed to set storage. Logging subsystem is disabled");
#endif
}

void KaaClient::setLogUploadStrategy(ILogUploadStrategyPtr strategy) {
#ifdef KAA_USE_LOGGING
        return logCollector_->setUploadStrategy(strategy);
#else
        throw KaaException("Failed to set strategy. Logging subsystem is disabled");
#endif
}

void KaaClient::setFailoverStrategy(IFailoverStrategyPtr strategy) {
    if (!strategy) {
        KAA_LOG_ERROR("Failed to set failover strategy: bad data");
        throw KaaException("Bad failover strategy");
    }

    KAA_LOG_INFO("New failover strategy was set");
    failoverStrategy_ = strategy;
    channelManager_->setFailoverStrategy(failoverStrategy_);
}

IKaaDataMultiplexer& KaaClient::getOperationMultiplexer()
{
    return *syncProcessor_;
}

IKaaDataDemultiplexer& KaaClient::getOperationDemultiplexer()
{
    return *syncProcessor_;
}

IKaaDataMultiplexer& KaaClient::getBootstrapMultiplexer()
{
    return *syncProcessor_;
}

IKaaDataDemultiplexer& KaaClient::getBootstrapDemultiplexer()
{
    return *syncProcessor_;
}

void KaaClient::updateProfile()
{
   profileManager_->updateProfile();
}
}


