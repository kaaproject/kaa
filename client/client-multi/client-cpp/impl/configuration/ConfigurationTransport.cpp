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

#include "kaa/configuration/ConfigurationTransport.hpp"

#include <utility>

#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/IConfigurationHashContainer.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/logging/Log.hpp"

namespace kaa {

ConfigurationTransport::ConfigurationTransport(IKaaChannelManager& channelManager, IKaaClientContext &context)
    : AbstractKaaTransport(channelManager, context)
    , configurationProcessor_(nullptr)
    , hashContainer_(nullptr)
{}

void ConfigurationTransport::sync()
{
    syncByType();
}

std::shared_ptr<ConfigurationSyncRequest> ConfigurationTransport::createConfigurationRequest()
{
    if (!hashContainer_) {
        throw KaaException("Can not generate ConfigurationSyncRequest: configuration transport is not initialized");
    }

    std::shared_ptr<ConfigurationSyncRequest> request(new ConfigurationSyncRequest);
    request->configurationHash = hashContainer_->getConfigurationHash();
    request->resyncOnly.set_bool(true); // Only full resyncs are currently supported
    return request;
}

void ConfigurationTransport::onConfigurationResponse(const ConfigurationSyncResponse &response)
{
    if (configurationProcessor_ && !response.confDeltaBody.is_null()) {
        configurationProcessor_->processConfigurationData(response.confDeltaBody.get_bytes()
                                                        , response.responseStatus == SyncResponseStatus::RESYNC);
    }

    if (response.responseStatus != SyncResponseStatus::NO_DELTA) {
        syncAck();
    }
}

}  // namespace kaa

#endif
