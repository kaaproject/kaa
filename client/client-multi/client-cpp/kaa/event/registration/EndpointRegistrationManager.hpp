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

#include <atomic>
#include <string>
#include <memory>
#include <unordered_map>

#include "kaa/KaaThread.hpp"

#include "kaa/IKaaClientStateStorage.hpp"

#include "kaa/event/registration/IRegistrationProcessor.hpp"
#include "kaa/event/registration/IEndpointRegistrationManager.hpp"
#include "kaa/event/registration/UserTransport.hpp"
#include "kaa/common/UuidGenerator.hpp"
#include "kaa/observer/KaaObservable.hpp"

namespace kaa {

class ClientStatus;
struct SyncResponse;
struct UserAttachRequest;
class IAttachedEndpointListListener;

class EndpointRegistrationManager : public IEndpointRegistrationManager
                                  , public IRegistrationProcessor
{
public:
    EndpointRegistrationManager(IKaaClientStateStoragePtr status);

    virtual void attachEndpoint(const std::string&  endpointAccessToken
                              , IAttachEndpointCallback* listener = nullptr);

    virtual void detachEndpoint(const std::string&  endpointKeyHash
                              , IDetachEndpointCallback* listener = nullptr);

    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , IUserAttachCallback* listener = nullptr);

    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , const std::string& userVerifierToken
                          , IUserAttachCallback* listener = nullptr);

    virtual bool isAttachedToUser() { return status_->getEndpointAttachStatus(); }

    virtual void setAttachStatusListener(IAttachStatusListener* listener) { attachStatusListener_ = listener; }

    virtual UserAttachRequestPtr getUserAttachRequest() { return userAttachRequest_; }

    virtual std::unordered_map<std::int32_t, std::string> getEndpointsToAttach();
    virtual std::unordered_map<std::int32_t, std::string> getEndpointsToDetach();

    virtual void onUserAttach(const UserAttachResponse& response);

    virtual void onEndpointsAttach(const std::vector<EndpointAttachResponse>& endpoints);
    virtual void onEndpointsDetach(const std::vector<EndpointDetachResponse>& endpoints);

    virtual void onCurrentEndpointAttach(const UserAttachNotification& response);
    virtual void onCurrentEndpointDetach(const UserDetachNotification& response);

    void setTransport(UserTransport * transport);

private:
    void onEndpointAccessTokenChanged(const std::string& old);

    void doSync();

private:
#ifdef KAA_THREADSAFE
    typedef std::atomic_int_fast32_t RequestId;
#else
    typedef std::int32_t RequestId;
#endif

private:
    UserTransport*            userTransport_;
    IKaaClientStateStoragePtr status_;

    UserAttachRequestPtr      userAttachRequest_;
    IUserAttachCallback*      userAttachResponseListener_;
    IAttachStatusListener*    attachStatusListener_;

    RequestId attachRequestId_;
    RequestId detachRequestId_;

    KAA_MUTEX_DECLARE(attachEndpointGuard_);
    KAA_MUTEX_DECLARE(detachEndpointGuard_);

    std::unordered_map<std::int32_t, std::string/* endpoint access token */> attachEndpointRequests_;
    std::unordered_map<std::int32_t, std::string/* endpoint key hash */>     detachEndpointRequests_;

    std::unordered_map<std::int32_t, IAttachEndpointCallback*> attachEndpointListeners;
    std::unordered_map<std::int32_t, IDetachEndpointCallback*> detachEndpointListeners;

};

}

#endif

#endif /* ENDPOINTREGISTRATIONMANAGER_HPP_ */
