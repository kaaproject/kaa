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

#ifndef I_CONFIGURATION_MANAGER_HPP_
#define I_CONFIGURATION_MANAGER_HPP_

#include "kaa/configuration/storage/IConfigurationStorage.hpp"
#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"

namespace kaa {

class IConfigurationReceiver;
class IConfigurationHashContainer;
class IConfigurationProcessor;

/**
 * Manages received configuration updates.
 * Manages subscriptions for configuration processed.
 * Subscribers must derive @link IConfigurationReceiver @endlink.
 */
class IConfigurationManager {
public:

    virtual void init() = 0;

    /**
     * Subscribes listener of configuration updates.
     *
     * @param receiver Listener to be added to notification list.
     */
    virtual void addReceiver(IConfigurationReceiver &receiver) = 0;

    /**
     * Unsubscribes listener of configuration updates.
     *
     * @param receiver Listener to be removed from notification list.
     */
    virtual void removeReceiver(IConfigurationReceiver &receiver) = 0;

    /**
     * Returns full configuration tree which is actual at current moment.
     *
     * @return @link ICommonRecord @endlink containing current configuration tree.
     */
    virtual const KaaRootConfiguration& getConfiguration() = 0;

    /**
     * Provide storage object which is able to persist encoded configuration data.
     *
     * @param storage Object which will save and load configuration data
     * @see ConfigurationStorage
     */
    virtual void setConfigurationStorage(IConfigurationStoragePtr storage) = 0;

    virtual IConfigurationProcessor& getConfigurationProcessor() = 0;

    virtual IConfigurationHashContainer& getConfigurationHashContainer() = 0;

    virtual ~IConfigurationManager() = default;
};

typedef std::shared_ptr<IConfigurationManager> IConfigurationManagerPtr;

}  // namespace kaa

#endif /* I_CONFIGURATION_MANAGER_HPP_ */
