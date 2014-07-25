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

#ifndef IDELTARECEIVER_HPP_
#define IDELTARECEIVER_HPP_

#include "kaa/configuration/delta/IConfigurationDelta.hpp"

namespace kaa {

/**
 * Interface for receivers of configuration deltas
 */
class IDeltaReceiver {
public:
    /**
     * This callback method will be called on each received appropriate delta
     *
     * @param delta configuration delta
     * @see IConfigurationDelta
     */
    virtual void loadDelta(ConfigurationDeltaPtr delta) = 0;

    virtual ~IDeltaReceiver() {}
};

} /* namespace kaa */

#endif /* IDELTARECEIVER_HPP_ */
