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

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/channel/SyncDataProcessor.hpp"
#include <kaa/utils/TimeUtils.hpp>

namespace kaa {

SyncDataProcessor::SyncDataProcessor(IMetaDataTransportPtr       metaDataTransport
                    , IBootstrapTransportPtr      bootstrapTransport
                    , IProfileTransportPtr        profileTransport
                    , IConfigurationTransportPtr  configurationTransport
                    , INotificationTransportPtr   notificationTransport
                    , IUserTransportPtr           userTransport
                    , IEventTransportPtr          eventTransport
                    , ILoggingTransportPtr        loggingTransport
                    , IRedirectionTransportPtr    redirectionTransport
                    , IKaaClientContext &context)
        : metaDataTransport_(metaDataTransport)
        , bootstrapTransport_(bootstrapTransport)
        , profileTransport_(profileTransport)
        , configurationTransport_(configurationTransport)
        , notificationTransport_(notificationTransport)
        , userTransport_(userTransport)
        , eventTransport_(eventTransport)
        , loggingTransport_(loggingTransport)
        , redirectionTransport_(redirectionTransport)
        , requestId(0)
        , context_(context)
{

}

std::vector<std::uint8_t> SyncDataProcessor::compileRequest(const std::map<TransportType, ChannelDirection>& transportTypes)
{
    SyncRequest request;

    request.requestId = ++requestId;
    request.bootstrapSyncRequest.set_null();
    request.configurationSyncRequest.set_null();
    request.eventSyncRequest.set_null();
    request.logSyncRequest.set_null();
    request.notificationSyncRequest.set_null();
    request.profileSyncRequest.set_null();
    request.userSyncRequest.set_null();

    KAA_LOG_DEBUG(boost::format("Compiling sync request. RequestId: %1%") % requestId);

    auto metaRequest = metaDataTransport_->createSyncRequestMetaData();
    request.syncRequestMetaData.set_SyncRequestMetaData(*metaRequest);
    KAA_LOG_DEBUG(boost::format("Compiled SyncRequestMetaData: %1%")
                        % LoggingUtils::toString(request.syncRequestMetaData));

    for (const auto& t : transportTypes) {
        bool isDownDirection = (t.second == ChannelDirection::DOWN);
        switch (t.first) {
            case TransportType::BOOTSTRAP :
                if (isDownDirection) {
                    request.bootstrapSyncRequest.set_null();
                } else if (bootstrapTransport_) {
                    auto ptr = bootstrapTransport_->createBootstrapSyncRequest();
                    if (ptr) {
                        request.bootstrapSyncRequest.set_BootstrapSyncRequest(*ptr);
                    } else {
                        request.bootstrapSyncRequest.set_null();
                    }
                } else {
                    KAA_LOG_WARN("Bootstrap transport was not specified.");
                }
                KAA_LOG_DEBUG(boost::format("Compiled BootstrapSyncRequest: %1%")
                    % LoggingUtils::toString(request.bootstrapSyncRequest));
            break;
            case TransportType::PROFILE :
                if (isDownDirection) {
                    request.profileSyncRequest.set_null();
                } else if (profileTransport_) {
                    auto ptr = profileTransport_->createProfileRequest();
                    if (ptr) {
                        request.profileSyncRequest.set_ProfileSyncRequest(*ptr);
                    } else {
                        request.profileSyncRequest.set_null();
                    }
                } else {
                    KAA_LOG_WARN("Profile transport was not specified.");
                }
                KAA_LOG_DEBUG(boost::format("Compiled ProfileSyncRequest: %1%")
                    % LoggingUtils::toString(request.profileSyncRequest));
                break;
            case TransportType::CONFIGURATION:
#ifdef KAA_USE_CONFIGURATION
                if (configurationTransport_) {
                    auto ptr = configurationTransport_->createConfigurationRequest();
                    if (ptr) {
                        request.configurationSyncRequest.set_ConfigurationSyncRequest(*ptr);
                    } else {
                        request.configurationSyncRequest.set_null();
                    }
                } else {
                    KAA_LOG_WARN("Configuration transport was not specified.");
                }
#endif
                KAA_LOG_DEBUG(boost::format("Compiled ConfigurationSyncRequest: %1%")
                   % LoggingUtils::toString(request.configurationSyncRequest));
                break;
            case TransportType::NOTIFICATION:
#ifdef KAA_USE_NOTIFICATIONS
                if (notificationTransport_) {
                    if (isDownDirection) {
                        request.notificationSyncRequest.set_NotificationSyncRequest(*notificationTransport_->createEmptyNotificationRequest());
                    } else {
                        auto ptr = notificationTransport_->createNotificationRequest();
                        if (ptr) {
                            request.notificationSyncRequest.set_NotificationSyncRequest(*ptr);
                        } else {
                            request.notificationSyncRequest.set_null();
                        }
                    }
                } else {
                    KAA_LOG_WARN("Notification transport was not specified.");
                }
#endif
                KAA_LOG_DEBUG(boost::format("Compiled NotificationSyncRequest: %1%")
                   % LoggingUtils::toString(request.notificationSyncRequest));
                break;
            case TransportType::USER:
#ifdef KAA_USE_EVENTS
                if (isDownDirection) {
                    UserSyncRequest user;
                    user.endpointAttachRequests.set_null();
                    user.endpointDetachRequests.set_null();
                    user.userAttachRequest.set_null();
                    request.userSyncRequest.set_UserSyncRequest(user);
                } else if (userTransport_) {
                    auto ptr = userTransport_->createUserRequest();
                    if (ptr) {
                        request.userSyncRequest.set_UserSyncRequest(*ptr);
                    } else {
                        request.userSyncRequest.set_null();
                    }
                } else {
                    KAA_LOG_WARN("User transport was not specified.");
                }
#endif
                KAA_LOG_DEBUG(boost::format("Compiled UserSyncRequest: %1%")
                    % LoggingUtils::toString(request.userSyncRequest));
                break;
            case TransportType::EVENT:
#ifdef KAA_USE_EVENTS
                if (isDownDirection) {
                    EventSyncRequest event;
                    event.eventListenersRequests.set_null();
                    event.events.set_null();
                    request.eventSyncRequest.set_EventSyncRequest(event);
                } else if (eventTransport_) {
                    auto ptr = eventTransport_->createEventRequest(requestId);
                    if (ptr) {
                        request.eventSyncRequest.set_EventSyncRequest(*ptr);
                    } else {
                        request.eventSyncRequest.set_null();
                    }
                } else {
                    KAA_LOG_WARN("Event transport was not specified.");
                }
#endif
                KAA_LOG_DEBUG(boost::format("Compiled EventSyncRequest: %1%")
                    % LoggingUtils::toString(request.eventSyncRequest));
                break;
            case TransportType::LOGGING:
#ifdef KAA_USE_LOGGING
                if (isDownDirection) {
                    LogSyncRequest log;
                    log.logEntries.set_null();
                    log.requestId = 0;
                    request.logSyncRequest.set_LogSyncRequest(log);
                } else if (loggingTransport_) {
                    auto ptr = loggingTransport_->createLogSyncRequest();
                    if (ptr) {
                        request.logSyncRequest.set_LogSyncRequest(*ptr);
                    } else {
                        request.logSyncRequest.set_null();
                    }
                } else {
                    KAA_LOG_WARN("Log upload transport was not specified.");
                }
#endif
                KAA_LOG_DEBUG(boost::format("Compiled LogSyncRequest: %1%")
                    % LoggingUtils::toString(request.logSyncRequest));
                break;
            default:
                break;
        }
    }

    std::vector<std::uint8_t> encodedData;
    requestConverter_.toByteArray(request, encodedData);

    return encodedData;
}

DemultiplexerReturnCode SyncDataProcessor::processResponse(const std::vector<std::uint8_t> &response)
{
    if (response.empty()) {
        return DemultiplexerReturnCode::FAILURE;
    }

    auto deliveryTime = TimeUtils::getCurrentTimeInMs();
    DemultiplexerReturnCode returnCode = DemultiplexerReturnCode::SUCCESS;

    try {
        SyncResponse syncResponse;
        responseConverter_.fromByteArray(response.data(), response.size(), syncResponse);

        KAA_LOG_INFO(boost::format("Got SyncResponse: requestId: %1%, result: %2%")
            % syncResponse.requestId % LoggingUtils::toString(syncResponse.status));

        KAA_LOG_DEBUG(boost::format("Got BootstrapSyncResponse: %1%")
            % LoggingUtils::toString(syncResponse.bootstrapSyncResponse));

        if (!syncResponse.bootstrapSyncResponse.is_null()) {
            if (bootstrapTransport_) {
                bootstrapTransport_->onBootstrapResponse(syncResponse.bootstrapSyncResponse.get_BootstrapSyncResponse());
            } else {
                KAA_LOG_ERROR("Got bootstrap sync response, but bootstrap transport was not set!");
            }
        }

        KAA_LOG_DEBUG(boost::format("Got ProfileSyncResponse: %1%")
            % LoggingUtils::toString(syncResponse.profileSyncResponse));

        if (!syncResponse.profileSyncResponse.is_null()) {
            if (profileTransport_) {
                profileTransport_->onProfileResponse(syncResponse.profileSyncResponse.get_ProfileSyncResponse());
            } else {
                KAA_LOG_ERROR("Got profile sync response, but profile transport was not set!");
            }
        }

        KAA_LOG_DEBUG(boost::format("Got ConfigurationSyncResponse: %1%")
                % LoggingUtils::toString(syncResponse.configurationSyncResponse));

#ifdef KAA_USE_CONFIGURATION
        if (!syncResponse.configurationSyncResponse.is_null()) {
            if (configurationTransport_) {
                configurationTransport_->onConfigurationResponse(syncResponse.configurationSyncResponse.get_ConfigurationSyncResponse());
            } else {
                KAA_LOG_ERROR("Got configuration sync response, but configuration transport was not set!");
            }
        }
#endif

        KAA_LOG_DEBUG(boost::format("Got EventSyncResponse: %1%")
                % LoggingUtils::toString(syncResponse.eventSyncResponse));

#ifdef KAA_USE_EVENTS
        if (eventTransport_) {
            eventTransport_->onSyncResponseId(syncResponse.requestId);
            if (!syncResponse.eventSyncResponse.is_null()) {
                    eventTransport_->onEventResponse(syncResponse.eventSyncResponse.get_EventSyncResponse());
            }
        } else {
            KAA_LOG_ERROR("Event transport was not set!");
        }
#endif

        KAA_LOG_DEBUG(boost::format("Got NotificationSyncResponse: %1%")
                % LoggingUtils::toString(syncResponse.notificationSyncResponse));

#ifdef KAA_USE_NOTIFICATIONS
        if (!syncResponse.notificationSyncResponse.is_null()) {
            if (notificationTransport_) {
                notificationTransport_->onNotificationResponse(syncResponse.notificationSyncResponse.get_NotificationSyncResponse());
            } else {
                KAA_LOG_ERROR("Got notification sync response, but notification transport was not set!");
            }
        }
#endif

        KAA_LOG_DEBUG(boost::format("Got UserSyncResponse: %1%")
                % LoggingUtils::toString(syncResponse.userSyncResponse));

#ifdef KAA_USE_EVENTS
        if (!syncResponse.userSyncResponse.is_null()) {
            if (userTransport_) {
                userTransport_->onUserResponse(syncResponse.userSyncResponse.get_UserSyncResponse());
            } else {
                KAA_LOG_ERROR("Got user sync response, but user transport was not set!");
            }
        }
#endif

        KAA_LOG_DEBUG(boost::format("Got LogSyncResponse: %1%")
                % LoggingUtils::toString(syncResponse.logSyncResponse));

#ifdef KAA_USE_LOGGING
        if (!syncResponse.logSyncResponse.is_null()) {
            if (loggingTransport_) {
                loggingTransport_->onLogSyncResponse(syncResponse.logSyncResponse.get_LogSyncResponse(), deliveryTime);
            } else {
                KAA_LOG_ERROR("Got log upload sync response, but logging transport was not set!");
            }
        }
#endif

        KAA_LOG_DEBUG(boost::format("Got RedirectSyncResponse: %1%")
                % LoggingUtils::toString(syncResponse.redirectSyncResponse));

        if (!syncResponse.redirectSyncResponse.is_null()) {
            if (redirectionTransport_) {
                redirectionTransport_->onRedirectionResponse(syncResponse.redirectSyncResponse.get_RedirectSyncResponse());
                returnCode = DemultiplexerReturnCode::REDIRECT;
            } else {
                KAA_LOG_ERROR("Got redirection sync response, but redirection transport was not set!");
            }
        }

        bool needProfileResync = (syncResponse.status == SyncResponseResultType::PROFILE_RESYNC);
        context_.getStatus().setProfileResyncNeeded(needProfileResync);

        if (needProfileResync) {
            if (profileTransport_) {
                KAA_LOG_INFO("Profile resync received");
                profileTransport_->sync();
            } else {
                KAA_LOG_ERROR("Got profile resync request, but profile transport was not set!");
            }
        }

        context_.getStatus().save();

        KAA_LOG_DEBUG("Processed SyncResponse");
    } catch (const std::exception& e) {
        KAA_LOG_ERROR(boost::format("Unable to process response: %s") % e.what());
        returnCode = DemultiplexerReturnCode::FAILURE;
    }
    return returnCode;
}

}  // namespace kaa

