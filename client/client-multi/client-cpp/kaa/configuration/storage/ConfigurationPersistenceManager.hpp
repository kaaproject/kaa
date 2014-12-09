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

#ifndef CONFIGURATIONPERSISTENCEMANAGER_HPP_
#define CONFIGURATIONPERSISTENCEMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/KaaThread.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/storage/IConfigurationPersistenceManager.hpp"

namespace kaa {

class IKaaClientStateStorage;

/**
 * \class ConfigurationPersistenceManager
 *
 * This class is responsible for persistence of configuration invoking
 * user-defined @link IConfigurationStorage @endlink routines.
 *
 * Receives configuration and data schema updates from
 * @link ConfigurationManager @endlink and @link SchemaProcessor @endlink
 * respectively.
 */
class ConfigurationPersistenceManager : public IConfigurationPersistenceManager {
public:
    ConfigurationPersistenceManager(IKaaClientStateStoragePtr state)
        : storage_(nullptr)
        , processor_(nullptr)
        , ignoreConfigurationUpdate_(false)
        , state_(state)
    {}
    ~ConfigurationPersistenceManager() {}

    /**
     * @link IConfigurationPersistenceManager @endlink implementation
     */
    void setConfigurationStorage(IConfigurationStorage *storage);

    /**
     * @link IConfigurationReceiver @endlink implementation
     */
    void onConfigurationUpdated(const ICommonRecord &configuration);

    /**
     * @link ISchemaUpdatesReceiver @endlink implementation
     */
    void onSchemaUpdated(std::shared_ptr<avro::ValidSchema> schema);

    /**
     * @link IConfigurationHashContainer @endlink implementation
     */
    EndpointObjectHash getConfigurationHash();

    /**
     * Sets the configuration processor (@link IConfigurationProcessor @endlink)
     * which will handle restored configuration on start-up.
     *
     * @param processor Configuration load handler.
     */
    void setConfigurationProcessor(IConfigurationProcessor *processor);
private:
    void readStoredConfiguration();

    KAA_MUTEX_DECLARE(schemaGuard_);
    KAA_MUTEX_DECLARE(confPersistenceGuard_);

    IConfigurationStorage *                 storage_;
    IConfigurationProcessor *               processor_;

    std::shared_ptr<avro::ValidSchema>      schema_;
    EndpointObjectHash                      configurationHash_;
    bool                                    ignoreConfigurationUpdate_;

    IKaaClientStateStoragePtr               state_;
};

}  // namespace kaa

#endif

#endif /* CONFIGURATIONPERSISTENCEMANAGER_HPP_ */
