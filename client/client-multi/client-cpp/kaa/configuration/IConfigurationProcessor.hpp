/*
 * Copyright 2014-2016 CyberVision, Inc.
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

#ifndef I_CONFIGURATION_PROCESSOR_HPP_
#define I_CONFIGURATION_PROCESSOR_HPP_

#include "kaa/KaaDefaults.hpp"

#include <cstdint>
#include <memory>


namespace kaa {

/**
 * Interface for a configuration processor.
 *
 * Receives and decodes the raw configuration data
 *
 */
class IConfigurationProcessor {
public:
    /**
     * Routine for processing received configuration data.
     *
     * @param data          Pointer to a memory where configuration data is placed.
     * @param data_length   Size of configuration data.
     * @param full_resunc   Signals if data contains full configuration resync or partial update
     */
    virtual void processConfigurationData(const std::vector<std::uint8_t>& data, bool fullResync) = 0;

    virtual ~IConfigurationProcessor() = default;
};

typedef std::shared_ptr<IConfigurationProcessor> IConfigurationProcessorPtr;

} /* namespace kaa */

#endif /* I_CONFIGURATION_PROCESSOR_HPP_ */
