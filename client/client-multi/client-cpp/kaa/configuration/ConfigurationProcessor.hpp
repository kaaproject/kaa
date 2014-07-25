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

#include <boost/cstdint.hpp>
#include <boost/signals2.hpp>
#include <boost/smart_ptr/shared_ptr.hpp>
#include <boost/thread/recursive_mutex.hpp>

#include "kaa/configuration/IConfigurationProcessor.hpp"

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
class ConfigurationProcessor : public IConfigurationProcessor {
public:
    typedef avro::ValidSchema Schema;

    ConfigurationProcessor() {}
    ConfigurationProcessor(boost::shared_ptr<avro::ValidSchema> schema) { schema_ = schema; }
    ~ConfigurationProcessor() { schema_.reset(); }

    /**
     * \c IConfigurationProcessor implementation
     */
    void processConfigurationData(const boost::uint8_t *data, size_t data_length, bool full_resync);

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

    /**
     * \c ISchemaUpdatesReceiver implementation
     */
    void onSchemaUpdated(boost::shared_ptr<avro::ValidSchema> schema);

private:
    typedef boost::recursive_mutex          mutex_type;
    typedef boost::unique_lock<mutex_type>  lock_type;

    mutex_type                                                      confProcessorMutex_;

    boost::signals2::signal<void (int, const avro::GenericDatum &, bool)> deltaReceivers_;
    boost::signals2::signal<void ()>                                onProcessedObservers_;

    boost::shared_ptr<Schema>                                       schema_;
};

} // namespace kaa

#endif /* CONFIGURATION_PROCESSOR_HPP_ */
