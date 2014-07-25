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

#ifndef UPDATEMANAGERMOCK_HPP_
#define UPDATEMANAGERMOCK_HPP_

#include <kaa/update/IUpdateManager.hpp>
#include "headers/update/RequestFactoryMock.hpp"

namespace kaa {

class ISyncRequestUpdater;

class UpdateManagerMock : public IUpdateManager {
public:
    UpdateManagerMock() { }
    ~UpdateManagerMock() { }

    virtual void start() { }
    virtual void stop() { }
    virtual void failover(boost::uint32_t milliseconds) { }

    virtual void resync() { }

    virtual void addSyncRequestUpdater(ISyncRequestUpdater* listener) { }
    virtual void removeSyncRequestUpdater(ISyncRequestUpdater* listener) { }

    virtual void addUpdateListener(IUpdateListenerPtr listener) { }
    virtual void removeUpdateListener(IUpdateListenerPtr listener) { }

    virtual void onProfileChanged(SharedDataBuffer serializedProfile) { }
    virtual void onEndpointAccessTokenChanged() { }

    virtual void setTransport(IExternalTransportManagerPtr transport) { }
    virtual void setPublicKey(const std::vector<uint8_t>& key) { }
    virtual void setSerializedProfileContainer(ISerializedProfileContainerPtr profileContainer) { }
    virtual void setConfigurtionHashContainer(IConfigurationHashContainerPtr hashContainer) { }
    virtual void setTransportExceptionHandler(ITransportExceptionHandlerPtr handler) { }

    virtual void updateSubscriptionCommands(const SubscriptionCommands& subsciptions) {}
    virtual const SubscriptionCommands& getSubscriptionCommands() {
        static SubscriptionCommands container;
        return container;
    }

    virtual void getAcceptedNotificationIds(AcceptedNotificationIds& container) const { }

    virtual void getTopicStates(TopicStates& container) const { }

    virtual void onSyncResponse(const SyncResponse& response) { }

    virtual KaaState getCurrentState() const { return KaaState(); }

    virtual IRequestFactoryPtr getRequestFactory() const { return IRequestFactoryPtr(new RequestFactoryMock()); }
};

}


#endif /* UPDATEMANAGERMOCK_HPP_ */
