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

#include "kaa/configuration/manager/ConfigurationManager.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <avro/Generic.hh>
#include <functional>
#include <vector>

#include "kaa/common/exception/KaaException.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/utils/IThreadPool.hpp"

namespace kaa {

void ConfigurationManager::subscribeForConfigurationChanges(IConfigurationReceiver &receiver)
{
    if (!configurationReceivers_.addCallback(&receiver,
            std::bind(&IConfigurationReceiver::onConfigurationUpdated,
                    &receiver, std::placeholders::_1))) {
        throw KaaException("Failed to add a configuration changes subscriber. Already subscribed");
    }
}

void ConfigurationManager::unsubscribeFromConfigurationChanges(IConfigurationReceiver &receiver)
{
    configurationReceivers_.removeCallback(&receiver);
}

const KaaRootConfiguration& ConfigurationManager::getConfiguration()
{
    KAA_MUTEX_LOCKING("configurationGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    return root_;
}

void ConfigurationManager::onDeltaReceived(int index, const KaaRootConfiguration& datum, bool fullResync)
{
    if (!fullResync) {
        throw KaaException("Partial configuration updates are not supported");
    }

    KAA_MUTEX_LOCKING("configurationGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    root_ = datum;

    KAA_LOG_DEBUG("Full configuration received");
}

void ConfigurationManager::onConfigurationProcessed()
{
    executorContext_.getCallbackExecutor().add([this] { configurationReceivers_(root_); });
}

}  // namespace kaa

#endif

