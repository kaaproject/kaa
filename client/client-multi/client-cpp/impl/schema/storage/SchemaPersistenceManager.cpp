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

#include "kaa/schema/storage/SchemaPersistenceManager.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"

namespace kaa {

void SchemaPersistenceManager::setSchemaStorage(ISchemaStorage *storage)
{
    KAA_LOG_INFO(boost::format("Setting user-defined schema storage %1%") % storage);

    if (storage) {
        KAA_MUTEX_LOCKING("schemaPersistenceGuard_")
        KAA_MUTEX_UNIQUE_DECLARE(lock, schemaPersistenceGuard_);
        KAA_MUTEX_LOCKED("schemaPersistenceGuard_");

        storage_ = storage;
        if (processor_ && !processor_->getSchema()) {
            readStoredSchema();
        }
    }
}

void SchemaPersistenceManager::onSchemaUpdated(std::shared_ptr<avro::ValidSchema> schema)
{
    if (!schema.get()) {
        throw KaaException("Empty schema was given");
    }

    if (ignoreSchemaUpdate_) {
        ignoreSchemaUpdate_ = false;
        return;
    }

    KAA_MUTEX_LOCKING("schemaPersistenceGuard_")
    KAA_MUTEX_UNIQUE_DECLARE(lock, schemaPersistenceGuard_);
    KAA_MUTEX_LOCKED("schemaPersistenceGuard_");

    KAA_LOG_DEBUG(boost::format("Going to pass schema to storage %1%") % storage_);

    if (storage_) {
        std::ostringstream osstr;
        schema->toJson(osstr);
        const std::string &str = osstr.str();

        ISchemaStorage::byte_buffer bytes;
        for (auto it = str.begin(); it != str.end(); ++it) {
            /*
             * workaround for AVRO C++ issue
             * https://issues.apache.org/jira/browse/AVRO-1352
             */
            if (*it == ',') {
                auto it2 = it + 1;
                while (it2 != str.end() && (isspace(*it2))) {
                    ++it2;
                }
                if (it2 != str.end() && *it2 == '}') {
                    continue;
                }
            } // end workaround

            bytes.push_back(*it);
        }
        storage_->saveSchema(bytes);
    }
}

void SchemaPersistenceManager::setSchemaProcessor(ISchemaProcessor *processor)
{
    processor_ = processor;
}

void SchemaPersistenceManager::readStoredSchema()
{
    if (!storage_) {
        throw KaaException("Can not read stored schema: reader is missing");
    }

    KAA_LOG_DEBUG(boost::format("Going to read schema from storage %1%") % storage_);

    ISchemaStorage::byte_buffer buffer = storage_->loadSchema();
    if (!buffer.empty()) {
        if (processor_) {
            ignoreSchemaUpdate_ = true;
            processor_->loadSchema(buffer.data(), buffer.size());
        }
    }

}

}  // namespace kaa
