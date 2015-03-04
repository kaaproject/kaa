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

#ifndef I_CONFIGURATION_PROCESSED_OBSERVABLE_HPP_
#define I_CONFIGURATION_PROCESSED_OBSERVABLE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/configuration/IConfigurationProcessedObserver.hpp"

namespace kaa {

/**
 * Notifies subscribers when all deltas have been already processed
 */
class IConfigurationProcessedObservable {
public:
    /**
     * Subscribes observer of configuration processing finished.
     *
     * @param observer Receiver to be subscribed.
     * @see IConfigurationProcessedObserver
     *
     */
    virtual void addOnProcessedObserver(IConfigurationProcessedObserver &observer) = 0;

    /**
     * Unsubscribes observer of configuration processing finished.
     *
     * @param observer Receiver to be unsubscribed.
     * @see IConfigurationProcessedObserver
     *
     */
    virtual void removeOnProcessedObserver(IConfigurationProcessedObserver &observer) = 0;

    virtual ~IConfigurationProcessedObservable()
    {
    }
};

} /* namespace kaa */

#endif

#endif /* I_CONFIGURATION_PROCESSED_OBSERVABLE_HPP_ */
