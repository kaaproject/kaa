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

#include "kaa/event/registration/EndpointRegistrationManager.hpp"

#ifdef KAA_USE_EVENTS

#include <cstdlib>

#include "kaa/logging/Log.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/event/registration/IUserAttachCallback.hpp"
#include "kaa/event/registration/IAttachStatusListener.hpp"
#include "kaa/event/registration/IAttachEndpointCallback.hpp"
#include "kaa/event/registration/IDetachEndpointCallback.hpp"
#include "kaa/common/exception/TransportNotFoundException.hpp"

namespace kaa {

EndpointRegistrationManager::EndpointRegistrationManager(IKaaClientStateStoragePtr status)
    : userTransport_(nullptr), status_(status)
    , userAttachResponseListener_(nullptr), attachStatusListener_(nullptr)
    , attachRequestId_(0), detachRequestId_(0)
{
}

void EndpointRegistrationManager::onUserAttach(const UserAttachResponse& response)
{
    if (response.result == SyncResponseResultType::SUCCESS) {
        if (!userAttachRequest_) {
            KAA_LOG_ERROR(boost::format("Got UserAttachResponse without leading UserAttachRequest. "
                                        "Probably duplicated message from server."));
            return;
        }

        status_->setEndpointAttachStatus(true);

        KAA_LOG_INFO(boost::format("Current endpoint was attached to '%1%'") % userAttachRequest_->userExternalId);

        if (userAttachResponseListener_) {
            userAttachResponseListener_->onAttachSuccess();
        }

        userAttachRequest_.reset();
    } else {
        KAA_LOG_ERROR("Failed to attach to user");

        if (userAttachResponseListener_) {
            userAttachResponseListener_->onAttachFailed(
                    response.errorCode.is_null() ? UserAttachErrorCode::OTHER : response.errorCode.get_UserAttachErrorCode(),
                    response.errorReason.is_null() ? "" : response.errorReason.get_string());
        }
    }
}

void EndpointRegistrationManager::onEndpointsAttach(const std::vector<EndpointAttachResponse>& attachResponses)
{
    for (const auto& attachResponse : attachResponses) {
        KAA_MUTEX_LOCKING(attachEndpointGuard_);
        KAA_MUTEX_UNIQUE_DECLARE(attachEndpointLock, attachEndpointGuard_);
        KAA_MUTEX_LOCKED(attachEndpointGuard_);

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
                auto *callback = listenerIt->second;
                attachEndpointListeners_.erase(listenerIt);

                KAA_MUTEX_UNLOCKING(attachEndpointGuard_);
                KAA_UNLOCK(attachEndpointLock);
                KAA_MUTEX_UNLOCKED(attachEndpointGuard_);

                if (isAttachSuccess) {
                    callback->onAttachSuccess(attachResponse.endpointKeyHash.is_null() ?
                                                "" : attachResponse.endpointKeyHash.get_string());
                } else {
                    callback->onAttachFailed();
                }
            }
        }
    }
}

void EndpointRegistrationManager::onEndpointsDetach(const std::vector<EndpointDetachResponse>& detachResponses)
{
    for (const auto& detachResponse : detachResponses) {
        KAA_MUTEX_LOCKING(detachEndpointGuard_);
        KAA_MUTEX_UNIQUE_DECLARE(detachEndpointLock, detachEndpointGuard_);
        KAA_MUTEX_LOCKED(detachEndpointGuard_);

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
                auto *callback = listenerIt->second;
                detachEndpointListeners_.erase(listenerIt);

                KAA_MUTEX_UNLOCKING(detachEndpointGuard_);
                KAA_UNLOCK(detachEndpointLock);
                KAA_MUTEX_UNLOCKED(detachEndpointGuard_);

                if (isDetachSuccess) {
                    callback->onDetachSuccess();
                } else {
                    callback->onDetachFailed();
                }
            }
        }
    }
}

void EndpointRegistrationManager::onCurrentEndpointAttach(const UserAttachNotification& response)
{
    KAA_LOG_INFO(boost::format("Current endpoint was attached to '%1%' by '%2%'")
                                    % response.userExternalId % response.endpointAccessToken);

    if (attachStatusListener_) {
        status_->setEndpointAttachStatus(true);
        attachStatusListener_->onAttach(response.userExternalId, response.endpointAccessToken);
    }
}

void EndpointRegistrationManager::onCurrentEndpointDetach(const UserDetachNotification& response)
{
    KAA_LOG_INFO(boost::format("Current endpoint was detached by '%1%'") % response.endpointAccessToken);

    if (attachStatusListener_) {
        status_->setEndpointAttachStatus(false);
        attachStatusListener_->onDetach(response.endpointAccessToken);
    }
}

