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

#ifndef CONFIGURATIONTRANSPORT_HPP_
#define CONFIGURATIONTRANSPORT_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/channel/transport/IConfigurationTransport.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/storage/IConfigurationPersistenceManager.hpp"
#include "kaa/schema/ISchemaProcessor.hpp"

namespace kaa {

class ConfigurationTransport : public AbstractKaaTransport<TransportType::CONFIGURATION>, public IConfigurationTransport
{
public:
    ConfigurationTransport(IKaaChannelManager& channelManager, IConfigurationProcessor *configProcessor, IConfigurationHashContainer *hashContainer, IKaaClientStateStoragePtr status);

    void sync();

    virtual std::shared_ptr<ConfigurationSyncRequest> createConfigurationRequest();
    virtual void onConfigurationResponse(const ConfigurationSyncResponse &response);

private:
    IConfigurationProcessor     *configurationProcessor_;
    IConfigurationHashContainer *hashContainer_;
};

}  // namespace kaa

#endif

#endif /* CONFIGURATIONTRANSPORT_HPP_ */
