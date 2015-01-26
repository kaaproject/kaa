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

#ifndef KAACLIENT_HPP_
#define KAACLIENT_HPP_

#include "kaa/IKaaClient.hpp"

#include "kaa/ClientStatus.hpp"
#include "kaa/event/EventManager.hpp"
#include "kaa/schema/ISchemaProcessor.hpp"
#include "kaa/profile/IProfileManager.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/event/gen/EventFamilyFactory.hpp"
#include "kaa/profile/ProfileManager.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/notification/NotificationManager.hpp"
#include "kaa/schema/storage/ISchemaPersistenceManager.hpp"
#include "kaa/configuration/manager/IConfigurationManager.hpp"
#include "kaa/event/registration/EndpointRegistrationManager.hpp"
#include "kaa/configuration/delta/manager/DefaultDeltaManager.hpp"
#include "kaa/ClientStatus.hpp"
#include "kaa/configuration/storage/IConfigurationPersistenceManager.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/channel/BootstrapDataProcessor.hpp"
#include "kaa/channel/OperationsDataProcessor.hpp"
#include "kaa/channel/impl/DefaultBootstrapChannel.hpp"
#include "kaa/channel/impl/DefaultOperationTcpChannel.hpp"
#include "kaa/channel/impl/DefaultOperationHttpChannel.hpp"
#include "kaa/channel/impl/DefaultOperationLongPollChannel.hpp"
#include "kaa/log/LogCollector.hpp"

namespace kaa {

typedef std::shared_ptr<IBootstrapManager> IBootstrapManagerPtr;

#ifdef KAA_USE_CONFIGURATION
typedef std::shared_ptr<DefaultDeltaManager> DefaultDeltaManagerPtr;
#endif

typedef enum KaaOption {
    USE_DEFAULT_BOOTSTRAP_HTTP_CHANNEL      = 0x01,
    USE_DEFAULT_OPERATION_KAATCP_CHANNEL    = 0x02,
    USE_DEFAULT_OPERATION_HTTP_CHANNEL      = 0x04,
    USE_DEFAULT_OPERATION_LONG_POLL_CHANNEL = 0x08,
    USE_DEFAULT_CONNECTIVITY_CHECKER        = 0x10
} KaaOption;

class KaaClient : public IKaaClient {
public:
    KaaClient();
    virtual ~KaaClient() { }

    void init(int options = KAA_DEFAULT_OPTIONS);
    void start();
    void stop();
    void pause();
    void resume();

    virtual IProfileManager&                    getProfileManager() { return *profileManager_; }
#ifdef KAA_USE_CONFIGURATION
    virtual ISchemaPersistenceManager&          getSchemaPersistenceManager() { return *schemaPersistenceManager_; }
    virtual IConfigurationPersistenceManager&   getConfigurationPersistenceManager() { return *configurationPersistenceManager_; }
    virtual IDeltaManager&                      getDeltaManager() { return *deltaManager_; }
    virtual IConfigurationManager&              getConfigurationManager() { return *configurationManager_; }
#endif
#ifdef KAA_USE_NOTIFICATIONS
    virtual INotificationManager&               getNotificationManager() { return *notificationManager_; }
#endif
#ifdef KAA_USE_EVENTS
    virtual IEndpointRegistrationManager&       getEndpointRegistrationManager() { return *registrationManager_; }
    virtual EventFamilyFactory&                 getEventFamilyFactory() { return *eventFamilyFactory_; }
    virtual IEventListenersResolver&            getEventListenersResolver() { return *eventManager_; }
#endif
    virtual IKaaChannelManager&                 getChannelManager()  { return *channelManager_; }
    virtual const KeyPair&                      getClientKeyPair() { return *clientKeys_; }
#ifdef KAA_USE_LOGGING
    virtual ILogCollector&                      getLogCollector() { return *logCollector_; }
#endif
    virtual IKaaDataMultiplexer&                getOperationMultiplexer() { return *operationsProcessor_; }
    virtual IKaaDataDemultiplexer&              getOperationDemultiplexer() { return *operationsProcessor_; }
    virtual IKaaDataMultiplexer&                getBootstrapMultiplexer() { return *bootstrapProcessor_; }
    virtual IKaaDataDemultiplexer&              getBootstrapDemultiplexer() { return *bootstrapProcessor_; }
private:
    void initKaaConfiguration();
    void initKaaTransport();
    void initClientKeys();

    void setDefaultConfiguration();

public:
    static const int KAA_DEFAULT_OPTIONS = KaaOption::USE_DEFAULT_BOOTSTRAP_HTTP_CHANNEL   |
                                           KaaOption::USE_DEFAULT_OPERATION_KAATCP_CHANNEL |
                                           KaaOption::USE_DEFAULT_CONNECTIVITY_CHECKER;
private:
    IKaaClientStateStoragePtr                       status_;
    IBootstrapManagerPtr                            bootstrapManager_;
    std::unique_ptr<ProfileManager>                 profileManager_;
#ifdef KAA_USE_NOTIFICATIONS
    std::unique_ptr<NotificationManager>            notificationManager_;
#endif

    std::unique_ptr<KeyPair> clientKeys_;
    std::string     publicKeyHash_;

#ifdef KAA_USE_CONFIGURATION
    ISchemaProcessorPtr                   schemaProcessor_;
    DefaultDeltaManagerPtr                deltaManager_;
    IConfigurationManagerPtr              configurationManager_;
    IConfigurationProcessorPtr            configurationProcessor_;
    ISchemaPersistenceManagerPtr          schemaPersistenceManager_;
    IConfigurationPersistenceManagerPtr   configurationPersistenceManager_;
#endif
#ifdef KAA_USE_EVENTS
    std::unique_ptr<EventManager>         eventManager_;
    std::unique_ptr<EventFamilyFactory>   eventFamilyFactory_;
    std::unique_ptr<EndpointRegistrationManager>    registrationManager_;
#endif
    std::unique_ptr<IKaaChannelManager>   channelManager_;
    std::unique_ptr<BootstrapDataProcessor>   bootstrapProcessor_;
    std::unique_ptr<OperationsDataProcessor>  operationsProcessor_;

#ifdef KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL
    std::unique_ptr<DefaultBootstrapChannel>          bootstrapChannel_;
#endif
#ifdef KAA_DEFAULT_TCP_CHANNEL
    std::unique_ptr<DefaultOperationTcpChannel>       opsTcpChannel_;
#endif
#ifdef KAA_DEFAULT_OPERATION_HTTP_CHANNEL
    std::unique_ptr<DefaultOperationHttpChannel>      opsHttpChannel_;
#endif
#ifdef KAA_DEFAULT_LONG_POLL_CHANNEL
    std::unique_ptr<DefaultOperationLongPollChannel>  opsLongPollChannel_;
#endif
#ifdef KAA_USE_LOGGING
    std::unique_ptr<LogCollector>      logCollector_;
#endif

    int options_;
};

}



#endif /* KAACLIENT_HPP_ */
