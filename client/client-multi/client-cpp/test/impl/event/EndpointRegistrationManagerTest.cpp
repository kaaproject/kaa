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

#include "kaa/event/registration/EndpointRegistrationManager.hpp"
#include "kaa/event/registration/IAttachStatusListener.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/channel/MockChannelManager.hpp"
#include "headers/event/registration/MockUserAttachCallback.hpp"
#include "headers/event/registration/MockAttachStatusListener.hpp"
#include "headers/event/registration/MockAttachEndpointCallback.hpp"
#include "headers/event/registration/MockDetachEndpointCallback.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(EndpointRegistrationSuite)

BOOST_AUTO_TEST_CASE(EmptyUserSyncRequestTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    BOOST_CHECK_MESSAGE(!registrationManager.getUserAttachRequest(), "User attach request should be empty");
    BOOST_CHECK_MESSAGE(registrationManager.getEndpointsToAttach().empty(), "EP attach request should be empty");
    BOOST_CHECK_MESSAGE(registrationManager.getEndpointsToDetach().empty(), "EP detach request should be empty");
}

BOOST_AUTO_TEST_CASE(BadUserCredentialsTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    std::string userExternalId = "userExternalId";
    std::string userAccessToken = "userAccessToken";
    std::string userVerifierToken = "userVerifierToken";

    if (!strlen(DEFAULT_USER_VERIFIER_TOKEN)) {
        BOOST_CHECK_THROW(registrationManager.attachUser(userExternalId, userAccessToken), KaaException);
    } else {
        BOOST_CHECK_THROW(registrationManager.attachUser("", userAccessToken), KaaException);
        BOOST_CHECK_THROW(registrationManager.attachUser(userExternalId, ""), KaaException);
    }

    BOOST_CHECK_THROW(registrationManager.attachUser("", userAccessToken, userVerifierToken), KaaException);
    BOOST_CHECK_THROW(registrationManager.attachUser(userExternalId, "", userVerifierToken), KaaException);
    BOOST_CHECK_THROW(registrationManager.attachUser(userExternalId, userAccessToken, ""), KaaException);
}

BOOST_AUTO_TEST_CASE(UserAttachRequestTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    MockChannelManager channelManager;
    UserTransport userTransport(registrationManager, channelManager);
    registrationManager.setTransport(&userTransport);

    std::unique_ptr<MockUserAttachCallback> resultListener(new MockUserAttachCallback);

    std::string userExternalId    = "userExternalId";
    std::string userAccessToken   = "userAccessToken";
    std::string userVerifierToken = "userVerifierToken";

    registrationManager.attachUser(userExternalId, userAccessToken, userVerifierToken, resultListener.get());

    BOOST_CHECK(registrationManager.getUserAttachRequest());
    BOOST_CHECK_EQUAL(registrationManager.getUserAttachRequest()->userExternalId, userExternalId);
    BOOST_CHECK_EQUAL(registrationManager.getUserAttachRequest()->userAccessToken, userAccessToken);
    BOOST_CHECK_EQUAL(registrationManager.getUserAttachRequest()->userVerifierId, userVerifierToken);
}


class PersistUserAttachCallback : public MockUserAttachCallback {
public:
    virtual void onAttachSuccess()
    {
        MockUserAttachCallback::onAttachSuccess();
    }

    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        MockUserAttachCallback::onAttachFailed(errorCode, reason);
        errorCode_ = errorCode;
        reason_ = reason;
    }

public:
    UserAttachErrorCode errorCode_;
    std::string reason_;
};

BOOST_AUTO_TEST_CASE(UserAttachResponseTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    MockChannelManager channelManager;
    UserTransport userTransport(registrationManager, channelManager);
    registrationManager.setTransport(&userTransport);

    PersistUserAttachCallback userAttachCallback;

    std::string userExternalId    = "userExternalId";
    std::string userAccessToken   = "userAccessToken";
    std::string userVerifierToken = "userVerifierToken";

    UserAttachResponse attachResponse1;
    attachResponse1.result = SUCCESS;

    /*
     * Firstly user attach request should be created, otherwise a response will be ignored.
     */
    registrationManager.attachUser(userExternalId, userAccessToken, userVerifierToken, &userAttachCallback);
    registrationManager.onUserAttach(attachResponse1);

    UserAttachErrorCode errorCode = UserAttachErrorCode::TOKEN_EXPIRED;
    std::string reason("some reason");

    UserAttachResponse attachResponse2;
    attachResponse2.result = FAILURE;
    attachResponse2.errorCode.set_UserAttachErrorCode(errorCode);
    attachResponse2.errorReason.set_string(reason);

    /*
     * Firstly user attach request should be created, otherwise a response will be ignored.
     */
    registrationManager.attachUser(userExternalId, userAccessToken, userVerifierToken, &userAttachCallback);
    registrationManager.onUserAttach(attachResponse2);

    BOOST_CHECK_EQUAL(userAttachCallback.on_attach_success_count, 1);
    BOOST_CHECK_EQUAL(userAttachCallback.on_attach_failed_count, 1);

    BOOST_CHECK_EQUAL(userAttachCallback.errorCode_, errorCode);
    BOOST_CHECK_EQUAL(userAttachCallback.reason_, reason);
}



