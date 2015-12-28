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
#include "kaa/DummyKaaClientStateListener.hpp"
#include "kaa/KaaClientProperties.hpp"

namespace kaa {

KaaClient::KaaClient(IKaaClientPlatformContextPtr context, IKaaClientStateListenerPtr listener)
    : platformContext_(context)
    , executorContext_(context->getExecutorContext())
    , clientProperties_(context->getProperties())
    , stateListener_(listener)
{
    if (!stateListener_) {
        stateListener_ = std::make_shared<DummyKaaClientStateListener>();
    }

    init();
}

void KaaClient::init()
{
    KAA_LOG_INFO(boost::format("Creating Kaa C++ SDK instance: version %1%, commit hash %2%")
                                                            % BUILD_VERSION % BUILD_COMMIT_HASH);

    status_.reset(new ClientStatus(clientProperties_.getStateFileName()));

    initClientKeys();

    bootstrapManager_.reset(new BootstrapManager);
    channelManager_.reset(new KaaChannelManager(*bootstrapManager_, getBootstrapServers()));
    failoverStrategy_.reset(new DefaultFailoverStrategy);
    channelManager_->setFailoverStrategy(failoverStrategy_);
    profileManager_.reset(new ProfileManager());

#ifdef KAA_USE_CONFIGURATION
    SequenceNumber sn = { 0, 0, 1 };
    status_->setAppSeqNumber(sn);
    configurationManager_.reset(new ConfigurationManager(executorContext_, status_));
#endif
#ifdef KAA_USE_EVENTS
    registrationManager_.reset(new EndpointRegistrationManager(status_, executorContext_));
    eventManager_.reset(new EventManager(status_, executorContext_));
    eventFamilyFactory_.reset(new EventFamilyFactory(*eventManager_, *eventManager_, executorContext_));
#endif
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_.reset(new NotificationManager(status_, executorContext_));
#endif
#ifdef KAA_USE_LOGGING
    logCollector_.reset(new LogCollector(channelManager_.get(), executorContext_, clientProperties_));
#endif

    initKaaTransport();
}

void KaaClient::start()
{
    /*
     * NOTE: Initialization of an executor context should be the first.
     */
    executorContext_.init();

    executorContext_.getLifeCycleExecutor().add([this]
        {
            try {
#ifdef KAA_USE_CONFIGURATION
                configurationManager_->init();
#endif
                bootstrapManager_->receiveOperationsServerList();
                stateListener_->onStarted();
            } catch (std::exception& e) {
                stateListener_->onStartFailure(KaaException(e));
            }
        });
}

void KaaClient::stop()
{
    /*
     * To prevent a race condition between stopping a client when it is already destroyed,
     * pass a reference to this client to a 'stop' task.
     */
    auto thisRef = shared_from_this();
    executorContext_.getLifeCycleExecutor().add([this, thisRef]
        {
            try {
                channelManager_->shutdown();
                status_->save();
                stateListener_->onStopped();
            } catch (std::exception& e) {
                stateListener_->onStopFailure(KaaException(e));
            }
        });

    executorContext_.stop();
}

void KaaClient::pause()
{
    executorContext_.getLifeCycleExecutor().add([this]
        {
            try {
                status_->save();
                channelManager_->pause();
                stateListener_->onPaused();
            } catch (std::exception& e) {
                stateListener_->onPauseFailure(KaaException(e));
            }
        });
}

void KaaClient::resume()
{
    executorContext_.getLifeCycleExecutor().add([this]
        {
            try {
                channelManager_->resume();
                stateListener_->onResumed();
            } catch (std::exception& e) {
                stateListener_->onResumeFailure(KaaException(e));
            }
        });
}

void KaaClient::initKaaTransport()
{
    IBootstrapTransportPtr bootstrapTransport(new BootstrapTransport(*channelManager_, *bootstrapManager_));

    bootstrapManager_->setTransport(bootstrapTransport.get());
    bootstrapManager_->setChannelManager(channelManager_.get());

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().begin(), clientKeys_->getPublicKey().size());

