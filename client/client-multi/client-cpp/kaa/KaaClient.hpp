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
#include "kaa/profile/IProfileManager.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/event/gen/EventFamilyFactory.hpp"
#include "kaa/profile/ProfileManager.hpp"
#include "kaa/channel/SyncDataProcessor.hpp"
#include "kaa/notification/NotificationManager.hpp"
#include "kaa/event/registration/EndpointRegistrationManager.hpp"
#include "kaa/ClientStatus.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/channel/impl/DefaultBootstrapChannel.hpp"
#include "kaa/channel/impl/DefaultOperationTcpChannel.hpp"
#include "kaa/channel/impl/DefaultOperationHttpChannel.hpp"
#include "kaa/channel/impl/DefaultOperationLongPollChannel.hpp"
#include "kaa/configuration/ConfigurationProcessor.hpp"
#include "kaa/configuration/manager/ConfigurationManager.hpp"
#include "kaa/configuration/storage/ConfigurationPersistenceManager.hpp"
#include "kaa/log/LogCollector.hpp"

namespace kaa {

typedef std::shared_ptr<IBootstrapManager> IBootstrapManagerPtr;

typedef enum KaaOption
{
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

    virtual IProfileManager&                    getProfileManager();
    virtual IConfigurationPersistenceManager&   getConfigurationPersistenceManager();
    virtual IConfigurationManager&              getConfigurationManager();
    virtual INotificationManager&               getNotificationManager();
    virtual IEndpointRegistrationManager&       getEndpointRegistrationManager();
    virtual EventFamilyFactory&                 getEventFamilyFactory();
    virtual IEventListenersResolver&            getEventListenersResolver();
    virtual IKaaChannelManager&                 getChannelManager();
    virtual const KeyPair&                      getClientKeyPair();
    virtual ILogCollector&                      getLogCollector();
    virtual IKaaDataMultiplexer&                getOperationMultiplexer();
    virtual IKaaDataDemultiplexer&              getOperationDemultiplexer();

    virtual IKaaDataMultiplexer&                getBootstrapMultiplexer();
    virtual IKaaDataDemultiplexer&              getBootstrapDemultiplexer();
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
    std::unique_ptr<NotificationManager>            notificationManager_;

    std::unique_ptr<KeyPair> clientKeys_;
    std::string              publicKeyHash_;

    std::unique_ptr<ConfigurationManager>            configurationManager_;
    std::unique_ptr<ConfigurationProcessor>          configurationProcessor_;
    std::unique_ptr<ConfigurationPersistenceManager> configurationPersistenceManager_;
    std::unique_ptr<EventManager>                    eventManager_;
    std::unique_ptr<EventFamilyFactory>              eventFamilyFactory_;
    std::unique_ptr<EndpointRegistrationManager>     registrationManager_;
    std::unique_ptr<LogCollector>                    logCollector_;
    std::unique_ptr<IKaaChannelManager>              channelManager_;
    std::unique_ptr<SyncDataProcessor>               syncProcessor_;

    std::unique_ptr<DefaultBootstrapChannel>          bootstrapChannel_;
    std::unique_ptr<DefaultOperationTcpChannel>       opsTcpChannel_;
    std::unique_ptr<DefaultOperationHttpChannel>      opsHttpChannel_;
    std::unique_ptr<DefaultOperationLongPollChannel>  opsLongPollChannel_;

    int options_;
};

}



#endif /* KAACLIENT_HPP_ */
