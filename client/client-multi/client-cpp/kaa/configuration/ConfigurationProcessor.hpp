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

#ifndef CONFIGURATION_PROCESSOR_HPP_
#define CONFIGURATION_PROCESSOR_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <cstdint>
#include <memory>

#include "kaa/observer/KaaObservable.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/IConfigurationProcessedObservable.hpp"
#include "kaa/configuration/IDecodedDeltaObservable.hpp"

namespace kaa {

/**
 * \class ConfigurationProcessor
 *
 * This class is responsible for processing binary configuration updates
 * received from \c IUpdateListener. This decodes bytes into a delta list
 * and notifies subscribers (\c IGenericDeltaReceiver) with each separate delta.
 * After delta list is processed it notifies subscribers (\c IConfigurationProcessedObserver)
 * about processing is finished.
 * This class receives data schema updates from \c ISchemaProcessor.
 *
 */
class ConfigurationProcessor: public IConfigurationProcessor,
                              public IDecodedDeltaObservable,
                              public IConfigurationProcessedObservable {
public:
    typedef avro::ValidSchema Schema;

    ConfigurationProcessor()
    {
    }
    ~ConfigurationProcessor()
    {
    }

    /**
     * \c IConfigurationProcessor implementation
     */
    void processConfigurationData(const std::uint8_t *data, std::size_t dataLength, bool fullResync);

    /**
     * \c IDecodedDeltaObservable implementation
     */
    void subscribeForUpdates(IGenericDeltaReceiver &receiver);
    void unsubscribeFromUpdates(IGenericDeltaReceiver &receiver);

    /**
     * \c IConfigurationProcessedObservable implementation
     */
    void addOnProcessedObserver(IConfigurationProcessedObserver &observer);
    void removeOnProcessedObserver(IConfigurationProcessedObserver &observer);

private:
    KAA_R_MUTEX_DECLARE(confProcessorMutex_);

    KaaObservable<void (int, const ConfigurationRootRecord &, bool), IGenericDeltaReceiver *> deltaReceivers_;
    KaaObservable<void (), IConfigurationProcessedObserver *> onProcessedObservers_;

};

}
 // namespace kaa

#endif

#endif /* CONFIGURATION_PROCESSOR_HPP_ */
