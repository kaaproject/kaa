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

#ifndef CONFIGURATION_MANAGER_HPP_
#define CONFIGURATION_MANAGER_HPP_

#include "kaa/observer/KaaObservable.hpp"

#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/manager/IConfigurationManager.hpp"
#include "kaa/configuration/IConfigurationHashContainer.hpp"
#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class IExecutorContext;
class IConfigurationReceiver;

/**
 * \class ConfigurationManager
 *
 * This class is responsible for correct configuration delta merging
 * and contains root configuration tree.
 * notifies registered observers (derived from @link IConfigurationReceiver @endlink)
 * with root configuration object presented as @link KaaRootConfiguration @endlink.
 */
class ConfigurationManager : public IConfigurationManager,
                             public IConfigurationProcessor,
                             public IConfigurationHashContainer {
public:
    ConfigurationManager(IKaaClientContext &context);

    virtual void init();

    virtual void processConfigurationData(const std::vector<std::uint8_t>& data, bool fullResync);

    virtual void addReceiver(IConfigurationReceiver &receiver);
    virtual void removeReceiver(IConfigurationReceiver &receiver);
    virtual const KaaRootConfiguration& getConfiguration();

    virtual void setConfigurationStorage(IConfigurationStoragePtr storage);

    virtual IConfigurationProcessor& getConfigurationProcessor()
    {
        return *this;
    }

    virtual IConfigurationHashContainer& getConfigurationHashContainer()
    {
        return *this;
    }

    virtual EndpointObjectHash getConfigurationHash()
    {
        return configurationHash_;
    }

    virtual ~ConfigurationManager() noexcept {}

private:
    void updateConfiguration(const std::uint8_t* data, const std::uint32_t dataSize);
    void loadConfiguration();
    void notifySubscribers(const KaaRootConfiguration& configuration);

private:
    bool isConfigurationLoaded_;

    IKaaClientContext &context_;
    KaaRootConfiguration configuration_;

    IConfigurationStoragePtr storage_;
    EndpointObjectHash configurationHash_;

    KAA_MUTEX_DECLARE(configurationGuard_);
    KaaObservable<void (const KaaRootConfiguration &), IConfigurationReceiver *> configurationReceivers_;
};

}  // namespace kaa

#endif /* CONFIGURATION_MANAGER_HPP_ */
