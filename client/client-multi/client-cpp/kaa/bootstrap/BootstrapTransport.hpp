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

#ifndef BOOTSTRAPTRANSPORT_HPP_
#define BOOTSTRAPTRANSPORT_HPP_

#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/channel/transport/IBootstrapTransport.hpp"

namespace kaa {

class IBootstrapManager;
class IKaaChannelManager;
struct BootstrapSyncRequest;
struct BootstrapSyncResponse;

class BootstrapTransport : public AbstractKaaTransport<TransportType::BOOTSTRAP>
                         , public IBootstrapTransport
{
public:
    BootstrapTransport(IKaaChannelManager& channelManager, IBootstrapManager &bootstrapManager, IKaaClientContext &context);

    virtual void sync();

    virtual std::shared_ptr<BootstrapSyncRequest> createBootstrapSyncRequest();
    virtual void onBootstrapResponse(const BootstrapSyncResponse& response);
private:
    std::uint32_t requestId_;

    IBootstrapManager &  bootstrapManager_;
};

}  // namespace kaa


#endif /* BOOTSTRAPTRANSPORT_HPP_ */
