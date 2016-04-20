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

#ifndef ENDPOINTREGISTRATIONMANAGER_HPP_
#define ENDPOINTREGISTRATIONMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#include <atomic>
#include <string>
#include <memory>
#include <unordered_map>

#include "kaa/KaaThread.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/event/registration/UserTransport.hpp"
#include "kaa/event/registration/IRegistrationProcessor.hpp"
#include "kaa/event/registration/IEndpointRegistrationManager.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

struct UserAttachRequest;
struct EndpointAttachResponse;
struct EndpointDetachResponse;
struct UserAttachNotification;
struct UserDetachNotification;

class IExecutorContext;

class EndpointRegistrationManager : public IEndpointRegistrationManager
                                  , public IRegistrationProcessor
{
public:
    EndpointRegistrationManager(IKaaClientContext &context);

    virtual void attachEndpoint(const std::string&  endpointAccessToken
                              , IAttachEndpointCallbackPtr listener = IAttachEndpointCallbackPtr());

    virtual void detachEndpoint(const std::string&  endpointKeyHash
                              , IDetachEndpointCallbackPtr listener = IDetachEndpointCallbackPtr());

    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr());

    virtual void attachUser(const std::string& userExternalId
                          , const std::string& userAccessToken
                          , const std::string& userVerifierToken
                          , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr());

    virtual bool isAttachedToUser() { return context_.getStatus().getEndpointAttachStatus(); }

    virtual void setAttachStatusListener(IAttachStatusListenerPtr listener) { attachStatusListener_ = listener; }

    virtual UserAttachRequestPtr getUserAttachRequest();

    virtual std::unordered_map<std::int32_t, std::string> getEndpointsToAttach();
    virtual std::unordered_map<std::int32_t, std::string> getEndpointsToDetach();

    virtual void onUserAttach(const UserAttachResponse& response);

    virtual void onEndpointsAttach(const std::vector<EndpointAttachResponse>& endpoints);
    virtual void onEndpointsDetach(const std::vector<EndpointDetachResponse>& endpoints);

    virtual void onCurrentEndpointAttach(const UserAttachNotification& response);
    virtual void onCurrentEndpointDetach(const UserDetachNotification& response);

    void setTransport(UserTransport * transport) { userTransport_ = transport; }

private:
    void doSync();

private:
#ifdef KAA_THREADSAFE
    typedef std::atomic_int_fast32_t RequestId;
#else
    typedef std::int32_t RequestId;
#endif

private:
    IKaaClientContext         &context_;
    UserTransport*            userTransport_;

    std::shared_ptr<UserAttachRequest> userAttachRequest_;
    KAA_MUTEX_DECLARE(userAttachRequestGuard_);

    IUserAttachCallbackPtr      userAttachResponseListener_;
    IAttachStatusListenerPtr    attachStatusListener_;

    RequestId attachRequestId_;
    RequestId detachRequestId_;

    KAA_MUTEX_DECLARE(attachEndpointGuard_);
    KAA_MUTEX_DECLARE(detachEndpointGuard_);

    std::unordered_map<std::int32_t, std::string/* endpoint access token */> attachEndpointRequests_;
    std::unordered_map<std::int32_t, std::string/* endpoint key hash */>     detachEndpointRequests_;

    std::unordered_map<std::int32_t, IAttachEndpointCallbackPtr> attachEndpointListeners_;
    std::unordered_map<std::int32_t, IDetachEndpointCallbackPtr> detachEndpointListeners_;
};

}

#endif /* ENDPOINTREGISTRATIONMANAGER_HPP_ */
