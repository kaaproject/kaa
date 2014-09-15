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

#include "kaa/ClientStatus.hpp"
#include "kaa/KaaDefaults.hpp"

#include <map>
#include <fstream>
#include <sstream>
#include <utility>

#ifdef RESOURCE_DIR
const char * const filename = RESOURCE_DIR"/kaa_status.file";
#else
const char * const filename = "kaa_status.file";
#endif

void cleanfile()
{
    std::ofstream of(filename);
    of.close();
}

namespace kaa {

BOOST_AUTO_TEST_SUITE(ClientStatusSuite);

BOOST_AUTO_TEST_CASE(checkDefaults)
{
    cleanfile();
    ClientStatus cs(filename);
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().configurationSequenceNumber, 0);
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().notificationSequenceNumber, 0);
    BOOST_CHECK_EQUAL(cs.isRegistered(), false);
    BOOST_CHECK_EQUAL(cs.getProfileHash().second, 0);
    BOOST_CHECK_EQUAL(cs.getTopicStates().size(), 0);
    BOOST_CHECK_EQUAL(cs.getAttachedEndpoints().size(), 0);
    BOOST_CHECK_EQUAL(cs.getEndpointAccessToken().empty(), true);
    BOOST_CHECK_EQUAL(cs.getEndpointAttachStatus(), false);
}

BOOST_AUTO_TEST_CASE(checkSetAndSaveParameters)
{
    cleanfile();
    ClientStatus cs(filename);
    cs.setAppSeqNumber({1,2,3});
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().configurationSequenceNumber, 1);
    BOOST_CHECK_EQUAL(cs.getAppSeqNumber().notificationSequenceNumber, 2);
    cs.setRegistered(true);
    BOOST_CHECK_EQUAL(cs.isRegistered(), true);

    SharedDataBuffer sdb;
    sdb.first.reset(new boost::uint8_t[5]);
    boost::uint8_t *temp = sdb.first.get();
    temp[0] = 1;
    temp[1] = 2;
    temp[2] = 2;
    temp[3] = 3;
    temp[4] = 4;

    sdb.second = 5;
    cs.setProfileHash(sdb);

    BOOST_CHECK_EQUAL(cs.getProfileHash().second, 5);
    BOOST_CHECK_EQUAL_COLLECTIONS(cs.getProfileHash().first.get(), cs.getProfileHash().first.get() + 5, temp, temp + 5);

    DetailedTopicStates empty_ts = cs.getTopicStates();
    BOOST_CHECK_EQUAL(empty_ts.size(), 0);

    DetailedTopicStates ts;
    DetailedTopicState ts1;
    ts1.topicId = "topic1";
    ts1.sequenceNumber = 100;
    ts1.topicName = "topicName1";
    ts1.subscriptionType = SubscriptionType::MANDATORY;

    DetailedTopicState ts2;
    ts2.topicId = "topic2";
    ts2.sequenceNumber = 200;
    ts2.topicName = "topicName2";
    ts2.subscriptionType = SubscriptionType::VOLUNTARY;

    ts.insert(std::make_pair(ts1.topicId, ts1));
    ts.insert(std::make_pair(ts2.topicId, ts2));

    cs.setTopicStates(ts);
    DetailedTopicStates act_ts = cs.getTopicStates();
    BOOST_CHECK_EQUAL(act_ts.size(), 2);

    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].topicId, ts1.topicId);
    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].sequenceNumber, ts1.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].topicName, ts1.topicName);
    BOOST_CHECK_EQUAL(act_ts[ts1.topicId].subscriptionType, ts1.subscriptionType);

    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].topicId, ts2.topicId);
    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].sequenceNumber, ts2.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].topicName, ts2.topicName);
    BOOST_CHECK_EQUAL(act_ts[ts2.topicId].subscriptionType, ts2.subscriptionType);

    AttachedEndpoints attachedEndpoints;
    std::string token1("Token1"), hash1("hash1");
    std::string token2("Token2"), hash2("hash2");

    attachedEndpoints.insert(std::make_pair(token1, hash1));
    attachedEndpoints.insert(std::make_pair(token2, hash2));

    cs.setAttachedEndpoints(attachedEndpoints);

    std::string endpointAccessToken;

    bool isAttached = true;
    cs.setEndpointAttachStatus(isAttached);

    std::string endpointKeyHash = "thisEndpointKeyHash";
    cs.setEndpointKeyHash(endpointKeyHash);

    cs.save();
    ClientStatus cs_restored(filename);

    DetailedTopicStates act_ts1 = cs_restored.getTopicStates();
    BOOST_CHECK_EQUAL(act_ts1.size(), 2);

    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].topicId, ts1.topicId);
    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].sequenceNumber, ts1.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].topicName, ts1.topicName);
    BOOST_CHECK_EQUAL(act_ts1[ts1.topicId].subscriptionType, ts1.subscriptionType);

    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].topicId, ts2.topicId);
    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].sequenceNumber, ts2.sequenceNumber);
    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].topicName, ts2.topicName);
    BOOST_CHECK_EQUAL(act_ts1[ts2.topicId].subscriptionType, ts2.subscriptionType);

    AttachedEndpoints restoredAttachedEndpoints = cs.getAttachedEndpoints();
    BOOST_CHECK_EQUAL(restoredAttachedEndpoints[token1], hash1);
    BOOST_CHECK_EQUAL(restoredAttachedEndpoints[token2], hash2);

    BOOST_CHECK_EQUAL(cs_restored.getEndpointAccessToken(), endpointAccessToken);

    BOOST_CHECK_EQUAL(cs_restored.getEndpointAttachStatus(), isAttached);
    BOOST_CHECK_EQUAL(cs_restored.getEndpointKeyHash(), endpointKeyHash);
}

}  // namespace kaa

BOOST_AUTO_TEST_SUITE_END()

