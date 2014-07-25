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

#include "kaa/bootstrap/BootstrapTransport.hpp"
#include "kaa/KaaDefaults.hpp"

namespace kaa {

BootstrapTransport::BootstrapTransport(IKaaChannelManager& channelManager, IBootstrapManager &bootstrapManager)
    : AbstractKaaTransport(channelManager)
    , bootstrapManager_(bootstrapManager)
{

}

void BootstrapTransport::sync()
{
    syncByType();
}

boost::shared_ptr<Resolve> BootstrapTransport::createResolveRequest()
{
    boost::shared_ptr<Resolve> request(new Resolve);

    request->Application_Token = APPLICATION_TOKEN;

    return request;
}

void BootstrapTransport::onResolveResponse(OperationsServerList servers)
{
    if (!servers.operationsServerArray.empty()) {
        bootstrapManager_.onServerListUpdated(servers);
    }
}

}  // namespace kaa


