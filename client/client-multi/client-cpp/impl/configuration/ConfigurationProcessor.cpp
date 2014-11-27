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

    if (!schema_.get()) {
        throw KaaException("Attempting to process data without schema.");
    }

    KAA_LOG_INFO("Received configuration data.");

    AvroByteArrayConverter<avro::GenericDatum> converter;
    avro::GenericDatum datumArray(*schema_) ;
    converter.fromByteArray(data, dataLength, datumArray);

    if (datumArray.type() != avro::AVRO_ARRAY) {
        throw KaaException("Configuration data is not an array!");
    }

    const avro::GenericArray &deltaArray = datumArray.value<avro::GenericArray>();
    const std::vector<avro::GenericDatum> &array = deltaArray.value();

    KAA_LOG_DEBUG(boost::format("Deltas count is %1%") % array.size());

    for (auto it = array.begin(); it != array.end(); ++it) {
        const avro::GenericRecord &record = it->value<avro::GenericRecord>();
        const avro::GenericDatum &datum = record.field("delta");
        int index = datum.unionBranch();
        deltaReceivers_(index, datum, fullResync);
    }

    onProcessedObservers_();
}

void ConfigurationProcessor::subscribeForUpdates(IGenericDeltaReceiver &receiver)
{
    if (!deltaReceivers_.addCallback(&receiver,
            std::bind(&IGenericDeltaReceiver::onDeltaRecevied, &receiver,
                    std::placeholders::_1,
                    std::placeholders::_2,
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
    if (!onProcessedObservers_.addCallback(&observer,
            std::bind(&IConfigurationProcessedObserver::onConfigurationProcessed,&observer))) {
        throw KaaException(
                "Failed to register new IConfigurationProcessedObserver. Already registered");
    }
}

void ConfigurationProcessor::removeOnProcessedObserver(IConfigurationProcessedObserver &observer)
{
    onProcessedObservers_.removeCallback(&observer);
}

void ConfigurationProcessor::onSchemaUpdated(std::shared_ptr<avro::ValidSchema> schema)
{
    if (!schema.get()) {
        throw KaaException("Empty schema was given");
    }

    KAA_LOG_DEBUG("Received schema update");

    KAA_MUTEX_LOCKING("confProcessorMutex_");
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, confProcessorMutex_);
    KAA_MUTEX_LOCKED("confProcessorMutex_");

    schema_ = schema;
}

}  // namespace kaa

#endif
