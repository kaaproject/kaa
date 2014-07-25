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

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/test/unit_test.hpp>
#include <boost/bind.hpp>
#include "headers/update/UpdateManagerMock.hpp"
#include "headers/update/ExternalTransportManagerMock.hpp"
#include <kaa/update/UpdateCommand.hpp>
#include <kaa/update/RegisterCommand.hpp>
#include <kaa/update/PollCommand.hpp>
#include <kaa/update/LongPollCommand.hpp>
#include <kaa/update/ITransportExceptionHandler.hpp>

namespace kaa {

class ExceptionHandlerFake : public ITransportExceptionHandler {
public:
    ExceptionHandlerFake() : exceptionCaught_(false) { }
    ~ExceptionHandlerFake() { }
    virtual void onTransportException() { exceptionCaught_ = true; }
    void resetException() { exceptionCaught_ = false; }

    bool isExceptionCaught() const { return exceptionCaught_; }

private:
    bool exceptionCaught_;
};

class UpdateManagerFake : public UpdateManagerMock {
public:
    UpdateManagerFake() : responseReceived_(false) { }
    ~UpdateManagerFake() { }

    virtual void onSyncResponse(const SyncResponse& response)
    {
        responseReceived_ = true;
        response_ = response;
    }

    virtual KaaState getCurrentState() const
    {
        KaaState state;
        state.setApplicationSequenceNumber(0);
        state.setConfigurationHash(std::vector<boost::uint8_t>({ 'c', 'o', 'n', 'f' }));
        state.setProfileBody(std::vector<boost::uint8_t>({ 'p', 'r', 'o', 'f', 'i', 'l', 'e' }));
        state.setProfileHash(std::vector<boost::uint8_t>({ 'p', 'r', 'o', 'f' }));
        state.setPublicKey(std::vector<boost::uint8_t>({ 'k', 'e', 'y' }));
        state.setPublicKeyHash(std::vector<boost::uint8_t>({ 'k', 'e', 'y', 'h', 'a', 's', 'h' }));
        return state;
    }

    bool isResponseReceived() const { return responseReceived_; }
    SyncResponse getResponse() const { return response_; }

private:
    bool responseReceived_;
    SyncResponse response_;
};

class EndpointTransportFake : public ExternalTransportManagerMock {
public:
    EndpointTransportFake() : negative_(false) { }
    ~EndpointTransportFake() { }

    virtual void sendRegistrationCommand()
    {
        if (!negative_) {
            SyncResponse response;
            response.appStateSeqNumber = 1;
            callback_(response);
        } else {
            throw TransportException();
        }
    }

    virtual void sendProfileResync()
    {
        if (!negative_) {
            SyncResponse response;
            response.appStateSeqNumber = 2;
            callback_(response);
        } else {
            throw TransportException();
        }
    }

    virtual void sendPollCommand()
    {
        if (!negative_) {
            SyncResponse response;
            response.appStateSeqNumber = 3;
            callback_(response);
        } else {
            throw TransportException();
        }
    }

    virtual void sendLongPollCommand(boost::int64_t timeout)
    {
        if (!negative_) {
            SyncResponse response;
            response.appStateSeqNumber = 4;
            callback_(response);
        } else {
            throw TransportException();
        }
    }

    virtual void setOnOperationResponseCallback(boost::function<void (const SyncResponse&)> callback)
    {
        callback_ = callback;
    }

    virtual void close() {}

    void setNegative(bool negative) { negative_ = negative; }

private:
    bool negative_;
    boost::function<void (const SyncResponse&)> callback_;
};

void doTestCommand(
        boost::shared_ptr<EndpointTransportFake> transport,
        UpdateManagerFake& manager,
        boost::shared_ptr<ExceptionHandlerFake> handler, ICommand& cmd,
        boost::int32_t seqNum)
{
    transport->setNegative(true);
    cmd.run();
    BOOST_CHECK(handler->isExceptionCaught());
    BOOST_CHECK(!manager.isResponseReceived());

    transport->setNegative(false);
    handler->resetException();
    cmd.run();
    BOOST_CHECK(!handler->isExceptionCaught());
    BOOST_CHECK(manager.isResponseReceived());
    BOOST_CHECK_EQUAL(manager.getResponse().appStateSeqNumber, seqNum);
}

template<class T>
void testCommand(boost::int32_t seqNum)
{
    boost::shared_ptr<EndpointTransportFake> transport(new EndpointTransportFake);
    UpdateManagerFake manager;
    transport->setOnOperationResponseCallback(boost::bind(&UpdateManagerFake::onSyncResponse, &manager, _1));
    boost::shared_ptr<ExceptionHandlerFake> handler(new ExceptionHandlerFake);
    T cmd(transport, handler);
    doTestCommand(transport, manager, handler, cmd, seqNum);
}

BOOST_AUTO_TEST_SUITE(EndpointCommandsTest)

BOOST_AUTO_TEST_CASE(registerCommandTest)
{
    testCommand<RegisterCommand>(1);
}

BOOST_AUTO_TEST_CASE(updateCommandTest)
{
    testCommand<UpdateCommand>(2);
}

BOOST_AUTO_TEST_CASE(pollCommandTest)
{
    testCommand<PollCommand>(3);
}

BOOST_AUTO_TEST_CASE(longPollCommandTest)
{
    boost::shared_ptr<EndpointTransportFake> transport(new EndpointTransportFake);
    UpdateManagerFake manager;
    transport->setOnOperationResponseCallback(boost::bind(&UpdateManagerFake::onSyncResponse, &manager, _1));
    boost::shared_ptr<ExceptionHandlerFake> handler(new ExceptionHandlerFake);
    LongPollCommand cmd(transport, handler, 5000);
    doTestCommand(transport, manager, handler, cmd, 4);
}

BOOST_AUTO_TEST_SUITE_END()

}

