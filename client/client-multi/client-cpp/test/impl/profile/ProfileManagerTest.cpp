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
#include <memory>

#include "kaa/profile/ProfileManager.hpp"
#include "kaa/profile/DefaultProfileContainer.hpp"
#include "kaa/profile/gen/ProfileDefinitions.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(ProfileManagerTestSuite)

BOOST_AUTO_TEST_CASE(ProfileManagerIsInitializedTest)
{
    ProfileManager profileManager;

#if KAA_PROFILE_SCHEMA_VERSION > 0
    BOOST_CHECK(!profileManager.isInitialized());
    profileManager.setProfileContainer(std::make_shared<DefaultProfileContainer>());
    BOOST_CHECK(profileManager.isInitialized());
#else
    BOOST_CHECK(profileManager.isInitialized());
#endif
}

BOOST_AUTO_TEST_SUITE_END()

}
