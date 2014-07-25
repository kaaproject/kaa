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

#ifndef EXTERNALTRANSPORTMANAGERMOCK_HPP_
#define EXTERNALTRANSPORTMANAGERMOCK_HPP_

#include "kaa/transport/IExternalTransportManager.hpp"

namespace kaa {

class ExternalTransportManagerMock : public IExternalTransportManager {
public:
    virtual void setDataChannel(IKaaDataChannel *dataChannel) {}
    virtual void sendRegistrationCommand() {}
    virtual void sendPollCommand() {}
    virtual void sendLongPollCommand(boost::int64_t timeout) {}
    virtual void sendProfileResync() {}
    virtual void sendResolve() {}

    virtual void setOperationServer(const ServerInfo& serverInfo) {}
    virtual void setBootstrapServer(const ServerInfo& serverInfo) {}
    virtual void setRequestFactory(IRequestFactoryPtr factory) {}
    virtual void setClientKeyPair(const KeyPair& clientKeys) {}
    virtual void setOnOperationResponseCallback(boost::function<void (const SyncResponse&)> callback) {}
    virtual void setOnBootstrapResponseCallback(boost::function<void (const OperationsServerList&)> callback) {}

    virtual void onResponse(const boost::int8_t* data, size_t size) {}
    virtual void onResponse(const std::vector<boost::int8_t>& data) {}

    virtual void closeChannel() {}
};

} /* namespace kaa */

#endif /* EXTERNALTRANSPORTMANAGERMOCK_HPP_ */
