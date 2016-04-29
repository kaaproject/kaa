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
#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/KaaTcpParser.hpp"
#include "kaa/kaatcp/KaaTcpResponseProcessor.hpp"
#include "kaa/kaatcp/KaaSyncRequest.hpp"
#include "kaa/kaatcp/ConnectMessage.hpp"
#include "kaa/kaatcp/PingRequest.hpp"
#include "kaa/kaatcp/DisconnectMessage.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/context/MockExecutorContext.hpp"

namespace kaa
{

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static MockExecutorContext tmpExecContext;
static KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);

BOOST_AUTO_TEST_SUITE(KaaTcpTest)

BOOST_AUTO_TEST_CASE(testCreateBasicHeader)
{
    char header[6];
    std::fill_n(header, 6, 0);
    BOOST_CHECK_EQUAL(2, KaaTcpCommon::createBasicHeader(0x01, 0, header));
    BOOST_CHECK_EQUAL(0x10, (std::uint8_t) header[0]);
    BOOST_CHECK_EQUAL(0, (std::uint8_t) header[1]);

    std::fill_n(header, 6, 0);
    BOOST_CHECK_EQUAL(2, KaaTcpCommon::createBasicHeader(0x01, 0x7F, header));
    BOOST_CHECK_EQUAL(0x10, (std::uint8_t) header[0]);
    BOOST_CHECK_EQUAL(0x7F, (std::uint8_t) header[1]);

    std::fill_n(header, 6, 0);
    BOOST_CHECK_EQUAL(3, KaaTcpCommon::createBasicHeader(0x01, 0xFF, header));
    BOOST_CHECK_EQUAL(0x10, (std::uint8_t) header[0]);
    BOOST_CHECK_EQUAL(0xFF, (std::uint8_t) header[1]);
    BOOST_CHECK_EQUAL(0x01, (std::uint8_t) header[2]);

    std::fill_n(header, 6, 0);
    BOOST_CHECK_EQUAL(4, KaaTcpCommon::createBasicHeader(0x01, 0xFFFF, header));
    BOOST_CHECK_EQUAL(0x10, (std::uint8_t) header[0]);
    BOOST_CHECK_EQUAL(0xFF, (std::uint8_t) header[1]);
    BOOST_CHECK_EQUAL(0xFF, (std::uint8_t) header[2]);
    BOOST_CHECK_EQUAL(0x03, (std::uint8_t) header[3]);

}

BOOST_AUTO_TEST_CASE(testTcpParser)
{
    char buffer[] = { 0x10, 0x01, 0x05 };
    KaaTcpParser parser(clientContext);
    BOOST_CHECK_THROW(parser.parseBuffer(buffer, 3), KaaException);

    parser.resetParser();

    unsigned char buffer2[] = { 0x20, 0xFF, 0xFF, 0x03 };
    parser.parseBuffer((const char*) buffer2, 4);
    const auto& messages2 = parser.releaseMessages();
    BOOST_CHECK_EQUAL(true, messages2.empty());
    BOOST_CHECK_EQUAL((std::uint8_t)  KaaTcpMessageType::MESSAGE_CONNACK, (std::uint8_t) parser.getCurrentMessageType());
    BOOST_CHECK_EQUAL(0xFFFF, parser.getCurrentPayloadLength());

    parser.resetParser();

    unsigned char buffer3[] = { 0x20, 0x00 };
    parser.parseBuffer((const char*) buffer3, 2);
    const auto& messages3 = parser.releaseMessages();
    BOOST_CHECK_EQUAL(false, messages3.empty());
    BOOST_CHECK_EQUAL((std::uint8_t)  KaaTcpMessageType::MESSAGE_CONNACK, (std::uint8_t) messages3.begin()->first);
    BOOST_CHECK_EQUAL(0, messages3.begin()->second.second);
    BOOST_CHECK_EQUAL((const char *) nullptr, messages3.begin()->second.first.get());

    unsigned char buffer4[] = { 0xE0, 0x02, 0x00, 0x02 };
    parser.parseBuffer((const char *)buffer4, 4);
    const auto& message4 = parser.releaseMessages();
    BOOST_CHECK_EQUAL(false, message4.empty());
    BOOST_CHECK_EQUAL((std::uint8_t)  KaaTcpMessageType::MESSAGE_DISCONNECT, (std::uint8_t) message4.begin()->first);
    BOOST_CHECK_EQUAL(2, message4.begin()->second.second);
    BOOST_CHECK_EQUAL(0, message4.begin()->second.first[0]);
    BOOST_CHECK_EQUAL(0x02, message4.begin()->second.first[1]);
}

