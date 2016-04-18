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

#ifndef I_CONFIGURATION_RECEIVER_HPP_
#define I_CONFIGURATION_RECEIVER_HPP_

#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"

namespace kaa {

/**
 * Interface for configuration receivers.
 * Derived objects can be subscribed/unsubscribed
 * using @link IConfigurationManager @endlink.
 */
class IConfigurationReceiver {
public:
    /**
     * Specific routine to process updated configuration.
     * Will be called by @link IConfigurationManager @endlink
     * when received deltas are processed.
     *
     * @param configuration Root record containing merged configuration.
     */
    virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration) = 0;

    virtual ~IConfigurationReceiver() = default;
};

}  // namespace kaa

#endif /* I_CONFIGURATION_RECEIVER_HPP_ */
