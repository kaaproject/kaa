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

#include <botan/botan.h>
#include <botan/base64.h>

#include "kaa/KaaDefaults.hpp"
#include "kaa/bootstrap/ServerInfo.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(ServerInfoTestSuite)

BOOST_AUTO_TEST_CASE(EmptyServerInfoTest)
{
    ServerInfo emptyInfo;

    const std::string& host = emptyInfo.getHost();

    BOOST_CHECK_MESSAGE(host.empty(), "Server host isn't empty - " + host);
    BOOST_CHECK_MESSAGE(!emptyInfo.isValid(), "Server host is valid - " + host);

    const auto& publicKey = emptyInfo.getPublicKey();
    BOOST_CHECK_MESSAGE(publicKey.empty(), "Public key isn't empty");
}

BOOST_AUTO_TEST_CASE(ServerInfoTest)
{
    const std::string host("192.168.77.2:9889");
    std::string encodedPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA211wMpVTtp1L6kg872SIk2rq1fcFl9LzTgqmq2T7fmLu6JluuB4vQ7Rbc3XZADipWe5EAMsSFQ3JuiwojuKQzN1hN9Jf+Y5/DMPYQYM3XKBd6h4qnQbnB51k5YHXnZy4g8+qU1br70g3IAZvc45WnkM5A9PQSDLEhrVeWNGuIeDWvR0OimcRz3z4FpdvZISfp639GBH6uj/fuYkstaoI/HsRmuESP4dcf38vr4OiJTmdZlr4nVydtYobqF3qa5hxwwmFZahXyGlvJc66GZd1S4SoqCtn3QI790Ybx1ntpc3yb/CgNPrGScSr/8R+k9JfaEOcVNa3DRQQnvc0Gx7ycQIDAQAB");
    ServerInfo serverInfo1(host, encodedPublicKey);

    PublicKey decodedPublicKey(Botan::base64_decode(encodedPublicKey));
    ServerInfo *serverInfo2 = new ServerInfo(host, decodedPublicKey);

    BOOST_CHECK_MESSAGE(serverInfo1.isValid(), "Server host is invalid - " + serverInfo1.getHost());
    BOOST_CHECK_MESSAGE(serverInfo1.getHost() == host, "Server host" + serverInfo1.getHost() +
                                                                 " isn't equal to " + host);
    BOOST_CHECK_MESSAGE(serverInfo1.getPublicKey() == serverInfo2->getPublicKey(), "Public keys aren't equal");
}

BOOST_AUTO_TEST_SUITE_END()

}
