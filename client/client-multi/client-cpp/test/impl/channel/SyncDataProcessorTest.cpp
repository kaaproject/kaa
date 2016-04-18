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

#include <boost/test/unit_test.hpp>

#include <memory>

#include <kaa/channel/SyncDataProcessor.hpp>

#include "headers/channel/transport/MockProfileTransport.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/channel/MockChannelManager.hpp"
#include "headers/context/MockExecutorContext.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

namespace kaa {

SyncResponse createEmptySuccessSyncResponse(std::int32_t requestId = 0)
{
    SyncResponse syncResponse;
    syncResponse.requestId = requestId;
    syncResponse.status = SyncResponseResultType::SUCCESS;
    syncResponse.bootstrapSyncResponse.set_null();
    syncResponse.profileSyncResponse.set_null();
    syncResponse.configurationSyncResponse.set_null();
    syncResponse.notificationSyncResponse.set_null();
    syncResponse.userSyncResponse.set_null();
    syncResponse.eventSyncResponse.set_null();
    syncResponse.redirectSyncResponse.set_null();
    syncResponse.logSyncResponse.set_null();
    syncResponse.extensionSyncResponses.set_null();

    return syncResponse;
}

BOOST_AUTO_TEST_SUITE(SyncDataProcessorTestSuite)

BOOST_AUTO_TEST_CASE(GlobalProfileResyncTest)
{
    DefaultLogger logger("client_id");
    KaaClientProperties properties;
    MockChannelManager channelManager;
    auto statePtr = std::make_shared<MockKaaClientStateStorage>();
    MockExecutorContext executor;
    KaaClientContext clientContext(properties, logger, executor, statePtr);

    auto profileTransportMock = std::make_shared<MockProfileTransport>();

    SyncDataProcessor syncDataProcessor(IMetaDataTransportPtr()
                                      , IBootstrapTransportPtr()
                                      , profileTransportMock
                                      , IConfigurationTransportPtr()
                                      , INotificationTransportPtr()
                                      , IUserTransportPtr()
                                      , IEventTransportPtr()
                                      , ILoggingTransportPtr()
                                      , IRedirectionTransportPtr()
                                      , clientContext);

    auto syncResponse = createEmptySuccessSyncResponse();
    syncResponse.status = SyncResponseResultType::PROFILE_RESYNC;

    std::vector<std::uint8_t> serializedResponse;
    AvroByteArrayConverter<SyncResponse> responseConverter;
    responseConverter.toByteArray(syncResponse, serializedResponse);
    syncDataProcessor.processResponse(serializedResponse);

    BOOST_CHECK_EQUAL(profileTransportMock->onSync_, 1);
    BOOST_CHECK_EQUAL(profileTransportMock->onProfileResponse_, 0);
    BOOST_CHECK_EQUAL(statePtr->onSetProfileResyncNeeded_, 1);
    BOOST_CHECK(statePtr->isProfileResyncNeeded_);
}

BOOST_AUTO_TEST_SUITE_END()

}
;
