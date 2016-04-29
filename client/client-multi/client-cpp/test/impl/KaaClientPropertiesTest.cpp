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

#include "kaa/KaaClientProperties.hpp"

#if __GNUC__
    #define FILE_SEPARATOR '/'
#else
    #define FILE_SEPARATOR '\\'
#endif

namespace kaa {

BOOST_AUTO_TEST_SUITE(KaaClientPropertiesTestSuite)

BOOST_AUTO_TEST_CASE(DefaultPropertiesTest)
{
    KaaClientProperties properties;

    BOOST_CHECK_EQUAL(properties.getWorkingDirectoryPath(), KaaClientProperties::DEFAULT_WORKING_DIR);
    BOOST_CHECK_EQUAL(properties.getStateFileName(), KaaClientProperties::DEFAULT_WORKING_DIR + KaaClientProperties::DEFAULT_STATE_FILE);
    BOOST_CHECK_EQUAL(properties.getPublicKeyFileName(), KaaClientProperties::DEFAULT_WORKING_DIR + KaaClientProperties::DEFAULT_PUB_KEY_FILE);
    BOOST_CHECK_EQUAL(properties.getPrivateKeyFileName(), KaaClientProperties::DEFAULT_WORKING_DIR + KaaClientProperties::DEFAULT_PRIV_KEY_FILE);
    BOOST_CHECK_EQUAL(properties.getLogsDatabaseFileName(), KaaClientProperties::DEFAULT_WORKING_DIR + KaaClientProperties::DEFAULT_LOGS_DB);
    BOOST_CHECK_EQUAL(properties.getConfigurationFileName(), KaaClientProperties::DEFAULT_WORKING_DIR + KaaClientProperties::DEFAULT_CONF_FILE);
}

BOOST_AUTO_TEST_CASE(SetWorkingDirTest)
{
    KaaClientProperties properties;

    std::string newWorkingPath = "/home/kaa/client-cpp/test";

    properties.setWorkingDirectoryPath(newWorkingPath);
    BOOST_CHECK(properties.getWorkingDirectoryPath() != newWorkingPath);

    newWorkingPath.push_back(FILE_SEPARATOR);
    BOOST_CHECK_EQUAL(properties.getWorkingDirectoryPath(), newWorkingPath);

    BOOST_CHECK_EQUAL(properties.getStateFileName(), newWorkingPath + KaaClientProperties::DEFAULT_STATE_FILE);
    BOOST_CHECK_EQUAL(properties.getPublicKeyFileName(), newWorkingPath + KaaClientProperties::DEFAULT_PUB_KEY_FILE);
    BOOST_CHECK_EQUAL(properties.getPrivateKeyFileName(), newWorkingPath + KaaClientProperties::DEFAULT_PRIV_KEY_FILE);
    BOOST_CHECK_EQUAL(properties.getLogsDatabaseFileName(), newWorkingPath + KaaClientProperties::DEFAULT_LOGS_DB);
    BOOST_CHECK_EQUAL(properties.getConfigurationFileName(), newWorkingPath + KaaClientProperties::DEFAULT_CONF_FILE);
}

BOOST_AUTO_TEST_CASE(SetStateFileNameTest)
{
    KaaClientProperties properties;

    const std::string newStateFileName = "test_state_file.txt";
    properties.setStateFileName(newStateFileName);

    BOOST_CHECK_EQUAL(properties.getStateFileName(), properties.getWorkingDirectoryPath() + newStateFileName);
}

BOOST_AUTO_TEST_CASE(SetPublicKeyFileNameTest)
{
    KaaClientProperties properties;

    const std::string newPublicKeyFileName = "test_public_key_file.txt";
    properties.setPublicKeyFileName(newPublicKeyFileName);

    BOOST_CHECK_EQUAL(properties.getPublicKeyFileName(), properties.getWorkingDirectoryPath() + newPublicKeyFileName);
}

BOOST_AUTO_TEST_CASE(SetPrivateKeyFileNameTest)
{
    KaaClientProperties properties;

    const std::string newPrivateKeyFileName = "test_private_key_file.txt";
    properties.setPrivateKeyFileName(newPrivateKeyFileName);

    BOOST_CHECK_EQUAL(properties.getPrivateKeyFileName(), properties.getWorkingDirectoryPath() + newPrivateKeyFileName);
}

BOOST_AUTO_TEST_CASE(SetLogsDatabaseFileNameTest)
{
    KaaClientProperties properties;

    const std::string newLogsDatabaseFileName = "test_logs_database_file.txt";
    properties.setLogsDatabaseFileName(newLogsDatabaseFileName);

    BOOST_CHECK_EQUAL(properties.getLogsDatabaseFileName(), properties.getWorkingDirectoryPath() + newLogsDatabaseFileName);
}

BOOST_AUTO_TEST_CASE(SetConfigurationFileNameTest)
{
    KaaClientProperties properties;

    const std::string newConfigurationFileName = "test_configuration_file.txt";
    properties.setConfigurationFileName(newConfigurationFileName);

    BOOST_CHECK_EQUAL(properties.getConfigurationFileName(), properties.getWorkingDirectoryPath() + newConfigurationFileName);
}

BOOST_AUTO_TEST_CASE(SetArbitraryPropertyTest)
{
    KaaClientProperties properties;

    const std::string propertyName = "RandomProperty";
    const std::string propertyValue = "RandomPropertyValue";

    properties.setProperty(propertyName, propertyValue);

    BOOST_CHECK_EQUAL(properties.getProperty(propertyName), propertyValue);
}

BOOST_AUTO_TEST_CASE(GetUnknownPropertyTest)
{
    KaaClientProperties properties;

    const std::string unknownPropertyName = "UnknownProperty";
    const std::string defaultPropertyValue = "DefaultValue";

    BOOST_CHECK_EQUAL(properties.getProperty(unknownPropertyName, defaultPropertyValue), defaultPropertyValue);
}

BOOST_AUTO_TEST_SUITE_END()

} /* namespace kaa */
