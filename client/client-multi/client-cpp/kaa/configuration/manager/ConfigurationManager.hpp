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

#ifndef CONFIGURATION_MANAGER_HPP_
#define CONFIGURATION_MANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/configuration/IGenericDeltaReceiver.hpp"
#include "kaa/configuration/IConfigurationProcessedObserver.hpp"
#include "kaa/configuration/manager/IConfigurationManager.hpp"
#include "kaa/observer/KaaObservable.hpp"

#include <memory>

namespace kaa {

/**
 * \class ConfigurationManager
 *
 * This class is responsible for correct configuration delta merging
 * and contains root configuration tree.
 * notifies registered observers (derived from @link IConfigurationReceiver @endlink)
 * with root configuration object presented as @link KaaRootConfiguration @endlink.
 */
class ConfigurationManager: public IConfigurationManager,
                            public IConfigurationProcessedObserver,
                            public IGenericDeltaReceiver

{
public:
    ConfigurationManager()
    {
    }
    ~ConfigurationManager()
    {
    }

    void onDeltaReceived(int index, const KaaRootConfiguration& datum, bool fullResync);

    /**
     * @link IConfigurationManager @endlink implementation
     */
    void subscribeForConfigurationChanges(IConfigurationReceiver &receiver);
    void unsubscribeFromConfigurationChanges(IConfigurationReceiver &receiver);
    const KaaRootConfiguration& getConfiguration();

    /**
     * @link IConfigurationProcessedObserver @endlink implementation
     */
    void onConfigurationProcessed();

private:
    KaaRootConfiguration root_;

    KAA_MUTEX_DECLARE(configurationGuard_);
    KaaObservable<void (const KaaRootConfiguration &), IConfigurationReceiver *> configurationReceivers_;
};

}
  // namespace kaa

#endif

#endif /* CONFIGURATION_MANAGER_HPP_ */
