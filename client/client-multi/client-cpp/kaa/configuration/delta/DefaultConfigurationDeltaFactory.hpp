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

#ifndef DEFAULTCONFIGURATIONDELTAFACTORY_HPP_
#define DEFAULTCONFIGURATIONDELTAFACTORY_HPP_

#include <avro/Generic.hh>

#include "kaa/configuration/delta/IDeltaType.hpp"
#include "kaa/configuration/delta/IConfigurationDelta.hpp"
#include "kaa/configuration/delta/IConfigurationDeltaFactory.hpp"

namespace kaa {

class DefaultConfigurationDeltaFactory: public IConfigurationDeltaFactory {
public:
    /**
     * Creates configuration delta from the given Avro Generic delta
     * @param genericDelta avro generic delta object
     * @return new configuration delta (\ref IConfigurationDelta)
     */
    virtual ConfigurationDeltaPtr createDelta(const avro::GenericDatum& genericDelta);

private:
    /**
     * Create delta type for avro array
     * @param array avro generic array
     * @return new delta type for avro array (\ref IDeltaType)
     */
    DeltaTypePtr createArrayDeltaType(const avro::GenericArray& array);

    /**
     * Create delta type for primitive avro object
     * @param datum avro generic object
     * @param type retrieved avro type
     * @return new delta type for primitive avro object (\ref IDeltaType)
     */
    IDeltaType::DeltaValue createDeltaValue(const avro::GenericDatum& datum, avro::Type& type);
};

} /* namespace kaa */

#endif /* DEFAULTCONFIGURATIONDELTAFACTORY_HPP_ */
