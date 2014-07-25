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

#include <kaa/transport/HttpExternalTransportManager.hpp>
#include <kaa/transport/IKaaDataChannel.hpp>
#include <kaa/security/KeyUtils.hpp>
#include <kaa/http/HttpResponse.hpp>

#include "headers/update/RequestFactoryMock.hpp"
#include "headers/security/EncoderDecoderStub.hpp"

namespace kaa {

HttpDataProcessorPtr httpDataProcessor;
boost::shared_ptr<EncoderDecoderStub> encDec;

class FakeRequestFactory : public RequestFactoryMock {
public:
    FakeRequestFactory() { }
    ~FakeRequestFactory() { }

    virtual SyncRequest createSyncRequest()
    {
        SyncRequest s;
        s.appStateSeqNumber = 5;
        return s;
    }

    virtual LongSyncRequest createLongSyncRequest(boost::int64_t timeout)
    {
        return LongSyncRequest();
    }

    virtual ProfileUpdateRequest createProfileUpdateRequest()
    {
        return ProfileUpdateRequest();
    }

    virtual EndpointRegistrationRequest createEndpointRegistrationRequest()
    {
        return EndpointRegistrationRequest();
    }
};

class FakeDataChannel : public IKaaDataChannel {
public:
    FakeDataChannel(HttpExternalTransportManager& manager) : manager_(manager){ }
    ~FakeDataChannel() { }

    virtual void sendData(const ServerInfo& serverInfo, const std::vector<boost::int8_t>& data)
    {
        BOOST_CHECK_EQUAL("localhost:9889", serverInfo.getHost());

        SyncResponse response;
        response.appStateSeqNumber = 123;
        response.confSyncResponse.set_null();
        response.notificationSyncResponse.set_null();
        response.redirectSyncResponse.set_null();
        response.responseType = SyncResponseStatus::DELTA;

        AvroByteArrayConverter<SyncResponse> converter;
        SharedDataBuffer buffer = converter.toByteArray(response);

        std::stringstream ss;
        ss << "HTTP/1.1\t200\r\nX-SIGNATURE: \r\n";
        ss << "Content-Length: " << buffer.second << "\r\n\r\n";
        for (size_t i = 0; i < buffer.second; ++i) {
            ss << (unsigned char) buffer.first[i];
        }
        ss << "\r\n\r\n";

        const auto& httpResponse = ss.str();
        manager_.onResponse(reinterpret_cast<const boost::int8_t *>(httpResponse.c_str()), httpResponse.length());
    }

private:
    HttpExternalTransportManager& manager_;

};

void responseCallback(const SyncResponse& response)
{
    BOOST_CHECK_EQUAL(response.appStateSeqNumber, 123);
}

BOOST_AUTO_TEST_SUITE(HttpExternalTransportManagerSuite)

BOOST_AUTO_TEST_CASE(sendSyncTest)
{
    HttpExternalTransportManager manager;
    manager.setOnOperationResponseCallback(&responseCallback);

    const auto& keys = KeyUtils().generateKeyPair(2048);
    manager.setClientKeyPair(keys);

    boost::scoped_ptr<IKaaDataChannel> dataChannel(new FakeDataChannel(manager));
    manager.setDataChannel(dataChannel.get());

    boost::shared_ptr<IRequestFactory> factory(new FakeRequestFactory());
    manager.setRequestFactory(factory);

    ServerInfo server("localhost:9889", keys.first);
    manager.setOperationServer(server);

    encDec.reset(new EncoderDecoderStub());
    encDec->setSignatureVerified(true);

    httpDataProcessor.reset(new HttpDataProcessor());
    httpDataProcessor->setEncoderDecoder(encDec);
    manager.setHttpDataProcessor(httpDataProcessor);

    manager.sendPollCommand();
}

BOOST_AUTO_TEST_SUITE_END()

}
