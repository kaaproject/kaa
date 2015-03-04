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

#include "kaa/configuration/storage/ConfigurationPersistenceManager.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/common/types/ICommonRecord.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa {

void ConfigurationPersistenceManager::setConfigurationStorage(IConfigurationStorage *storage)
{
    if (storage) {
        KAA_MUTEX_LOCKING("confPersistenceGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lock, confPersistenceGuard_); KAA_MUTEX_LOCKED("confPersistenceGuard_");

        storage_ = storage;
        readStoredConfiguration();
    }
}

void ConfigurationPersistenceManager::setConfigurationProcessor(IConfigurationProcessor *processor)
{
    processor_ = processor;
}

void ConfigurationPersistenceManager::onConfigurationUpdated(const KaaRootConfiguration &configuration)
{
    if (ignoreConfigurationUpdate_) {
        ignoreConfigurationUpdate_ = false;
        return;
    }

    KAA_LOG_INFO("Configuration updated.");

    AvroByteArrayConverter<KaaRootConfiguration> converter;
    SharedDataBuffer buffer = converter.toByteArray(configuration);

    KAA_LOG_INFO(boost::format("Going to store configuration using configuration storage %1%") % storage_);

    KAA_MUTEX_LOCKING("confPersistenceGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(storage_lock, confPersistenceGuard_); KAA_MUTEX_LOCKED("confPersistenceGuard_");

    if (storage_) {
        storage_->saveConfiguration(std::vector<std::uint8_t>(buffer.first.get(), buffer.first.get() + buffer.second));
    }

    KAA_MUTEX_UNLOCKING("confPersistenceGuard_");
    KAA_UNLOCK(storage_lock); KAA_MUTEX_UNLOCKED("confPersistenceGuard_");

    configurationHash_ = EndpointObjectHash(buffer);

    KAA_LOG_INFO(
            boost::format("Calculated configuration hash: %1%") % LoggingUtils::ByteArrayToString(
                    configurationHash_.getHashDigest()));
}

EndpointObjectHash ConfigurationPersistenceManager::getConfigurationHash()
{
    return configurationHash_;
}

void ConfigurationPersistenceManager::readStoredConfiguration()
{
    if (state_->isConfigurationVersionUpdated()) {
        KAA_LOG_INFO("Ignore loading configuration from storage: configuration version updated");
        return;
    }

    auto hash = configurationHash_.getHashDigest();
    if (hash.empty()) {
        KAA_LOG_DEBUG("Going to read stored configuration.");

        std::vector<std::uint8_t> bytes = storage_->loadConfiguration();

        if (!bytes.empty()) {
            if (processor_) {
                ignoreConfigurationUpdate_ = true;
                processor_->processConfigurationData(bytes.data(), bytes.size(), true);
            }

            configurationHash_ = EndpointObjectHash(bytes.data(), bytes.size());
            KAA_LOG_INFO(
                    boost::format("Calculated configuration hash: %1%") % LoggingUtils::ByteArrayToString(
                            configurationHash_.getHashDigest()));
        }
    }
}

}  // namespace kaa

#endif

