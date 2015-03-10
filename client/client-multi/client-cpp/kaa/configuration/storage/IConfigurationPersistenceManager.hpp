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

#ifndef ICONFIGURATIONPERSISTENCEMANAGER_HPP_
#define ICONFIGURATIONPERSISTENCEMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#include <memory>

#include "kaa/configuration/storage/IConfigurationStorage.hpp"

namespace kaa {

/**
 * Interface for configuration persistence manager.
 */
class IConfigurationPersistenceManager {
public:
    virtual ~IConfigurationPersistenceManager() {}

    /**
     * Registers new configuration persistence routines. Replaces previously set value.
     * Memory pointed by given parameter should be managed by user.
     *
     * @param storage User-defined persistence routines.
     * @see IConfigurationStorage
     */
    virtual void setConfigurationStorage(IConfigurationStoragePtr storage) = 0;
};

typedef std::shared_ptr<IConfigurationPersistenceManager> IConfigurationPersistenceManagerPtr;

}  // namespace kaa

#endif /* ICONFIGURATIONPERSISTENCEMANAGER_HPP_ */
