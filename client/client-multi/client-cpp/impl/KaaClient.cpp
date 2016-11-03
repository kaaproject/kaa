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

#include <fstream>

#include "kaa/KaaClient.hpp"

#include "kaa/channel/connectivity/PingConnectivityChecker.hpp"
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
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

namespace kaa {

KaaClient::KaaClient(IKaaClientPlatformContextPtr platformContext, KaaClientStateListenerPtr listener)
    : logger_(new DefaultLogger(platformContext->getProperties().getClientId(), platformContext->getProperties().getLogFileName())),
      context_(platformContext->getProperties(), *logger_, platformContext->getExecutorContext(), nullptr,
               (listener == nullptr) ? std::make_shared<KaaClientStateListener>() : listener),
      status_(new ClientStatus(context_)),
      platformContext_(platformContext)
{
    init();
}

void KaaClient::init()
{
    KAA_LOG_INFO(boost::format("Creating Kaa C++ SDK instance: version %1%, commit hash %2%")
                                                            % BUILD_VERSION % BUILD_COMMIT_HASH);

    initClientKeys();
    context_.setStatus(status_);
    bootstrapManager_.reset(new BootstrapManager(context_, this));
    channelManager_.reset(new KaaChannelManager(*bootstrapManager_, getBootstrapServers(), context_, this));
    failoverStrategy_.reset(new DefaultFailoverStrategy(context_));
    channelManager_->setFailoverStrategy(failoverStrategy_);
    profileManager_.reset(new ProfileManager(context_));

#ifdef KAA_USE_CONFIGURATION
    configurationManager_.reset(new ConfigurationManager(context_));
#endif
#ifdef KAA_USE_EVENTS
    registrationManager_.reset(new EndpointRegistrationManager(context_));
    eventManager_.reset(new EventManager(context_));
    eventFamilyFactory_.reset(new EventFamilyFactory(*eventManager_, *eventManager_, context_));
#endif
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_.reset(new NotificationManager(context_));
#endif
#ifdef KAA_USE_LOGGING
    logCollector_.reset(new LogCollector(channelManager_.get(), context_));
#endif

    initKaaTransport();
}

void KaaClient::checkReadiness()
{
    if (!profileManager_ || !profileManager_->isInitialized()) {
        KAA_LOG_ERROR("Profile manager isn't initialized: maybe profile container isn't set");
        throw KaaException("Profile manager isn't initialized: maybe profile container isn't set");
    }
}

void KaaClient::start()
{
    checkClientStateNot(State::STARTED, "Kaa client is already started");
    checkClientStateNot(State::PAUSED, "Kaa client is paused, need to be resumed");

    checkReadiness();

    KAA_LOG_TRACE("Kaa client starting");

    /*
     * NOTE: Initialization of an executor context should be the first.
     */
    context_.getExecutorContext().init();

    context_.getExecutorContext().getLifeCycleExecutor().add([this]
        {
            try {
#ifdef KAA_USE_CONFIGURATION
                configurationManager_->init();
#endif
                bootstrapManager_->receiveOperationsServerList();
                context_.getClientStateListener().onStarted();

                KAA_LOG_INFO("Kaa client started");
            } catch (std::exception& e) {
                KAA_LOG_ERROR(boost::format("Caught exception on start: %s") % e.what());
                context_.getClientStateListener().onStartFailure(KaaException(e));
            }
        });

    setClientState(State::STARTED);
}

void KaaClient::stop()
{
    checkClientStateNot(State::CREATED, "Kaa client is not started");
    checkClientStateNot(State::STOPPED, "Kaa client is already stopped");

    KAA_LOG_TRACE("Kaa client stopping...");

    /*
     * To prevent a race condition between stopping a client when it is already destroyed,
     * pass a reference to this client to a 'stop' task.
     */
    auto thisRef = shared_from_this();
    context_.getExecutorContext().getLifeCycleExecutor().add([this, thisRef]
        {
            try {
                channelManager_->shutdown();
                status_->save();
                context_.getClientStateListener().onStopped();

                KAA_LOG_INFO("Kaa client stopped");
            } catch (std::exception& e) {
                KAA_LOG_ERROR(boost::format("Caught exception on stop: %s") % e.what());
                context_.getClientStateListener().onStopFailure(KaaException(e));
            }
        });

    context_.getExecutorContext().stop();
    setClientState(State::STOPPED);
}

void KaaClient::pause()
{
    checkClientState(State::STARTED, "Kaa client is not started");

    KAA_LOG_TRACE("Kaa client pausing");

    context_.getExecutorContext().getLifeCycleExecutor().add([this]
        {
            try {
                status_->save();
                channelManager_->pause();
                context_.getClientStateListener().onPaused();

                KAA_LOG_INFO("Kaa client paused");
            } catch (std::exception& e) {
                KAA_LOG_ERROR(boost::format("Caught exception on pause: %s") % e.what());
                context_.getClientStateListener().onPauseFailure(KaaException(e));
            }
        });

    setClientState(State::PAUSED);
}

void KaaClient::resume()
{
    checkClientState(State::PAUSED, "Kaa client isn't paused");

    KAA_LOG_TRACE("Kaa client resuming");

    context_.getExecutorContext().getLifeCycleExecutor().add([this]
        {
            try {
                channelManager_->resume();
                context_.getClientStateListener().onResumed();

                KAA_LOG_INFO("Kaa client resumed");
            } catch (std::exception& e) {
                KAA_LOG_ERROR(boost::format("Caught exception on resume: %s") % e.what());
                context_.getClientStateListener().onResumeFailure(KaaException(e));
            }
        });

    setClientState(State::STARTED);
}

void KaaClient::initKaaTransport()
{
    IBootstrapTransportPtr bootstrapTransport(new BootstrapTransport(*channelManager_, *bootstrapManager_, context_));

    bootstrapManager_->setTransport(bootstrapTransport.get());
    bootstrapManager_->setChannelManager(channelManager_.get());

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().data(), clientKeys_->getPublicKey().size());