class ResponseChecker
{
public:
    ResponseChecker() : isConnackReceived_(false), isPingResponseReceived_(false), isKaaSyncReceived_(false), isDisconnectReceived_(false) { }
    ~ResponseChecker() { }

    void onConnack(const ConnackMessage& message)
    {
        isConnackReceived_ = true;
    }

    void onPing()
    {
        isPingResponseReceived_ = true;
    }

    void onKaaSync(const KaaSyncResponse& response)
    {
        isKaaSyncReceived_ = true;
    }

    void onDisconnect(const DisconnectMessage& message)
    {
        isDisconnectReceived_ = true;
    }

    bool isConnackReceived() const { return isConnackReceived_; }
    bool isPingResponseReceived() const { return isPingResponseReceived_; }
    bool isKaaSyncReceived() const { return isKaaSyncReceived_; }
    bool isDisconnectReceived() const { return isDisconnectReceived_; }

private:
    bool isConnackReceived_;
    bool isPingResponseReceived_;
    bool isKaaSyncReceived_;
    bool isDisconnectReceived_;

};

BOOST_AUTO_TEST_CASE(testResponseProcessor)
{
    ResponseChecker checker;
    KaaTcpResponseProcessor processor(clientContext);
    unsigned char connackAndPingMessages[] = { 0x20, 0x02, 0x00, 0x03, 0xD0, 0x00 };
    processor.registerConnackReceiver(
            [&checker](const ConnackMessage& message)
            {
                BOOST_CHECK_EQUAL((std::uint8_t)ConnackReturnCode::REFUSE_ID_REJECT, (std::uint8_t)message.getReturnCode());
                checker.onConnack(message);
            });
    processor.registerPingResponseReceiver(
            [&checker]()
            {
                checker.onPing();
            });

    processor.processResponseBuffer((const char *)connackAndPingMessages, 6);

    BOOST_CHECK_EQUAL(true, checker.isPingResponseReceived());
    BOOST_CHECK_EQUAL(true, checker.isConnackReceived());

    unsigned char kaaSyncMessage[] = { 0xF0, 0x0D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x04, 0xFF };
    processor.registerKaaSyncReceiver(
            [&checker](const KaaSyncResponse& response)
            {
                BOOST_CHECK_EQUAL(false, response.isZipped());
                BOOST_CHECK_EQUAL(true, response.isEncrypted());
                BOOST_CHECK_EQUAL(0x05, response.getMessageId());
                BOOST_CHECK_EQUAL(1, response.getPayload().size());
                BOOST_CHECK_EQUAL(0xFF, response.getPayload()[0]);
                checker.onKaaSync(response);
            });
    processor.processResponseBuffer((const char *)kaaSyncMessage, 15);
    BOOST_CHECK_EQUAL(true, checker.isKaaSyncReceived());

    unsigned char disconnectMessage[] = { 0xE0, 0x02, 0x00, 0x01 };
    processor.registerDisconnectReceiver(
                [&checker](const DisconnectMessage& response)
                {
                    BOOST_CHECK_EQUAL((std::uint8_t) DisconnectReason::BAD_REQUEST, (std::uint8_t)response.getReason());
                    checker.onDisconnect(response);
                });
    processor.processResponseBuffer((const char *)disconnectMessage, 4);
    BOOST_CHECK_EQUAL(true, checker.isDisconnectReceived());
}

BOOST_AUTO_TEST_CASE(testKaaSyncRequest)
{
    KaaSyncRequest request(false, true, 0x07, std::vector<std::uint8_t>({ 0xFF }), KaaSyncMessageType::SYNC);
    unsigned char checkKaaSync[] = { 0xF0, 0x0D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x07, 0x15, 0xFF };
    const auto& rawMessage = request.getRawMessage();
    BOOST_CHECK_EQUAL(15, rawMessage.size());
    BOOST_CHECK_EQUAL_COLLECTIONS(checkKaaSync, checkKaaSync + 15, rawMessage.begin(), rawMessage.end());

}

