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

#include "kaa/configuration/storage/ConfigurationPersistanceManager.hpp"
#include "kaa/common/types/ICommonRecord.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa {

void ConfigurationPersistanceManager::setConfigurationStorage(IConfigurationStorage *storage)
{
    if (storage) {
        KAA_MUTEX_LOCKING("confPersistanceGuard_");
        lock_type lock(confPersistanceGuard_);
        KAA_MUTEX_LOCKED("confPersistanceGuard_");

        storage_ = storage;
        auto hash = configurationHash_.getHash();
        if (!hash.first || !hash.second) {
            readStoredConfiugration();
        }
    }
}

void ConfigurationPersistanceManager::setConfigurationProcessor(IConfigurationProcessor *processor)
{
    processor_ = processor;
}

void ConfigurationPersistanceManager::onConfigurationUpdated(const ICommonRecord &configuration)
{
    if (ignoreConfigurationUpdate_) {
        ignoreConfigurationUpdate_ = false;
        return;
    }

    KAA_MUTEX_LOCKING("schemaGuard_");
    lock_type schema_lock(schemaGuard_);
    KAA_MUTEX_LOCKED("schemaGuard_");

    if (!schema_.get()) {
        throw KaaException("Can not process configuration without schema");
    }

    KAA_LOG_INFO("Configuration updated.");

    avro::GenericDatum datum(*schema_);
    avro::GenericArray &arrayW = datum.value<avro::GenericArray>();

    avro::GenericDatum arrayItemDatum(schema_->root()->leafAt(0));
    avro::GenericRecord &arrayItem = arrayItemDatum.value<avro::GenericRecord>();

    avro::GenericDatum &unionDatum = arrayItem.field("delta");
    unionDatum.selectBranch(0);
    avro::GenericRecord &unionItem = unionDatum.value<avro::GenericRecord>();
    unionItem = configuration.toAvro().value<avro::GenericRecord>();

    arrayW.value().push_back(arrayItemDatum);

    AvroByteArrayConverter<avro::GenericDatum> converter;
    SharedDataBuffer buffer = converter.toByteArray(datum);

    KAA_MUTEX_UNLOCKING("schemaGuard_");
    schema_lock.unlock();
    KAA_MUTEX_UNLOCKED("schemaGuard_");

    KAA_LOG_INFO(boost::format("Going to store configuration using configuration storage %1%") % storage_);

    KAA_MUTEX_LOCKING("confPersistanceGuard_");
    lock_type storage_lock(confPersistanceGuard_);
    KAA_MUTEX_LOCKED("confPersistanceGuard_");

    if (storage_ != NULL) {
        std::vector<boost::uint8_t> bytes (buffer.second);
        std::copy(buffer.first.get(), buffer.first.get() + buffer.second, bytes.begin());
        storage_->saveConfiguration(bytes);
    }

    KAA_MUTEX_UNLOCKING("confPersistanceGuard_");
    storage_lock.unlock();
    KAA_MUTEX_UNLOCKED("confPersistanceGuard_");

    EndpointObjectHash temp(buffer);
    configurationHash_ = temp;

    KAA_LOG_INFO(boost::format("Calculated configuration hash: %1%") % LoggingUtils::ByteArrayToString(configurationHash_.getHash()));
}

void ConfigurationPersistanceManager::onSchemaUpdated(boost::shared_ptr<avro::ValidSchema> schema)
{
    if (!schema.get()) {
        throw KaaException("Empty schema was given");
    }

    KAA_MUTEX_LOCKING("schemaGuard_");
    lock_type lock(schemaGuard_);
    KAA_MUTEX_LOCKED("schemaGuard_");

    schema_ = schema;
}

EndpointObjectHash ConfigurationPersistanceManager::getConfigurationHash()
{
    return configurationHash_;
}

void ConfigurationPersistanceManager::readStoredConfiugration()
{
    if (storage_ == NULL) {
        throw KaaException("Can not read stored configuration: Configuration storage is empty.");
    }

    KAA_LOG_DEBUG("Going to read stored confguration.");

    std::vector<boost::uint8_t> bytes = storage_->loadConfiguration();

    if (!bytes.empty()) {
        if (processor_ != NULL) {
            ignoreConfigurationUpdate_ = true;
            processor_->processConfigurationData(bytes.data(), bytes.size(), true);
        }

        EndpointObjectHash temp(bytes.data(), bytes.size());
        configurationHash_ = temp;
        KAA_LOG_INFO(boost::format("Calculated configuration hash: %1%") % LoggingUtils::ByteArrayToString(configurationHash_.getHash()));
    }

}

}  // namespace kaa
