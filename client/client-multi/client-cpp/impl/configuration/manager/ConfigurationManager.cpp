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

#include "kaa/configuration/manager/ConfigurationManager.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <vector>

#include "kaa/common/exception/KaaException.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/utils/IThreadPool.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/configuration/manager/IConfigurationReceiver.hpp"

namespace kaa {

ConfigurationManager::ConfigurationManager(IKaaClientContext &context)
    : isConfigurationLoaded_(false), context_(context)
{}

void ConfigurationManager::addReceiver(IConfigurationReceiver &receiver)
{
    if (!configurationReceivers_.addCallback(&receiver,
            std::bind(&IConfigurationReceiver::onConfigurationUpdated,
                    &receiver, std::placeholders::_1))) {
        throw KaaException("Failed to add a configuration changes subscriber. Already subscribed");
    }
}

void ConfigurationManager::removeReceiver(IConfigurationReceiver &receiver)
{
    configurationReceivers_.removeCallback(&receiver);
}

void ConfigurationManager::init()
{
    KAA_MUTEX_LOCKING("configurationGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(configurationGuardLock, configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    if (!isConfigurationLoaded_) {
        loadConfiguration();
    }

    notifySubscribers(configuration_);
}

const KaaRootConfiguration& ConfigurationManager::getConfiguration()
{
    KAA_MUTEX_LOCKING("configurationGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(configurationGuardLock, configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    if (!isConfigurationLoaded_) {
        loadConfiguration();
    }

    return configuration_;
}

void ConfigurationManager::updateConfiguration(const std::uint8_t* data, const std::uint32_t dataSize)
{
    AvroByteArrayConverter<KaaRootConfiguration> converter;

    converter.fromByteArray(data, dataSize, configuration_);
    configurationHash_ = EndpointObjectHash(data, dataSize);

    KAA_LOG_TRACE(boost::format("Calculated configuration hash: %1%") %
            LoggingUtils::toString(configurationHash_.getHashDigest()));
}

void ConfigurationManager::loadConfiguration()
{
    if (storage_) {
        if (context_.getStatus().isSDKPropertiesUpdated()) {
            KAA_LOG_INFO("Ignore loading configuration from storage: configuration version updated");
            storage_->clearConfiguration();
        } else {
            auto data = storage_->loadConfiguration();
            if (!data.empty()) {
                updateConfiguration(data.data(), data.size());
                isConfigurationLoaded_ = true;
                KAA_LOG_INFO("Loaded configuration from storage");
            }
        }
    }

    if (!isConfigurationLoaded_) {
        const Botan::secure_vector<std::uint8_t>& config = getDefaultConfigData();

        updateConfiguration(config.data(), config.size());
        isConfigurationLoaded_ = true;
        KAA_LOG_INFO("Loaded default configuration");
    }
}

void ConfigurationManager::processConfigurationData(const std::vector<std::uint8_t>& data, bool fullResync)
{
    if (!fullResync) {
        throw KaaException("Partial configuration updates are not supported");
    }

    KAA_MUTEX_LOCKING("configurationGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(configurationGuardLock, configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    updateConfiguration(data.data(), data.size());

    if (storage_) {
        storage_->saveConfiguration(data);
    }

    notifySubscribers(configuration_);
}

void ConfigurationManager::setConfigurationStorage(IConfigurationStoragePtr storage)
{
    KAA_MUTEX_LOCKING("configurationGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(configurationGuardLock, configurationGuard_);
    KAA_MUTEX_LOCKED("configurationGuard_");

    storage_ = storage;
}

void ConfigurationManager::notifySubscribers(const KaaRootConfiguration& configuration)
{
    context_.getExecutorContext().getCallbackExecutor().add([this, configuration]
        {
            configurationReceivers_(configuration);
        });
}

}  // namespace kaa

#endif

