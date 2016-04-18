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

#include "kaa/event/registration/UserTransport.hpp"

#ifdef KAA_USE_EVENTS

#include "kaa/logging/Log.hpp"

namespace kaa {

UserTransport::UserTransport(IRegistrationProcessor& manager, IKaaChannelManager& channelManager, IKaaClientContext &context)
    : AbstractKaaTransport(channelManager, context)
    , manager_(manager)
{

}

std::shared_ptr<UserSyncRequest> UserTransport::createUserRequest()
{
    auto attachUsr = manager_.getUserAttachRequest();
    auto attachEps = manager_.getEndpointsToAttach();
    auto detachEps = manager_.getEndpointsToDetach();

    std::shared_ptr<UserSyncRequest> request(new UserSyncRequest);
    if (attachUsr.get() == nullptr) {
        request->userAttachRequest.set_null();
    } else {
        request->userAttachRequest.set_UserAttachRequest(*attachUsr);
    }

    if (attachEps.empty()) {
        request->endpointAttachRequests.set_null();
    } else {
        std::vector<EndpointAttachRequest> requests;
        for (const auto& idToTokenPair : attachEps) {
            EndpointAttachRequest subRequest;
            subRequest.requestId = idToTokenPair.first;
            subRequest.endpointAccessToken = idToTokenPair.second;
            requests.push_back(subRequest);
        }
        request->endpointAttachRequests.set_array(requests);
    }

    if (detachEps.empty()) {
        request->endpointDetachRequests.set_null();
    } else {
        std::vector<EndpointDetachRequest> requests;
        for (const auto& idToHashPair : detachEps) {
            EndpointDetachRequest subRequest;
            subRequest.requestId = idToHashPair.first;
            subRequest.endpointKeyHash = idToHashPair.second;
            requests.push_back(subRequest);
        }
        request->endpointDetachRequests.set_array(requests);
    }

    return request;
}

void UserTransport::onUserResponse(const UserSyncResponse& response)
{
    if (!response.userAttachResponse.is_null()) {
        manager_.onUserAttach(response.userAttachResponse.get_UserAttachResponse());
    }

    if (!response.endpointAttachResponses.is_null()) {
        manager_.onEndpointsAttach(response.endpointAttachResponses.get_array());
    }

    if (!response.endpointDetachResponses.is_null()) {
        manager_.onEndpointsDetach(response.endpointDetachResponses.get_array());
    }

    if (!response.userAttachNotification.is_null()) {
        manager_.onCurrentEndpointAttach(response.userAttachNotification.get_UserAttachNotification());
    }

    if (!response.userDetachNotification.is_null()) {
        manager_.onCurrentEndpointDetach(response.userDetachNotification.get_UserDetachNotification());
    }
}

void UserTransport::sync()
{
    syncByType();
}

void UserTransport::syncProfile()
{
    syncByType(TransportType::PROFILE);
}

}  // namespace kaa

#endif


