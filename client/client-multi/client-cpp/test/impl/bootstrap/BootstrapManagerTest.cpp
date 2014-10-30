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

#include <memory>
#include <functional>

#include "kaa/KaaDefaults.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/bootstrap/BootstrapManager.hpp"
#include "headers/update/ExternalTransportManagerMock.hpp"

namespace kaa {

class BootstrapTransportMock : public ExternalTransportManagerMock
{
public:
    virtual void sendResolve() {
        OperationsServer server1;

        server1.DNSName.assign("test.com");
        server1.Priority = 2;
        server1.PublicKey = { 0x44, 0x33, 0x22 };

        OperationsServer server2;

        server2.DNSName.assign("last.bootstrap.com");
        server2.Priority = 1;
        server2.PublicKey = { 0x44, 0x33, 0x22 };

        OperationsServer server3;

        server3.DNSName.assign("main.bootstrap.com");
        server3.Priority = 3;
        server3.PublicKey = { 0x44, 0x33, 0x22 };

        static OperationsServerList list;
        list.operationsServerArray = { server1, server2, server3 };

        if (callback_) {
            callback_(list);
        }
    }

    virtual void setOnBootstrapResponseCallback(std::function<void (const OperationsServerList&)> callback) {
        callback_ = callback;
    }

    virtual void setOperationServer(const ServerInfo& serverInfo)
    {
        lastServer_ = serverInfo;
    }

    ServerInfo getLastServerInfo() const { return lastServer_; }

private:
    std::function<void (const OperationsServerList&)> callback_;
    ServerInfo lastServer_;
};

BOOST_AUTO_TEST_SUITE(BootstrapManagerTestSuite)

BOOST_AUTO_TEST_CASE(EmptyEndpointServerListTest)
{
    BootstrapManager manager;
    IExternalTransportManagerPtr transport(new BootstrapTransportMock);

    BOOST_CHECK_THROW(manager.useNextOperationServer(), KaaException);
    BOOST_CHECK_THROW(manager.useOperationServerByName("fake"), KaaException);

    /*
     * Added fake creation to check branch coverage quality
     */
    BootstrapTransportMock fake;
    fake.sendResolve();
}

BOOST_AUTO_TEST_CASE(EndpointServerResolvingTest)
{
    BootstrapManager manager;
    std::shared_ptr<BootstrapTransportMock> transport(new BootstrapTransportMock);
    transport->setOnBootstrapResponseCallback(std::bind(&BootstrapManager::onResolveResponse, &manager, std::placeholders::_1));

    BOOST_CHECK_THROW(manager.useNextOperationServer(), KaaException);
    BOOST_CHECK_THROW(manager.useOperationServerByName("fake"), KaaException);

    manager.setTransport(static_cast<IExternalTransportManagerPtr>(transport));
    manager.useNextOperationServer();
    ServerInfo info = transport->getLastServerInfo();
    BOOST_CHECK_EQUAL(info.getHost(), "main.bootstrap.com");

    manager.useOperationServerByName("test.com");
    info = transport->getLastServerInfo();
    BOOST_CHECK_EQUAL(info.getHost(), "test.com");

    manager.useNextOperationServer();
    info = transport->getLastServerInfo();
    BOOST_CHECK_EQUAL(info.getHost(), "test.com");

    manager.useNextOperationServer();
    info = transport->getLastServerInfo();
    BOOST_CHECK_EQUAL(info.getHost(), "last.bootstrap.com");

    manager.useNextOperationServer();
    info = transport->getLastServerInfo();
    BOOST_CHECK_EQUAL(info.getHost(), "main.bootstrap.com");

    BOOST_CHECK_MESSAGE(manager.getEndpointServerList().at(2).Priority == 1
                                        , "Endpoint server priority isn't kept");
}

BOOST_AUTO_TEST_SUITE_END()

}
