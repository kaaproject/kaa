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

#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa {

void ConfigurationProcessor::processConfigurationData(const boost::uint8_t *data, size_t data_length, bool full_resync)
{
    KAA_MUTEX_LOCKING("confProcessorMutex_");
    lock_type lock(confProcessorMutex_);
    KAA_MUTEX_LOCKED("confProcessorMutex_");

    if (!schema_.get()) {
        throw KaaException("Attempting to process data without schema.");
    }

    KAA_LOG_INFO("Received configuration data.");

    AvroByteArrayConverter<avro::GenericDatum> converter;
    avro::GenericDatum datumArray(*schema_) ;
    converter.fromByteArray(data, data_length, datumArray);

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
        deltaReceivers_(index, datum, full_resync);
    }

    onProcessedObservers_();
}

void ConfigurationProcessor::subscribeForUpdates(IGenericDeltaReceiver &receiver)
{
    boost::signals2::connection c = deltaReceivers_.connect(boost::bind(&IGenericDeltaReceiver::onDeltaRecevied, &receiver, _1, _2, _3));
    if (!c.connected()) {
        throw KaaException("Failed to register new delta receiver.");
    }
}

void ConfigurationProcessor::unsubscribeFromUpdates(IGenericDeltaReceiver &receiver)
{
    deltaReceivers_.disconnect(boost::bind(&IGenericDeltaReceiver::onDeltaRecevied, &receiver, _1, _2, _3));
}

void ConfigurationProcessor::addOnProcessedObserver(IConfigurationProcessedObserver &observer)
{
    boost::signals2::connection c = onProcessedObservers_.connect(boost::bind(&IConfigurationProcessedObserver::onConfigurationProcessed, &observer));
    if (!c.connected()) {
        throw KaaException("Failed to register new IConfigurationProcessedObserver.");
    }
}

void ConfigurationProcessor::removeOnProcessedObserver(IConfigurationProcessedObserver &observer)
{
    onProcessedObservers_.disconnect(boost::bind(&IConfigurationProcessedObserver::onConfigurationProcessed, &observer));
}

void ConfigurationProcessor::onSchemaUpdated(boost::shared_ptr<avro::ValidSchema> schema)
{
    if (!schema.get()) {
        throw KaaException("Empty schema was given");
    }

    KAA_LOG_DEBUG("Received schema update");

    KAA_MUTEX_LOCKING("confProcessorMutex_");
    lock_type lock(confProcessorMutex_);
    KAA_MUTEX_LOCKED("confProcessorMutex_");

    schema_ = schema;
}

}  // namespace kaa
