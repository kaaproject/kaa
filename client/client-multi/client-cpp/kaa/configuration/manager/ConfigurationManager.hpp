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

#ifndef CONFIGURATION_MANAGER_HPP_
#define CONFIGURATION_MANAGER_HPP_

#include "kaa/configuration/manager/IConfigurationManager.hpp"
#include "kaa/observer/KaaObservable.hpp"

#include <memory>

namespace kaa {

/**
 * \class ConfigurationManager
 *
 * This class is responsible for correct configuration delta merging
 * and contains root configuration tree. This class receives separate
 * deltas form the @link ConfigurationProcessor @endlink and merges the
 * tree performing full or partial update. After getting notification about
 * configuration processing is finished from @link ConfigurationProcessor @endlink
 * notifies registered observers (derived from @link IConfigurationReceiver @endlink)
 * with root configuration object presented as @link ICommonRecord @endlink.
 */
class ConfigurationManager : public IConfigurationManager {
public:
    ConfigurationManager() {}
    ~ConfigurationManager() {}

    /**
     * @link IConfigurationManager @endlink implementation
     */
    void subscribeForConfigurationChanges(IConfigurationReceiver &receiver);
    void unsubscribeFromConfigurationChanges(IConfigurationReceiver &receiver);
    ICommonRecord &getConfiguration();

    /**
     * @link IGenericDeltaReceiver @endlink implementation
     */
    void onDeltaRecevied(int index, const avro::GenericDatum & data, bool full_resync);

    /**
     * @link IConfigurationProcessedObserver @endlink implementation
     */
    void onConfigurationProcessed();

private:
    /**
     * Loads data from Avro datum to a record.
     *
     * @param rec   Record where avro data will be loaded to.
     * @param data  Avro object containing deserialized data.
     */
    void updateRecord(std::shared_ptr<ICommonRecord> rec, const avro::GenericDatum &data);

    /**
     * Checks if object with given uuid was already registered.
     *
     * @param uuid UUID to search
     */
    bool isSubscribed(uuid_t uuid);

    /**
     * Adds a record with uuid to a map for partial configuration update
     *
     * @param uuid      UUID of a new object
     * @param record    Record which must be referenced by given UUID
     */
    void subscribe(uuid_t uuid, std::shared_ptr<ICommonRecord> record);

    /**
     * Unsubscribes an object by given UUID
     *
     * @param uuid UUID to search
     */
    void unsubscribe(uuid_t uuid);

    std::map<uuid_t, std::shared_ptr<ICommonRecord> >   records_;
    std::shared_ptr<ICommonRecord>                      root_;

    KAA_MUTEX_DECLARE(configurationGuard_);
    KaaObservable<void (ICommonRecord &), IConfigurationReceiver *> configurationReceivers_;
};

}  // namespace kaa


#endif /* CONFIGURATION_MANAGER_HPP_ */
