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

#include "kaa/event/registration/EndpointRegistrationManager.hpp"

#ifdef KAA_USE_EVENTS

#include <ctime>
#include <cstdlib>

#include "kaa/logging/Log.hpp"

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/event/registration/IEndpointAttachStatusListener.hpp"
#include "kaa/event/registration/IAttachedEndpointListListener.hpp"

namespace kaa {

EndpointRegistrationManager::EndpointRegistrationManager(IKaaClientStateStoragePtr status)
    : status_(status), userTransport_(nullptr), attachStatusListener_(nullptr)
{
    attachedEndpoints_ = status_->getAttachedEndpoints();
    endpointAccessToken_ = status_->getEndpointAccessToken();
    endpointKeyHash_ = status_->getEndpointKeyHash();

    if (endpointAccessToken_.empty()) {
        regenerateEndpointAccessToken();
    }
}

void EndpointRegistrationManager::regenerateEndpointAccessToken()
{
    bool isRegenerated = !endpointAccessToken_.empty();
    std::string oldToken = endpointAccessToken_;

    endpointAccessToken_.assign(UuidGenerator::generateUuid());

    status_->setEndpointAccessToken(endpointAccessToken_);

    KAA_LOG_INFO(boost::format("New endpoint acccess token is generated: %1%") % endpointAccessToken_);

    /*
     * First time access token is sent with Endpoint Registration request
     */
    if (isRegenerated) {
        onEndpointAccessTokenChanged(oldToken);
    }
}

void EndpointRegistrationManager::onUserAttach(const UserSyncResponse::userAttachResponse_t& response)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(endpointLock, endpointsGuard_);

    if (!response.is_null()) {
        if (response.get_UserAttachResponse().result == SyncResponseResultType::SUCCESS) {
            if (userAttachRequest_.get() == nullptr) {
                KAA_LOG_ERROR(boost::format("Got UserAttachResponse without leading UserAttachRequest. Probably duplicated message from server."));
                return;
            }

            status_->setEndpointAttachStatus(true);

            KAA_LOG_INFO(boost::format("Current endpoint was attached to '%1%'") % userAttachRequest_->userExternalId);

            if (attachStatusListener_ != nullptr) {
                attachStatusListener_->onAttachSuccess(userAttachRequest_->userExternalId, endpointAccessToken_);
            }

            userAttachRequest_.reset();
        } else {
            KAA_LOG_ERROR("Failed to attach to user");

            if (attachStatusListener_ != nullptr) {
                attachStatusListener_->onDetachSuccess(endpointAccessToken_);
            }
        }
    }
}

void EndpointRegistrationManager::onEndpointsAttach(const std::vector<EndpointAttachResponse>& endpoints)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(endpointLock, endpointsGuard_);

    bool hasChanged = false;

    std::string externalUserId;
    if (userAttachRequest_) {
        externalUserId = userAttachRequest_->userExternalId;
    }

    for (const auto& endpoint : endpoints) {
        auto requestIt = attachingEndpoints_.find(endpoint.requestId);
        if (requestIt != attachingEndpoints_.end()) {
            if (endpoint.result == SyncResponseResultType::SUCCESS) {
                if (!endpoint.endpointKeyHash.is_null()) {
                    attachedEndpoints_.insert(std::make_pair(
                            requestIt->second.endpointData_, endpoint.endpointKeyHash.get_string()));
                    hasChanged = true;
                }
                if (requestIt->second.listener_) {
                    requestIt->second.listener_->onAttachSuccess(externalUserId, endpointAccessToken_);
                }
            } else {
                KAA_LOG_ERROR(boost::format("Failed to attach endpoint. Attach endpoint request id: %1%")
                    % LoggingUtils::ByteArrayToString(endpoint.requestId));
                if (requestIt->second.listener_) {
                    requestIt->second.listener_->onAttachFailure();
                }
            }
            attachingEndpoints_.erase(requestIt);
        } else {
                KAA_LOG_ERROR(boost::format("Failed to find endpoint attach request by id: %1%")
                    % LoggingUtils::ByteArrayToString(endpoint.requestId));
        }
    }

    if (hasChanged) {
        status_->setAttachedEndpoints(attachedEndpoints_);
        attachedEPListListeners_(attachedEndpoints_);
    }
}

