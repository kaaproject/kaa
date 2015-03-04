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

#include "kaa/configuration/delta/manager/DefaultDeltaManager.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/logging/Log.hpp"

namespace kaa {

DefaultDeltaManager::DefaultDeltaManager()
        : rootReceiver_(nullptr)
{
}

void DefaultDeltaManager::onDeltaRecevied(int index, const avro::GenericDatum &data, bool full_resync)
{
    DefaultConfigurationDeltaFactory deltaFactory;
    ConfigurationDeltaPtr deltaResult = deltaFactory.createDelta(data);

    KAA_MUTEX_LOCKING("subscriptionMutex_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, subscriptionMutex_); KAA_MUTEX_LOCKED("subscriptionMutex_");

    if (full_resync) {
        if (rootReceiver_) {
            rootReceiver_->loadDelta(deltaResult);
        }
    } else {
        auto it = subscriptionStorage_.find(deltaResult->getHandlerId());
        if (it != subscriptionStorage_.end()) {
            it->second->loadDelta(deltaResult);
        }
    }
}

void DefaultDeltaManager::registerRootReceiver(IDeltaReceiver* rootReceiver)
{
    KAA_MUTEX_LOCKING("subscriptionMutex_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, subscriptionMutex_); KAA_MUTEX_LOCKED("subscriptionMutex_");

    rootReceiver_ = rootReceiver;
}

void DefaultDeltaManager::subscribeForDeltaUpdates(const DeltaHandlerId& handlerId, IDeltaReceiver* receiver)
{
    if (receiver) {
        KAA_MUTEX_LOCKING("subscriptionMutex_");
        KAA_MUTEX_UNIQUE_DECLARE(lock, subscriptionMutex_); KAA_MUTEX_LOCKED("subscriptionMutex_");

        subscriptionStorage_[handlerId] = receiver;
    }
}

void DefaultDeltaManager::unsubscribeFromDeltaUpdates(const DeltaHandlerId& handlerId)
{
    KAA_MUTEX_LOCKING("subscriptionMutex_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, subscriptionMutex_); KAA_MUTEX_LOCKED("subscriptionMutex_");

    subscriptionStorage_.erase(handlerId);
}

} /* namespace kaa */

#endif

