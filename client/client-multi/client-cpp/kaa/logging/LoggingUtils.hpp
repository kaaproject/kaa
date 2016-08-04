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

#ifndef LOGGINGUTILS_HPP_
#define LOGGINGUTILS_HPP_

#include <cstdint>
#include <cstddef>
#include <string>
#include <vector>

#include <boost/shared_array.hpp>

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/common/TransportType.hpp"
#include "kaa/channel/TransportProtocolId.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/failover/FailoverCommon.hpp"
#include "kaa/channel/ITransportConnectionInfo.hpp"
#include "kaa/channel/ServerType.hpp"

namespace kaa {

class LoggingUtils {
public:
    static std::string toString(const SyncRequest::notificationSyncRequest_t& request);

    static std::string toString(const SyncRequest::configurationSyncRequest_t& request);

    static std::string toString(const ProtocolVersionPair& protocolVersion);

    static std::string toString(const SyncRequest::bootstrapSyncRequest_t& request);

    static std::string toString(const SyncRequest::profileSyncRequest_t& request);

    static std::string toString(const SyncRequest::syncRequestMetaData_t& request);

    static std::string toString(const SyncRequest::eventSyncRequest_t& request);

    static std::string toString(const UserSyncRequest::userAttachRequest_t& request);

    static std::string toString(const SyncRequest::userSyncRequest_t& request);

    static std::string toString(const SyncResponse::configurationSyncResponse_t& response);

    static std::string toString(const SyncResponse::profileSyncResponse_t& response);

    static std::string toString(const SyncResponse::bootstrapSyncResponse_t& response);

    static std::string toString(const SyncResponse::eventSyncResponse_t& response);

    static std::string toString(const SyncResponse::notificationSyncResponse_t& response);

    static std::string toString(const UserSyncResponse::userAttachResponse_t& response);

    static std::string toString(const UserSyncResponse::userDetachNotification_t& response);

    static std::string toString(const SyncResponse::userSyncResponse_t& response);

    static std::string toString(const SyncResponse::redirectSyncResponse_t& response);

    static std::string toString(const TransportProtocolId& protocolId);

    static std::string toString(const std::vector<std::uint8_t>& vec) {
        return toString(vec.data(), vec.size());
    }

    static std::string toString(const boost::shared_array<std::uint8_t>& vec, std::size_t length) {
        return toString(vec.get(), length);
    }

    static std::string toString(const SharedDataBuffer& buffer) {
        return toString(buffer.first, buffer.second);
    }

    static std::string toString(const std::string& data) {
        return toString(reinterpret_cast<const std::uint8_t*>(data.data()), data.length());
    }

    static std::string toString(const std::uint8_t* vec, std::size_t length);

    static std::string toString(SyncResponseStatus status);

    static std::string toString(const NotificationSyncRequest::acceptedUnicastNotifications_t& notifications);

    static std::string toString(const NotificationSyncRequest::topicStates_t& states);

    static std::string toString(const NotificationSyncRequest::subscriptionCommands_t& commands);

    static std::string toString(SubscriptionCommandType type);

    static std::string toString(const Notification& notification);

    static std::string toString(const NotificationSyncResponse::notifications_t& notifications);

    static std::string toString(NotificationType type);

    static std::string toString(const NotificationSyncResponse::availableTopics_t& topics);

    static std::string toString(SubscriptionType type);

    static std::string toString(const UserSyncRequest::endpointAttachRequests_t& attachRequests);

    static std::string toString(const UserSyncRequest::endpointDetachRequests_t& detachRequests);

    static std::string toString(const UserSyncResponse::endpointAttachResponses_t& attachResponses);

    static std::string toString(const UserSyncResponse::endpointDetachResponses_t& detachResponse);

    static std::string toString(SyncResponseResultType type);

    static std::string toString(const EventSyncRequest::events_t& events);

    static std::string toString(const EventSyncResponse::events_t& events);

    static std::string toString(const EventSyncRequest::eventListenersRequests_t& request);

    static std::string toString(const EventSyncResponse::eventListenersResponses_t& response);

    static std::string toString(const EventSyncResponse::eventSequenceNumberResponse_t& response);

    static std::string toString(TransportType type);

    static std::string toString(const SyncRequest::logSyncRequest_t& logSyncRequest);

    static std::string toString(const SyncResponse::logSyncResponse_t& logSyncResponse);

    static std::string toString(LogDeliveryErrorCode code);

    static std::string toString(KaaFailoverReason reason);

    static std::string toString(FailoverStrategyAction action);

    static std::string toString(const FailoverStrategyDecision& decision);

    static std::string toString(const ITransportConnectionInfo& connectionInfo);

    static std::string toString(ServerType type);

private:
    static std::string toString(const std::vector<Event>& events);
};

}  // namespace kaa


#endif /* LOGGINGUTILS_HPP_ */
