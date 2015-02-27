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

#ifndef ENDPOINTREGISTRATIONMANAGER_HPP_
#define ENDPOINTREGISTRATIONMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_EVENTS

#include <map>
#include <list>
#include <string>
#include <memory>

#include "kaa/IKaaClientStateStorage.hpp"

#include "kaa/event/registration/IRegistrationProcessor.hpp"
#include "kaa/event/registration/IEndpointRegistrationManager.hpp"
#include "kaa/event/registration/UserTransport.hpp"
#include "kaa/common/UuidGenerator.hpp"
#include "kaa/observer/KaaObservable.hpp"

namespace kaa {

class ClientStatus;
class SyncResponse;
class UserAttachRequest;
class IAttachedEndpointListListener;

class EndpointRegistrationManager : public IEndpointRegistrationManager
                                  , public IRegistrationProcessor
{
public:
    EndpointRegistrationManager(IKaaClientStateStoragePtr status);

    virtual void regenerateEndpointAccessToken();

    virtual const std::string& getEndpointAccessToken() {
        return endpointAccessToken_;
    }

    virtual void attachEndpoint(const std::string&  endpointAccessToken
                              , IEndpointAttachStatusListener* listener = nullptr);

    virtual void detachEndpoint(const std::string&  endpointKeyHash
                              , IEndpointAttachStatusListener* listener = nullptr);

    virtual void detachEndpoint(IEndpointAttachStatusListener* listener = nullptr);

    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , IEndpointAttachStatusListener* listener = nullptr);

    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , const std::string& userVerifierToken
                          , IEndpointAttachStatusListener* listener = nullptr);

    virtual const AttachedEndpoints& getAttachedEndpoints();

    virtual void addAttachedEndpointListListener(IAttachedEndpointListListener *listener);

    virtual void removeAttachedEndpointListListener(IAttachedEndpointListListener *listener);

    virtual std::map<std::int32_t, std::string>  getEndpointsToAttach();
    virtual std::map<std::int32_t, std::string>  getEndpointsToDetach();
    virtual UserAttachRequestPtr                getUserAttachRequest();

    virtual void onUserAttach(const UserSyncResponse::userAttachResponse_t& response);

    virtual void onEndpointsAttach(const std::vector<EndpointAttachResponse>& endpoints);
    virtual void onEndpointsDetach(const std::vector<EndpointDetachResponse>& endpoints);

    virtual void onCurrentEndpointAttach(const UserAttachNotification& response);
    virtual void onCurrentEndpointDetach(const UserDetachNotification& response);

    virtual bool isCurrentEndpointAttached() {
        return status_->getEndpointAttachStatus();
    }

    virtual void setTransport(UserTransport * transport) {
        userTransport_ = transport;
        if (userTransport_ != nullptr && (userAttachRequest_.get() != nullptr || !attachingEndpoints_.empty() || !detachingEndpoints_.empty())) {
            userTransport_->sync();
        }
    }

    virtual void setAttachStatusListener(IEndpointAttachStatusListener* listener) {
        if (listener != nullptr) {
            attachStatusListener_ = listener;
        }
    }

private:
    void onEndpointAccessTokenChanged(const std::string& old);

private:
    struct EndpointOperationInfo {
        std::string endpointData_;/* endpoint's token or hash */
        IEndpointAttachStatusListener* listener_;
    };


private:
    IKaaClientStateStoragePtr     status_;

    std::string endpointAccessToken_;
    std::string endpointKeyHash_;

    UserAttachRequestPtr userAttachRequest_;

    std::map<std::int32_t/*requestId*/, EndpointOperationInfo>  attachingEndpoints_;
    std::map<std::int32_t/*requestId*/, EndpointOperationInfo>  detachingEndpoints_;
    std::map<std::string/*epToken*/, std::string/*epHash*/>    attachedEndpoints_;
    KAA_R_MUTEX_DECLARE(endpointsGuard_);

    UserTransport *                                            userTransport_;

    KaaObservable<void (const AttachedEndpoints&), IAttachedEndpointListListener *> attachedEPListListeners_;
    KAA_R_MUTEX_DECLARE(listenerGuard_);

    IEndpointAttachStatusListener*                             attachStatusListener_;
};

}

#endif

#endif /* ENDPOINTREGISTRATIONMANAGER_HPP_ */