    auto metaDataTransport = std::make_shared<MetaDataTransport>(status_, publicKeyHash, 60000L);
    profileTransport_ = std::make_shared<ProfileTransport>(*channelManager_, clientKeys_->getPublicKey());
#ifdef KAA_USE_CONFIGURATION
    auto configurationTransport = std::make_shared<ConfigurationTransport>(*channelManager_, status_);
    configurationTransport->setConfigurationProcessor(&configurationManager_->getConfigurationProcessor());
    configurationTransport->setConfigurationHashContainer(&configurationManager_->getConfigurationHashContainer());
#endif
#ifdef KAA_USE_NOTIFICATIONS
    auto notificationTransport = std::make_shared<NotificationTransport>(status_, *channelManager_);
#endif
#ifdef KAA_USE_EVENTS
    auto userTransport = std::make_shared<UserTransport>(*registrationManager_, *channelManager_);
    auto eventTransport = std::make_shared<EventTransport>(*eventManager_, *channelManager_, status_);
    eventTransport->setClientState(status_);
#endif
#ifdef KAA_USE_LOGGING
    auto loggingTransport = std::make_shared<LoggingTransport>(*channelManager_, *logCollector_);
#endif
    auto redirectionTransport = std::make_shared<RedirectionTransport>(*bootstrapManager_);

    profileTransport_->setProfileManager(profileManager_.get());
    dynamic_cast<ProfileTransport*>(profileTransport_.get())->setClientState(status_);
    profileManager_->setTransport(profileTransport_);

    syncProcessor_.reset(
            new SyncDataProcessor(
              metaDataTransport
            , bootstrapTransport
            , profileTransport_
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
    bootstrapChannel_.reset(new DefaultBootstrapChannel(channelManager_.get(), *clientKeys_, status_));
    bootstrapChannel_->setDemultiplexer(syncProcessor_.get());
    bootstrapChannel_->setMultiplexer(syncProcessor_.get());
    KAA_LOG_INFO(boost::format("Going to set default bootstrap channel: %1%") % bootstrapChannel_.get());
    channelManager_->addChannel(bootstrapChannel_.get());
#endif
#ifdef KAA_DEFAULT_TCP_CHANNEL
    opsTcpChannel_.reset(new DefaultOperationTcpChannel(channelManager_.get(), *clientKeys_, status_));
    opsTcpChannel_->setDemultiplexer(syncProcessor_.get());
    opsTcpChannel_->setMultiplexer(syncProcessor_.get());
    KAA_LOG_INFO(boost::format("Going to set default operations Kaa TCP channel: %1%") % opsTcpChannel_.get());
    channelManager_->addChannel(opsTcpChannel_.get());
#endif
#ifdef KAA_DEFAULT_CONNECTIVITY_CHECKER
    ConnectivityCheckerPtr connectivityChecker(new IPConnectivityChecker(
            *static_cast<KaaChannelManager*>(channelManager_.get())));
    channelManager_->setConnectivityChecker(connectivityChecker);
#endif
}

void KaaClient::initClientKeys()
{
    std::string publicKeyLocation = clientProperties_.getPublicKeyFileName();
    std::string privateKeyLocation = clientProperties_.getPrivateKeyFileName();

    std::ifstream key(publicKeyLocation);
    bool exists = key.good();
    key.close();
    if (exists) {
        clientKeys_.reset(new KeyPair(KeyUtils::loadKeyPair(publicKeyLocation, privateKeyLocation)));
    } else {
        clientKeys_.reset(new KeyPair(KeyUtils().generateKeyPair(2048)));
        KeyUtils::saveKeyPair(*clientKeys_, publicKeyLocation, privateKeyLocation);
    }

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().begin(), clientKeys_->getPublicKey().size());
    auto digest = publicKeyHash.getHashDigest();
    publicKeyHash_ = Botan::base64_encode(digest.data(), digest.size());

    status_->setEndpointKeyHash(publicKeyHash_);
    status_->save();

}

void KaaClient::setProfileContainer(IProfileContainerPtr container) {
    profileManager_->setProfileContainer(container);
}

void KaaClient::setConfigurationStorage(IConfigurationStoragePtr storage) {
#ifdef KAA_USE_CONFIGURATION
    configurationManager_->setConfigurationStorage(storage);
#else
    throw KaaException("Failed to set configuration storage. Configuration subsystem is disabled");
#endif
}

void KaaClient::addConfigurationListener(IConfigurationReceiver &receiver) {
#ifdef KAA_USE_CONFIGURATION
    configurationManager_->addReceiver(receiver);
#else
    throw KaaException("Failed to subscribe to configuration changes. Configuration subsystem is disabled");
#endif
}

void KaaClient::removeConfigurationListener(IConfigurationReceiver &receiver) {
#ifdef KAA_USE_CONFIGURATION
    configurationManager_->removeReceiver(receiver);
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
    profileTransport_->sync();
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


