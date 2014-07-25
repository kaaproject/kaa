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

#include <boost/test/unit_test.hpp>

#include <string>

#include "headers/profile/TestProfileContainer.hpp"
#include "headers/update/UpdateManagerMock.hpp"

#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/profile/ProfileListener.hpp"
#include "kaa/profile/SerializedProfileContainer.hpp"
#include "kaa/profile/ProfileManager.hpp"

#include "headers/gen/EndpointGen.hpp"

namespace kaa {

class ProfileUpdateManager : public UpdateManagerMock {
public:
    virtual void onProfileChanged(SharedDataBuffer serializedProfile) {
        AvroByteArrayConverter<BasicEndpointProfile> converter;
        SharedDataBuffer encodedProfile = converter.toByteArray(profile);

        BOOST_CHECK_MESSAGE(EndpointObjectHash::isEqual(encodedProfile, serializedProfile),
                "Serialized profiles are mismatched");
    }

public:
    BasicEndpointProfile profile;
};

BOOST_AUTO_TEST_SUITE(UpdateProfileSuite)

BOOST_AUTO_TEST_CASE(ProfileUpdateListener)
{
    ProfileUpdateManager updateManager;
    ProfileListenerPtr listener(new ProfileListener(updateManager));
    TestProfileContainer profileContainer;

    std::string profileBody("New big interesting body");
    updateManager.profile.profileBody = profileBody;

    profileContainer.setProfileListener(listener);
    profileContainer.setProfileBody(profileBody);
}

BOOST_AUTO_TEST_CASE(SerializedProfileContainer)
{
    std::string profileBody("Going to test Default Serialized Profile Container stuff");
    ProfileUpdateManager updateManager;
    boost::shared_ptr<TestProfileContainer> profileContainer(new TestProfileContainer);
    SerializedProfileContainer serializedProfileContainer;

    serializedProfileContainer.setProfileContainer(static_cast<ProfileContainerPtr>(profileContainer));
    updateManager.profile.profileBody = profileBody;
    profileContainer->setProfileBody(profileBody);
    updateManager.onProfileChanged(serializedProfileContainer.getSerializedProfile());
}

BOOST_AUTO_TEST_CASE(ProfileManager)
{
    std::string profileBody("Going to test Default Profile Manager stuff");
    ProfileUpdateManager updateManager;
    ProfileManager profileManager(updateManager);
    boost::shared_ptr<TestProfileContainer> profileContainer(new TestProfileContainer);

    updateManager.profile.profileBody = profileBody;
    profileManager.setProfileContainer(static_cast<ProfileContainerPtr>(profileContainer));

    /*
     * First test case (profile update via default listener)
     */
    profileContainer->setProfileBody(profileBody);

    /*
     * Second test case (explicit profile update)
     */
    updateManager.onProfileChanged(profileManager.getSerializedProfileContainer()->getSerializedProfile());
}

BOOST_AUTO_TEST_SUITE_END()

}
