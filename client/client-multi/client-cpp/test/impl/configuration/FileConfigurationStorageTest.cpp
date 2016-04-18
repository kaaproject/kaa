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


#include "kaa/configuration/storage/FileConfigurationStorage.hpp"

#include <boost/test/unit_test.hpp>

namespace kaa {


BOOST_AUTO_TEST_SUITE(FileConfigurationStorageSuite)

BOOST_AUTO_TEST_CASE(fileStorageTest)
{
    const std::uint8_t testData[] = { 't', 'e', 's', 't' };
    FileConfigurationStorage storage("configuration.bin");

    storage.saveConfiguration(std::vector<std::uint8_t>(testData, testData + 4));

    auto result = storage.loadConfiguration();

    BOOST_CHECK_EQUAL_COLLECTIONS(result.begin(), result.end(), testData, testData + 4);
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa



