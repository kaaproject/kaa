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
#include <climits>
#include <sstream>

#include <cstdint>

#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/channel/server/AbstractServerInfo.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(AbstractServerInfoTestSuite)

BOOST_AUTO_TEST_CASE(BadServerInfoTest)
{
    /* Bad host */
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "", 55, "encoded_public_key"), KaaException);

    /* Bad port */
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "host", -1, "encoded_public_key"), KaaException);
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "host", USHRT_MAX + 1, "encoded_public_key"), KaaException);

    /* Bad encoded public key */
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "host", 443, ""), KaaException);

    /* Bad host/port */
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "host", ""), KaaException);
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "host:dsfds", ""), KaaException);
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "host:4546546", ""), KaaException);
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, ":", ""), KaaException);
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, ":55", ""), KaaException);

    PublicKey emptyDecodedKey;
    BOOST_CHECK_THROW(AbstractServerInfo<HTTP>(ServerType::OPERATIONS, "host", 55, emptyDecodedKey), KaaException);
}

BOOST_AUTO_TEST_CASE(CheckServerInfoTest)
{
    const ChannelType channelType = HTTP_LP;
    const ServerType serverType = ServerType::OPERATIONS;
    std::string host = "superhost.com";
    std::int32_t port = 7955;
    std::string encodedPublicKey = "rfewjkvalcxkjczxjvjfdlsfa";
    auto decodedPubKey = Botan::base64_decode(encodedPublicKey);

    AbstractServerInfo<channelType> info1(serverType, host, port, encodedPublicKey);

    BOOST_CHECK(info1.getChannelType() == channelType);
    BOOST_CHECK(info1.getServerType() == serverType);
    BOOST_CHECK(info1.getHost() == host);
    BOOST_CHECK(info1.getPort() == port);
    BOOST_CHECK(info1.getPublicKey() == decodedPubKey);

    AbstractServerInfo<channelType> info2(serverType, host, port, decodedPubKey);
    BOOST_CHECK(info2.getPublicKey() == decodedPubKey);

    std::ostringstream ss;
    ss << host << ":" << port;
    AbstractServerInfo<channelType> info3(serverType, ss.str(), encodedPublicKey);

    BOOST_CHECK(info3.getHost() == host);
    BOOST_CHECK(info3.getPort() == port);
    BOOST_CHECK(info3.getPublicKey() == decodedPubKey);
}

BOOST_AUTO_TEST_SUITE_END()

}