    auto metaDataTransport = std::make_shared<MetaDataTransport>(status_, publicKeyHash, 60000L);
    auto profileTransport = std::make_shared<ProfileTransport>(*channelManager_, clientKeys_->getPublicKey(), context_);

#ifdef KAA_USE_CONFIGURATION
    auto configurationTransport = std::make_shared<ConfigurationTransport>(*channelManager_, context_);
    configurationTransport->setConfigurationProcessor(&configurationManager_->getConfigurationProcessor());
    configurationTransport->setConfigurationHashContainer(&configurationManager_->getConfigurationHashContainer());
#endif
#ifdef KAA_USE_NOTIFICATIONS
    auto notificationTransport = std::make_shared<NotificationTransport>(*channelManager_, context_);
#endif
#ifdef KAA_USE_EVENTS
    auto userTransport = std::make_shared<UserTransport>(*registrationManager_, *channelManager_, context_);
    auto eventTransport = std::make_shared<EventTransport>(*eventManager_, *channelManager_, context_);
#endif
#ifdef KAA_USE_LOGGING
    auto loggingTransport = std::make_shared<LoggingTransport>(*channelManager_, *logCollector_, context_);
#endif
    auto redirectionTransport = std::make_shared<RedirectionTransport>(*bootstrapManager_);

    profileTransport->setProfileManager(profileManager_.get());
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
            , context_));

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
    bootstrapChannel_.reset(new DefaultBootstrapChannel(*channelManager_, *clientKeys_, context_));
    bootstrapChannel_->setDemultiplexer(syncProcessor_.get());
    bootstrapChannel_->setMultiplexer(syncProcessor_.get());
    KAA_LOG_INFO(boost::format("Going to set default bootstrap channel: %1%") % bootstrapChannel_.get());
    channelManager_->addChannel(bootstrapChannel_.get());
