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

#include "kaa/configuration/ConfigurationProcessor.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"

#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa {

void ConfigurationProcessor::processConfigurationData(const std::uint8_t *data, std::size_t dataLength, bool fullResync)
{
    KAA_MUTEX_LOCKING("confProcessorMutex_");
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, confProcessorMutex_);
    KAA_MUTEX_LOCKED("confProcessorMutex_");

    KAA_LOG_INFO("Received configuration data.");

    AvroByteArrayConverter<KaaRootConfiguration> converter;
    KaaRootConfiguration rootConfiguration;
    converter.fromByteArray(data, dataLength, rootConfiguration);

    deltaReceivers_(0, rootConfiguration, fullResync);
    onProcessedObservers_();
}

void ConfigurationProcessor::subscribeForUpdates(IGenericDeltaReceiver &receiver)
{
    if (!deltaReceivers_.addCallback(
            &receiver,
            std::bind(&IGenericDeltaReceiver::onDeltaReceived, &receiver, std::placeholders::_1, std::placeholders::_2,
                      std::placeholders::_3))) {
        throw KaaException("Failed to register new delta receiver. Receiver is already registered");
    }
}

void ConfigurationProcessor::unsubscribeFromUpdates(IGenericDeltaReceiver &receiver)
{
    deltaReceivers_.removeCallback(&receiver);
}

void ConfigurationProcessor::addOnProcessedObserver(IConfigurationProcessedObserver &observer)
{
    if (!onProcessedObservers_.addCallback(
            &observer, std::bind(&IConfigurationProcessedObserver::onConfigurationProcessed, &observer))) {
        throw KaaException("Failed to register new IConfigurationProcessedObserver. Already registered");
    }
}

void ConfigurationProcessor::removeOnProcessedObserver(IConfigurationProcessedObserver &observer)
{
    onProcessedObservers_.removeCallback(&observer);
}

}  // namespace kaa

#endif
