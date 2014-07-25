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
#include <vector>

#include "kaa/ClientStatus.hpp"
#include "kaa/event/registration/EndpointRegistrationManager.hpp"
#include "kaa/event/registration/IEndpointAttachStatusListener.hpp"

#include "headers/channel/MockChannelManager.hpp"

namespace kaa {

class TestEndpointAttachStatusListener : public IEndpointAttachStatusListener {
public:
    TestEndpointAttachStatusListener()
        : isAttached_(false), isDetached_(false) {}

    virtual void onAttachSuccess(const std::string& userExternalId, const std::string& endpointAccessToken) {
        isAttached_ = true;
    }

    virtual void onAttachFailure() {}

    virtual void onDetachSuccess(const std::string& endpointAccessToken) {
        isDetached_ = true;
    }

    virtual void onDetachFailure() {}

public:
    bool isAttached_;
    bool isDetached_;
};

BOOST_AUTO_TEST_SUITE(EndpointRegistrationSuite)

BOOST_AUTO_TEST_CASE(NullEPRequest)
{
    IKaaClientStateStoragePtr status(new ClientStatus("fake.txt"));
    EndpointRegistrationManager registrationManager(status);

    BOOST_CHECK_MESSAGE(registrationManager.getUserAttachRequest().get() == nullptr
            , "User attach request should be empty");
    BOOST_CHECK_MESSAGE(registrationManager.getEndpointsToAttach().size() == 0
            , "EP attach request should be empty");
    BOOST_CHECK_MESSAGE(registrationManager.getEndpointsToDetach().size() == 0
            , "EP detach request should be empty");
}

BOOST_AUTO_TEST_CASE(UserAttachTest)
{
    IKaaClientStateStoragePtr status(new ClientStatus("fake.txt"));
    EndpointRegistrationManager registrationManager(status);

    TestEndpointAttachStatusListener* resultListener = new TestEndpointAttachStatusListener;

    std::string userExternalId = "externalId";
    std::string userAccessToken = "token";

    registrationManager.attachUser(userExternalId, userAccessToken, resultListener);

    BOOST_CHECK(registrationManager.getUserAttachRequest().get() != nullptr);
    BOOST_CHECK(registrationManager.getUserAttachRequest()->userExternalId == userExternalId);
    BOOST_CHECK(registrationManager.getUserAttachRequest()->userAccessToken == userAccessToken);

    UserSyncResponse userSyncResponse;

    UserAttachResponse attachResponse;
    attachResponse.result = SUCCESS;
    userSyncResponse.userAttachResponse.set_UserAttachResponse(attachResponse);

    registrationManager.onUserAttach(userSyncResponse.userAttachResponse);

    registrationManager.attachUser(userExternalId, userAccessToken, resultListener);

    attachResponse.result = FAILURE;
    userSyncResponse.userAttachResponse.set_UserAttachResponse(attachResponse);

    registrationManager.onUserAttach(userSyncResponse.userAttachResponse);

    BOOST_CHECK(resultListener->isAttached_);
    BOOST_CHECK(resultListener->isDetached_);

    delete resultListener;
}

BOOST_AUTO_TEST_CASE(FilledEPRequest)
{
    IKaaClientStateStoragePtr status(new ClientStatus("fake.txt"));
    EndpointRegistrationManager registrationManager(status);

    std::string userId("Big ID");
    std::string userToken("Big user's token");

    registrationManager.attachUser(userId, userToken);

    std::string epToken1("Token1");
    std::string epToken2("Token2");

    registrationManager.attachEndpoint(epToken1);
    registrationManager.attachEndpoint(epToken2);

    std::string detachHash("hash");

    registrationManager.detachEndpoint(detachHash);

    UserAttachRequestPtr userAttachRequest = registrationManager.getUserAttachRequest();
    auto endpointsToAttach = registrationManager.getEndpointsToAttach();
    auto endpointsToDetach = registrationManager.getEndpointsToDetach();
    BOOST_CHECK_MESSAGE(userAttachRequest.get() != nullptr
                        , "User attach request should be not empty");
    BOOST_CHECK_MESSAGE(endpointsToAttach.size() == 2
                        , "EP attach request should be not empty");
    BOOST_CHECK_MESSAGE(endpointsToDetach.size() == 1
                                    , "EP detach request is empty");

    UserSyncResponse response;

    EndpointAttachResponse eap1;
    std::string ep1Hash("Token1Hash");
    eap1.requestId = endpointsToAttach.begin()->first;
    eap1.result = SyncResponseResultType::SUCCESS;
    eap1.endpointKeyHash.set_string(ep1Hash);

    EndpointAttachResponse eap2;
    std::string ep2Hash("Token2Hash");
    eap2.requestId = (++endpointsToAttach.begin())->first;
    eap2.result = SyncResponseResultType::FAILURE;
    eap2.endpointKeyHash.set_null();

    std::vector<EndpointAttachResponse> epAtResp = { eap1, eap2 };

    EndpointDetachResponse edr1;
    edr1.requestId = endpointsToDetach.begin()->first;
    edr1.result = SyncResponseResultType::SUCCESS;

    std::vector<EndpointDetachResponse> epDetResp = { edr1 };

    response.endpointAttachResponses.set_array(epAtResp);
    response.endpointDetachResponses.set_array(epDetResp);

    registrationManager.onUserAttach(response.userAttachResponse);
    registrationManager.onEndpointsAttach(response.endpointAttachResponses.get_array());
    registrationManager.onEndpointsDetach(response.endpointDetachResponses.get_array());

    auto attachedEPs = registrationManager.getAttachedEndpoints();

    BOOST_CHECK_MESSAGE(attachedEPs.size() == 1, "Unexpected attached EP response");
    BOOST_CHECK_MESSAGE(attachedEPs.begin()->second == ep1Hash, "Unexpected attached EP");

    registrationManager.detachEndpoint(ep1Hash);
    auto detachMap = registrationManager.getEndpointsToDetach();

    BOOST_CHECK_MESSAGE(detachMap.begin()->second == ep1Hash
            , "EP detach request is empty");
}

BOOST_AUTO_TEST_CASE(AttachStatusUpdatedTest)
{
    MockChannelManager channelManager;
    IKaaClientStateStoragePtr status(new ClientStatus("fake.txt"));
    EndpointRegistrationManager registrationManager(status);
    UserTransport transport(registrationManager, channelManager);
    TestEndpointAttachStatusListener statusListener;

    registrationManager.setTransport(&transport);
    registrationManager.setAttachStatusListener(&statusListener);

    BOOST_CHECK(!registrationManager.isCurrentEndpointAttached());

    registrationManager.attachUser("id", "token");

    UserAttachResponse attachResponse;
    attachResponse.result = SyncResponseResultType::SUCCESS;

    UserSyncResponse syncResp1;
    syncResp1.userAttachResponse.set_UserAttachResponse(attachResponse);
    syncResp1.endpointAttachResponses.set_null();
    syncResp1.endpointDetachResponses.set_null();
    syncResp1.userAttachNotification.set_null();
    syncResp1.userDetachNotification.set_null();

    transport.onUserResponse(syncResp1);

    BOOST_CHECK(registrationManager.isCurrentEndpointAttached());
    BOOST_CHECK(statusListener.isAttached_);

    UserDetachNotification detachNf;
    detachNf.endpointAccessToken = "token";

    UserSyncResponse syncResp2;
    syncResp2.userAttachResponse.set_null();
    syncResp2.endpointAttachResponses.set_null();
    syncResp2.endpointDetachResponses.set_null();
    syncResp2.userAttachNotification.set_null();
    syncResp2.userDetachNotification.set_UserDetachNotification(detachNf);

    transport.onUserResponse(syncResp2);

    BOOST_CHECK(!registrationManager.isCurrentEndpointAttached());
    BOOST_CHECK(statusListener.isDetached_);

    UserAttachNotification attachNf;
    attachNf.userExternalId = "id";
    attachNf.endpointAccessToken = "token";

    UserSyncResponse syncResp3;
    syncResp3.userAttachNotification.set_UserAttachNotification(attachNf);
    syncResp3.userAttachResponse.set_null();
    syncResp3.endpointAttachResponses.set_null();
    syncResp3.endpointDetachResponses.set_null();
    syncResp3.userDetachNotification.set_null();

    transport.onUserResponse(syncResp3);

    BOOST_CHECK(registrationManager.isCurrentEndpointAttached());
}

BOOST_AUTO_TEST_SUITE_END()

}