class PersistAttachStatusStorage : public MockKaaClientStateStorage {
public:
    virtual bool getEndpointAttachStatus() const { return attachedStatus_; }
    virtual void setEndpointAttachStatus(bool status) { attachedStatus_ = status; }

public:
    bool attachedStatus_ = false;
};

class PersistAttachStatusListener : public MockAttachStatusListener {
public:
    virtual void onAttach(const std::string& userExternalId, const std::string& endpointAccessToken)
    {
        MockAttachStatusListener::onAttach(userExternalId, endpointAccessToken);
        userExternalId_ = userExternalId;
        attachEndpointAccessToken_ = endpointAccessToken;
    }

    virtual void onDetach(const std::string& endpointAccessToken)
    {
        MockAttachStatusListener::onDetach(endpointAccessToken);
        detachEndpointAccessToken_ = endpointAccessToken;
    }

public:
    std::string userExternalId_;
    std::string attachEndpointAccessToken_;
    std::string detachEndpointAccessToken_;
};

BOOST_AUTO_TEST_CASE(AttachStatusUpdatedTest)
{
    IKaaClientStateStoragePtr status(new PersistAttachStatusStorage);
    EndpointRegistrationManager registrationManager(status);

    MockChannelManager channelManager;
    UserTransport userTransport(registrationManager, channelManager);
    registrationManager.setTransport(&userTransport);

    PersistAttachStatusListener attachStatusListener;
    registrationManager.setAttachStatusListener(&attachStatusListener);

    BOOST_CHECK(!registrationManager.isAttachedToUser());

    UserAttachNotification attachNotification;
    attachNotification.userExternalId = "id";
    attachNotification.endpointAccessToken = "attach token";

    registrationManager.onCurrentEndpointAttach(attachNotification);

    BOOST_CHECK(registrationManager.isAttachedToUser());

    UserDetachNotification detachNotification;
    detachNotification.endpointAccessToken = "detach token";

    registrationManager.onCurrentEndpointDetach(detachNotification);

    BOOST_CHECK(!registrationManager.isAttachedToUser());

    BOOST_CHECK_EQUAL(attachStatusListener.on_attach_count, 1);
    BOOST_CHECK_EQUAL(attachStatusListener.on_detach_count, 1);

    BOOST_CHECK_EQUAL(attachStatusListener.userExternalId_, attachNotification.userExternalId);
    BOOST_CHECK_EQUAL(attachStatusListener.attachEndpointAccessToken_, attachNotification.endpointAccessToken);
    BOOST_CHECK_EQUAL(attachStatusListener.detachEndpointAccessToken_, detachNotification.endpointAccessToken);
}

BOOST_AUTO_TEST_CASE(BadCredentialsOfAttachEndpointTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    MockChannelManager channelManager;
    UserTransport userTransport(registrationManager, channelManager);
    registrationManager.setTransport(&userTransport);

    BOOST_CHECK_THROW(registrationManager.attachEndpoint(""), KaaException);
}



class PersistAttachEndpointCallback : public MockAttachEndpointCallback {
public:
    virtual void onAttachSuccess(const std::string& endpointKeyHash)
    {
        MockAttachEndpointCallback::onAttachSuccess(endpointKeyHash);
        endpointKeyHash_ = endpointKeyHash;
    }

public:
    std::string endpointKeyHash_;
};

static EndpointAttachResponse constructEndpointAttachResponse(SyncResponseResultType type
                                                            , std::int32_t requstId
                                                            , const std::string& hash = "")
{
    EndpointAttachResponse response;
    response.requestId = requstId;
    response.result = type;

    if (hash.empty()) {
        response.endpointKeyHash.set_null();
    } else {
        response.endpointKeyHash.set_string(hash);
    }

    return response;
}

static std::int32_t getRequestId(const std::string& accessToken, const std::unordered_map<std::int32_t, std::string>& requests)
{
    for (const auto& request : requests) {
        if (accessToken == request.second) {
            return request.first;
        }
    }

    return -1;
}