void EndpointRegistrationManager::attachEndpoint(const std::string& endpointAccessToken, IAttachEndpointCallback* listener)
{
    if (endpointAccessToken.empty()) {
        KAA_LOG_WARN("Failed to attach endpoint: bad endpoint access token");
        throw KaaException("Bad endpoint access token");
    }

    std::int32_t requestId = attachRequestId_++;

    KAA_MUTEX_LOCKING(attachEndpointGuard_);
    KAA_MUTEX_UNIQUE_DECLARE(attachEndpointLock, attachEndpointGuard_);
    KAA_MUTEX_LOCKED(attachEndpointGuard_);

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

        KAA_MUTEX_UNLOCKING(attachEndpointGuard_);
        KAA_UNLOCK(attachEndpointLock);
        KAA_MUTEX_UNLOCKED(attachEndpointGuard_);

        doSync();
    } else {
        KAA_MUTEX_UNLOCKING(attachEndpointGuard_);
        KAA_UNLOCK(attachEndpointLock);
        KAA_MUTEX_UNLOCKED(attachEndpointGuard_);

        KAA_LOG_WARN(boost::format("Ignore attach endpoint request (access token: '%1%'): already exists")
                                                                                        % endpointAccessToken);
    }
}

void EndpointRegistrationManager::detachEndpoint(const std::string& endpointKeyHash, IDetachEndpointCallback* listener)
{
    if (endpointKeyHash.empty()) {
        KAA_LOG_WARN("Failed to add endpoint detach request: bad endpoint key hash")
        throw KaaException("Bad endpoint key hash");
    }

    std::int32_t requestId = detachRequestId_++;

    KAA_MUTEX_LOCKING(detachEndpointGuard_);
    KAA_MUTEX_UNIQUE_DECLARE(detachEndpointLock, detachEndpointGuard_);
    KAA_MUTEX_LOCKED(detachEndpointGuard_);

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

        KAA_MUTEX_UNLOCKING(detachEndpointGuard_);
        KAA_UNLOCK(detachEndpointLock);
        KAA_MUTEX_UNLOCKED(detachEndpointGuard_);

        doSync();
    } else {
        KAA_MUTEX_UNLOCKING(detachEndpointGuard_);
        KAA_UNLOCK(detachEndpointLock);
        KAA_MUTEX_UNLOCKED(detachEndpointGuard_);

        KAA_LOG_WARN(boost::format("Ignore detach endpoint request (key hash: '%1%'): already exists")
                                                                                        % endpointKeyHash);
    }
}

void EndpointRegistrationManager::attachUser(const std::string& userExternalId
                                           , const std::string& userAccessToken
                                           , IUserAttachCallback* listener)
{
    if (!strlen(DEFAULT_USER_VERIFIER_TOKEN)) {
        KAA_LOG_ERROR("Failed to attach to user: default user verifier is not specified");
        throw KaaException("Default user verifier is not specified");
    }

    attachUser(userExternalId, userAccessToken, DEFAULT_USER_VERIFIER_TOKEN, listener);
}

void EndpointRegistrationManager::attachUser(const std::string& userExternalId
                                           , const std::string& userAccessToken
                                           , const std::string& userVerifierToken
                                           , IUserAttachCallback* listener)
{
    if (userExternalId.empty() || userAccessToken.empty() || userVerifierToken.empty()) {
        KAA_LOG_ERROR(boost::format("Failed to attach to user: user '%1%', access token '%2%', user verifier '%3%'")
                                                                % userExternalId % userAccessToken % userVerifierToken);
        throw KaaException("Bad user credentials");
    }

    KAA_LOG_INFO(boost::format("Going to attach to user '%1%' by access token: '%2%' (user verifier '%3%')")
                                        % userExternalId % userAccessToken % DEFAULT_USER_VERIFIER_TOKEN);

    userAttachRequest_.reset(new UserAttachRequest);
    userAttachRequest_->userAccessToken = userAccessToken;
    userAttachRequest_->userExternalId  = userExternalId;
    userAttachRequest_->userVerifierId  = userVerifierToken;

    userAttachResponseListener_ = listener;

    doSync();
}

std::unordered_map<std::int32_t, std::string> EndpointRegistrationManager::getEndpointsToAttach()
{
    KAA_MUTEX_LOCKING(attachEndpointGuard_);
    KAA_MUTEX_UNIQUE_DECLARE(attachEndpointLock, attachEndpointGuard_);
    KAA_MUTEX_LOCKED(attachEndpointGuard_);
    return attachEndpointRequests_;
}

std::unordered_map<std::int32_t, std::string> EndpointRegistrationManager::getEndpointsToDetach()
{
    KAA_MUTEX_LOCKING(detachEndpointGuard_);
    KAA_MUTEX_UNIQUE_DECLARE(detachEndpointLock, detachEndpointGuard_);
    KAA_MUTEX_LOCKED(detachEndpointGuard_);
    return detachEndpointRequests_;
}

void EndpointRegistrationManager::setTransport(UserTransport * transport) {
    userTransport_ = transport;
    if (userAttachRequest_ || !attachEndpointRequests_.empty() || !detachEndpointRequests_.empty()) {
        doSync();
    }
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

