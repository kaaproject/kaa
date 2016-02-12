/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef LOGGINGUTILS_HPP_
#define LOGGINGUTILS_HPP_

#include <iomanip>
#include <vector>
#include <string>
#include <sstream>
#include <cstdint>
#include <iterator>
#include <boost/shared_array.hpp>

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/common/TransportType.hpp"

namespace kaa {

#define KVSTRING(K,V) "\"" #K "\": \"" << V << "\""

class LoggingUtils {
public:
    static std::string NotificationSyncRequestToString(const SyncRequest::notificationSyncRequest_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            ss << KVSTRING(topicListHash, ((request.get_NotificationSyncRequest().topicListHash == 0) ? 0 :
                           request.get_NotificationSyncRequest().topicListHash));
            ss << KVSTRING(acceptedUnicastNotifications,
                    AcceptedUnicastNotificationsToString(request.get_NotificationSyncRequest().acceptedUnicastNotifications)) << ", ";
            ss << KVSTRING(subscriptionCommands, SubscriptionCommandsToString(request.get_NotificationSyncRequest().subscriptionCommands)) << ", ";
            ss << KVSTRING(topicStates, TopicStatesToString(request.get_NotificationSyncRequest().topicStates)) << ", ";
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string ConfigurationSyncRequestToString(const SyncRequest::configurationSyncRequest_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            std::stringstream hashStream;
            for (auto &byte : request.get_ConfigurationSyncRequest().configurationHash) {
                 hashStream << byte;
            }
            ss << KVSTRING(configurationHash, (std::string(hashStream.str()).empty() ? "null" : hashStream.str()));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string ProtocolVersionToString(const ProtocolVersionPair& protocolVersion) {
        std::ostringstream ss;

        ss << "[" << std::hex << "id=0x" << protocolVersion.id << std::dec << ",";
        ss << KVSTRING(version, protocolVersion.version) << "]";

        return ss.str();
    }

    static std::string BootstrapSyncRequestToString(const SyncRequest::bootstrapSyncRequest_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            const auto& syncRequest = request.get_BootstrapSyncRequest();
            ss << KVSTRING(requestId, syncRequest.requestId) << ",";
            ss << "protocols: ";

            const auto& protocols = syncRequest.supportedProtocols;
            size_t protocolCount = protocols.size();

            if (protocolCount > 0) {
                for (const auto& protocol : protocols) {
                    ss << ProtocolVersionToString(protocol);
                    if (--protocolCount > 0) {
                        ss << ",";
                    }
                }
            } else {
                ss << "null";
            }
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string ProfileSyncRequestToString(const SyncRequest::profileSyncRequest_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            const auto profileRequest = request.get_ProfileSyncRequest();

            if (!request.get_ProfileSyncRequest().endpointAccessToken.is_null()) {
                ss << KVSTRING(endpointAccessToken, profileRequest.endpointAccessToken.get_string());
            } else {
                ss << KVSTRING(endpointAccessToken, "null");
            }
            if (!profileRequest.endpointPublicKey.is_null()) {
                ss << KVSTRING(endpointPublicKey, ByteArrayToString(profileRequest.endpointPublicKey.get_bytes()));
            } else {
                ss << KVSTRING(endpointPublicKey, "null");
            }

            ss << KVSTRING(profileBody, ByteArrayToString(profileRequest.profileBody));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string MetaDataSyncRequestToString(const SyncRequest::syncRequestMetaData_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            const auto& syncRequest = request.get_SyncRequestMetaData();

            ss << KVSTRING(sdkToken, syncRequest.sdkToken) << ", ";

            if (syncRequest.endpointPublicKeyHash.is_null()) {
                ss << KVSTRING(endpointPublicKeyHash, "null") << ",";
            } else {
                ss << KVSTRING(endpointPublicKeyHash, ByteArrayToString(syncRequest.endpointPublicKeyHash.get_bytes())) << ",";
            }

            if (syncRequest.profileHash.is_null()) {
                ss << KVSTRING(profileHash, "null") << ",";
            } else {
                ss << KVSTRING(profileHash, ByteArrayToString(syncRequest.profileHash.get_bytes())) << ",";
            }

            if (syncRequest.timeout.is_null()) {
                ss << KVSTRING(timeout, "null") << ",";
            } else {
                ss << KVSTRING(timeout, syncRequest.timeout.get_long()) << ",";
            }
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string EventSyncRequestToString(const SyncRequest::eventSyncRequest_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            ss << KVSTRING(events, OutcomingEventsToString(request.get_EventSyncRequest().events));
            ss << KVSTRING(eventListenersRequest, EventListenersRequestToString(request.get_EventSyncRequest().eventListenersRequests));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string AttachUserRequestToString(const UserSyncRequest::userAttachRequest_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            ss << KVSTRING(userAccessToken, request.get_UserAttachRequest().userAccessToken);
            ss << KVSTRING(userExternalId, request.get_UserAttachRequest().userExternalId);
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string UserSyncRequestToString(const SyncRequest::userSyncRequest_t& request) {
        std::ostringstream ss;
        if (!request.is_null()) {
            ss << KVSTRING(endpointAttachRequests, AttachEPRequestsToString(request.get_UserSyncRequest().endpointAttachRequests));
            ss << KVSTRING(endpointDetachRequests, DetachEPRequestsToString(request.get_UserSyncRequest().endpointDetachRequests));
            ss << KVSTRING(userAttachRequest, AttachUserRequestToString(request.get_UserSyncRequest().userAttachRequest));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string ConfigurationSyncResponseToString(const SyncResponse::configurationSyncResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(confDeltaBody,
                    (response.get_ConfigurationSyncResponse().confDeltaBody.is_null()
                            ? "null"
                            : ByteArrayToString(response.get_ConfigurationSyncResponse().confDeltaBody.get_bytes()))
                              );
            ss << KVSTRING(confSchemaBody,
                    (response.get_ConfigurationSyncResponse().confSchemaBody.is_null()
                            ? "null"
                            : ByteArrayToString(response.get_ConfigurationSyncResponse().confSchemaBody.get_bytes()))
                              );
            ss <<  KVSTRING(responseStatus, SyncResponseStatusToString(response.get_ConfigurationSyncResponse().responseStatus));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string ProfileSyncResponseToString(const SyncResponse::profileSyncResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(responseStatus, SyncResponseStatusToString(response.get_ProfileSyncResponse().responseStatus));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string BootstrapSyncResponseToString(const SyncResponse::bootstrapSyncResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            const auto& bootstrapSync = response.get_BootstrapSyncResponse();
            const auto& supportedChannels = bootstrapSync.supportedProtocols;
            size_t supportedChannelCount = supportedChannels.size();

            ss << KVSTRING(requestId, bootstrapSync.requestId) << ", ";
            ss << "supportedChannels: ";

            if (supportedChannelCount > 0) {
                for (const auto& supportedChannel : supportedChannels) {
                    ss << "[" << std::hex << "accessPointId=0x" << supportedChannel.accessPointId << std::dec << ",";
                    ss << ProtocolVersionToString(supportedChannel.protocolVersionInfo) << ",";
                    ss << KVSTRING(connectionInfoLen, supportedChannel.connectionInfo.size()) << "]";

                    if (--supportedChannelCount > 0) {
                        ss << ",";
                    }
                }
            } else {
                ss << "null";
            }
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string EventSyncResponseToString(const SyncResponse::eventSyncResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(events, IncomingEventsToString(response.get_EventSyncResponse().events));
            ss << KVSTRING(eventListenersResponse, EventListenersResponseToString(response.get_EventSyncResponse().eventListenersResponses));
            ss << KVSTRING(eventSequenceNumberResponse, EventSequenceNumberResponseToString(response.get_EventSyncResponse().eventSequenceNumberResponse));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string NotificationSyncResponseToString(const SyncResponse::notificationSyncResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(responseStatus, SyncResponseStatusToString(response.get_NotificationSyncResponse().responseStatus)) << ", ";
            ss << KVSTRING(availableTopics, TopicsToString(response.get_NotificationSyncResponse().availableTopics)) << ", ";
            ss << KVSTRING(notifications, NotificationToString(response.get_NotificationSyncResponse().notifications));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string AttachUserResponseToString(const UserSyncResponse::userAttachResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(result, SyncResponseResultTypeToString(response.get_UserAttachResponse().result));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string DetachUserNotificationToString(const UserSyncResponse::userDetachNotification_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(accessToken, response.get_UserDetachNotification().endpointAccessToken);
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string UserSyncResponseToString(const SyncResponse::userSyncResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(endpointAttachResponses, AttachEPResponsesToString(response.get_UserSyncResponse().endpointAttachResponses));
            ss << KVSTRING(endpointDetachResponses, DetachEPResponsesToString(response.get_UserSyncResponse().endpointDetachResponses));
            ss << KVSTRING(userAttachResponse, AttachUserResponseToString(response.get_UserSyncResponse().userAttachResponse));
            ss << KVSTRING(userDetachNotification, DetachUserNotificationToString(response.get_UserSyncResponse().userDetachNotification));
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string RedirectSyncResponseToString(const SyncResponse::redirectSyncResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << std::hex << "accessPointId: 0x" << response.get_RedirectSyncResponse().accessPointId;
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string TransportProtocolIdToString(const TransportProtocolId& protocolId) {
        std::ostringstream ss;
        ss << "(protocol: id=0x" << std::hex << protocolId.getId() << ", version=" << std::dec << protocolId.getVersion() << ")";
        return ss.str();
    }

    static std::string ByteArrayToString(const std::uint8_t* vec, const size_t& length) {
        std::ostringstream ss;
        ss << "[ ";
        if (vec != nullptr && length > 0) {
            for (size_t i = 0; i < length; ++i) {
                ss << std::setw(2) << std::uppercase << std::setfill('0') << std::hex << (int) *(vec + i) << " ";
            }
        }
        ss << "]";
        return ss.str();
    }

    static std::string ByteArrayToString(const std::vector<std::uint8_t>& vec) {
        return ByteArrayToString(vec.data(), vec.size());
    }

    static std::string ByteArrayToString(const boost::shared_array<std::uint8_t>& vec, const size_t&length) {
        return ByteArrayToString(vec.get(), length);
    }

    static std::string ByteArrayToString(const SharedDataBuffer& buffer) {
        return ByteArrayToString(buffer.first, buffer.second);
    }

    static std::string ByteArrayToString(const std::string& data) {
        return ByteArrayToString(reinterpret_cast<const std::uint8_t*>(data.data()), data.length());
    }

    static std::string SyncResponseStatusToString(SyncResponseStatus status) {
        std::string description;

        switch (status) {
            case NO_DELTA: description = "NO_DELTA"; break;
            case DELTA: description = "DELTA"; break;
            case RESYNC: description = "RESYNC"; break;
            default: description = "UNKNOWN"; break;
        }

        return description;
    }

    static std::string AcceptedUnicastNotificationsToString(const NotificationSyncRequest::acceptedUnicastNotifications_t& notifications) {
        std::ostringstream stream;

        if (!notifications.is_null()) {
            const auto& container = notifications.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << *it;
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string TopicStatesToString(const NotificationSyncRequest::topicStates_t& states) {
        std::ostringstream stream;

        if (!states.is_null()) {
            const auto& container = states.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{id: " << it->topicId << ", sn: " << it->seqNumber << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string SubscriptionCommandsToString(const NotificationSyncRequest::subscriptionCommands_t& commands) {
        std::ostringstream stream;

        if (!commands.is_null()) {
            const auto& container = commands.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{id: " << it->topicId << ", cmd: " << SubscriptionCommandToString(it->command) << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string SubscriptionCommandToString(SubscriptionCommandType type) {
        std::string description;

        switch (type) {
            case ADD: description = "ADD"; break;
            case REMOVE: description = "REMOVE"; break;
            default: description = "UNKNOWN"; break;
        }

        return description;
    }

    static std::string SingleNotificationToString(const Notification& notification) {
        std::ostringstream stream;
        stream << "{";
        stream << KVSTRING(id, notification.topicId) << ", ";
        stream << KVSTRING(type, notification.type) << ", ";
        stream << KVSTRING(sn, (notification.seqNumber.is_null() ? 0 : notification.seqNumber.get_int())) << ", ";
        stream << KVSTRING(uid, (notification.uid.is_null() ? "null" : notification.uid.get_string()));
        stream << "}";
        return stream.str();
    }

    static std::string NotificationToString(const NotificationSyncResponse::notifications_t& notifications) {
        std::ostringstream stream;

        if (!notifications.is_null()) {
            const auto& container = notifications.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{";
                stream << KVSTRING(id, it->topicId) << ", ";
                stream << KVSTRING(type, it->type) << ", ";
                stream << KVSTRING(sn, (it->seqNumber.is_null() ? 0 : it->seqNumber.get_int())) << ", ";
                stream << KVSTRING(uid, (it->uid.is_null() ? "null" : it->uid.get_string()));
                stream << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string NotificationTypeToString(NotificationType type) {
        std::string description;

        switch (type) {
            case SYSTEM: description = "SYSTEM"; break;
            case CUSTOM: description = "CUSTOM"; break;
            default: description = "UNKNOWN"; break;
        }

        return description;
    }

    static std::string TopicsToString(const NotificationSyncResponse::availableTopics_t& topics) {
        std::ostringstream stream;

        if (!topics.is_null()) {
            const auto& container = topics.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{id: " << it->id << ", type: " << TopicSubscriptionTypeToString(it->subscriptionType) << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string TopicSubscriptionTypeToString(SubscriptionType type) {
        std::string description;

        switch (type) {
            case MANDATORY_SUBSCRIPTION: description = "MANDATORY_SUBSCRIPTION"; break;
            case OPTIONAL_SUBSCRIPTION: description = "OPTIONAL_SUBSCRIPTION"; break;
            default: description = "UNKNOWN"; break;
        }

        return description;
    }

    static std::string AttachEPRequestsToString(const UserSyncRequest::endpointAttachRequests_t& attachRequests) {
        std::ostringstream stream;

        if (!attachRequests.is_null()) {
            const auto& container = attachRequests.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{id: " << it->requestId << ", token: " << it->endpointAccessToken << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string DetachEPRequestsToString(const UserSyncRequest::endpointDetachRequests_t& detachRequests) {
        std::ostringstream stream;

        if (!detachRequests.is_null()) {
            const auto& container = detachRequests.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{id: " << it->requestId << ", epHash: " << it->endpointKeyHash << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string AttachEPResponsesToString(const UserSyncResponse::endpointAttachResponses_t& attachResponses) {
        std::ostringstream stream;

        if (!attachResponses.is_null()) {
            const auto& container = attachResponses.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{id: " << it->requestId << ", ";
                stream << "token: " << (it->endpointKeyHash.is_null() ? "null" : it->endpointKeyHash.get_string()) << ", ";
                stream << "type: " << SyncResponseResultTypeToString(it->result) << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string DetachEPResponsesToString(const UserSyncResponse::endpointDetachResponses_t& detachResponse) {
        std::ostringstream stream;

        if (!detachResponse.is_null()) {
            const auto& container = detachResponse.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{id: " << it->requestId << ", ";
                stream << "epHash: " << SyncResponseResultTypeToString(it->result) << "}";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string SyncResponseResultTypeToString(SyncResponseResultType type) {
        std::string description;

        switch (type) {
            case SyncResponseResultType::SUCCESS: description = "SUCCESS"; break;
            case SyncResponseResultType::FAILURE: description = "FAILURE"; break;
            case SyncResponseResultType::REDIRECT: description = "REDIRECT"; break;
            case SyncResponseResultType::PROFILE_RESYNC: description = "PROFILE_RESYNC"; break;
            default: description = "UNKNOWN"; break;
        }

        return description;
    }

    static std::string OutcomingEventsToString(const EventSyncRequest::events_t& events) {
        if (!events.is_null()) {
            return EventsToString(events.get_array());
        }
        static std::string null("null");
        return null;
    }

    static std::string IncomingEventsToString(const EventSyncResponse::events_t& events) {
        if (!events.is_null()) {
            return EventsToString(events.get_array());
        }
        static std::string null("null");
        return null;
    }

    static std::string EventListenersRequestToString(const EventSyncRequest::eventListenersRequests_t& request) {
        std::ostringstream stream;

        if (!request.is_null()) {
            const auto& container = request.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{requestId: " << it->requestId << ", fqn's: [";
                for (auto fqnIt = it->eventClassFQNs.begin(); fqnIt != it->eventClassFQNs.end(); ++fqnIt) {
                    stream << *fqnIt;
                    if ((fqnIt + 1) != it->eventClassFQNs.end()) { stream << ", "; }
                }
                stream << "]";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string EventListenersResponseToString(const EventSyncResponse::eventListenersResponses_t& response) {
        std::ostringstream stream;

        if (!response.is_null()) {
            const auto& container = response.get_array();
            stream << "[";
            for (auto it = container.begin(); it != container.end(); ++it) {
                stream << "{requestId: " << it->requestId << ", ";
                stream << "res: " << SyncResponseResultTypeToString(it->result) << ", ";
                stream << "listeners: [";

                if (!it->listeners.is_null()) {
                    const auto& listeners = it->listeners.get_array();

                    for (auto listenerIt = listeners.begin(); listenerIt != listeners.end(); ++listenerIt) {
                        stream << *listenerIt;
                        if ((listenerIt + 1) != listeners.end()) { stream << ", "; }
                    }
                }
                stream << "]";
                if ((it + 1) != container.end()) { stream << ", "; }
            }
            stream << "]";
        } else {
            stream << "null";
        }

        return stream.str();
    }

    static std::string EventSequenceNumberResponseToString(const EventSyncResponse::eventSequenceNumberResponse_t& response) {
        std::ostringstream ss;
        if (!response.is_null()) {
            ss << KVSTRING(sequenceNumber, response.get_EventSequenceNumberResponse().seqNum);
        } else {
            ss << "null";
        }
        return ss.str();
    }

    static std::string TransportTypeToString(TransportType type) {
        std::string description;

        switch (type) {
            case TransportType::BOOTSTRAP:     description = "BOOTSTRAP"; break;
            case TransportType::CONFIGURATION: description = "CONFIGURATION"; break;
            case TransportType::EVENT:         description = "EVENT"; break;
            case TransportType::LOGGING:       description = "LOGGING"; break;
            case TransportType::NOTIFICATION:  description = "NOTIFICATION"; break;
            case TransportType::PROFILE:       description = "PROFILE"; break;
            case TransportType::USER:          description = "USER"; break;
            default: description = "UNKNOWN"; break;
        }

        return description;
    }

    static std::string LogSyncRequestToString(const SyncRequest::logSyncRequest_t& logSyncRequest) {
        if (!logSyncRequest.is_null()) {
            const auto& request = logSyncRequest.get_LogSyncRequest();
            std::ostringstream stream;
            size_t entriesCount = request.logEntries.is_null() ? 0 : request.logEntries.get_array().size();
            stream << "{ requestId: " << request.requestId << ", logEntriesCount: " << entriesCount << "}";
            return stream.str();
        }
        static std::string null("null");
        return null;
    }

    static std::string LogSyncResponseToString(const SyncResponse::logSyncResponse_t& logSyncResponse) {
        if (!logSyncResponse.is_null()) {
            const auto& syncResponse = logSyncResponse.get_LogSyncResponse();
            if (!syncResponse.deliveryStatuses.is_null()) {
                const auto& deliveryStatuses = syncResponse.deliveryStatuses.get_array();
                std::ostringstream stream;
                for (size_t i = 0; i < deliveryStatuses.size(); ++i) {
                    if (i > 0) {
                        stream << ",";
                    }

                    stream << "{ requestId: " << deliveryStatuses[i].requestId
                           << ", result: " << SyncResponseResultTypeToString(deliveryStatuses[i].result)
                           << ", code: " << (deliveryStatuses[i].errorCode.is_null() ? "null" :
                           LogDeliveryErrorCodeToString(deliveryStatuses[i].errorCode.get_LogDeliveryErrorCode())) << "}";
                }
                return stream.str();
            }
        }
        static std::string null("null");
        return null;
    }

    static std::string LogDeliveryErrorCodeToString(LogDeliveryErrorCode code) {
        std::string result;
        switch (code) {
            case LogDeliveryErrorCode::NO_APPENDERS_CONFIGURED:
                result = "NO_APPENDERS_CONFIGURED";
                break;
            case LogDeliveryErrorCode::APPENDER_INTERNAL_ERROR:
                result = "APPENDER_INTERNAL_ERROR";
                break;
            case LogDeliveryErrorCode::REMOTE_CONNECTION_ERROR:
                result = "REMOTE_CONNECTION_ERROR";
                break;
            case LogDeliveryErrorCode::REMOTE_INTERNAL_ERROR:
                result = "REMOTE_INTERNAL_ERROR";
                break;
            default:
                result = "UNKNOWN";
                break;
        }
        return result;
    }

private:
    static std::string EventsToString(const std::vector<Event>& events) {
        std::ostringstream stream;
        stream << "[";
        for (auto it = events.begin(); it != events.end(); ++it) {
            stream << "{fqn: " << it->eventClassFQN << ", ";
            stream << "sn: " << it->seqNum << ", ";
            stream << "data_size: " << it->eventData.size() << ", ";
            stream << "source: " << (it->source.is_null() ? "null" : it->source.get_string()) << ", ";
            stream << "target: " << (it->target.is_null() ? "null" : it->target.get_string()) << "}";
            if ((it + 1) != events.end()) { stream << ", "; }
        }
        stream << "]";
        return stream.str();
    }
};

}  // namespace kaa


#endif /* LOGGINGUTILS_HPP_ */
