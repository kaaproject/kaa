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

#include "kaa/bootstrap/BootstrapTransport.hpp"

#include <set>

#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"

namespace kaa {

BootstrapTransport::BootstrapTransport(IKaaChannelManager& channelManager, IBootstrapManager &bootstrapManager, IKaaClientContext &context)
    : AbstractKaaTransport(channelManager, context)
    , requestId_(0)
    , bootstrapManager_(bootstrapManager)
{

}

void BootstrapTransport::sync()
{
    syncByType();
}

std::shared_ptr<BootstrapSyncRequest> BootstrapTransport::createBootstrapSyncRequest()
{
    std::shared_ptr<BootstrapSyncRequest> sync;

    const auto& channels = channelManager_.getChannels();

    if (channels.size()) {
        /**
         * To avoid duplicates.
         */
        std::set<TransportProtocolId> protocolIds;

        for (const auto& channel : channels) {
            protocolIds.insert(channel->getTransportProtocolId());
        }

        sync.reset(new BootstrapSyncRequest);
        sync->requestId = ++requestId_;
        sync->supportedProtocols.reserve(protocolIds.size());

        ProtocolVersionPair protocolVersion;
        for (auto& protocolId : protocolIds) {
            protocolVersion.id = protocolId.getId();
            protocolVersion.version = protocolId.getVersion();

            sync->supportedProtocols.push_back(protocolVersion);
        }
    }

    return sync;
}

void BootstrapTransport::onBootstrapResponse(const BootstrapSyncResponse& response)
{
    bootstrapManager_.onServerListUpdated(response.supportedProtocols);
}

}  // namespace kaa


