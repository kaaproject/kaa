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

#include <cstdio>
#include <cstring>

#include "kaa/security/KeyUtils.hpp"

#ifndef RESOURCE_DIR
#error "No path to resources defined!"
#endif

namespace kaa {

BOOST_AUTO_TEST_SUITE(KeyUtilsTestSuite)

BOOST_AUTO_TEST_CASE(SaveLoadKeyPairTest)
{
    const size_t keyLength = 2048;
    const std::string newPublicKeyPath = RESOURCE_DIR"/key.new_public";
    const std::string newPrivateKeyPath = RESOURCE_DIR"/key.new_private";

    auto generatedKeyPair = KeyUtils().generateKeyPair(keyLength);

    KeyUtils::saveKeyPair(generatedKeyPair, newPublicKeyPath, newPrivateKeyPath);
    auto loadedKeyPair = KeyUtils::loadKeyPair(newPublicKeyPath, newPrivateKeyPath);

    BOOST_CHECK_MESSAGE(generatedKeyPair.first == loadedKeyPair.first
                                    , "Loaded and saved public key don't match");
    BOOST_CHECK_MESSAGE(generatedKeyPair.second == loadedKeyPair.second
                                    , "Loaded and saved private key don't match");

    std::remove(newPublicKeyPath.c_str());
    std::remove(newPrivateKeyPath.c_str());
}

BOOST_AUTO_TEST_SUITE_END()

}
