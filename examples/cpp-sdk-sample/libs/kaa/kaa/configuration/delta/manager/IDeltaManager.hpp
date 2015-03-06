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

#ifndef IDELTAMANAGER_HPP_
#define IDELTAMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/configuration/delta/DeltaHandlerId.hpp"
#include "kaa/configuration/delta/manager/IDeltaReceiver.hpp"

namespace kaa {

/**
 * Interface for the delta manager
 */
class IDeltaManager {
public:
    /**
     * Registers root receiver to receive first and full resync deltas
     *
     * @param receiver the root receiver object
     * @see DeltaReceiver
     *
     */
    virtual void registerRootReceiver(IDeltaReceiver* receiver) = 0;

    /**
     * Subscribes receiver for delta updates by the given handler id
     *
     * @param handlerId id of the delta handler
     * @param receiver the object to receive updates
     *
     * @see DeltaHandlerId
     * @see IDeltaReceiver
     *
     */
    virtual void subscribeForDeltaUpdates(const DeltaHandlerId& handlerId, IDeltaReceiver* receiver) = 0;

    /**
     * Unsubscribes receiver from delta updates
     *
     * @param handlerId id of the handler to be unsubscribed
     * @see DeltaHandlerId
     *
     */
    virtual void unsubscribeFromDeltaUpdates(const DeltaHandlerId& handlerId) = 0;

    virtual ~IDeltaManager() {}
};

} /* namespace kaa */

#endif

#endif /* IDELTAMANAGER_HPP_ */
