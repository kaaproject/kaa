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

#include "kaa/event/registration/EndpointRegistrationManager.hpp"

#ifdef KAA_USE_EVENTS

#include <cstdlib>

#include "kaa/logging/Log.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/registration/IUserAttachCallback.hpp"
#include "kaa/event/registration/IAttachStatusListener.hpp"
#include "kaa/event/registration/IAttachEndpointCallback.hpp"
#include "kaa/event/registration/IDetachEndpointCallback.hpp"
#include "kaa/common/exception/BadCredentials.hpp"
#include "kaa/common/exception/TransportNotFoundException.hpp"
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/utils/IThreadPool.hpp"

namespace kaa {

EndpointRegistrationManager::EndpointRegistrationManager(IKaaClientContext &context)
    : context_(context), userTransport_(nullptr), attachRequestId_(0), detachRequestId_(0)
{
}

void EndpointRegistrationManager::onUserAttach(const UserAttachResponse& response)
{
    KAA_MUTEX_LOCKING("userAttachRequestGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(userAttachRequestLock, userAttachRequestGuard_);
    KAA_MUTEX_LOCKED("userAttachRequestGuard_");

    if (!userAttachRequest_) {
        KAA_LOG_ERROR(boost::format("Got UserAttachResponse without leading UserAttachRequest. "
                                    "Probably duplicated message from server."));
        return;
    }

    std::string userExternalId = userAttachRequest_->userExternalId;
    userAttachRequest_.reset();

    KAA_MUTEX_UNLOCKING("userAttachRequestGuard_");
    KAA_UNLOCK(userAttachRequestLock);
    KAA_MUTEX_UNLOCKED("userAttachRequestGuard_");

    if (response.result == SyncResponseResultType::SUCCESS) {
        context_.getStatus().setEndpointAttachStatus(true);

        KAA_LOG_INFO(boost::format("Endpoint was successfully attached to '%1%' user") % userExternalId);

        if (userAttachResponseListener_) {
            context_.getExecutorContext().getCallbackExecutor().add([this] {userAttachResponseListener_->onAttachSuccess(); } );
        }
    } else {
        KAA_LOG_ERROR(boost::format("Failed to attach endpoint to '%1%' user") % userExternalId);

        if (userAttachResponseListener_) {
            context_.getExecutorContext().getCallbackExecutor().add([this, response]
            {
                userAttachResponseListener_->onAttachFailed(
                        response.errorCode.is_null() ? UserAttachErrorCode::OTHER : response.errorCode.get_UserAttachErrorCode(),
                        response.errorReason.is_null() ? "" : response.errorReason.get_string());
            } );
        }
    }
}

void EndpointRegistrationManager::onEndpointsAttach(const std::vector<EndpointAttachResponse>& attachResponses)
{
    KAA_MUTEX_LOCKING("attachEndpointGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(attachEndpointLock, attachEndpointGuard_);
    KAA_MUTEX_LOCKED("attachEndpointGuard_");

    for (const auto& attachResponse : attachResponses) {
        auto requestIt = attachEndpointRequests_.find(attachResponse.requestId);
        if (requestIt != attachEndpointRequests_.end()) {
            bool isAttachSuccess = (attachResponse.result == SyncResponseResultType::SUCCESS);

            if (isAttachSuccess) {
                KAA_LOG_INFO(boost::format("Endpoint '%1%' (request id: %2%) was successfully attached")
                                                                % requestIt->second % requestIt->first);
            } else {
                KAA_LOG_ERROR(boost::format("Failed to attach endpoint '%1%' (request id: %2%)")
                                                            % requestIt->second % requestIt->first);
            }

            attachEndpointRequests_.erase(requestIt);

            auto listenerIt = attachEndpointListeners_.find(attachResponse.requestId);
            if (listenerIt != attachEndpointListeners_.end() && listenerIt->second) {
                auto callback = listenerIt->second;
                attachEndpointListeners_.erase(listenerIt);

                if (isAttachSuccess) {
                    std::string endpointKeyHash = attachResponse.endpointKeyHash.is_null() ?
                                                    "" : attachResponse.endpointKeyHash.get_string();
                    context_.getExecutorContext().getCallbackExecutor().add([callback, endpointKeyHash]
                                                                {
                                                                    callback->onAttachSuccess(endpointKeyHash);
                                                                });
                } else {
                    context_.getExecutorContext().getCallbackExecutor().add([callback] { callback->onAttachFailed(); });
                }
            }
        }
    }
}

void EndpointRegistrationManager::onEndpointsDetach(const std::vector<EndpointDetachResponse>& detachResponses)
{
    for (const auto& detachResponse : detachResponses) {
        KAA_MUTEX_LOCKING("detachEndpointGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(detachEndpointLock, detachEndpointGuard_);
        KAA_MUTEX_LOCKED("detachEndpointGuard_");

        auto requestIt = detachEndpointRequests_.find(detachResponse.requestId);
        if (requestIt != detachEndpointRequests_.end()) {
            bool isDetachSuccess = (detachResponse.result == SyncResponseResultType::SUCCESS);

            if (isDetachSuccess) {
                KAA_LOG_INFO(boost::format("Endpoint '%1%' (request id: %2%) was successfully detached")
                                                                    % requestIt->second % requestIt->first);
            } else {
                KAA_LOG_ERROR(boost::format("Failed to detach endpoint '%1%' (request id: %2%)")
                                                            % requestIt->second % requestIt->first);
            }

            detachEndpointRequests_.erase(requestIt);

            auto listenerIt = detachEndpointListeners_.find(detachResponse.requestId);
            if (listenerIt != detachEndpointListeners_.end() && listenerIt->second) {
                auto callback = listenerIt->second;
                detachEndpointListeners_.erase(listenerIt);

                KAA_MUTEX_UNLOCKING("detachEndpointGuard_");
                KAA_UNLOCK(detachEndpointLock);
                KAA_MUTEX_UNLOCKED("detachEndpointGuard_");

                if (isDetachSuccess) {
                    context_.getExecutorContext().getCallbackExecutor().add([callback] { callback->onDetachSuccess(); });
                } else {
                    context_.getExecutorContext().getCallbackExecutor().add([callback] { callback->onDetachFailed(); });
                }
            }
        }
    }
}

void EndpointRegistrationManager::onCurrentEndpointAttach(const UserAttachNotification& response)
{
    context_.getStatus().setEndpointAttachStatus(true);

    KAA_LOG_INFO(boost::format("Current endpoint was attached to '%1%' by '%2%'")
                                    % response.userExternalId % response.endpointAccessToken);

    if (attachStatusListener_) {
        context_.getExecutorContext().getCallbackExecutor().add([this, response]
                                                    {
                                                        attachStatusListener_->onAttach(response.userExternalId,
                                                                                        response.endpointAccessToken);
                                                    });

    }
}

void EndpointRegistrationManager::onCurrentEndpointDetach(const UserDetachNotification& response)
{
    context_.getStatus().setEndpointAttachStatus(false);

    KAA_LOG_INFO(boost::format("Current endpoint was detached by '%1%'") % response.endpointAccessToken);

    if (attachStatusListener_) {
        context_.getExecutorContext().getCallbackExecutor().add([this, response]
                                                    {
                                                        attachStatusListener_->onDetach(response.endpointAccessToken);
                                                    });
    }
}

void EndpointRegistrationManager::attachEndpoint(const std::string& endpointAccessToken, IAttachEndpointCallbackPtr listener)
{
    if (endpointAccessToken.empty()) {
        KAA_LOG_WARN("Failed to attach endpoint: bad endpoint access token");
        throw BadCredentials("Bad endpoint access token");
    }

    std::int32_t requestId = attachRequestId_++;

    KAA_MUTEX_LOCKING("attachEndpointGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(attachEndpointLock, attachEndpointGuard_);
    KAA_MUTEX_LOCKED("attachEndpointGuard_");

    auto requestResult = attachEndpointRequests_.insert(std::make_pair(requestId, endpointAccessToken));
    if (requestResult.second) {
        KAA_LOG_INFO(boost::format("Going to attach endpoint by access token '%1%' (requestId: %2%)")
                                                                    % endpointAccessToken % requestId);
        if (listener) {
            auto listenerResult = attachEndpointListeners_.insert(std::make_pair(requestId, listener));
            if (!listenerResult.second) {
                KAA_LOG_WARN("Failed to add listener to notify of endpoint attach result");
            }
        }

        KAA_MUTEX_UNLOCKING("attachEndpointGuard_");
        KAA_UNLOCK(attachEndpointLock);
        KAA_MUTEX_UNLOCKED("attachEndpointGuard_");

        doSync();
    } else {
        KAA_MUTEX_UNLOCKING("attachEndpointGuard_");
        KAA_UNLOCK(attachEndpointLock);
        KAA_MUTEX_UNLOCKED("attachEndpointGuard_");

        KAA_LOG_WARN(boost::format("Ignore attach endpoint request (access token: '%1%'): already exists")
                                                                                        % endpointAccessToken);
        throw KaaException("Failed to add attach endpoint request: already exists");
    }
}

void EndpointRegistrationManager::detachEndpoint(const std::string& endpointKeyHash, IDetachEndpointCallbackPtr listener)
{
    if (endpointKeyHash.empty()) {
        KAA_LOG_WARN("Failed to add endpoint detach request: bad endpoint key hash")
        throw BadCredentials("Bad endpoint key hash");
    }

    std::int32_t requestId = detachRequestId_++;

    KAA_MUTEX_LOCKING("detachEndpointGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(detachEndpointLock, detachEndpointGuard_);
    KAA_MUTEX_LOCKED("detachEndpointGuard_");

    auto result = detachEndpointRequests_.insert(std::make_pair(requestId, endpointKeyHash));
    if (result.second) {
        KAA_LOG_INFO(boost::format("Going to detach Endpoint by key hash '%1%' (requestId: %2%)")
                                                                    % endpointKeyHash % requestId);
        if (listener) {
            auto listenerResult = detachEndpointListeners_.insert(std::make_pair(requestId, listener));
            if (!listenerResult.second) {
                KAA_LOG_WARN("Failed to add listener to notify of endpoint detach result");
            }
        }

        KAA_MUTEX_UNLOCKING("detachEndpointGuard_");
        KAA_UNLOCK(detachEndpointLock);
        KAA_MUTEX_UNLOCKED("detachEndpointGuard_");

        doSync();
    } else {
        KAA_MUTEX_UNLOCKING("detachEndpointGuard_");
        KAA_UNLOCK(detachEndpointLock);
        KAA_MUTEX_UNLOCKED("detachEndpointGuard_");

        KAA_LOG_WARN(boost::format("Ignore detach endpoint request (key hash: '%1%'): already exists")
                                                                                        % endpointKeyHash);
        throw KaaException("Failed to add detach endpoint request: already exists");
    }
}

void EndpointRegistrationManager::attachUser(const std::string& userExternalId
                                           , const std::string& userAccessToken
                                           , IUserAttachCallbackPtr listener)
{
    if (!strlen(DEFAULT_USER_VERIFIER_TOKEN)) {
        KAA_LOG_ERROR("Failed to attach to user: default user verifier is not specified");
        throw BadCredentials("Default user verifier is not specified");
    }

    attachUser(userExternalId, userAccessToken, DEFAULT_USER_VERIFIER_TOKEN, listener);
}

void EndpointRegistrationManager::attachUser(const std::string& userExternalId
                                           , const std::string& userAccessToken
                                           , const std::string& userVerifierToken
                                           , IUserAttachCallbackPtr listener)
{
    if (userExternalId.empty() || userAccessToken.empty() || userVerifierToken.empty()) {
        KAA_LOG_ERROR(boost::format("Failed to attach to user: user '%1%', access token '%2%', user verifier '%3%'")
                                                                % userExternalId % userAccessToken % userVerifierToken);
        throw BadCredentials("Bad user credentials");
    }

    KAA_MUTEX_LOCKING("userAttachRequestGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(userAttachRequestLock, userAttachRequestGuard_);
    KAA_MUTEX_LOCKED("userAttachRequestGuard_");

    if (!userAttachRequest_) {
        userAttachRequest_.reset(new UserAttachRequest);

        KAA_MUTEX_UNLOCKING("userAttachRequestGuard_");
        KAA_UNLOCK(userAttachRequestLock);
        KAA_MUTEX_UNLOCKED("userAttachRequestGuard_");

        KAA_LOG_INFO(boost::format("Going to attach to user '%1%' by access token: '%2%' (user verifier '%3%')")
                                                            % userExternalId % userAccessToken % userVerifierToken);

        userAttachRequest_->userAccessToken = userAccessToken;
        userAttachRequest_->userExternalId  = userExternalId;
        userAttachRequest_->userVerifierId  = userVerifierToken;

        userAttachResponseListener_ = listener;

        doSync();
    } else {
        KAA_LOG_WARN(boost::format("Failed to attach to user %1%. Another request to %2% is being processed")
                                                            % userExternalId % userAttachRequest_->userExternalId);
        throw KaaException("Another user attach request is being processed");
    }
}

std::shared_ptr<UserAttachRequest> EndpointRegistrationManager::getUserAttachRequest()
{
    KAA_MUTEX_LOCKING("userAttachRequestGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(userAttachRequestLock, userAttachRequestGuard_);
    KAA_MUTEX_LOCKED("userAttachRequestGuard_");
    return userAttachRequest_;
}

std::unordered_map<std::int32_t, std::string> EndpointRegistrationManager::getEndpointsToAttach()
{
    KAA_MUTEX_LOCKING("attachEndpointGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(attachEndpointLock, attachEndpointGuard_);
    KAA_MUTEX_LOCKED("attachEndpointGuard_");
    return attachEndpointRequests_;
}

std::unordered_map<std::int32_t, std::string> EndpointRegistrationManager::getEndpointsToDetach()
{
    KAA_MUTEX_LOCKING("detachEndpointGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(detachEndpointLock, detachEndpointGuard_);
    KAA_MUTEX_LOCKED("detachEndpointGuard_");
    return detachEndpointRequests_;
}

void EndpointRegistrationManager::doSync()
{
    if (userTransport_) {
        userTransport_->sync();
    } else {
        KAA_LOG_ERROR("Failed to sync: user transport not found");
        throw TransportNotFoundException("User transport not found");
    }
}

}

#endif

