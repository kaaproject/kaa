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

#include "kaa/ClientStatus.hpp"
#include "kaa/KaaDefaults.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/context/MockExecutorContext.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

#include <map>
#include <cstdlib>
#include <fstream>
#include <sstream>
#include <utility>
#include <memory>
#include <string>

#ifdef RESOURCE_DIR
const char * const directory = RESOURCE_DIR;
#else
const char *const directory ="./";
#endif
const char * const filename = "kaa_status.file";


void cleanfile() {
  std::remove(std::string(std::string(directory) + std::string("/") + std::string(filename)).c_str());
}

namespace kaa {

static MockExecutorContext context;
static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());

BOOST_AUTO_TEST_SUITE(ClientStatusSuite);

BOOST_AUTO_TEST_CASE(checkDefaults)
{
    cleanfile();
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    properties.setStateFileName(filename);
    properties.setWorkingDirectoryPath(directory);
    KaaClientContext clientContext(properties, tmp_logger, context, stateMock);

    ClientStatus cs(clientContext);
    BOOST_CHECK_EQUAL(cs.isRegistered(), false);
    BOOST_CHECK_EQUAL(cs.getProfileHash().empty(), true);
    BOOST_CHECK_EQUAL(cs.getAttachedEndpoints().size(), 0);
    BOOST_CHECK_EQUAL(cs.getEndpointAccessToken().empty(), false);
    BOOST_CHECK_EQUAL(cs.getEndpointAttachStatus(), false);
    BOOST_CHECK_EQUAL(cs.isProfileResyncNeeded(), false);

    cleanfile();
}

BOOST_AUTO_TEST_CASE(checkSetAndSaveParameters)
{
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    properties.setStateFileName(filename);
    properties.setWorkingDirectoryPath(directory);
    KaaClientContext clientContext(properties, tmp_logger, context, stateMock);

    const bool isRegisteredExpected = true;
    const bool isProfileResyncNeededExpected = true;

    ClientStatus cs(clientContext);

    cs.setRegistered(isRegisteredExpected);
    BOOST_CHECK_EQUAL(cs.isRegistered(), isRegisteredExpected);

    cs.setProfileResyncNeeded(isProfileResyncNeededExpected);
    BOOST_CHECK_EQUAL(cs.isProfileResyncNeeded(), isProfileResyncNeededExpected);

    HashDigest sdb;
    for (uint8_t i = 0; i < 5; ++i) {
        sdb.push_back(i);
    }
    cs.setProfileHash(sdb);
    auto checkHash = cs.getProfileHash();
    BOOST_CHECK_EQUAL_COLLECTIONS(checkHash.begin(), checkHash.end(), sdb.begin(), sdb.end());

    Topics empty_ts = cs.getTopicList();
    BOOST_CHECK_EQUAL(empty_ts.size(), 0);

    Topics ts;
    Topic ts1;
    std::int64_t topic1 = 0x01;
    ts1.id = topic1;
    ts1.name = "name1";
    ts1.subscriptionType = SubscriptionType::MANDATORY_SUBSCRIPTION;

    Topic ts2;
    std::int64_t topic2 = 0x02;
    ts2.id = topic2;
    ts2.name = "name2";
    ts2.subscriptionType = SubscriptionType::MANDATORY_SUBSCRIPTION;

    ts.push_back(ts1);
    ts.push_back(ts2);

    cs.setTopicList(ts);
    auto act_ts = cs.getTopicStates();
    auto topicList = cs.getTopicList();
    BOOST_CHECK_EQUAL(topicList.size(), 2);

    BOOST_CHECK_EQUAL(topicList[0].name, ts1.name);
    BOOST_CHECK_EQUAL(topicList[0].subscriptionType, ts1.subscriptionType);

    BOOST_CHECK_EQUAL(topicList[1].name, ts2.name);
    BOOST_CHECK_EQUAL(topicList[1].subscriptionType, ts2.subscriptionType);

    AttachedEndpoints attachedEndpoints;
    std::string token1("Token1"), hash1("hash1");
    std::string token2("Token2"), hash2("hash2");

    attachedEndpoints.insert(std::make_pair(token1, hash1));
    attachedEndpoints.insert(std::make_pair(token2, hash2));

    cs.setAttachedEndpoints(attachedEndpoints);

    bool isAttached = true;
    cs.setEndpointAttachStatus(isAttached);

    std::string endpointKeyHash = "thisEndpointKeyHash";
    cs.setEndpointKeyHash(endpointKeyHash);

    cs.save();

    ClientStatus cs_restored(clientContext);

    auto topicList2 = cs_restored.getTopicList();
    auto topicStates = cs_restored.getTopicStates();

    BOOST_CHECK_EQUAL(topicList2.size(), 2);

    BOOST_CHECK_EQUAL(topicList2[0].name, ts1.name);
    BOOST_CHECK_EQUAL(topicList2[0].subscriptionType, ts1.subscriptionType);

    BOOST_CHECK_EQUAL(topicList2[1].name, ts2.name);
    BOOST_CHECK_EQUAL(topicList2[1].subscriptionType, ts2.subscriptionType);

    AttachedEndpoints restoredAttachedEndpoints = cs.getAttachedEndpoints();
    BOOST_CHECK_EQUAL(restoredAttachedEndpoints[token1], hash1);
    BOOST_CHECK_EQUAL(restoredAttachedEndpoints[token2], hash2);

    BOOST_CHECK_EQUAL(cs_restored.getEndpointAttachStatus(), isAttached);
    BOOST_CHECK_EQUAL(cs_restored.getEndpointKeyHash(), endpointKeyHash);

    BOOST_CHECK_EQUAL(cs_restored.isRegistered(), isRegisteredExpected);
    BOOST_CHECK_EQUAL(cs_restored.isProfileResyncNeeded(), isProfileResyncNeededExpected);

    cleanfile();
}

}  // namespace kaa

BOOST_AUTO_TEST_SUITE_END()