#endif
#ifdef KAA_DEFAULT_TCP_CHANNEL
    opsTcpChannel_.reset(new DefaultOperationTcpChannel(*channelManager_, *clientKeys_, context_));
    opsTcpChannel_->setDemultiplexer(syncProcessor_.get());
    opsTcpChannel_->setMultiplexer(syncProcessor_.get());
    KAA_LOG_INFO(boost::format("Going to set default operations Kaa TCP channel: %1%") % opsTcpChannel_.get());
    channelManager_->addChannel(opsTcpChannel_.get());
#endif
#ifdef KAA_DEFAULT_CONNECTIVITY_CHECKER
    channelManager_->setConnectivityChecker(std::make_shared<PingConnectivityChecker>());
#endif
}

void KaaClient::initClientKeys()
{
    std::string publicKeyLocation = context_.getProperties().getPublicKeyFileName();
    std::string privateKeyLocation = context_.getProperties().getPrivateKeyFileName();
    KeyUtils utils;
    bool regenerate = true;

    std::ifstream key(publicKeyLocation);
    bool exists = key.good();
    key.close();

    if (exists) {
        KeyPair keys(utils.loadKeyPair(publicKeyLocation, privateKeyLocation));
        if (utils.checkKeyPair(keys)) {
            clientKeys_.reset(new KeyPair(keys));
            regenerate = false; // Keys are valid, no need to create them again
        }
    }

    if (regenerate) {
#ifdef KAA_RUNTIME_KEY_GENERATION
        clientKeys_.reset(new KeyPair(utils.generateKeyPair(2048)));
        utils.saveKeyPair(*clientKeys_, publicKeyLocation, privateKeyLocation);
#else
        KAA_LOG_ERROR("KAA_RUNTIME_KEY_GENERATION is disabled. Generate keys and put them to the working directory.");
        throw KaaException("Keys are missing.");
#endif
    }

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().data(), clientKeys_->getPublicKey().size());
    auto digest = publicKeyHash.getHashDigest();
    std::string endpointKeyHash = Botan::base64_encode(digest.data(), digest.size());

    status_->setEndpointKeyHash(endpointKeyHash);
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
    checkClientState(State::STARTED, "Kaa client isn't started");
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
void KaaClient::addNotificationListener(std::int64_t topicId, INotificationListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->addNotificationListener(topicId, listener);
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

void KaaClient::removeNotificationListener(std::int64_t topicId, INotificationListener& listener) {
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->removeNotificationListener(topicId, listener);
#else
    throw KaaException("Failed to remove notification listener. Notification subsystem is disabled");
#endif
}

void KaaClient::subscribeToTopic(std::int64_t id, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    checkClientState(State::STARTED, "Kaa client isn't started");
    notificationManager_->subscribeToTopic(id, forceSync);
#else
    throw KaaException("Failed to subscribe to topics. Notification subsystem is disabled");
#endif
}

void KaaClient::subscribeToTopics(const std::list<std::int64_t>& idList, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    checkClientState(State::STARTED, "Kaa client isn't started");
    notificationManager_->subscribeToTopics(idList, forceSync);
#else
    throw KaaException("Failed to subscribe to topics. Notification subsystem is disabled");
#endif
}
void KaaClient::unsubscribeFromTopic(std::int64_t id, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    checkClientState(State::STARTED, "Kaa client isn't started");
    notificationManager_->unsubscribeFromTopic(id, forceSync);
#else
    throw KaaException("Failed to unsubscribe to topics. Notification subsystem is disabled");
#endif
}

void KaaClient::unsubscribeFromTopics(const std::list<std::int64_t>& idList, bool forceSync) {
#ifdef KAA_USE_NOTIFICATIONS
    checkClientState(State::STARTED, "Kaa client isn't started");
    notificationManager_->unsubscribeFromTopics(idList, forceSync);
#else
    throw KaaException("Failed to unsubscribe to topics. Notification subsystem is disabled");
#endif
}
void KaaClient::syncTopicSubscriptions() {
#ifdef KAA_USE_NOTIFICATIONS
    checkClientState(State::STARTED, "Kaa client isn't started");
    notificationManager_->sync();
#else
    throw KaaException("Failed to get synchronized . Notification subsystem is disabled");
#endif
}

void KaaClient::attachEndpoint(const std::string&  endpointAccessToken
                              , IAttachEndpointCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    checkClientState(State::STARTED, "Kaa client isn't started");
    return registrationManager_->attachEndpoint(endpointAccessToken, listener);
#else
    throw KaaException("Failed to attach endpoint. Event subsystem is disabled");
#endif
}

void KaaClient::detachEndpoint(const std::string&  endpointKeyHash
                              , IDetachEndpointCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    checkClientState(State::STARTED, "Kaa client isn't started");
    return registrationManager_->detachEndpoint(endpointKeyHash, listener);
#else
    throw KaaException("Failed to detach endpoint. Event subsystem is disabled");
#endif
}

void KaaClient::attachUser(const std::string& userExternalId, const std::string& userAccessToken
                          , IUserAttachCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    checkClientState(State::STARTED, "Kaa client isn't started");
    return registrationManager_->attachUser(userExternalId, userAccessToken, listener);
#else
    throw KaaException("Failed to attach user. Event subsystem is disabled");
#endif
}

void KaaClient::attachUser(const std::string& userExternalId, const std::string& userAccessToken
                          , const std::string& userVerifierToken, IUserAttachCallbackPtr listener) {
#ifdef KAA_USE_EVENTS
    checkClientState(State::STARTED, "Kaa client isn't started");
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
    //TODO: on which stage do we need to check client's state, here or in a specific event factory?
    return *eventFamilyFactory_;
#else
    throw KaaException("Failed to retrieve EventFamilyFactory. Event subsystem is disabled");
#endif
}

std::int32_t KaaClient::findEventListeners(const std::list<std::string>& eventFQNs, IFetchEventListenersPtr listener)
{
#ifdef KAA_USE_EVENTS
    checkClientState(State::STARTED, "Kaa client isn't started");
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
    profileManager_->updateProfile();
}

std::string KaaClient::refreshEndpointAccessToken()
{
    return status_->refreshEndpointAccessToken();
}

std::string KaaClient::getEndpointAccessToken() const
{
    return status_->getEndpointAccessToken();
}

std::string KaaClient::getEndpointKeyHash() const
{
    return status_->getEndpointKeyHash();
}

RecordFuture KaaClient::addLogRecord(const KaaUserLogRecord& record)
{
#ifdef KAA_USE_LOGGING
    checkClientState(State::STARTED, "Kaa client isn't started");
    return logCollector_->addLogRecord(record);
#else
    throw KaaException("Failed to add log record. Logging subsystem is disabled");
#endif
}

void KaaClient::setLogDeliveryListener(ILogDeliveryListenerPtr listener)
{
#ifdef KAA_USE_LOGGING
    return logCollector_->setLogDeliveryListener(listener);
#else
    throw KaaException("Failed to add log record. Logging subsystem is disabled");
#endif
}

void KaaClient::setLogStorage(ILogStoragePtr storage)
{
#ifdef KAA_USE_LOGGING
    return logCollector_->setStorage(storage);
#else
    throw KaaException("Failed to set storage. Logging subsystem is disabled");
#endif
}

void KaaClient::setLogUploadStrategy(ILogUploadStrategyPtr strategy)
{
#ifdef KAA_USE_LOGGING
        return logCollector_->setUploadStrategy(strategy);
#else
        throw KaaException("Failed to set strategy. Logging subsystem is disabled");
#endif
}

void KaaClient::setFailoverStrategy(IFailoverStrategyPtr strategy)
{
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
    checkClientState(State::STARTED, "Kaa client isn't started");
    profileManager_->updateProfile();
}

void KaaClient::setClientState(State state)
{
    clientState_ = state;
}

void KaaClient::checkClientState(State expected, const std::string& message)
{
    if (clientState_ != expected) {
        throw KaaException(message);
    }
}

void KaaClient::checkClientStateNot(State unexpected, const std::string& message)
{
    if (clientState_ == unexpected) {
        throw KaaException(message);
    }
}

IKaaClientContext& KaaClient::getKaaClientContext()
{
    return context_;
}

}
