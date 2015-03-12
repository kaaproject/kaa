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

#include "kaa/KaaThread.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/storage/IConfigurationPersistenceManager.hpp"
#include "kaa/configuration/manager/IConfigurationReceiver.hpp"
#include "kaa/configuration/IConfigurationHashContainer.hpp"

namespace kaa {

class IKaaClientStateStorage;

/**
 * \class ConfigurationPersistenceManager
 *
 * This class is responsible for persistence of configuration invoking
 * user-defined @link IConfigurationStorage @endlink routines.
 *
 */
class ConfigurationPersistenceManager : public IConfigurationPersistenceManager,
                                        public IConfigurationReceiver,
                                        public IConfigurationHashContainer {
public:
    ConfigurationPersistenceManager(IKaaClientStateStoragePtr state)
        : processor_(nullptr)
        , ignoreConfigurationUpdate_(false)
        , state_(state)
    {}
    ~ConfigurationPersistenceManager() {}

    /**
     * @link IConfigurationPersistenceManager @endlink implementation
     */
    void setConfigurationStorage(IConfigurationStoragePtr storage);

    /**
     * @link IConfigurationReceiver @endlink implementation
     */
    void onConfigurationUpdated(const KaaRootConfiguration &configuration);

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

    KAA_MUTEX_DECLARE(confPersistenceGuard_);

    IConfigurationStoragePtr storage_;
    IConfigurationProcessor* processor_;

    EndpointObjectHash configurationHash_;
    bool_type ignoreConfigurationUpdate_;

    IKaaClientStateStoragePtr state_;
};

}  // namespace kaa

#endif /* CONFIGURATIONPERSISTENCEMANAGER_HPP_ */