BOOST_AUTO_TEST_CASE(testConnectMessage)
{
    Botan::secure_vector<std::uint8_t> signature(32);
    *(signature.end() - 1) = 0x01;

    Botan::secure_vector<std::uint8_t> sessionKey(16);
    *(sessionKey.end() - 1) = 0x02;

    std::string payload = { (char) 0xFF, 0x01, 0x02, 0x03 };

    ConnectMessage message(200, 0xf291f2d4, signature, sessionKey, payload);

    unsigned char checkConnectHeader[] = { 0x10, 0x46, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0xF2, 0x91, 0xF2, 0xD4, 0x11, 0x01, 0x00, 0xC8 };

    const auto& rawMessage = message.getRawMessage();
    BOOST_CHECK_EQUAL(72, rawMessage.size());

    BOOST_CHECK_EQUAL_COLLECTIONS(checkConnectHeader, checkConnectHeader + 20, rawMessage.begin(), rawMessage.begin() + 20);

    BOOST_CHECK_EQUAL_COLLECTIONS(sessionKey.begin(), sessionKey.end(), rawMessage.begin() + 20, rawMessage.begin() + 36);
    BOOST_CHECK_EQUAL_COLLECTIONS(signature.begin(), signature.end(), rawMessage.begin() + 36, rawMessage.begin() + 68);
    BOOST_CHECK_EQUAL_COLLECTIONS(
            reinterpret_cast<const std::uint8_t*>(payload.data()),
            reinterpret_cast<const std::uint8_t*>(payload.data() + payload.size()),
            rawMessage.begin() + 68,
            rawMessage.end());
}

BOOST_AUTO_TEST_CASE(testConnectMessageWithoutKey)
{
    std::string payload = { (char) 0xFF, 0x01, 0x02, 0x03 };

    ConnectMessage message(200, 0xf291f2d4, std::string(), std::string(), payload);

    unsigned char checkConnectHeader[] = { 0x10, 0x16, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0xF2, 0x91, 0xF2, 0xD4, 0x00, 0x00, 0x00, 0xC8 };

    const auto& rawMessage = message.getRawMessage();
    BOOST_CHECK_EQUAL(24, rawMessage.size());

    BOOST_CHECK_EQUAL_COLLECTIONS(checkConnectHeader, checkConnectHeader + 20, rawMessage.begin(), rawMessage.begin() + 20);

    BOOST_CHECK_EQUAL_COLLECTIONS(
            reinterpret_cast<const std::uint8_t*>(payload.data()),
            reinterpret_cast<const std::uint8_t*>(payload.data() + payload.size()),
            rawMessage.begin() + 20,
            rawMessage.end());
}

BOOST_AUTO_TEST_CASE(testPingRequest)
{
    PingRequest request;
    const auto& tcpHeader = request.getRawMessage();
    BOOST_CHECK_EQUAL(2, tcpHeader.size());
    BOOST_CHECK_EQUAL(0xC0, tcpHeader[0]);
    BOOST_CHECK_EQUAL(0, tcpHeader[1]);
}

BOOST_AUTO_TEST_CASE(testDisconnectRequest)
{
    DisconnectMessage request(DisconnectReason::BAD_REQUEST);
    const auto& tcpHeader = request.getRawMessage();
    BOOST_CHECK_EQUAL(4, tcpHeader.size());
    BOOST_CHECK_EQUAL(0xE0, tcpHeader[0]);
    BOOST_CHECK_EQUAL(0x02, tcpHeader[1]);
    BOOST_CHECK_EQUAL(0, tcpHeader[2]);
    BOOST_CHECK_EQUAL(0x01, tcpHeader[3]);

    unsigned char buffer[] = { 0xE0, 0x02, 0x00, 0x02 };
    DisconnectMessage response((const char *)buffer, 4);
    BOOST_CHECK_EQUAL((std::uint8_t)DisconnectReason::INTERNAL_ERROR, (std::uint8_t)response.getReason());
}


BOOST_AUTO_TEST_SUITE_END()

}