BOOST_AUTO_TEST_CASE(AttachAnotherEndpointTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    MockChannelManager channelManager;
    UserTransport userTransport(registrationManager, channelManager);
    registrationManager.setTransport(&userTransport);

    BOOST_CHECK(registrationManager.getEndpointsToAttach().empty());

    PersistAttachEndpointCallback attachEndpointCallback1;
    PersistAttachEndpointCallback attachEndpointCallback2;

    size_t requestCount = 0;

    std::string targetEndpointAccessToken1 = "some id 1";
    std::string targetEndpointKeyHash1 = "some key hash 1";

    std::string targetEndpointAccessToken2 = "some id 2";
    std::string targetEndpointKeyHash2 = "some key hash 2";

    registrationManager.attachEndpoint(targetEndpointAccessToken1, &attachEndpointCallback1);
    ++requestCount;
    registrationManager.attachEndpoint(targetEndpointAccessToken2, &attachEndpointCallback2);
    ++requestCount;

    auto attachRequests = registrationManager.getEndpointsToAttach();

    BOOST_CHECK_EQUAL(attachRequests.size(), requestCount);

    std::vector<EndpointAttachResponse> responses{
            constructEndpointAttachResponse(SyncResponseResultType::SUCCESS
                                          , getRequestId(targetEndpointAccessToken1, attachRequests)
                                          , targetEndpointKeyHash1),
            constructEndpointAttachResponse(SyncResponseResultType::FAILURE
                                          , getRequestId(targetEndpointAccessToken2, attachRequests)
                                          , targetEndpointKeyHash2)};

    registrationManager.onEndpointsAttach(responses);

    BOOST_CHECK_EQUAL(attachEndpointCallback1.on_attach_success_count, 1);
    BOOST_CHECK_EQUAL(attachEndpointCallback1.on_attach_failed_count, 0);
    BOOST_CHECK_EQUAL(attachEndpointCallback1.endpointKeyHash_, targetEndpointKeyHash1);

    BOOST_CHECK_EQUAL(attachEndpointCallback2.on_attach_success_count, 0);
    BOOST_CHECK_EQUAL(attachEndpointCallback2.on_attach_failed_count, 1);
    BOOST_CHECK_EQUAL(attachEndpointCallback2.endpointKeyHash_, "");
}

BOOST_AUTO_TEST_CASE(BadCredentialsOfDetachEndpointTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    MockChannelManager channelManager;
    UserTransport userTransport(registrationManager, channelManager);
    registrationManager.setTransport(&userTransport);

    BOOST_CHECK_THROW(registrationManager.detachEndpoint(""), KaaException);
}

static EndpointDetachResponse constructEndpointDetachResponse(SyncResponseResultType type
                                                            , std::int32_t requstId)
{
    EndpointDetachResponse response;
    response.requestId = requstId;
    response.result = type;

    return response;
}

BOOST_AUTO_TEST_CASE(DetachAnotherEndpointTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    EndpointRegistrationManager registrationManager(status);

    MockChannelManager channelManager;
    UserTransport userTransport(registrationManager, channelManager);
    registrationManager.setTransport(&userTransport);

    BOOST_CHECK(registrationManager.getEndpointsToAttach().empty());

    MockDetachEndpointCallback detachEndpointCallback1;
    MockDetachEndpointCallback detachEndpointCallback2;

    size_t requestCount = 0;

    std::string targetEndpointKeyHash1 = "some key hash 1";
    std::string targetEndpointKeyHash2 = "some key hash 2";

    registrationManager.detachEndpoint(targetEndpointKeyHash1, &detachEndpointCallback1);
    ++requestCount;
    registrationManager.detachEndpoint(targetEndpointKeyHash2, &detachEndpointCallback2);
    ++requestCount;

    auto detachRequests = registrationManager.getEndpointsToDetach();

    BOOST_CHECK_EQUAL(detachRequests.size(), requestCount);

    std::vector<EndpointDetachResponse> responses{
            constructEndpointDetachResponse(SyncResponseResultType::SUCCESS
                                          , getRequestId(targetEndpointKeyHash1, detachRequests)),
            constructEndpointDetachResponse(SyncResponseResultType::FAILURE
                                          , getRequestId(targetEndpointKeyHash2, detachRequests))};

    registrationManager.onEndpointsDetach(responses);

    BOOST_CHECK_EQUAL(detachEndpointCallback1.on_detach_success_count, 1);
    BOOST_CHECK_EQUAL(detachEndpointCallback1.on_detach_failed_count, 0);

    BOOST_CHECK_EQUAL(detachEndpointCallback2.on_detach_success_count, 0);
    BOOST_CHECK_EQUAL(detachEndpointCallback2.on_detach_failed_count, 1);
}

BOOST_AUTO_TEST_SUITE_END()

}
