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

#ifndef I_GENERIC_DELTA_RECEIVER_HPP_
#define I_GENERIC_DELTA_RECEIVER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"

namespace kaa {

/**
 * Interface for subscriber to receive deltas in avro generic objects
 */
class IGenericDeltaReceiver {
public:
    /**
     * Will be called on each deserialized configuration data
     *
     * @param index index of the current delta in the union list
     * @param data configuration object with deserialized data
     * @param fullResync signals if delta contains full configuration resync or partial update
     *
     */
    virtual void onDeltaReceived(int index, const KaaRootConfiguration& data, bool fullResync) = 0;

    virtual ~IGenericDeltaReceiver() {}
};

}  // namespace kaa

#endif

#endif /* I_GENERIC_DELTA_RECEIVER_HPP_ */
