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
#include <fstream>
#include <cstdio>
#include <cstring>

#include "kaa/security/KeyUtils.hpp"

#ifndef RESOURCE_DIR
#error "No path to resources defined!"
#endif

namespace kaa {

struct KeyUtilsFixture
{
    KeyUtilsFixture()
        :newPublicKeyPath{RESOURCE_DIR"/key.new_public"}
        ,newPrivateKeyPath{RESOURCE_DIR"/key.new_private"}
        ,keyUtils{}
        ,generatedKeyPair{keyUtils.generateKeyPair(keyLength)}
    {

    }

    ~KeyUtilsFixture()
    {
        std::remove(newPublicKeyPath.c_str());
        std::remove(newPrivateKeyPath.c_str());
    }

    void saveKeyPairToFiles()
    {
        KeyUtils::saveKeyPair(generatedKeyPair, newPublicKeyPath, newPrivateKeyPath);
    }

    KeyPair loadKeyPairsFromFiles()
    {
        return KeyUtils::loadKeyPair(newPublicKeyPath, newPrivateKeyPath);
    }

    static constexpr size_t keyLength = 2048;

    const std::string newPublicKeyPath;
    const std::string newPrivateKeyPath;

    KeyUtils keyUtils;
    KeyPair  generatedKeyPair;
};

BOOST_AUTO_TEST_SUITE(KeyUtilsTestSuite)

BOOST_FIXTURE_TEST_CASE(GeneratedKeysAreValid, KeyUtilsFixture)
{
    BOOST_CHECK(keyUtils.checkKeyPair(generatedKeyPair));
}

BOOST_FIXTURE_TEST_CASE(CorruptedPrivateKeyIsNotValid, KeyUtilsFixture)
{
    saveKeyPairToFiles();

    std::fstream priv(newPrivateKeyPath.c_str(),
                      std::ios::out | std::ios::in);

    // Corrupt private key somewhere in the middle of the file
    priv.seekp(50);
    priv << "TheAnswerToTheUltimateQuestionOfLifeTheUniverseAndEverythingIs42\n";
    priv.close();

    auto loadedKeyPair = loadKeyPairsFromFiles();
    BOOST_CHECK(!keyUtils.checkKeyPair(loadedKeyPair));
}

BOOST_FIXTURE_TEST_CASE(CorruptedPublicKeyIsNotValid, KeyUtilsFixture)
{
    saveKeyPairToFiles();

    std::fstream pub(newPublicKeyPath.c_str(),
                     std::ios::out | std::ios::in);

    // Corrupt public key somewhere in the middle of the file
    pub.seekp(20);
    pub << 42;
    pub.close();

    auto loadedKeyPair = loadKeyPairsFromFiles();
    BOOST_CHECK(!keyUtils.checkKeyPair(loadedKeyPair));
}

BOOST_FIXTURE_TEST_CASE(SaveLoadKeyPairTest, KeyUtilsFixture)
{
    saveKeyPairToFiles();
    auto loadedKeyPair = loadKeyPairsFromFiles();

    BOOST_CHECK_MESSAGE(generatedKeyPair.getPublicKey() == loadedKeyPair.getPublicKey()
                                    , "Loaded and saved public key don't match");
    BOOST_CHECK_MESSAGE(generatedKeyPair.getPrivateKey() == loadedKeyPair.getPrivateKey()
                                    , "Loaded and saved private key don't match");

    BOOST_CHECK(keyUtils.checkKeyPair(loadedKeyPair));
}

BOOST_AUTO_TEST_SUITE_END()


}
