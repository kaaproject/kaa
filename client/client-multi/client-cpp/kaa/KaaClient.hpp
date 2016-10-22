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

#ifndef KAACLIENT_HPP_
#define KAACLIENT_HPP_

#include <memory>

#include "kaa/IKaaClient.hpp"
#include "kaa/ClientStatus.hpp"
#include "kaa/event/EventManager.hpp"
#include "kaa/profile/IProfileManager.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/event/gen/EventFamilyFactory.hpp"
#include "kaa/profile/ProfileManager.hpp"
#include "kaa/channel/SyncDataProcessor.hpp"
#include "kaa/notification/NotificationManager.hpp"
#include "kaa/event/registration/EndpointRegistrationManager.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/channel/impl/DefaultBootstrapChannel.hpp"
#include "kaa/channel/impl/DefaultOperationTcpChannel.hpp"
#include "kaa/channel/impl/DefaultOperationHttpChannel.hpp"
#include "kaa/channel/impl/DefaultOperationLongPollChannel.hpp"
#include "kaa/configuration/manager/ConfigurationManager.hpp"
#include "kaa/log/LogCollector.hpp"
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/KaaClientStateListener.hpp"
#include "kaa/IKaaClientPlatformContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/KaaClientContext.hpp"

namespace kaa {

class KaaClient : public IKaaClient,
                  public std::enable_shared_from_this<KaaClient> {
public:
    KaaClient(IKaaClientPlatformContextPtr platformContext, KaaClientStateListenerPtr listener);

    virtual void start();
    virtual void stop();
    virtual void pause();
    virtual void resume();

    virtual void                                updateProfile();
    virtual IKaaChannelManager&                 getChannelManager();
    virtual const KeyPair&                      getClientKeyPair();
    virtual void                                setEndpointAccessToken(const std::string& token);
    virtual std::string                         refreshEndpointAccessToken();
    virtual std::string                         getEndpointAccessToken() const;
    virtual std::string                         getEndpointKeyHash() const;
    virtual IKaaDataMultiplexer&                getOperationMultiplexer();
    virtual IKaaDataDemultiplexer&              getOperationDemultiplexer();
    virtual EventFamilyFactory&                 getEventFamilyFactory();

    virtual RecordFuture                        addLogRecord(const KaaUserLogRecord& record);
    virtual void                                setLogDeliveryListener(ILogDeliveryListenerPtr listener);
    virtual void                                setLogStorage(ILogStoragePtr storage);
    virtual void                                setLogUploadStrategy(ILogUploadStrategyPtr strategy);
    virtual void                                setFailoverStrategy(IFailoverStrategyPtr strategy);
    virtual void                                setProfileContainer(IProfileContainerPtr container);
    virtual void                                addTopicListListener(INotificationTopicListListener& listener);
    virtual void                                removeTopicListListener(INotificationTopicListListener& listener);
    virtual Topics                              getTopics();
    virtual void                                addNotificationListener(INotificationListener& listener);
    virtual void                                addNotificationListener(std::int64_t topicId, INotificationListener& listener);
    virtual void                                removeNotificationListener(INotificationListener& listener);
    virtual void                                removeNotificationListener(std::int64_t topicId, INotificationListener& listener);
    virtual void                                subscribeToTopic(std::int64_t id, bool forceSync);
    virtual void                                subscribeToTopics(const std::list<std::int64_t>& idList, bool forceSync);
    virtual void                                unsubscribeFromTopic(std::int64_t id, bool forceSync);
    virtual void                                unsubscribeFromTopics(const std::list<std::int64_t>& idList, bool forceSync);
    virtual void                                syncTopicSubscriptions();
    virtual void                                addConfigurationListener(IConfigurationReceiver &receiver);
    virtual void                                removeConfigurationListener(IConfigurationReceiver &receiver);
    virtual const KaaRootConfiguration&         getConfiguration();
    virtual void                                setConfigurationStorage(IConfigurationStoragePtr storage);
    virtual void                                attachEndpoint(const std::string&  endpointAccessToken
                                                , IAttachEndpointCallbackPtr listener = IAttachEndpointCallbackPtr());
    virtual void                                detachEndpoint(const std::string&  endpointKeyHash
                                                , IDetachEndpointCallbackPtr listener = IDetachEndpointCallbackPtr());
    virtual void                                attachUser(const std::string& userExternalId, const std::string& userAccessToken
                                                          , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr());
    virtual void                                attachUser(const std::string& userExternalId, const std::string& userAccessToken
                                                          , const std::string& userVerifierToken
                                                          , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr());
    virtual void                                setAttachStatusListener(IAttachStatusListenerPtr listener);
    virtual bool                                isAttachedToUser();
    virtual std::int32_t                        findEventListeners(const std::list<std::string>& eventFQNs
                                                                  , IFetchEventListenersPtr listener);

    virtual IKaaDataMultiplexer&                getBootstrapMultiplexer();
    virtual IKaaDataDemultiplexer&              getBootstrapDemultiplexer();
    virtual IKaaClientContext&                  getKaaClientContext();

private:
    void init();

    void initKaaTransport();
    void initClientKeys();

    void checkReadiness();

private:

    enum class State {
        CREATED,
        STARTED,
        PAUSED,
        STOPPED
    };

    void setClientState(State state);
    void checkClientState(State expected, const std::string& message);
    void checkClientStateNot(State unexpected, const std::string& message);

    State                                            clientState_ = State::CREATED;
    LoggerPtr                                        logger_;
    KaaClientContext                                 context_;
    IKaaClientStateStoragePtr                        status_;

#ifdef KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL
    std::unique_ptr<DefaultBootstrapChannel>         bootstrapChannel_;
#endif
#ifdef KAA_DEFAULT_TCP_CHANNEL
    std::unique_ptr<DefaultOperationTcpChannel>      opsTcpChannel_;
#endif
#ifdef KAA_DEFAULT_OPERATION_HTTP_CHANNEL
    std::unique_ptr<DefaultOperationHttpChannel>     opsHttpChannel_;
#endif
#ifdef KAA_DEFAULT_LONG_POLL_CHANNEL
    std::unique_ptr<DefaultOperationLongPollChannel> opsLongPollChannel_;
#endif

    std::unique_ptr<IBootstrapManager>               bootstrapManager_;
    std::unique_ptr<IKaaChannelManager>              channelManager_;
    std::unique_ptr<SyncDataProcessor>               syncProcessor_;
    IFailoverStrategyPtr                             failoverStrategy_;

    std::unique_ptr<KeyPair>                         clientKeys_;
    std::unique_ptr<ProfileManager>                  profileManager_;
#ifdef KAA_USE_NOTIFICATIONS
    std::unique_ptr<NotificationManager>             notificationManager_;
#endif
#ifdef KAA_USE_CONFIGURATION
    std::unique_ptr<ConfigurationManager>            configurationManager_;
#endif
#ifdef KAA_USE_EVENTS
    std::unique_ptr<EventManager>                    eventManager_;
    std::unique_ptr<EventFamilyFactory>              eventFamilyFactory_;
    std::unique_ptr<EndpointRegistrationManager>     registrationManager_;
#endif
#ifdef KAA_USE_LOGGING
    std::unique_ptr<LogCollector>                    logCollector_;
#endif

    IKaaClientPlatformContextPtr                     platformContext_;
};

}



#endif /* KAACLIENT_HPP_ */
