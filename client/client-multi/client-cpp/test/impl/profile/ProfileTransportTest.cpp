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

#include <string>
#include <memory>

#include "kaa/profile/ProfileTransport.hpp"
#include "kaa/profile/ProfileManager.hpp"
#include "kaa/profile/DefaultProfileContainer.hpp"
#include "kaa/profile/gen/ProfileDefinitions.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"
#include "kaa/gen/EndpointGen.hpp"

#include "headers/channel/MockChannelManager.hpp"
#include "headers/context/MockExecutorContext.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(ProfileTransportTestSuite)

BOOST_AUTO_TEST_CASE(CreateClientSyncWhenEndpointIsUnregistered)
{
    DefaultLogger logger("client_id");
    KaaClientProperties properties;
    MockChannelManager channelManager;
    auto statePtr = std::make_shared<MockKaaClientStateStorage>();
    MockExecutorContext executor;
    KaaClientContext clientContext(properties, logger, executor, statePtr);
    PublicKey publicKey;

    KaaProfile profile;
    auto profileContainer = std::make_shared<kaa::DefaultProfileContainer>(profile);
    ProfileManager profileManager(clientContext);
    profileManager.setProfileContainer(profileContainer);

    ProfileTransport profileTransport(channelManager, publicKey, clientContext);
    profileTransport.setProfileManager(&profileManager);

    /*
     * Registered and profile hash mismatch.
     */
    statePtr->isRegistered_ = true;
    statePtr->isProfileResyncNeeded_ = false;
    BOOST_CHECK(profileTransport.createProfileRequest());

    /*
     * Registered and profile hash is equal to one that is stored.
     */
    statePtr->isRegistered_ = true;
    statePtr->isProfileResyncNeeded_ = false;
    statePtr->profileHash_ = EndpointObjectHash(profileManager.getSerializedProfile()).getHashDigest();
    BOOST_CHECK(!profileTransport.createProfileRequest());

    /*
     * Unregistered and profile hash is equal to one that is stored.
     */
    statePtr->isRegistered_ = false;
    statePtr->isProfileResyncNeeded_ = false;
    statePtr->profileHash_ = EndpointObjectHash(profileManager.getSerializedProfile()).getHashDigest();
    BOOST_CHECK(profileTransport.createProfileRequest());

    /*
     * PROFILE RESYNC IS NEEDED + Registered and profile hash is equal to one that is stored.
     */
    statePtr->isRegistered_ = false;
    statePtr->isProfileResyncNeeded_ = true;
    statePtr->profileHash_ = EndpointObjectHash(profileManager.getSerializedProfile()).getHashDigest();
    BOOST_CHECK(profileTransport.createProfileRequest());
}

BOOST_AUTO_TEST_CASE(ResyncResponseTest)
{
    DefaultLogger logger("client_id");
    KaaClientProperties properties;
    MockChannelManager channelManager;
    auto statePtr = std::make_shared<MockKaaClientStateStorage>();
    MockExecutorContext executor;
    KaaClientContext clientContext(properties, logger, executor, statePtr);
    PublicKey publicKey;
    ProfileManager profileManager(clientContext);

    ProfileTransport profileTransport(channelManager, publicKey, clientContext);
    profileTransport.setProfileManager(&profileManager);

    BOOST_CHECK(!statePtr->isProfileResyncNeeded_);

    ProfileSyncResponse response1;
    response1.responseStatus = SyncResponseStatus::NO_DELTA;
    profileTransport.onProfileResponse(response1);
    BOOST_CHECK(!statePtr->isProfileResyncNeeded_);

    ProfileSyncResponse response2;
    response2.responseStatus = SyncResponseStatus::RESYNC;
    profileTransport.onProfileResponse(response2);
    BOOST_CHECK(statePtr->isProfileResyncNeeded_);
}

BOOST_AUTO_TEST_SUITE_END()

}