void EndpointRegistrationManager::onEndpointsDetach(const std::vector<EndpointDetachResponse>& endpoints)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(endpointLock, endpointsGuard_);
    bool hasChanges = false;
    for (EndpointDetachResponse endpoint : endpoints) {
        auto requestIt = detachingEndpoints_.find(endpoint.requestId);
        if (requestIt != detachingEndpoints_.end()) {
            if (endpoint.result == SyncResponseResultType::SUCCESS) {
                std::string epHash = requestIt->second.endpointData_;
                for (auto attachedEpIt = attachedEndpoints_.begin(); attachedEpIt != attachedEndpoints_.end(); ++attachedEpIt) {
                    if (attachedEpIt->second.compare(epHash) == 0) {
                        attachedEndpoints_.erase(attachedEpIt);
                        hasChanges = true;
                        break;
                    }
                }
                if (epHash.compare(endpointKeyHash_) == 0) {
                    status_->setEndpointAttachStatus(false);
                }
                if (requestIt->second.listener_) {
                    requestIt->second.listener_->onDetachSuccess(endpointAccessToken_);
                }
            } else {
                KAA_LOG_ERROR(boost::format("Failed to detach endpoint. Detach endpoint request id: %1%")
                    % LoggingUtils::ByteArrayToString(endpoint.requestId));
                if (requestIt->second.listener_) {
                    requestIt->second.listener_->onDetachFailure();
                }
            }
            detachingEndpoints_.erase(endpoint.requestId);
        } else {
                KAA_LOG_ERROR(boost::format("Failed to find endpoint detach request by id: %1%")
                    % LoggingUtils::ByteArrayToString(endpoint.requestId));
        }
    }

    if (hasChanges) {
        status_->setAttachedEndpoints(attachedEndpoints_);
        attachedEPListListeners_(attachedEndpoints_);
    }
}

void EndpointRegistrationManager::onCurrentEndpointAttach(const UserAttachNotification& response)
{
    if (attachStatusListener_ != nullptr) {
        KAA_LOG_INFO(boost::format("Current endpoint was attached to '%1%' by '%2%'")
                        % response.userExternalId % response.endpointAccessToken);

        status_->setEndpointAttachStatus(true);
        attachStatusListener_->onAttachSuccess(response.userExternalId, response.endpointAccessToken);
    }
}

void EndpointRegistrationManager::onCurrentEndpointDetach(const UserDetachNotification& response)
{
    if (attachStatusListener_ != nullptr) {
        KAA_LOG_INFO(boost::format("Current endpoint was detached by '%1%'") % response.endpointAccessToken);

        status_->setEndpointAttachStatus(false);
        attachStatusListener_->onDetachSuccess(response.endpointAccessToken);
    }
}

const AttachedEndpoints& EndpointRegistrationManager::getAttachedEndpoints() {
    return attachedEndpoints_;
}

void EndpointRegistrationManager::addAttachedEndpointListListener(IAttachedEndpointListListener *listener)
{
    if (listener) {
        KAA_R_MUTEX_UNIQUE_DECLARE(lock, listenerGuard_);
        /* Disconnecting for case whether listener was already subscribed, no effect otherwise */
        attachedEPListListeners_.removeCallback(listener);
        attachedEPListListeners_.addCallback(listener,
                std::bind(&IAttachedEndpointListListener::onListUpdated, listener, std::placeholders::_1));
    }
}

void EndpointRegistrationManager::removeAttachedEndpointListListener(IAttachedEndpointListListener *listener)
{
    if (listener) {
        KAA_R_MUTEX_UNIQUE_DECLARE(lock, listenerGuard_);
        attachedEPListListeners_.removeCallback(listener);
    }
}

