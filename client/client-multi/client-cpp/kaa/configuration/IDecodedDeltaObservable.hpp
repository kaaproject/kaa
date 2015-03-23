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

#ifndef I_DECODED_DELTA_OBSERVABLE_HPP_
#define I_DECODED_DELTA_OBSERVABLE_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/configuration/IGenericDeltaReceiver.hpp"

namespace kaa {

/**
 * Sends notifications with decoded configuration
 */
class IDecodedDeltaObservable {
public:
    /**
     * Subscribes new receiver for decoded data updates
     *
     * @param receiver receiver which is going to get decoded configuration updates
     * @see IGenericDeltaReceiver
     *
     */
    virtual void subscribeForUpdates(IGenericDeltaReceiver &receiver) = 0;

    /**
     * Unsubscribes receiver from decoded data updates
     *
     * @param receiver receiver which is going to be unsubscribed from configuration updates
     * @see IGenericDeltaReceiver
     */
    virtual void unsubscribeFromUpdates(IGenericDeltaReceiver &receiver) = 0;

    virtual ~IDecodedDeltaObservable() {};
};

}  // namespace kaa

#endif /* I_DECODED_DELTA_OBSERVABLE_HPP_ */
