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

#ifndef DEFAULTNOTIFICATIONTRANSPORT_HPP_
#define DEFAULTNOTIFICATIONTRANSPORT_HPP_

#include <map>
#include <set>
#include <string>

#include "kaa/channel/transport/IKaaTransport.hpp"
#include "kaa/channel/transport/INotificationTransport.hpp"
#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/notification/INotificationProcessor.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class IKaaChannelManager;

class NotificationTransport: public AbstractKaaTransport<TransportType::NOTIFICATION>,
                             public INotificationTransport
{
public:
    NotificationTransport(IKaaChannelManager& manager, IKaaClientContext &context);

    virtual NotificationSyncRequestPtr createEmptyNotificationRequest();

    virtual NotificationSyncRequestPtr createNotificationRequest();

    virtual void onNotificationResponse(const NotificationSyncResponse& response);

    virtual void onSubscriptionChanged(SubscriptionCommands&& commands);

    virtual void setNotificationProcessor(INotificationProcessor* processor) {
        if (processor) {
            notificationProcessor_ = processor;
        }
    }

    virtual void sync() {
        syncByType(type_);
    }

private:
    Notifications getUnicastNotifications(const Notifications & notifications);
    Notifications getMulticastNotifications(const Notifications & notifications);
    std::vector<TopicState> prepareTopicStatesForRequest();

private:
    INotificationProcessor*                         notificationProcessor_;

    std::set<std::string>                  acceptedUnicastNotificationIds_;
    SubscriptionCommands                                    subscriptions_;
};

} /* namespace kaa */

#endif /* DEFAULTNOTIFICATIONTRANSPORT_HPP_ */