void EndpointRegistrationManager::attachEndpoint(const std::string& endpointAccessToken, IEndpointAttachStatusListener* listener)
{
    if (endpointAccessToken.empty()) {
        KAA_LOG_WARN("Failed to add endpoint attach request: bad input data")
        return;
    }

    KAA_R_MUTEX_UNIQUE_DECLARE(lock, endpointsGuard_);
    std::string requestId;
    UuidGenerator::generateUuid(requestId, endpointAccessToken);

    EndpointOperationInfo info;
    info.endpointData_ = endpointAccessToken;
    info.listener_ = listener;

    auto result = attachingEndpoints_.insert(std::make_pair(requestId, info));

    if (result.second) {
        KAA_LOG_INFO(boost::format("Going to attach Endpoint by access token: %1% (requestId: %2%)")
            % endpointAccessToken % LoggingUtils::ByteArrayToString(requestId));
        if (userTransport_ != nullptr) {
            userTransport_->sync();
        } else {
            KAA_LOG_WARN("Can not attach endpoint now: transport was not set.");
        }
    }
}

void EndpointRegistrationManager::detachEndpoint(IEndpointAttachStatusListener* listener)
{
    this->detachEndpoint(endpointKeyHash_, listener);
}

void EndpointRegistrationManager::detachEndpoint(const std::string& endpointKeyHash, IEndpointAttachStatusListener* listener)
{
    if (endpointKeyHash.empty()) {
        KAA_LOG_WARN("Failed to add endpoint detach request: bad input data")
        return;
    }

    KAA_R_MUTEX_UNIQUE_DECLARE(lock, endpointsGuard_);

    std::string requestId;
    UuidGenerator::generateUuid(requestId, endpointKeyHash);

    EndpointOperationInfo info;
    info.endpointData_ = endpointKeyHash;
    info.listener_ = listener;

    auto result = detachingEndpoints_.insert(std::make_pair(requestId, info));

    if (result.second) {
        KAA_LOG_INFO(boost::format("Going to detach Endpoint by keyHash: %1% (requestId: %2%)")
            % endpointKeyHash % LoggingUtils::ByteArrayToString(requestId));
        if (userTransport_ != nullptr) {
            userTransport_->sync();
        } else {
            KAA_LOG_WARN("Can not detach endpoint now: transport was not set.");
        }
    }
}

void EndpointRegistrationManager::attachUser(const std::string& userExternalId
                                           , const std::string& userAccessToken
                                           , IEndpointAttachStatusListener* listener)
{
    if (!userExternalId.empty() && !userAccessToken.empty()) {
        KAA_LOG_INFO(boost::format("Going to attach to user %1% by access token: %2%")
            % userExternalId % userAccessToken);

        UserAttachRequest *request = new UserAttachRequest;
        request->userAccessToken = userAccessToken;
        request->userExternalId = userExternalId;

        if (listener != nullptr) {
            attachStatusListener_ = listener;
        }

        userAttachRequest_.reset(request);

        if (userTransport_ != nullptr) {
            userTransport_->sync();
        } else {
            KAA_LOG_WARN("Can not attach user now: transport was not set.");
        }
    }
}

void EndpointRegistrationManager::onEndpointAccessTokenChanged(const std::string& old)
{
    if (userTransport_ != nullptr) {
        userTransport_->syncProfile();
    } else {
        KAA_LOG_WARN("Not synchronizing profile: transport was not set.");
    }

    auto it = attachedEndpoints_.find(old);
    if (it != attachedEndpoints_.end()) {
        detachEndpoint(it->second, nullptr);
    }
}

std::map<std::string, std::string>  EndpointRegistrationManager::getEndpointsToAttach()
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, endpointsGuard_);
    std::map<std::string, std::string> resultingMap;
    for (const auto& idToTokenPair : attachingEndpoints_) {
        resultingMap.insert(std::make_pair(idToTokenPair.first, idToTokenPair.second.endpointData_));
    }
    return resultingMap;
}

std::map<std::string, std::string>  EndpointRegistrationManager::getEndpointsToDetach()
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, endpointsGuard_);
    std::map<std::string, std::string> resultingMap;
    for (const auto& idToHashPair : detachingEndpoints_) {
        resultingMap.insert(std::make_pair(idToHashPair.first, idToHashPair.second.endpointData_));
    }
    return resultingMap;
}

UserAttachRequestPtr EndpointRegistrationManager::getUserAttachRequest()
{
    return userAttachRequest_;
}

}

#endif

