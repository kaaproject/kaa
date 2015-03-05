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

#ifndef DEFAULTDELTAMANAGER_HPP_
#define DEFAULTDELTAMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <map>
#include "kaa/KaaThread.hpp"

#include "kaa/configuration/delta/manager/IDeltaReceiver.hpp"
#include "kaa/configuration/delta/DeltaHandlerId.hpp"
#include "kaa/configuration/IGenericDeltaReceiver.hpp"
#include "kaa/configuration/delta/manager/IDeltaManager.hpp"
#include "kaa/configuration/delta/DefaultConfigurationDeltaFactory.hpp"

namespace kaa {

class DefaultDeltaManager: public IDeltaManager, public IGenericDeltaReceiver {
public:
    /**
     * Default constructor
     */
    DefaultDeltaManager();

    /**
     * Will be called on each deserialized delta
     * @param index index of the current delta in the union list
     * @param data avro generic object with deserialized delta
     * @param full_resunc signals if data contains full configuration resync or partial update
     */
    virtual void onDeltaRecevied(int index, const avro::GenericDatum &data, bool full_resync);

    /**
     * Registers root receiver to receive first and full resync deltas
     * @param receiver the root receiver object
     */
    virtual void registerRootReceiver(IDeltaReceiver* rootReceiver);

    /**
     * Subscribes receiver for delta updates by the given handler id
     * @param handlerId id of the delta handler
     * @param receiver the object which is going to receive updates
     */
    virtual void subscribeForDeltaUpdates(const DeltaHandlerId& handlerId, IDeltaReceiver* receiver);

    /**
     * Unsubscribes receiver from delta updates
     * @param handlerId id of the handler which is going to be unsubscribed
     */
    virtual void unsubscribeFromDeltaUpdates(const DeltaHandlerId& handlerId);

private:
    IDeltaReceiver *rootReceiver_;

    KAA_MUTEX_DECLARE(subscriptionMutex_);
    std::map<DeltaHandlerId, IDeltaReceiver*> subscriptionStorage_;
};

}
/* namespace kaa */

#endif

#endif /* DEFAULTDELTAMANAGER_HPP_ */
